package com.myproject.movie_ticket.domain.reservation.service;

import com.myproject.movie_ticket.domain.movie.*;
import com.myproject.movie_ticket.domain.reservation.ReservationRepository;
import com.myproject.movie_ticket.domain.reservation.ReservationSeatRepository;
import com.myproject.movie_ticket.domain.reservation.dto.ReservationCreateRequestDto;
import com.myproject.movie_ticket.domain.user.User;
import com.myproject.movie_ticket.domain.user.UserRepository;
import com.myproject.movie_ticket.domain.user.enums.UserRole;
import com.myproject.movie_ticket.domain.user.enums.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ReservationServiceConcurrencyTest {

    @Autowired private ReservationService reservationService;
    @Autowired private UserRepository userRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ReservationSeatRepository reservationSeatRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private TheaterRepository theaterRepository;


    private List<User> testUsers = new ArrayList<>();
    private Theater testTheater;
    private Movie testMovie;
    private Schedule testSchedule;
    private Seat testSeat;


    @BeforeEach
    void setUp() {
        for (int i = 0; i < 100; i++) {
            String uniquePhoneNumber = String.format("010-0000-%04d", i);
             User user = new User("user" + i + "test@test.com",
                     "password", "유저" + i, uniquePhoneNumber,
                     UserRole.USER, UserStatus.ACTIVE);
             testUsers.add(userRepository.save(user));
        }
        testMovie = new Movie(
                "1",
                "오디세이",
                135,
                "12세",
                "https://image.tmdb.org/t/p/w1280/8ze9OcVuFiy94s6FFPvsn4oC2e1.jpg",
                "미지의 우주를 탐험하며 인류의 새로운 희망을 찾아 나서는 우주 비행사들의 장엄한 어드벤처. 새로운 우주 시대의 서막이 오른다."
        );
        testTheater = new Theater("1관", 30);
        LocalTime t1Time = LocalTime.of(9, 0);
        LocalDateTime start = LocalDateTime.of(LocalDate.now(),t1Time);
        LocalDateTime end = start.plusMinutes(testMovie.getDuration());
        testSchedule = new Schedule(testMovie, testTheater, start, end);
        testSeat = new Seat(testTheater, "A", 1);
        movieRepository.save(testMovie);
        theaterRepository.save(testTheater);
        scheduleRepository.save(testSchedule);
        seatRepository.save(testSeat);
    }
    @AfterEach
    void tearDown() {
        reservationSeatRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        scheduleRepository.deleteAllInBatch();
        seatRepository.deleteAllInBatch();
        theaterRepository.deleteAllInBatch();
        movieRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("100명의 유저가 동시에 같은 좌석을 예매 시도하면 단 1명만 성공해야한다.")
    void concurrentReservationTest() throws InterruptedException {
        //given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ReservationCreateRequestDto requestDto = new ReservationCreateRequestDto();
        ReflectionTestUtils.setField(requestDto, "scheduleId", testSchedule.getId());
        ReflectionTestUtils.setField(requestDto, "seatIds", List.of(testSeat.getId()));

        //when
        for (int i = 0; i <threadCount; i++) {
             final Long currentUserId = testUsers.get(i).getId();

             executorService.submit(() -> {
                 try {
                     reservationService.createReservation(currentUserId, requestDto);
                     successCount.incrementAndGet();
                 } catch (Exception e) {
                     System.out.println("예매 실패 원인: " + e.getMessage());
                     failCount.incrementAndGet();
                 } finally {
                     latch.countDown();
                 }
             });
        }
        latch.await();

        //then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(99);

        long savedReservationCount = reservationRepository.count();
        assertThat(savedReservationCount).isEqualTo(1);
    }
}