package com.myproject.movie_ticket.domain.movie.service;

import com.myproject.movie_ticket.domain.movie.Schedule;
import com.myproject.movie_ticket.domain.movie.ScheduleRepository;
import com.myproject.movie_ticket.domain.movie.SeatRepository;
import com.myproject.movie_ticket.domain.movie.dto.ScheduleResponseDto;
import com.myproject.movie_ticket.domain.movie.dto.SeatResponseDto;
import com.myproject.movie_ticket.domain.reservation.ReservationRepository;
import com.myproject.movie_ticket.domain.reservation.ReservationSeatRepository;
import com.myproject.movie_ticket.domain.reservation.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    private final SeatRepository seatRepository;
    private final ReservationSeatRepository reservationSeatRepository;

    //특정 영화 특정 날짜 상영시간표 조회
    public List<ScheduleResponseDto> getSchedulesByMovieAndDate(Long movieId, LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        LocalDateTime now = LocalDateTime.now();

        List<Schedule> schedules = scheduleRepository.findAvailableSchedules(
                movieId, startOfDay, endOfDay, now);

        return schedules.stream()
                .map(schedule -> {
                    int totalSeats = seatRepository.countByTheaterId(schedule.getTheater().getId());

                    int reservedSeats = reservationSeatRepository.countReservedSeatsByScheduleId(
                            schedule.getId(),
                            ReservationStatus.CANCELED);
                    int availableSeats = totalSeats - reservedSeats;
                    return new ScheduleResponseDto(schedule, totalSeats, availableSeats);
                })
                .collect(Collectors.toList());
    }
}
