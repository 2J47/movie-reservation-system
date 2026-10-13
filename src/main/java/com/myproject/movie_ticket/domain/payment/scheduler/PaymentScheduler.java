package com.myproject.movie_ticket.domain.payment.scheduler;

import com.myproject.movie_ticket.domain.payment.Payment;
import com.myproject.movie_ticket.domain.payment.PaymentRepository;
import com.myproject.movie_ticket.domain.payment.PaymentStatus;
import com.myproject.movie_ticket.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduler {
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @Scheduled(cron = "0 * * * * *")
    public void cleanupExpiredPayments() {

        LocalDateTime limitTime = LocalDateTime.now().minusMinutes(10);

        List<Payment> expiredPayments = paymentRepository.findByStatusAndCreatedAtBefore(
                PaymentStatus.PENDING, limitTime
        );

        if (!expiredPayments.isEmpty()) {
            log.info("만료된 결제 내역 {}건을 취소 처리합니다.", expiredPayments.size());
        }

        for (Payment payment : expiredPayments) {
            try {
                paymentService.failPayment(payment.getReservation().getId());
                log.info("결제 취소 성공 - Reservation ID: {}", payment.getReservation().getId());
            } catch (Exception e) {
                log.error("결제 취소 실패 - ReservationID: {}", payment.getReservation().getId(), e);
            }
        }
    }
}
