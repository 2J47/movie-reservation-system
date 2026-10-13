package com.myproject.movie_ticket.domain.movie.service;

import com.myproject.movie_ticket.domain.movie.Movie;
import com.myproject.movie_ticket.domain.movie.MovieRepository;
import com.myproject.movie_ticket.domain.movie.dto.MovieResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieService {
    private final MovieRepository movieRepository;

    //전체 상영작 조회 키워드
    public List<MovieResponseDto> getMovies(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) { //키워드 없을 때 전체 조회
            return movieRepository.findAll().stream()
                    .map(MovieResponseDto::new)
                    .collect(Collectors.toList());
        } else { //키워드 있을 때
            return movieRepository.findByTitleContaining(keyword).stream()
                    .map(MovieResponseDto::new)
                    .collect(Collectors.toList());
        }
    }

    //영화 단건 상세 조회
    public MovieResponseDto getMovieById(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("해당 영화를 찾을 수 없습니다."));
        return new MovieResponseDto(movie);
    }

    //전체 상영작 조회
    @Cacheable(value = "nowPlayingMovies")
    public List<MovieResponseDto> getNowPlayingMovies() {
        List<Movie> movies = movieRepository.findNowPlayingMovies(LocalDateTime.now());

        return movies.stream()
                .map(MovieResponseDto::new).toList();
    }
}
