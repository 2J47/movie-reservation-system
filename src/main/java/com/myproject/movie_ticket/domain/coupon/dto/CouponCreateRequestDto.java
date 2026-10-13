package com.myproject.movie_ticket.domain.coupon.dto;

import com.myproject.movie_ticket.domain.coupon.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CouponCreateRequestDto {
    private String name;
    private DiscountType discountType;
    private Integer discountValue;
    private int expireDays;

    public CouponCreateRequestDto(CouponCreateRequestDto requestDto) {
        this.name = requestDto.getName();
        this.discountType = requestDto.getDiscountType();
        this.discountValue = requestDto.getDiscountValue();
        this.expireDays = requestDto.getExpireDays();
    }
}
