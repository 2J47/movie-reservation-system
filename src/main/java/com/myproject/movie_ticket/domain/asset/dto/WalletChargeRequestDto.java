package com.myproject.movie_ticket.domain.asset.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WalletChargeRequestDto {
    private int amount; //충전할 금액
}
