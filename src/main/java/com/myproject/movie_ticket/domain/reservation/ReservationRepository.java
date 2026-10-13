package com.myproject.movie_ticket.domain.reservation;

import com.myproject.movie_ticket.domain.movie.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 외부 노출용 영수증 번호(난수)로 예매 내역 찾기
    Optional<Reservation> findByReservationNo(String reservationNo);

    // 특정 유저의 전체 예매 내역 리스트 조회 FETCH JOIN을 사용하여 스케줄, 영화, 상영관을 한 방 쿼리로 가져옴
    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.schedule s " +
            "JOIN FETCH s.movie m " +
            "JOIN FETCH s.theater t " +
            "WHERE r.user.id = :userId " +
            "AND r.status != :pendingStatus " +
            "ORDER BY r.id DESC")
    List<Reservation> findValidReservationsByUserId(
            @Param("userId") Long userId,
            @Param("pendingStatus") ReservationStatus pendingStatus
    );

    @Query("SELECT COUNT(rs) > 0 FROM ReservationSeat rs JOIN rs.reservation r " +
    "WHERE r.schedule.id = :scheduleId " +
    "AND rs.seat.id IN :seatIds " +
    "AND r.status <> 'CANCELED'")
    boolean existsByScheduleIdAndSeatIdIn(@Param("scheduleId") Long scheduleId, @Param("seatIds") List<Long> seatIds);

    boolean existsByUserIdAndStatusAndSchedule_StartTimeAfter(
            Long userId,
            ReservationStatus status,
            LocalDateTime time
    );

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.schedule.id = :scheduleId AND r.status != 'CANCELED'")
    int countByScheduleIdAndStatusNotCanceled(@Param("scheduleId") Long scheduleId);

    List<Reservation> findByStatusAndCreatedAtBefore(ReservationStatus status, LocalDateTime dateTime);

    //유효한 티켓 조회
    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.schedule s " +
            "JOIN FETCH s.movie " +
            "JOIN FETCH s.theater " +
            "WHERE r.user.id = :userId " +
            "AND r.status = :status " +
            "AND s.startTime > :now " +
            "ORDER BY s.startTime ASC")
    List<Reservation> findUpcomingTickets(
            @Param("userId") Long usrId,
            @Param("status") ReservationStatus status,
            @Param("now") LocalDateTime now
    );
}
