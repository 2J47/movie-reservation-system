package com.myproject.movie_ticket.domain.coupon;

import com.myproject.movie_ticket.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    //결제시 회원이 가지고 있는 사용하지 않은 쿠폰 리스트
    List<UserCoupon> findByUserIdAndIsUsedFalse(Long userId);

    // 유저와 쿠폰 정보로 이미 존재하는지 확인
    boolean existsByUserAndCoupon(User user, Coupon coupon);
}
