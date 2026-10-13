package com.myproject.movie_ticket.domain.user.service;

import com.myproject.movie_ticket.domain.asset.Wallet;
import com.myproject.movie_ticket.domain.asset.WalletRepository;
import com.myproject.movie_ticket.domain.reservation.ReservationRepository;
import com.myproject.movie_ticket.domain.reservation.ReservationStatus;
import com.myproject.movie_ticket.domain.user.User;
import com.myproject.movie_ticket.domain.user.UserRepository;
import com.myproject.movie_ticket.domain.user.dto.PasswordUpdateRequestDto;
import com.myproject.movie_ticket.domain.user.dto.UserProfileResponseDto;
import com.myproject.movie_ticket.domain.user.dto.UserUpdateDto;
import com.myproject.movie_ticket.domain.user.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    private User getActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new IllegalArgumentException("탈퇴한 사용자입니다.");
        }
        return user;
    }

    //마이페이지 내 정보 조회
    public UserProfileResponseDto getUserProfile(Long userId) {
        User user = getActiveUser(userId);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("지갑 정보를 찾을 수 없습니다."));

        return new UserProfileResponseDto(user, wallet);
    }

    @Transactional
    public UserProfileResponseDto updateMyProfile(Long userId, UserUpdateDto requestDto) {
        User user = getActiveUser(userId);

        user.updateProfile(requestDto.getName(), requestDto.getPhoneNumber());

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("지갑 정보를 찾을 수 없습니다."));

        return new UserProfileResponseDto(user, wallet);
    }

    @Transactional
    public void updatePassword(Long userId, PasswordUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        //기존 비밀번호 확인
        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }
        String encodedNewPassword = passwordEncoder.encode(requestDto.getNewPassword());
        user.updatePassword(encodedNewPassword);
    }

    @Transactional
    public void withdrawUser(Long userId) {
        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 이미 탈퇴한 사용자입니다."));

        boolean hasActiveReservations = reservationRepository.existsByUserIdAndStatusAndSchedule_StartTimeAfter(
                userId, ReservationStatus.CONFIRMED, LocalDateTime.now());

        if (hasActiveReservations) {
            throw new IllegalArgumentException("아직 관람하지 않은 예매 내역이 있어 탈퇴할 수 없습니다.");
        }
        walletRepository.findByUserId(userId).ifPresent(Wallet::clearBalance);
        user.withdraw();
    }
}
