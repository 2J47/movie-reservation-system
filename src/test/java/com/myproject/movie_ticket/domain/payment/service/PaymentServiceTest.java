package com.myproject.movie_ticket.domain.payment.service;

import com.myproject.movie_ticket.domain.asset.*;
import com.myproject.movie_ticket.domain.coupon.Coupon;
import com.myproject.movie_ticket.domain.coupon.UserCoupon;
import com.myproject.movie_ticket.domain.coupon.UserCouponRepository;
import com.myproject.movie_ticket.domain.payment.Payment;
import com.myproject.movie_ticket.domain.payment.PaymentRepository;
import com.myproject.movie_ticket.domain.payment.dto.PaymentResponseDto;
import com.myproject.movie_ticket.domain.payment.dto.PaymentVerifyRequestDto;
import com.myproject.movie_ticket.domain.reservation.Reservation;
import com.myproject.movie_ticket.domain.reservation.ReservationRepository;
import com.myproject.movie_ticket.domain.reservation.ReservationStatus;
import com.myproject.movie_ticket.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private UserCouponRepository userCouponRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PortOneService portOneService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 금액이 일치하면 예매가 확정되고 결제 정보가 저장된다.")
    void 결제성공() {
        //given
        Long userId = 1L;
        Long reservationId = 10L;
        Long userCouponId = 100L;
        String paymentId = "1234";
        int reservationPrice = 15000;
        int usePoint = 2000;
        int balance = 5000;
        int discountAmount = 3000;
        int actualAmount = 10000;

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        //15,000원 예매 대기 내역 생성
        Reservation reservation = Reservation.builder()
                .user(user)
                .totalPrice(reservationPrice)
                .build();
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        //3,000원 쿠폰 생성
        Coupon coupon = Coupon.builder().discountValue(discountAmount).build();
        UserCoupon userCoupon = UserCoupon.builder()
                .user(user)
                .coupon(coupon)
                .build();
        ReflectionTestUtils.setField(userCoupon, "id", userCouponId);

        //5,000원 있는 지갑 생성
        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(balance)
                .build();

        PaymentVerifyRequestDto requestDto = new PaymentVerifyRequestDto();
        ReflectionTestUtils.setField(requestDto, "reservationId", reservationId);
        ReflectionTestUtils.setField(requestDto, "paymentId", paymentId);
        ReflectionTestUtils.setField(requestDto, "userCouponId", userCouponId);
        ReflectionTestUtils.setField(requestDto, "usePointAmount", usePoint);



        Payment payment = Payment.builder()
                .reservation(reservation)
                .finalAmount(actualAmount)
                .build();

        given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(userCouponRepository.findById(anyLong())).willReturn(Optional.of(userCoupon));
        given(walletRepository.findByUserId(anyLong())).willReturn(Optional.of(wallet));
        given(portOneService.getPaymentAmount(paymentId)).willReturn(actualAmount);
        given(paymentRepository.save(any(Payment.class))).willReturn(payment);

        //when
        PaymentResponseDto result = paymentService.verifyAndProcessPayment(userId, requestDto);

        //then
        assertThat(result).isNotNull();
        //외부 서비스 & 내부 로직 호출 여부
        verify(portOneService, times(1)).getPaymentAmount(paymentId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(walletRepository, times(1)).findByUserId(userId);
        verify(userCouponRepository, times(1)).findById(userCouponId);
        //최종 결제액 확인
        assertThat(result.getFinalAmount()).isEqualTo(10000);
        //예약 상태 확정으로 변경되었는지 확인
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        //지갑에서 포인트 차갑되었는지 확인
        assertThat(wallet.getBalance()).isEqualTo(3000);
    }

    @Test
    @DisplayName("결제 금액이 위변조된 경우 자동 환불을 요청하고 예외를 발생시킨다.")
    void 결제실패() {
        //given
        Long userId = 1L;
        Long reservationId = 10L;
        Long userCouponId = 100L;
        String paymentId = "1234";
        int reservationPrice = 15000;

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        Reservation reservation = Reservation.builder()
                .user(user)
                .totalPrice(reservationPrice)
                .build();
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        PaymentVerifyRequestDto requestDto = new PaymentVerifyRequestDto();
        ReflectionTestUtils.setField(requestDto, "reservationId", reservationId);
        ReflectionTestUtils.setField(requestDto, "paymentId", paymentId);
        ReflectionTestUtils.setField(requestDto, "usePointAmount", 0);

        //금액이 조작되어 100원만 결제
        int tamperedActualAmount = 100;

        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
        given(portOneService.getPaymentAmount(paymentId)).willReturn(tamperedActualAmount);

        //when&then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.verifyAndProcessPayment(userId, requestDto);
        });
        //예외 메시지, 환불 로직, db 저장 수행x 검증
        assertThat(exception.getMessage()).isEqualTo("결제 요청 금액과 실제 결제 금액이 일치하지 않습니다.");
        verify(portOneService, times(1)).cancelPayment(paymentId, "결제 금액 위변조 감지 (자동 환불)");
        verify(paymentRepository, times(0)).save(any());
    }

}