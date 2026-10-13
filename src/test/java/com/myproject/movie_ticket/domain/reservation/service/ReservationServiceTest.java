package com.myproject.movie_ticket.domain.reservation.service;

import com.myproject.movie_ticket.domain.movie.*;
import com.myproject.movie_ticket.domain.reservation.*;
import com.myproject.movie_ticket.domain.reservation.dto.ReservationCreateRequestDto;
import com.myproject.movie_ticket.domain.reservation.dto.ReservationResponseDto;
import com.myproject.movie_ticket.domain.user.User;
import com.myproject.movie_ticket.domain.user.UserRepository;
import com.myproject.movie_ticket.domain.user.enums.UserRole;
import com.myproject.movie_ticket.domain.user.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private ReservationSeatRepository reservationSeatRepository;
    @Mock private MovieRepository movieRepository;
    @Mock private TheaterRepository theaterRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    @DisplayName("빈 좌석을 선택하면 정상적으로 예매(대기)가 생성되고 총액이 계산된다.")
    void 예매성공() {
        //given
        ReservationCreateRequestDto requestDto = new ReservationCreateRequestDto(1L, List.of(10L, 20L));

        User user = User.builder().build();
        Schedule schedule = Schedule.builder().build();
        Seat seat1 = Seat.builder().build();
        Seat seat2 = Seat.builder().build();

        Reservation reservation = Reservation.builder()
                .user(user)
                .schedule(schedule)
                .totalPrice(30000)
                .build();

        //중복 예매 아니라는 것 가정
        given(reservationSeatRepository.existsValidReservation(anyLong(), anyList(), ReservationStatus.CANCELED)).willReturn(false);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(scheduleRepository.findById(anyLong())).willReturn(Optional.of(schedule));
        given(seatRepository.findAllById(anyList())).willReturn(List.of(seat1, seat2));
        given(reservationRepository.save(any(Reservation.class))).willReturn(reservation);

        //when
        ReservationResponseDto result = reservationService.createReservation(1L, requestDto);

        //then
        assertThat(result.getTotalPrice()).isEqualTo(30000);
        assertThat(result.getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("이미 누군가가 선점한 좌석을 예매하려고 하면 예외가 발생한다.")
    void 예매실패() {
        //given
        ReservationCreateRequestDto requestDto = new ReservationCreateRequestDto(1L, List.of(10L, 20L));

        //해당 좌석이 예매되었다고 가정
        given(reservationSeatRepository.existsValidReservation(anyLong(), anyList(), ReservationStatus.CANCELED)).willReturn(true);

        //when&then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                reservationService.createReservation(1L, requestDto));

        assertThat(exception.getMessage()).isEqualTo("이미 예매가 진행 중이거나 완료된 좌석이 포함되어 있습니다.");
    }

    @Test
    @DisplayName("상영 시작 14분 전에 예매를 취소하려고 하면 실패(예외 발생)해야 한다.")
    void cancelReservation_Fail_WhenDeadlinePassed() {
        //given
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", 1L);

        LocalDateTime startTime = LocalDateTime.now().plusMinutes(14);
        Schedule schedule = Schedule.builder()
                .startTime(startTime)
                .build();

        String reservationNo = "TKT-FAIL-14MIN";
        Reservation reservation = Reservation.builder()
                .user(user)
                .schedule(schedule)
                .totalPrice(15000)
                .build();
        ReflectionTestUtils.setField(reservation, "reservationNo", reservationNo);

        given(reservationRepository.findByReservationNo(reservationNo))
                .willReturn(Optional.of(reservation));

        //when&then
        assertThatThrownBy(() -> reservationService.cancelReservation(user.getId(), reservationNo))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("상영 시작 15분 전까지만 예매 취소가 가능합니다.");
    }

    @ParameterizedTest
    @CsvSource({
            "16, true",  // 16분 전, 예외 없이 통과
            "15, true",  // 15분 전, 예외 없이 통과
            "14, false"  // 14분 전, 정책 위반으로 예외 발생
    })
    @DisplayName("상영 시작 시간에 따른 예매 취소 정책 경계값을 검증한다.")
    void cancelReservation_BoundaryTest(int remainingMinutes, boolean shouldSuccess) {
        //given
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", 1L);
        LocalDateTime startTime = LocalDateTime.now().plusMinutes(remainingMinutes)
                .plusSeconds(10); //테스트-서비스 실행 사이 시간 차이로 10초 버퍼 더함
        Schedule schedule = Schedule.builder()
                .startTime(startTime)
                .build();

        String reservationNo = "TKT-TEST";
        Reservation reservation = Reservation.builder()
                .user(user)
                .schedule(schedule)
                .build();
        ReflectionTestUtils.setField(reservation, "reservationNo", reservationNo);
        given(reservationRepository.findByReservationNo(reservationNo))
                .willReturn(Optional.of(reservation));

        //when&then
        if (shouldSuccess) { //15, 16분 성공
            assertDoesNotThrow(() -> reservationService.cancelReservation(reservation.getUser().getId(), reservationNo));
        } else {
            assertThatThrownBy(() -> reservationService.cancelReservation(reservation.getUser().getId(), reservationNo)
                    )
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("상영 시작 15분 전까지만 예매 취소가 가능합니다.");
        }
    }
}