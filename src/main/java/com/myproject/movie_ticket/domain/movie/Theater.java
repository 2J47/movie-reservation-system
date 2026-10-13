package com.myproject.movie_ticket.domain.movie;

import com.myproject.movie_ticket.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "theaters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Theater extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Integer totalSeats;

    @Builder
    public Theater(String name, Integer totalSeats) {

        this.name = name;
        this.totalSeats = totalSeats;
    }
}
