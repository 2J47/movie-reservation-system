package com.myproject.movie_ticket.domain.movie.dto;

import com.myproject.movie_ticket.domain.movie.Schedule;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ScheduleResponseDto {
    private Long scheduleId;
    private String theaterName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer availableSeats;
    private Integer totalSeats;

    public ScheduleResponseDto(Schedule schedule, int totalSeats, int availableSeats) {
        this.scheduleId = schedule.getId();
        this.theaterName = schedule.getTheater().getName();
        this.startTime = schedule.getStartTime();
        this.endTime = schedule.getEndTime();
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
    }
}
