package com.myproject.movie_ticket.domain.payment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PayMethod {
    CARD("신용카드"),
    KAKAOPAY("카카오페이"),
    NAVERPAY("네이버페이"),
    TOSSPAY("토스페이"),
    TRANSFER("계좌이체"),
    VIRTUAL_ACCOUNT("가상계좌");

    private final String description;
}
