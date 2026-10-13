package com.myproject.movie_ticket.domain.movie.api;

import com.myproject.movie_ticket.domain.movie.dto.TheaterResponseDto;
import com.myproject.movie_ticket.domain.movie.service.TheaterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/theaters")
@RequiredArgsConstructor
public class TheaterController {
    private final TheaterService theaterService;

    @GetMapping
    public ResponseEntity<List<TheaterResponseDto>> getTheaters() {
        List<TheaterResponseDto> theaters = theaterService.getAllTheaters();
        return ResponseEntity.ok(theaters);
    }
}
