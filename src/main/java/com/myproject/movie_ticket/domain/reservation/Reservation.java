package com.myproject.movie_ticket.domain.reservation;

import com.myproject.movie_ticket.domain.movie.Schedule;
import com.myproject.movie_ticket.domain.user.User;
import com.myproject.movie_ticket.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String reservationNo;

    //예매:회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //예매:시간표
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(nullable = false)
    private Integer totalPrice;

    @Builder
    public Reservation(User user, Schedule schedule, Integer totalPrice) {
        this.reservationNo = UUID.randomUUID().toString(); //자동 난수
        this.user = user;
        this.schedule = schedule;
        this.totalPrice = totalPrice;
        this.status = ReservationStatus.PENDING; //초기 상태는 대기
    }

    // 💡 DDD 비즈니스 메서드: 상태 변경 로직을 캡슐화합니다.
    public void confirm() {
        if (this.status != ReservationStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 예매만 확정할 수 있습니다.");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == ReservationStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 예매입니다.");
        }
        this.status = ReservationStatus.CANCELED;
    }
}
