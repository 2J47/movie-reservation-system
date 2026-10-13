package com.myproject.movie_ticket.domain.coupon.api;

import com.myproject.movie_ticket.domain.coupon.dto.CouponResponseDto;
import com.myproject.movie_ticket.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<List<CouponResponseDto>> getCoupons() {
        List<CouponResponseDto> coupons = couponService.getAvailableEventCoupons();
        return ResponseEntity.ok(coupons);
    }
}
