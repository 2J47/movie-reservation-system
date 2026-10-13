package com.myproject.movie_ticket.domain.movie.service;

import com.myproject.movie_ticket.domain.movie.Schedule;
import com.myproject.movie_ticket.domain.movie.ScheduleRepository;
import com.myproject.movie_ticket.domain.movie.Seat;
import com.myproject.movie_ticket.domain.movie.SeatRepository;
import com.myproject.movie_ticket.domain.movie.dto.SeatResponseDto;
import com.myproject.movie_ticket.domain.reservation.ReservationSeatRepository;
import com.myproject.movie_ticket.domain.reservation.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {
    private final ScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final ReservationSeatRepository reservationSeatRepository;

    public List<SeatResponseDto> getSeatsWithStatus(Long scheduleId) {
        //스케줄 정보를 조회해 어떤 상영관인지 파악
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(()-> new IllegalArgumentException("해당 시간표를 찾을 수 없습니다."));

        Long theaterId = schedule.getTheater().getId();

        //해당 상영관의 모든 좌석 가져옴
        List<Seat> allSeats = seatRepository.findByTheaterId(theaterId);

        //해당 스케줄에서 이미 예약된 좌석 id 목록 가져옴
        List<Long> reservedSeatIds = reservationSeatRepository.findReservedSeatIdsByScheduleId(
                scheduleId,
                ReservationStatus.CANCELED);

        //모든 좌석 순회하며 예약된 id 목록에 포함되어있는지 확인
        return allSeats.stream()
                .map(seat -> {
                    boolean isAvailable = !reservedSeatIds.contains(seat.getId());
                    return new SeatResponseDto(seat, isAvailable);
                })
                .collect(Collectors.toList());
    }
}
