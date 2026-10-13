package com.myproject.movie_ticket.domain.reservation.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TicketResponseDto (
        Long reservationId,
        String movieTitle,
        String posterUrl,
        String theaterName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<String> seatNumbers,
        String reservationNumber
) {}
