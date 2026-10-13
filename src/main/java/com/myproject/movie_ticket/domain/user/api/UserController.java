package com.myproject.movie_ticket.domain.user.api;

import com.myproject.movie_ticket.domain.reservation.dto.MyReservationResponseDto;
import com.myproject.movie_ticket.domain.reservation.dto.ReservationResponseDto;
import com.myproject.movie_ticket.domain.reservation.service.ReservationService;
import com.myproject.movie_ticket.domain.user.dto.PasswordUpdateRequestDto;
import com.myproject.movie_ticket.domain.user.dto.UserProfileResponseDto;
import com.myproject.movie_ticket.domain.user.dto.UserUpdateDto;
import com.myproject.movie_ticket.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<UserProfileResponseDto> getUserProfile(
            @AuthenticationPrincipal Long userId){

        UserProfileResponseDto responseDto = userService.getUserProfile(userId);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping
    public ResponseEntity<Void> updateProfile(@AuthenticationPrincipal Long userId,
                                          @RequestBody UserUpdateDto requestDto) {
        userService.updateMyProfile(userId, requestDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> withdrawUser(@AuthenticationPrincipal Long userId) {
        userService.withdrawUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal Long userId,
                                               @RequestBody PasswordUpdateRequestDto requestDto) {
        userService.updatePassword(userId, requestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<MyReservationResponseDto>> getMyReservations(
            @AuthenticationPrincipal Long userId) {
        List<MyReservationResponseDto> reservations = reservationService.getMyReservations(userId);
        return ResponseEntity.ok(reservations);
    }
}
