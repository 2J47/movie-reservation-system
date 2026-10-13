package com.myproject.movie_ticket.domain.movie.api;

import com.myproject.movie_ticket.domain.movie.dto.ScheduleResponseDto;
import com.myproject.movie_ticket.domain.movie.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/api/v1/movies/{movieId}/schedules")
    public ResponseEntity<List<ScheduleResponseDto>> getSchedules(
            @PathVariable("movieId") Long movieId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        if (date == null) {
            date = LocalDate.now();
        }

                List<ScheduleResponseDto> schedules = scheduleService.getSchedulesByMovieAndDate(movieId, date);
                return ResponseEntity.ok(schedules);
    }
}
