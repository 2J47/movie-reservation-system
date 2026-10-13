package com.myproject.movie_ticket.domain.asset.service;

import com.myproject.movie_ticket.domain.coupon.*;
import com.myproject.movie_ticket.domain.coupon.dto.CouponResponseDto;
import com.myproject.movie_ticket.domain.coupon.service.CouponService;
import com.myproject.movie_ticket.domain.user.User;
import com.myproject.movie_ticket.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CouponRepository couponRepository;
    @Mock
    private UserCouponRepository userCouponRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    @DisplayName("회원에게 쿠폰을 발급한다.")
    void 쿠폰발급성공() {
        //given
        User user = User.builder().build();
        Coupon coupon = Coupon.builder().name("가입 축하 쿠폰").build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(couponRepository.findById(anyLong())).willReturn(Optional.of(coupon));

        //when
        couponService.issueCoupon(1L, 1L);

        //then
        verify(userCouponRepository, times(1)).save(any(UserCoupon.class));
    }

    @Test
    @DisplayName("회원의 사용 가능한 쿠폰 목록을 조회한다.")
    void 쿠폰조회성공() {
        //given
        User user = User.builder().build();
        Coupon coupon = Coupon.builder()
                .name("5000원 할인")
                .discountType(DiscountType.AMOUNT)
                .discountValue(5000)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        UserCoupon userCoupon = UserCoupon.builder()
                .user(user)
                .coupon(coupon)
                .build();

        given(userCouponRepository.findByUserIdAndIsUsedFalse(anyLong()))
                .willReturn(List.of(userCoupon));

        //when
        List<CouponResponseDto> result = couponService.getMyAvailableCoupons(1L);

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("5000원 할인");
        assertThat(result.get(0).getDiscountValue()).isEqualTo(5000);
    }
}