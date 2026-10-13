package com.myproject.movie_ticket.domain.coupon.dto;

import com.myproject.movie_ticket.domain.coupon.Coupon;
import com.myproject.movie_ticket.domain.coupon.DiscountType;
import com.myproject.movie_ticket.domain.coupon.UserCoupon;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CouponResponseDto {
    private Long id;
    private Long userCouponId;
    private String name;
    private DiscountType discountType;
    private Integer discountValue;
    private LocalDateTime expiresAt;

    public CouponResponseDto(Coupon coupon) {
        this.id = coupon.getId();
        this.name = coupon.getName();
        this.discountType = coupon.getDiscountType();
        this.discountValue = coupon.getDiscountValue();
        this.expiresAt = coupon.getExpiresAt();
    }

    public CouponResponseDto(UserCoupon userCoupon) {
        this.id = userCoupon.getCoupon().getId();
        this.userCouponId = userCoupon.getId();
        this.name = userCoupon.getCoupon().getName();
        this.discountType = userCoupon.getCoupon().getDiscountType();
        this.discountValue = userCoupon.getCoupon().getDiscountValue();
        this.expiresAt = userCoupon.getCoupon().getExpiresAt();
    }
}
