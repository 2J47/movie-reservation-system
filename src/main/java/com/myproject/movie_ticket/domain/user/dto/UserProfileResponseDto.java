package com.myproject.movie_ticket.domain.user.dto;

import com.myproject.movie_ticket.domain.asset.Wallet;
import com.myproject.movie_ticket.domain.user.User;
import lombok.Getter;

@Getter
public class UserProfileResponseDto {
    private Long userId;
    private String email;
    private String name;
    private String phoneNumber;
    private Integer balance;

    public UserProfileResponseDto(User user, Wallet wallet) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.phoneNumber = user.getPhoneNumber();
        this.balance = wallet.getBalance();
    }
}
