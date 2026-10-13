package com.myproject.movie_ticket.domain.movie.api;

import com.myproject.movie_ticket.domain.movie.dto.MovieResponseDto;
import com.myproject.movie_ticket.domain.movie.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<List<MovieResponseDto>> getMovies(
            @RequestParam(value = "keyword", required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(movieService.getMovies(keyword));
        }
        return ResponseEntity.ok(movieService.getMovies(keyword));
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieResponseDto> getMovieById(@PathVariable("movieId") Long movieId) {
        return ResponseEntity.ok(movieService.getMovieById(movieId));
    }

    @GetMapping("/now-playing")
    public ResponseEntity<List<MovieResponseDto>> getNowPlayingMovies() {
        return ResponseEntity.ok(movieService.getNowPlayingMovies());
    }

}
