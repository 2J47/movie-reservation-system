package com.myproject.movie_ticket.domain.payment.service;

import com.myproject.movie_ticket.domain.coupon.Coupon;
import com.myproject.movie_ticket.domain.coupon.UserCoupon;
import com.myproject.movie_ticket.domain.coupon.UserCouponRepository;
import com.myproject.movie_ticket.domain.asset.Wallet;
import com.myproject.movie_ticket.domain.asset.WalletRepository;
import com.myproject.movie_ticket.domain.payment.Payment;
import com.myproject.movie_ticket.domain.payment.PaymentRepository;
import com.myproject.movie_ticket.domain.payment.PaymentStatus;
import com.myproject.movie_ticket.domain.payment.dto.PaymentResponseDto;
import com.myproject.movie_ticket.domain.payment.dto.PaymentVerifyRequestDto;
import com.myproject.movie_ticket.domain.reservation.Reservation;
import com.myproject.movie_ticket.domain.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final ReservationRepository reservationRepository;
    private final WalletRepository walletRepository;
    private final UserCouponRepository userCouponRepository;
    private final PaymentRepository paymentRepository;
    private final PortOneService portOneService;

    @Transactional
    public PaymentResponseDto verifyAndProcessPayment(Long userId, PaymentVerifyRequestDto requestDto) {
        //예매내역 조회
        Reservation reservation = reservationRepository.findById(requestDto.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("예매 내역을 찾을 수 없습니다."));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 예매 내역만 결제할 수 있습니다.");
        }

        //지갑 잔액 사전 검증
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("지갑 정보를 찾을 수 없습니다."));

        if (requestDto.getUsePointAmount() > 0) {
            if (wallet.getBalance() < requestDto.getUsePointAmount()) {
                throw new IllegalArgumentException("보유한 포인트가 부족합니다.");
            }
        }

        //쿠폰 할인 적용
        int discountAmount = 0;
        UserCoupon userCoupon = null;
        if (requestDto.getUserCouponId() != null) {
            userCoupon = userCouponRepository.findById(requestDto.getUserCouponId())
                    .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));

            //할인 금액 계산
            discountAmount = userCoupon.getCoupon()
                    .calculateDiscountAmount(reservation.getTotalPrice());
        }

        //최종 결제금액 계산(총액-할인금액-포인트금액)
        int finalAmount = reservation.getTotalPrice() - discountAmount - requestDto.getUsePointAmount();
        if (finalAmount < 0) {
            finalAmount = 0; //결제 금액이 마이너스가 될 수는 없음
        }
        //실제 결제한 금액
        Integer actualCardAmount = 0;

        if (finalAmount == 0) {
            // 전액 쿠폰/포인트 결제인 경우 포트원 PG사 조회 생략
            actualCardAmount = 0;
        } else {
            //실제 결제 금액이 있는 경우에만 포트원 서버에 검증 요청
            actualCardAmount = portOneService.getPaymentAmount(requestDto.getPaymentId());

            // 금액 위변조 검증
            if (finalAmount != actualCardAmount) {
                portOneService.cancelPayment(requestDto.getPaymentId(), "결제 금액 위변조 감지 (자동 환불)");
                throw new IllegalArgumentException("결제 요청 금액과 실제 결제 금액이 일치하지 않습니다.");
            }
        }

        //쿠폰 사용 처리
        if (userCoupon != null) {
            userCoupon.use();
        }
        //포인트 사용시 포인트 차감
        if (wallet != null && requestDto.getUsePointAmount() > 0) {
            wallet.deduct(requestDto.getUsePointAmount());
        }

        //예매 상태 확정으로 변경
        reservation.confirm();

        //결제 영수증 생성 및 저장
        Payment payment = Payment.builder()
                .reservation(reservation)
                .userCoupon(userCoupon)
                .pgTid(requestDto.getPaymentId())
                .finalAmount(actualCardAmount)
                .pointAmount(requestDto.getUsePointAmount())
                .couponAmount(discountAmount)
                .payMethod(requestDto.getPayMethod())
                .status(PaymentStatus.SUCCESS)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        if (actualCardAmount > 0) {
            int rewardPoints = (int) (actualCardAmount * 0.05);
            wallet.charge(rewardPoints);
            log.info("결제 완료 보상 포인트 지급: {}p (유저ID: {})", rewardPoints, userId);
        }

        return new PaymentResponseDto(savedPayment);
    }

    @Transactional //결제 실패
    public void failPayment(Long reservationId) {
        paymentRepository.findByReservationId(reservationId)
                .ifPresent(payment -> {
                    if (payment.getStatus() == PaymentStatus.PENDING) {
                        //포인트 복구
                        if (payment.getPointAmount() > 0) {
                            Wallet wallet = walletRepository.findByUserId(payment.getReservation().getUser().getId())
                                    .orElseThrow(() -> new IllegalArgumentException("지갑을 찾을 수 없습니다."));
                            wallet.charge(payment.getPointAmount());
                        }

                        //쿠폰 복구
                        if (payment.getUserCoupon() != null) {
                            payment.getUserCoupon().restore();
                        }

                        //결제 상태 변경
                        payment.fail();

                        //예매내역 취소처리
                        payment.getReservation().cancel();
                    }
                });
    }

    @Transactional
    public void refundPayment(Reservation reservation) {
        paymentRepository.findByReservationId(reservation.getId())
                .ifPresent(payment -> {
                    //결제 성공 상태일 때만 환불
                    if (payment.getStatus() == PaymentStatus.SUCCESS) {
                        //실 카드 결제 취소
                        if (payment.getFinalAmount() != null && payment.getFinalAmount() > 0 && payment.getPgTid() != null) {
                            portOneService.cancelPayment(payment.getPgTid(), "사용자 요청에 의한 예매 취소");
                        }
                        Wallet wallet = walletRepository.findByUserId(reservation.getUser().getId())
                                .orElseThrow(() -> new IllegalArgumentException("지갑을 찾을 수 없습니다."));

                        log.info("결제에 쓴 포인트 : {}, 보상 포인트: {}", payment.getPointAmount(), payment.getFinalAmount());
                        //사용했던 포인트, 보상 포인트 환불
                        if (payment.getPointAmount() != null && payment.getPointAmount() > 0) {
                            wallet.charge(payment.getPointAmount());
                        }
                        if (payment.getFinalAmount() != null && payment.getFinalAmount() > 0) {
                            int rollbackPoints = (int) (payment.getFinalAmount() * 0.05);
                            int actualDeduct = Math.min(wallet.getBalance(), rollbackPoints);

                            log.info("회수 예정 포인트: {}, 실제 회수 포인트: {}", rollbackPoints, actualDeduct);

                            if (actualDeduct > 0) {
                                wallet.deduct(actualDeduct);
                            }
                        }
                        walletRepository.save(wallet);

                        //쿠폰 복구
                        if (payment.getUserCoupon() != null) {
                            payment.getUserCoupon().restore();
                        }
                        payment.refund();
                    }
                });
    }
}
