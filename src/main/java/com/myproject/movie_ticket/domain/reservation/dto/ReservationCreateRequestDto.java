package com.myproject.movie_ticket.domain.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreateRequestDto {
    private Long scheduleId;
    private List<Long> seatIds; //선택한 좌석id 목록
}
