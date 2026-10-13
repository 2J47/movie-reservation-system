package com.myproject.movie_ticket.domain.movie;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    //특정 상영관의 전체 좌석을 가져옴
    List<Seat> findByTheaterId(Long theaterId);

    int countByTheaterId(Long theaterId);

    List<Seat> findByTheaterIdOrderBySeatRowAscSeatColumnAsc(Long theaterId);

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :seatId")
    Optional<Seat> findById(@Param("seatId") Long seatId);
}
