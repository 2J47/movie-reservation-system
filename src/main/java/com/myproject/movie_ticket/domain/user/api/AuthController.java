package com.myproject.movie_ticket.domain.user.api;

import com.myproject.movie_ticket.domain.user.dto.TokenReissueRequestDto;
import com.myproject.movie_ticket.domain.user.dto.TokenResponseDto;
import com.myproject.movie_ticket.domain.user.dto.UserLoginRequestDto;
import com.myproject.movie_ticket.domain.user.dto.UserSignupRequestDto;
import com.myproject.movie_ticket.domain.user.service.AuthService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody UserSignupRequestDto requestDto) {
        Long userId = authService.signup(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userId);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody UserLoginRequestDto requestDto) {
        TokenResponseDto responseDto = authService.login(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponseDto> reissue(@RequestBody TokenReissueRequestDto requestDto) {
        TokenResponseDto responseDto = authService.reissueToken(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Long userId) {
        if (userId != null) {
            authService.logout(userId);
        }
        return ResponseEntity.ok().build();
    }
}
