package com.myproject.movie_ticket.domain.payment.api;

import com.myproject.movie_ticket.domain.payment.dto.PaymentRequestDto;
import com.myproject.movie_ticket.domain.payment.dto.PaymentResponseDto;
import com.myproject.movie_ticket.domain.payment.dto.PaymentVerifyRequestDto;
import com.myproject.movie_ticket.domain.payment.service.PaymentService;
import com.myproject.movie_ticket.domain.reservation.Reservation;
import com.myproject.movie_ticket.domain.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final ReservationRepository reservationRepository;

    @PostMapping("/verify")
    public ResponseEntity<PaymentResponseDto> verifyPayment(
            @AuthenticationPrincipal Long userId,
            @RequestBody PaymentVerifyRequestDto requestDto) {
        PaymentResponseDto responseDto = paymentService.verifyAndProcessPayment(userId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/refund/{reservationId}")
    public ResponseEntity<Void> refundPayment(
            @AuthenticationPrincipal Long userId,
            @PathVariable("reservationId") Long reservationId) {

        Reservation reservation = reservationRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("예매 내역을 찾을 수 없습니다."));
        if (!reservation.getUser().equals(userId)) {
            throw new IllegalArgumentException("본인의 예매내역만 취소할 수 있습니다.");
        }
        paymentService.refundPayment(reservation);
        return ResponseEntity.ok().build();
    }
}
