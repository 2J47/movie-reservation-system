package com.myproject.movie_ticket.domain.reservation;

import com.myproject.movie_ticket.domain.movie.Schedule;
import com.myproject.movie_ticket.domain.movie.Seat;
import com.myproject.movie_ticket.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservation_seats", uniqueConstraints = {
        @UniqueConstraint(name = "uk_schedule_seat", columnNames = {"schedule_id", "seat_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationSeat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //예매_좌석:예매
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private  Reservation reservation;

    //예매_좌석:좌석
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    //동시성 제어를 위해
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Builder
    public ReservationSeat(Reservation reservation, Seat seat, Schedule schedule) {
        this.reservation = reservation;
        this.seat = seat;
        this.schedule = schedule;
    }
}
