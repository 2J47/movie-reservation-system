package com.myproject.movie_ticket.domain.coupon;

import com.myproject.movie_ticket.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType discountType; //AMOUNT, PERCENT

    @Column(nullable = false)
    private Integer discountValue;

    @Column(nullable = false)
    private LocalDateTime expiresAt; //만료일

    @Builder

    public Coupon(String name, DiscountType discountType, Integer discountValue, LocalDateTime expiresAt) {
        this.name = name;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.expiresAt = expiresAt;
    }

    public int calculateDiscountAmount(int originalPrice) {
        int rawDiscount = 0;
        if (this.discountType == DiscountType.PERCENT) {
            rawDiscount = (int) (originalPrice * (this.discountValue / 100.0));
        } else {
            rawDiscount = this.discountValue;
        }
        return Math.min(Math.max(rawDiscount, 0), originalPrice);
    }

}
