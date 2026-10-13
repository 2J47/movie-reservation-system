package com.myproject.movie_ticket.domain.movie.dto;

public record TheaterResponseDto(
        Long id,
        String name,
        Integer totalSeats
) {}
