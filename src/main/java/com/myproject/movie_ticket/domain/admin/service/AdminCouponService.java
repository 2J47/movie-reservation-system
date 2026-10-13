package com.myproject.movie_ticket.domain.admin.service;

import com.myproject.movie_ticket.domain.coupon.*;
import com.myproject.movie_ticket.domain.coupon.dto.CouponCreateRequestDto;
import com.myproject.movie_ticket.domain.user.User;
import com.myproject.movie_ticket.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AdminCouponService {
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;

    //마스터 쿠폰 생성
    @Transactional
    public Long createCoupon(CouponCreateRequestDto requestDto) {

        if (requestDto.getDiscountType() == DiscountType.PERCENT && requestDto.getDiscountValue() > 100) {
            throw new IllegalArgumentException("정률 할인은 100%를 초과할 수 없습니다.");
        }

        LocalDateTime expiresAt = LocalDate.now()
                .plusDays(requestDto.getExpireDays())
                .atTime(LocalTime.MAX);

        Coupon coupon = Coupon.builder()
                .name(requestDto.getName())
                .discountType(requestDto.getDiscountType())
                .discountValue(requestDto.getDiscountValue())
                .expiresAt(expiresAt)
                .build();

        return couponRepository.save(coupon).getId();
    }

    @Transactional
    public void issueCouponToUser(Long couponId, Long userId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        UserCoupon userCoupon = UserCoupon.builder()
                .user(user)
                .coupon(coupon)
                .build();

        userCouponRepository.save(userCoupon);
    }
}
