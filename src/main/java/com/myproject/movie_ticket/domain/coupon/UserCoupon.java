package com.myproject.movie_ticket.domain.coupon;

import com.myproject.movie_ticket.domain.user.User;
import com.myproject.movie_ticket.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(nullable = false)
    private boolean isUsed;

    private LocalDateTime usedAt; //사용 전일 때는 null

    @Builder
    public UserCoupon(User user, Coupon coupon, boolean isUsed) {
        this.user = user;
        this.coupon = coupon;
        this.isUsed = false;
    }

    public void use() {
        if (this.isUsed) {
            throw new IllegalArgumentException("이미 사용된 쿠폰입니다.");
        }
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }

    public void restore() {
        if (!this.isUsed) {
            throw new IllegalArgumentException("사용되지 않은 쿠폰입니다.");
        }
        this.isUsed = false;
        this.usedAt = null;
    }
}
