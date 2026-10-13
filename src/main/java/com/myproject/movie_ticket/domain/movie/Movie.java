package com.myproject.movie_ticket.domain.movie;

import com.myproject.movie_ticket.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Movie extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //영진위 영화 고유 코드
    @Column(unique = true)
    private String koficMovieCd;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false, length = 20)
    private String rating; //관람 등급(ALL, 12, 15, 18)

    private String posterUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder

    public Movie(String koficMovieCd, String title, Integer duration, String rating, String posterUrl, String description) {
        this.koficMovieCd = koficMovieCd;
        this.title = title;
        this.duration = duration;
        this.rating = rating;
        this.posterUrl = posterUrl;
        this.description = description;
    }

    public void updateMovieData(Integer duration, String rating, String posterUrl, String description) {
        this.duration = duration;
        this.rating = rating;
        this.posterUrl = posterUrl;
        this.description = description;
    }
}
