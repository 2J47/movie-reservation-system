package com.myproject.movie_ticket.domain.user.service;

import com.myproject.movie_ticket.domain.asset.Wallet;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public Long signup(UserSignupRequestDto requestDto) {
        //이메일 중복 확인
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        //User 엔티티 저장
        User user = User.builder()
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .name(requestDto.getName())
                .phoneNumber(requestDto.getPhoneNumber())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        //유저의 빈 지갑 생성
        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .balance(0)
                .build();
        walletRepository.save(wallet);

        return savedUser.getId();
    }

    @Transactional(readOnly = true)
    public TokenResponseDto login(UserLoginRequestDto requestDto) {

        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new IllegalArgumentException("탈퇴 처리된 회원입니다.");
        }

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        //Access & Refresh Token 모두 생성
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        //발급한 Refresh Token을 DB에 저장 (기존에 있으면 업데이트, 없으면 새로 생성)
        RefreshToken storedToken = refreshTokenRepository.findById(user.getId())
                .orElse(new RefreshToken(user.getId(), refreshToken));
        storedToken.updateToken(refreshToken);
        refreshTokenRepository.save(storedToken);

        return new TokenResponseDto(accessToken, refreshToken);
    }

    public TokenResponseDto reissueToken(TokenReissueRequestDto requestDto) {
        String refreshToken = requestDto.getRefreshToken();
        //리프레시 토큰 유효성 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 리프레시 토큰입니다.");
        }
        Long userId = jwtProvider.getUserIdFromToken(refreshToken);
        //DB 저장된 실제 토큰과 비교
        RefreshToken storedToken = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("로그인 기록이 존재하지 않습니다."));
        if (!storedToken.getToken().equals(refreshToken)) {
            throw new IllegalArgumentException("토큰 정보가 일치하지 않습니다. 다시 로그인해주세요.");
        }
        //유저 상태 재검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new IllegalArgumentException("탈퇴 처리된 사용자입니다.");
        }
        //토큰 재발급
        String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getId());

        storedToken.updateToken(newRefreshToken);
        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.findById(userId)
                .ifPresent(refreshTokenRepository::delete);
    }

}
