package com.myproject.movie_ticket.domain.movie.service;

import com.myproject.movie_ticket.domain.movie.*;
import com.myproject.movie_ticket.domain.movie.dto.SeatResponseDto;
import com.myproject.movie_ticket.domain.reservation.ReservationSeatRepository;
import com.myproject.movie_ticket.domain.reservation.ReservationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ReservationSeatRepository reservationSeatRepository;

    @InjectMocks
    private SeatService seatService;

    @Test
    @DisplayName("상영 시간표의 좌석 조회시 예약된 좌석은 isAvailable이 false로 반환된다.")
    void 좌석상태조회성공() {
        //given
        Theater theater = Theater.builder().name("1관").build();
        ReflectionTestUtils.setField(theater, "id", 1L); //id강제 주입

        Schedule schedule = Schedule.builder().theater(theater).build();

        //1관에 A1, A2 두 개의 좌석이 있다고 가정
        Seat seat1 = Seat.builder()
                .seatRow("A")
                .seatColumn(1)
                .build();
        ReflectionTestUtils.setField(seat1, "id", 10L);

        Seat seat2 = Seat.builder()
                .seatRow("A")
                .seatColumn(2)
                .build();
        ReflectionTestUtils.setField(seat2, "id", 20L);

        given(scheduleRepository.findById(anyLong())).willReturn(Optional.of(schedule));
        given(seatRepository.findByTheaterId(1L)).willReturn(List.of(seat1, seat2));

        //A2 좌석은 예매되었다고 가정
        given(reservationSeatRepository.findReservedSeatIdsByScheduleId(anyLong(), ReservationStatus.CANCELED))
                .willReturn(List.of(20L));

        //when
        List<SeatResponseDto> result = seatService.getSeatsWithStatus(1L);

        //then
        assertThat(result).hasSize(2);

        //A1은 빈자리
        assertThat(result.get(0).getSeatNumber()).isEqualTo("A1");
        assertThat(result.get(0).isAvailable()).isTrue();

        //A2는 예매된 자리
        assertThat(result.get(1).getSeatNumber()).isEqualTo("A2");
        assertThat(result.get(1).isAvailable()).isFalse();
    }

}