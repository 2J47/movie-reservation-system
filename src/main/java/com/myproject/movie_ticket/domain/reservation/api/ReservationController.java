package com.myproject.movie_ticket.domain.reservation.api;

import com.myproject.movie_ticket.domain.reservation.ReservationStatus;
import com.myproject.movie_ticket.domain.reservation.dto.MyReservationResponseDto;
import com.myproject.movie_ticket.domain.reservation.dto.ReservationCreateRequestDto;
import com.myproject.movie_ticket.domain.reservation.dto.ReservationResponseDto;
import com.myproject.movie_ticket.domain.reservation.dto.TicketResponseDto;
import com.myproject.movie_ticket.domain.reservation.service.ReservationLockFacade;
import com.myproject.movie_ticket.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;
    private final ReservationLockFacade reservationLockFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ReservationResponseDto> createReservation(
            @AuthenticationPrincipal Long userId,
            @RequestBody ReservationCreateRequestDto requestDto) {

        ReservationResponseDto responseDto = reservationLockFacade.reserve(userId, requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/{reservationNo}")
    public ResponseEntity<ReservationResponseDto> getReservationDetail(
            @PathVariable("reservationNo") String reservationNo) {
        ReservationResponseDto responseDto = reservationService.getReservationDetail(reservationNo);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/{reservationNo}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @AuthenticationPrincipal Long userId,
            @PathVariable("reservationNo") String reservationNo) {

        reservationService.cancelReservation(userId, reservationNo);

        return ResponseEntity.noContent().build();
    }


    @GetMapping("/me")
    public ResponseEntity<List<MyReservationResponseDto>> getMyReservations(
            @AuthenticationPrincipal Long userId) {
        List<MyReservationResponseDto> reservations = reservationService.getMyReservations(userId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/me/upcoming")
    public ResponseEntity<List<TicketResponseDto>> getUpcomingTickets(@AuthenticationPrincipal Long userId) {
       return ResponseEntity.ok(reservationService.getUpcomingTickets(userId));
    }
}

