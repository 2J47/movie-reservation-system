package com.myproject.movie_ticket.domain.admin.service;

import com.myproject.movie_ticket.domain.movie.*;
import com.myproject.movie_ticket.domain.movie.dto.MovieResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleSyncService {
    private final MovieRepository movieRepository;
    private final ScheduleRepository scheduleRepository;
    private final TheaterRepository theaterRepository;

    @Transactional(readOnly = true)
    public List<MovieResponseDto> getAdminMovieList() {
        List<Movie> movies = movieRepository.findTop50ByOrderByIdDesc();

        return movies.stream().map(MovieResponseDto::new).toList();

    }

    @Transactional
    public void generateSchedules(List<Long> movieIds, Long theaterId, LocalDate targetDate) {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new IllegalArgumentException("상영관이 존재하지 않습니다."));

        LocalDateTime currentTime = targetDate.atTime(9, 0);
        LocalDateTime endTimeLimit = targetDate.atTime(23, 59);

        List<Schedule> newSchedules = new ArrayList<>();

        for (Long movieId : movieIds) {
            Movie movie = movieRepository.findById(movieId)
                    .orElseThrow(() -> new IllegalArgumentException("영화 ID " + movieId + "를 찾을 수 없습니다."));

            LocalDateTime endTime = currentTime.plusMinutes(movie.getDuration());

            if (endTime.isAfter(endTimeLimit)) {
                throw new IllegalArgumentException(
                        String.format("스케줄 배정 실패: [%s]의 상영 종료 시간(%s)이 자정을 초과합니다. 리스트를 줄여주세요.",
                                movie.getTitle(), endTime.toLocalTime())
                );
            }

            if (scheduleRepository.existsOverlappingSchedule(theaterId, currentTime, endTime)) {
                throw new IllegalArgumentException("해당 시간에 이미 다른 스케줄이 배정되어 있습니다: " + currentTime);
            }

            Schedule schedule = Schedule.builder()
                    .movie(movie)
                    .theater(theater)
                    .startTime(currentTime)
                    .endTime(endTime)
                    .build();
            newSchedules.add(schedule);

            //다음 회차 시작 시간
            currentTime = endTime.plusMinutes(20);
        }

        scheduleRepository.saveAll(newSchedules);
        log.info("{} 상영관에 {}일자 스케줄 {}건 생성완료", theater.getName(), targetDate, newSchedules.size());
    }
}
