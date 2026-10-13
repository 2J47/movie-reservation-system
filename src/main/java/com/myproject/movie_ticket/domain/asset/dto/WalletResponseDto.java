package com.myproject.movie_ticket.domain.asset.dto;

import com.myproject.movie_ticket.domain.asset.Wallet;
import lombok.Getter;

@Getter
public class WalletResponseDto {
    private Integer balance;

    public WalletResponseDto(Wallet wallet) {
        this.balance = wallet.getBalance();
    }
}
