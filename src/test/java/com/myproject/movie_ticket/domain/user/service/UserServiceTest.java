package com.myproject.movie_ticket.domain.user.service;

import com.myproject.movie_ticket.domain.asset.Wallet;
import com.myproject.movie_ticket.domain.asset.WalletRepository;
import com.myproject.movie_ticket.domain.user.User;
import com.myproject.movie_ticket.domain.user.UserRepository;
import com.myproject.movie_ticket.domain.user.dto.UserProfileResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("유저 ID로 프로필을 조회하면 유저 정보와 지갑 잔액이 포함된 DTO를 반환한다.")
    void 회원정보조회성공() {
        //given
        Long userId = 1L;

        User user = User.builder()
                .email("test@email.com")
                .name("김회원")
                .phoneNumber("010-1234-5678")
                .build();

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(50000)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(walletRepository.findByUserId(userId)).willReturn(Optional.of(wallet));

        //when
        UserProfileResponseDto result = userService.getUserProfile(userId);

        //then
        assertThat(result.getEmail()).isEqualTo("test@email.com");
        assertThat(result.getName()).isEqualTo("김회원");
        assertThat(result.getBalance()).isEqualTo(50000);


    }

    @Test
    @DisplayName("존재하지 않는 유저 ID로 조회하면 예외가 발생한다.")
    void 회원정보조회실패() {
        //given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        //when, then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserProfile(999L);
        });
    }

}