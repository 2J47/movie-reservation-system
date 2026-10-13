package com.myproject.movie_ticket.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {
    private Long reservationId;
    private Long userCouponId; //사용 안 할 경우 null
    private Integer usePointAmount; //사용할 포인트 금액
    private String payMethod; //결제 수단
}
