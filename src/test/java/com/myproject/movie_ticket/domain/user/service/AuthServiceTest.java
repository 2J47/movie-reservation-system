package com.myproject.movie_ticket.domain.user.service;

import com.myproject.movie_ticket.domain.asset.WalletRepository;
import com.myproject.movie_ticket.domain.user.RefreshToken;
import com.myproject.movie_ticket.domain.user.RefreshTokenRepository;
import com.myproject.movie_ticket.domain.user.User;
import com.myproject.movie_ticket.domain.user.UserRepository;
import com.myproject.movie_ticket.domain.user.dto.TokenReissueRequestDto;
import com.myproject.movie_ticket.domain.user.dto.TokenResponseDto;
import com.myproject.movie_ticket.domain.user.dto.UserLoginRequestDto;
import com.myproject.movie_ticket.domain.user.dto.UserSignupRequestDto;
import com.myproject.movie_ticket.domain.user.enums.UserRole;
import com.myproject.movie_ticket.domain.user.enums.UserStatus;
import com.myproject.movie_ticket.global.security.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtProvider jwtProvider;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입에 성공하면 유저와 지갑이 각각 1번씩 저장된다.")
    void 회원가입성공() {
        //given
        UserSignupRequestDto dto = new UserSignupRequestDto("test@test.com", "1234", "김회원", "010-1234-5678");

        User user = User.builder().email(dto.getEmail()).build();

        //이메일 중복 없는 설정
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(user);

        //when
        authService.signup(dto);

        //then user,wallet 리포지토리의 save가 각각 1번 호출되었는지 검증
        verify(userRepository, times(1)).save(any(User.class));
        verify(walletRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 가입 시도하면 예외가 발생한다.")
    void 회원가입실패() {

        //given
        UserSignupRequestDto dto = new UserSignupRequestDto("test@test.com", "1234", "김회원", "010-1234-5678");

        //이메일이 존재한다고 설정
        given(userRepository.existsByEmail(anyString())).willReturn(true);

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {authService.signup(dto);});

        //then
        assertThat(exception.getMessage()).isEqualTo("이미 가입된 이메일입니다.");
    }

    @Test
    @DisplayName("이메일과 비번이 맞으면 jwt 토큰이 발급된다.")
    void 로그인성공() {
        //given
        UserLoginRequestDto requestDto = new UserLoginRequestDto("test@test.com", "password123");
        User user = User.builder().email("test@test.com")
                .password("encodedPassword").build();

        ReflectionTestUtils.setField(user, "id", 1L);
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
        given(jwtProvider.createAccessToken(anyLong(), anyString())).willReturn("mock.jwt.token");

        //when
        TokenResponseDto response = authService.login(requestDto);

        //then
        assertThat(response.getAccessToken()).isEqualTo("mock.jwt.token");
    }

    @Test
    @DisplayName("비번이 틀리면 예외가 발생한다.")
    void 로그인실패() {
        //given
        UserLoginRequestDto requestDto = new UserLoginRequestDto("test@test.com", "password123");
        User user = User.builder().email("test@test.com")
                .password("encodedPassword").build();

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(any(CharSequence.class), anyString())).willReturn(false);

        //when&then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {authService.login(requestDto);
                });
        assertThat(exception.getMessage()).isEqualTo("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissueToken_Success() {
        //given
        Long userId = 1L;
        String oldRefreshToken = "old-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        TokenReissueRequestDto requestDto = new TokenReissueRequestDto();
        ReflectionTestUtils.setField(requestDto, "refreshToken", oldRefreshToken);

        User user = User.builder()
                .email("test@test.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);
        RefreshToken storedToken = new RefreshToken(userId, oldRefreshToken);

        given(jwtProvider.validateToken(oldRefreshToken)).willReturn(true);
        given(jwtProvider.getUserIdFromToken(oldRefreshToken)).willReturn(userId);
        given(refreshTokenRepository.findById(userId)).willReturn(Optional.of(storedToken));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        given(jwtProvider.createAccessToken(userId, user.getRole().name())).willReturn(newAccessToken);
        given(jwtProvider.createRefreshToken(userId)).willReturn(newRefreshToken);

        //when
        TokenResponseDto responseDto = authService.reissueToken(requestDto);

        //then
        assertThat(responseDto. getAccessToken()).isEqualTo(newAccessToken);
        assertThat(responseDto.getRefreshToken()).isEqualTo(newRefreshToken);
        assertThat(storedToken.getToken()).isEqualTo(newRefreshToken);
    }

    @Test
    @DisplayName("토큰 재발급 실패")
    void reissueToken_Fail_WithdrawnUser() {
        //given
        Long userId = 1L;
        String validRefreshToken = "valid-refresh-token";

        TokenReissueRequestDto requestDto = new TokenReissueRequestDto();
        ReflectionTestUtils.setField(requestDto, "refreshToken", validRefreshToken);

        User withdrawnUser = User.builder()
                .status(UserStatus.WITHDRAWN)
                .build();
        ReflectionTestUtils.setField(withdrawnUser, "id", userId);
        RefreshToken storedToken = new RefreshToken(userId, validRefreshToken);

        given(jwtProvider.validateToken(validRefreshToken)).willReturn(true);
        given(jwtProvider.getUserIdFromToken(validRefreshToken)).willReturn(userId);
        given(refreshTokenRepository.findById(userId)).willReturn(Optional.of(storedToken));
        given(userRepository.findById(userId)).willReturn(Optional.of(withdrawnUser));

        //when&then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.reissueToken(requestDto);
        });
        assertThat(exception.getMessage()).isEqualTo("탈퇴 처리된 사용자입니다.");
    }
}