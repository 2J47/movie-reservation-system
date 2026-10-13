package com.myproject.movie_ticket.domain.movie.dto;

import com.myproject.movie_ticket.domain.movie.Movie;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class MovieResponseDto implements Serializable {
    private Long id;
    private String koficMovieCd;
    private String title;
    private Integer duration;
    private String rating;
    private String posterUrl;
    private String description;

    public MovieResponseDto(Movie movie) {
        this.id = movie.getId();
        this.koficMovieCd = movie.getKoficMovieCd();
        this.title = movie.getTitle();
        this.duration = movie.getDuration();
        this.rating = movie.getRating();
        this.posterUrl = movie.getPosterUrl();
        this.description = movie.getDescription();
    }
}
