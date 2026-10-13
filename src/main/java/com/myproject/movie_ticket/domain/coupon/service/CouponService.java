package com.myproject.movie_ticket.domain.coupon.service;

import com.myproject.movie_ticket.domain.coupon.Coupon;
import com.myproject.movie_ticket.domain.coupon.CouponRepository;
import com.myproject.movie_ticket.domain.coupon.UserCoupon;
import com.myproject.movie_ticket.domain.coupon.UserCouponRepository;
import com.myproject.movie_ticket.domain.coupon.dto.CouponResponseDto;
import com.myproject.movie_ticket.domain.user.User;
import com.myproject.movie_ticket.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {
    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    //회원에게 쿠폰 발급
    @Transactional
    public void issueCoupon(Long userId, Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        if (coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("이미 만료된 쿠폰입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        if (userCouponRepository.existsByUserAndCoupon(user, coupon)) {
            throw new IllegalArgumentException("이미 발급받은 쿠폰입니다.");
        }

        //회원과 쿠폰 연결
        UserCoupon userCoupon = UserCoupon.builder()
                .user(user)
                .coupon(coupon)
                .build();

        userCouponRepository.save(userCoupon);
    }

    public List<CouponResponseDto> getAvailableEventCoupons() {
        return couponRepository.findByExpiresAtAfter(LocalDateTime.now())
                .stream()
                .map(CouponResponseDto::new)
                .collect(Collectors.toList());
    }

    //사용 가능한 쿠폰 리스트 조회
    public List<CouponResponseDto> getMyAvailableCoupons(Long userId) {
        return userCouponRepository.findByUserIdAndIsUsedFalse(userId)
                .stream()
                .map(CouponResponseDto::new)
                .collect(Collectors.toList());
    }

}
