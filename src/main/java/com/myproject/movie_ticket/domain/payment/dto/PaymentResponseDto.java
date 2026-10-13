package com.myproject.movie_ticket.domain.payment.dto;

import com.myproject.movie_ticket.domain.payment.Payment;
import com.myproject.movie_ticket.domain.payment.PaymentStatus;
import lombok.Getter;

@Getter
public class PaymentResponseDto {
    private Long paymentId;
    private String reservationNo;
    private Integer finalAmount;
    private PaymentStatus status;

    public PaymentResponseDto(Payment payment) {
        this.paymentId = payment.getId();
        this.reservationNo = payment.getReservation().getReservationNo();
        this.finalAmount = payment.getFinalAmount();
        this.status = payment.getStatus();
    }
}
