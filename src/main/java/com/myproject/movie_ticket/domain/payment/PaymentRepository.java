package com.myproject.movie_ticket.domain.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    //특정 예매 건에 대한 결제(영수증) 내역을 찾을 때 사용
    Optional<Payment> findByReservationId(Long reservationId);

    //외부 PG사(포트원 등)의 고유 승인 번호로 결제 내역을 찾을 때 사용(사후 검증 및 환불용)
    Optional<Payment> findByPgTid(String pgTid);

    //상태, 생성 시간을 기준으로 오래된 결제 내역 조회
    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime time);
}
