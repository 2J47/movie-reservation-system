package com.myproject.movie_ticket.domain.reservation.service;

import com.myproject.movie_ticket.domain.asset.Wallet;
import com.myproject.movie_ticket.domain.asset.WalletRepository;
import com.myproject.movie_ticket.domain.movie.Schedule;
import com.myproject.movie_ticket.domain.movie.ScheduleRepository;
import com.myproject.movie_ticket.domain.movie.Seat;
import com.myproject.movie_ticket.domain.movie.SeatRepository;
import com.myproject.movie_ticket.domain.payment.Payment;
import com.myproject.movie_ticket.domain.payment.PaymentRepository;
import com.myproject.movie_ticket.domain.payment.service.PaymentService;
import com.myproject.movie_ticket.domain.payment.service.PortOneService;
import com.myproject.movie_ticket.domain.reservation.*;
import com.myproject.movie_ticket.domain.reservation.dto.MyReservationResponseDto;
import com.myproject.movie_ticket.domain.reservation.dto.ReservationCreateRequestDto;
import com.myproject.movie_ticket.domain.reservation.dto.ReservationResponseDto;
import com.myproject.movie_ticket.domain.reservation.dto.TicketResponseDto;
import com.myproject.movie_ticket.domain.user.User;
import com.myproject.movie_ticket.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    private static final int TICKET_PRICE =15000;
    private static final int DEADLINE_MINUTES = 15;

    @Transactional
    public ReservationResponseDto createReservation(Long userId, ReservationCreateRequestDto requestDto) {

        //데이터 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        Schedule schedule = scheduleRepository.findById(requestDto.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("시간표를 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = schedule.getStartTime().minusMinutes(DEADLINE_MINUTES);

        if (now.isAfter(deadline)) {
            throw new IllegalStateException("상영 시작 15분 전까지만 예매가 가능합니다.");
        }

        List<Long> sortedSeatIds = requestDto.getSeatIds().stream()
                .sorted()
                .collect(Collectors.toList());

        List<Seat> seats = new ArrayList<>();
        //비관적 락+좌석 조회
        for (Long seatId : sortedSeatIds) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 좌석 ID입니다."));
            seats.add(seat);
        }
        //중복 예매 검증
        boolean isAlreadyReserved = reservationSeatRepository.existsValidReservation(
                requestDto.getScheduleId(),
                requestDto.getSeatIds(),
                ReservationStatus.CANCELED
        );
        if (isAlreadyReserved) {
            throw new IllegalArgumentException("이미 예매가 진행 중이거나 완료된 좌석이 포함되어있습니다.");
        }

        //총 결제 금액 계산
        int totalPrice = seats.size() * TICKET_PRICE;

        //예매 데이터 생성 및 저장, PENDING
        Reservation reservation = Reservation.builder()
                .user(user)
                .schedule(schedule)
                .totalPrice(totalPrice)
                .build();
        Reservation savedReservation = reservationRepository.save(reservation);

        //ReservationSeat 매핑 데이터 생성 및 저장
        for (Seat seat : seats) {
            ReservationSeat reservationSeat = ReservationSeat.builder()
                    .reservation(savedReservation)
                    .seat(seat)
                    .schedule(schedule)
                    .build();
            reservationSeatRepository.save(reservationSeat);
        }
        //예매 완료 결과
        return new ReservationResponseDto(savedReservation);
    }

    //내 전체 예매 내역 조회
    @Transactional(readOnly = true)
    public List<MyReservationResponseDto> getMyReservations(Long userId) {
        List<Reservation> reservations = reservationRepository.findValidReservationsByUserId(
                userId, ReservationStatus.PENDING);

        return reservations.stream().map(reservation -> {
            List<ReservationSeat> reservationSeats =
                    reservationSeatRepository.findByReservationId(reservation.getId());
            List<String> seatNums = reservationSeats.stream()
                    .map(rs -> rs.getSeat().getSeatNumber())
                    .collect(Collectors.toList());

            //결제 내역(Payment)에서 실제 결제 금액 꺼내기
            Optional<Payment> paymentOpt = paymentRepository.findByReservationId(reservation.getId());

            int actualPaidAmount = paymentOpt.map(Payment::getFinalAmount)
                    .orElse(reservation.getTotalPrice());

            return new MyReservationResponseDto(reservation, seatNums, actualPaidAmount);
                }).collect(Collectors.toList());
    }

    //특정 예매 내역 조회(예매번호)
    @Transactional(readOnly = true)
    public ReservationResponseDto getReservationDetail(String reservationNo) {
        Reservation reservation = reservationRepository.findByReservationNo(reservationNo)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 예매 번호입니다."));
        return new ReservationResponseDto(reservation);
    }

    @Transactional
    public void cancelReservation(Long userId, String reservationNo) {
        Reservation reservation = reservationRepository.findByReservationNo(reservationNo)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 예매 번호입니다"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = reservation.getSchedule().getStartTime().minusMinutes(DEADLINE_MINUTES);

        if (now.isAfter(deadline)) {
            throw new IllegalStateException("상영 시작 15분 전까지만 예매 취소가 가능합니다.");
        }

        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 예매 내역만 취소할 수 있습니다.");
        }
        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            throw new IllegalArgumentException("이미 취소된 예매입니다.");
        }
        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            paymentService.refundPayment(reservation);
        }
        reservation.cancel();
    }

    @Transactional(readOnly = true)
    public List<TicketResponseDto> getUpcomingTickets(Long userId) {
        List<Reservation> upcomingReservations = reservationRepository.findUpcomingTickets(
                userId,
                ReservationStatus.CONFIRMED,
                LocalDateTime.now()
        );
        return upcomingReservations.stream()
                .map(res -> {
                    List<String> seatNumbers = reservationSeatRepository.findByReservationId(res.getId())
                            .stream().map(rs -> rs.getSeat().getSeatNumber())
                            .toList();

                return new TicketResponseDto(
                            res.getId(),
                            res.getSchedule().getMovie().getTitle(),
                            res.getSchedule().getMovie().getPosterUrl(),
                            res.getSchedule().getTheater().getName(),
                            res.getSchedule().getStartTime(),
                            res.getSchedule().getEndTime(),
                            seatNumbers,
                            "TKT-" + res.getId() + "-" + res.getCreatedAt().getNano()
                    );
                }).toList();
    }
}