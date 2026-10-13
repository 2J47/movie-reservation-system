package com.myproject.movie_ticket.domain.movie.api;

import com.myproject.movie_ticket.domain.movie.dto.SeatResponseDto;
import com.myproject.movie_ticket.domain.movie.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/api/v1/schedules/{scheduleId}/seats")
    public ResponseEntity<List<SeatResponseDto>> getSeatsStatus(
            @PathVariable("scheduleId") Long scheduleId) {
        List<SeatResponseDto> seats = seatService.getSeatsWithStatus(scheduleId);
        return ResponseEntity.ok(seats);
    }
}
