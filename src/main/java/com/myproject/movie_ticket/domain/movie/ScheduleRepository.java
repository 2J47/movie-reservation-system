package com.myproject.movie_ticket.domain.movie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("SELECT s FROM Schedule s " +
            "WHERE s.movie.id = :movieId " +
            "AND s.startTime >= :startOfDay " +
            "AND s.startTime <= :endOfDay " +
            "AND s.startTime > :now " +
            "ORDER BY s.startTime ASC")
    List<Schedule> findAvailableSchedules(
            @Param("movieId") Long movieId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("now") LocalDateTime now
    );

    //스케줄이 겹치는지 확인
    @Query("SELECT COUNT(s) > 0 FROM Schedule s WHERE s.theater.id = :theaterId " +
            "AND s.startTime < :endTime AND s.endTime > :startTime")
    boolean existsOverlappingSchedule(
            @Param("theaterId") Long theaterId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
