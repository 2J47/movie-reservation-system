package com.myproject.movie_ticket.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {
    //특정 시간표에서 취소되지 않은(PENDING, CONFIRMED) 예매 좌석의 ID 목록
    @Query("SELECT rs.seat.id FROM ReservationSeat rs JOIN rs.reservation r "+
    "WHERE rs.schedule.id = :scheduleId AND r.status != :canceledStatus")
    List<Long> findReservedSeatIdsByScheduleId(
            @Param("scheduleId") Long scheduleId,
            @Param("canceledStatus") ReservationStatus canceledStatus);

    //선점된 좌석을 다시 예매하려 하는지 검증하기 위한 쿼리
    @Query("SELECT CASE WHEN COUNT(rs) > 0 THEN true ELSE false END " +
            "FROM ReservationSeat rs JOIN rs.reservation r " +
            "WHERE rs.schedule.id = :scheduleId " +
            "AND rs.seat.id IN :seatIds " +
            "AND r.status != :canceledStatus")
    boolean existsValidReservation(@Param("scheduleId") Long scheduleId,
                                   @Param("seatIds") List<Long> seatIds,
                                   @Param("canceledStatus") ReservationStatus canceledStatus);

    @Query("SELECT COUNT(rs) FROM ReservationSeat rs JOIN rs.reservation r " +
    "WHERE rs.schedule.id = :scheduleId AND r.status != :canceledStatus")
    int countReservedSeatsByScheduleId(
      @Param("scheduleId") Long scheduleId,
      @Param("canceledStatus") ReservationStatus canceledStatus
    );

    //예매 ID로 연관된 예매 좌석(ReservationSeat) 목록 조회
    @Query("SELECT rs FROM ReservationSeat rs JOIN FETCH rs.seat WHERE rs.reservation.id = :reservationId")
    List<ReservationSeat> findByReservationId(@Param("reservationId") Long reservationId);

}
