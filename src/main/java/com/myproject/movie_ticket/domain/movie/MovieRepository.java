package com.myproject.movie_ticket.domain.movie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    //제목 검색
    List<Movie> findByTitleContaining(String keyword);

    //영화 동기화시 영화 중복 여부 확인
    Optional<Movie> findByKoficMovieCd(String koficMovieCd);

    //스케줄이 있는 영화만 중복없이
    @Query("SELECT DISTINCT s.movie FROM Schedule s WHERE s.startTime >= :now")
    List<Movie> findNowPlayingMovies(@Param("now") LocalDateTime now);

    //최근 등록된 영화 50개
    List<Movie> findTop50ByOrderByIdDesc();
}
