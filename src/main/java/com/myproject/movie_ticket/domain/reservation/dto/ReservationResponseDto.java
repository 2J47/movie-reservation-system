package com.myproject.movie_ticket.domain.reservation.dto;

import com.myproject.movie_ticket.domain.reservation.Reservation;
import com.myproject.movie_ticket.domain.reservation.ReservationStatus;
import lombok.Getter;

@Getter
public class ReservationResponseDto {

    private Long reservationId;
    private String reservationNo;
    private Integer totalPrice;
    private ReservationStatus status;

    public ReservationResponseDto(Reservation reservation) {
        this.reservationId = reservation.getId();
        this.reservationNo = reservation.getReservationNo();
        this.totalPrice = reservation.getTotalPrice();
        this.status = reservation.getStatus();
    }
}