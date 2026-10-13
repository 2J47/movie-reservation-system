package com.myproject.movie_ticket.global.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponseDto {
    private final String timestamp = LocalDateTime.now().toString();
    private final int status;
    private final String error;
    private final String message;
}
