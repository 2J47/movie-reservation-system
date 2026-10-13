package com.myproject.movie_ticket.domain.asset.service;

import com.myproject.movie_ticket.domain.asset.Wallet;
import com.myproject.movie_ticket.domain.asset.WalletRepository;
import com.myproject.movie_ticket.domain.asset.dto.WalletChargeRequestDto;
import com.myproject.movie_ticket.domain.asset.dto.WalletResponseDto;
import com.myproject.movie_ticket.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    @DisplayName("정상적인 금액을 충전하면 잔액이 증가한다.")
    void 지갑충전성공() {
        //given
        Long userId = 1L;
        WalletChargeRequestDto requestDto = new WalletChargeRequestDto(10000);

        User user = User.builder().build();
        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(5000)
                .build();

        given(walletRepository.findByUserId(userId)).willReturn(Optional.of(wallet));

        //when
        WalletResponseDto result = walletService.chargeBalance(userId, requestDto);

        //then 기존 5000+충전 10000 = 15000
        assertThat(result.getBalance()).isEqualTo(15000);
    }

    @Test
    @DisplayName("0원 이하의 금액을 충전하려 하면 예외가 발생한다.")
    void 지갑충전실패() {
        //given
        Long userId = 1L;
        WalletChargeRequestDto requestDto = new WalletChargeRequestDto(-5000);

        User user = User.builder().build();
        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(5000)
                .build();

        given(walletRepository.findByUserId(userId)).willReturn(Optional.of(wallet));

        //when&then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            walletService.chargeBalance(userId, requestDto);
        });

        assertThat(exception.getMessage()).isEqualTo("충전 금액은 0원보다 커야 합니다.");
    }

}