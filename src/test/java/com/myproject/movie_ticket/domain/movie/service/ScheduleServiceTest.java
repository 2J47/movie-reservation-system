package com.myproject.movie_ticket.domain.movie.service;

import com.myproject.movie_ticket.domain.movie.Movie;
import com.myproject.movie_ticket.domain.movie.Schedule;
import com.myproject.movie_ticket.domain.movie.ScheduleRepository;
import com.myproject.movie_ticket.domain.movie.Theater;
import com.myproject.movie_ticket.domain.movie.dto.ScheduleResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    @DisplayName("특정 영화와 날짜로 시간표를 조회하면 DTO 리스트가 반환된다.")
    void 영화와_날짜로_시간표_조회() {
        //given
        Long movieId = 1L;
        LocalDate targetDate = LocalDate.of(2026, 7, 11);

        Movie movie = Movie.builder().title("토이 스토리5").build();
        Theater theater = Theater.builder().name("1관").build();

        Schedule schedule = Schedule.builder()
                .movie(movie)
                .theater(theater)
                .startTime(LocalDateTime.of(2026, 7, 11, 12, 30))
                .endTime(LocalDateTime.of(2026, 7, 11, 14, 22))
                .build();

        // 리포지토리가 이 메서드를 호출받으면, schedule이 담긴 리스트를 반환하라고 지시합니다.
        given(scheduleRepository.findAvailableSchedules(eq(movieId), any(LocalDateTime.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of(schedule));

        //when
        List<ScheduleResponseDto> result = scheduleService.getSchedulesByMovieAndDate(movieId, targetDate);

        //then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);

        assertThat(result.get(0).getTheaterName()).isEqualTo("1관");
        assertThat(result.get(0).getStartTime())
                .isEqualTo(LocalDateTime.of(2026, 7, 11, 12, 30));



    }
}