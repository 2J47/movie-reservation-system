package com.myproject.movie_ticket.domain.movie;

import com.myproject.movie_ticket.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //좌석:상영관
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    private Theater theater;

    @Column(nullable = false, length = 10)
    private String seatNumber;

    @Column(nullable = false, length = 10)
    private String seatRow;

    @Column(nullable = false, length = 10)
    private Integer seatColumn;

    @Builder
    public Seat(Theater theater, String seatRow, Integer seatColumn) {
        this.theater = theater;
        this.seatRow = seatRow;
        this.seatColumn = seatColumn;
        this.seatNumber = seatRow + seatColumn;
    }
}
