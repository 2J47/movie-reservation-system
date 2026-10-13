package com.myproject.movie_ticket.domain.payment;

public enum PaymentStatus {
    PENDING, //대기
    SUCCESS, //성공
    FAILED, //실패
    REFUNDED //환불 완료
}
