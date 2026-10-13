package com.myproject.movie_ticket.domain.reservation.dto;

import com.myproject.movie_ticket.domain.reservation.Reservation;
import com.myproject.movie_ticket.domain.reservation.ReservationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class MyReservationResponseDto {

    private Long reservationId;
    private String reservationNo;
    private String movieTitle;
    private String theaterName;
    private LocalDateTime startTime;
    private Integer totalPrice;
    private Integer discountAmount;
    private Integer finalAmount;
    private ReservationStatus status;
    private List<String> seatNums;

    public MyReservationResponseDto(Reservation reservation, List<String> seatNums, Integer actualPaidAmount) {
        this.reservationId = reservation.getId();
        this.reservationNo = reservation.getReservationNo();
        this.movieTitle = reservation.getSchedule().getMovie().getTitle();
        this.theaterName = reservation.getSchedule().getTheater().getName();
        this.startTime = reservation.getSchedule().getStartTime();
        this.status = reservation.getStatus();
        this.seatNums = seatNums;
        this.totalPrice = reservation.getTotalPrice();
        this.finalAmount = actualPaidAmount;
        this.discountAmount = this.totalPrice - this.finalAmount;
    }
}
