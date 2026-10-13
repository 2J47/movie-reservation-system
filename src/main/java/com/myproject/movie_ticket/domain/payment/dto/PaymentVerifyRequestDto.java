package com.myproject.movie_ticket.domain.payment.dto;

import com.myproject.movie_ticket.domain.payment.PayMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerifyRequestDto {
    private Long reservationId;
    private String paymentId;
    private Long userCouponId;
    private Integer usePointAmount;
    private PayMethod payMethod;
}

