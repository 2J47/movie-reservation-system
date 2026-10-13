package com.myproject.movie_ticket.domain.movie.service;

import com.myproject.movie_ticket.domain.movie.TheaterRepository;
import com.myproject.movie_ticket.domain.movie.dto.TheaterResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;

    @Transactional(readOnly = true)
    public List<TheaterResponseDto> getAllTheaters() {
        return theaterRepository.findAll().stream()
                .map(theater -> new TheaterResponseDto(
                        theater.getId(),
                        theater.getName(),
                        theater.getTotalSeats()
                )).toList();
    }
}
