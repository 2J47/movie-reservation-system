package com.myproject.movie_ticket.domain.reservation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {
    private final ReservationRepository reservationRepository;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void cleanupReservations() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

        List<Reservation> expiredReservations = reservationRepository
                .findByStatusAndCreatedAtBefore(ReservationStatus.PENDING, fiveMinutesAgo);

        if (!expiredReservations.isEmpty()) {
            log.info("5분 경과 예매 대기 건수 [{}]건 자동 취소 시작", expiredReservations.size());

            for (Reservation expiredReservation : expiredReservations) {
                expiredReservation.cancel();
                log.info("- 예매 취소 완료: {}", expiredReservation.getReservationNo());
            }
        }

    }

}
