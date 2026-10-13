package com.myproject.movie_ticket.domain.coupon;


import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {


    List<Coupon> findByExpiresAtAfter(LocalDateTime now);

}
