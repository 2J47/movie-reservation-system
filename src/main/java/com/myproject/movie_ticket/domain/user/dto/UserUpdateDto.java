package com.myproject.movie_ticket.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateDto {
    private String name;
    private String phoneNumber;
}
