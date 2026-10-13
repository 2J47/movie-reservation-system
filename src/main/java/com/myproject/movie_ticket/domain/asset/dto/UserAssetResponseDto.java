package com.myproject.movie_ticket.domain.asset.dto;

import com.myproject.movie_ticket.domain.coupon.dto.CouponResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserAssetResponseDto {
    private Integer balance;
    private List<CouponResponseDto> coupons;
}
