package com.myproject.movie_ticket.domain.movie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.myproject.movie_ticket.domain.movie.Seat;
import lombok.Getter;

@Getter
public class SeatResponseDto {
    private Long seatId;
    private String seatNumber;
    private String seatRow;
    private Integer seatColumn;
    @JsonProperty("isAvailable")
    private boolean isAvailable; //true 빈자리, false 예매 가능

    public SeatResponseDto(Seat seat, boolean isAvailable) {
        this.seatId = seat.getId();
        this.seatNumber = seat.getSeatNumber();
        this.seatRow = seat.getSeatRow();
        this.seatColumn = seat.getSeatColumn();
        this.isAvailable = isAvailable;
    }
}
