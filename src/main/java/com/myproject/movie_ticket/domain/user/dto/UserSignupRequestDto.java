package com.myproject.movie_ticket.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupRequestDto {
    private String email;
    private String password;
    private String name;
    private String phoneNumber;

}
