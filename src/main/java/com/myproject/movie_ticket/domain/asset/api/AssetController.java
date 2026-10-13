package com.myproject.movie_ticket.domain.asset.api;

import com.myproject.movie_ticket.domain.coupon.dto.CouponResponseDto;
import com.myproject.movie_ticket.domain.asset.dto.UserAssetResponseDto;
import com.myproject.movie_ticket.domain.asset.dto.WalletChargeRequestDto;
import com.myproject.movie_ticket.domain.asset.dto.WalletResponseDto;
import com.myproject.movie_ticket.domain.coupon.service.CouponService;
import com.myproject.movie_ticket.domain.asset.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AssetController {

    private final WalletService walletService;
    private final CouponService couponService;

    //지갑&쿠폰 조회
    @GetMapping("/me/assets")
    public ResponseEntity<UserAssetResponseDto> getMyAssets(
            @AuthenticationPrincipal Long userId) {

        Integer balance = walletService.getBalance(userId);
        List<CouponResponseDto> coupons = couponService.getMyAvailableCoupons(userId);

        UserAssetResponseDto responseDto = new UserAssetResponseDto(balance, coupons);

        return ResponseEntity.ok(responseDto);
    }

    //쿠폰 발급
    @PostMapping("/me/assets/coupons/{couponId}")
    public ResponseEntity<Void> issueCoupon(
            @AuthenticationPrincipal Long userId,
            @PathVariable("couponId") Long couponId) {

        couponService.issueCoupon(userId, couponId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //포인트 충전
    @PostMapping("/me/assets/wallet/charge")
    public ResponseEntity<WalletResponseDto> chargeWallet(
            @AuthenticationPrincipal Long userId,
            @RequestBody WalletChargeRequestDto requestDto) {
        WalletResponseDto responseDto = walletService.chargeBalance(userId, requestDto);
        return ResponseEntity.ok(responseDto);
    }
}
