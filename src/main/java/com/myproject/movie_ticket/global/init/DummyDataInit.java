package com.myproject.movie_ticket.global.init;

import com.myproject.movie_ticket.domain.movie.*;
import com.myproject.movie_ticket.domain.user.User;
import com.myproject.movie_ticket.domain.user.UserRepository;
import com.myproject.movie_ticket.domain.user.enums.UserRole;
import com.myproject.movie_ticket.domain.user.enums.UserStatus;
import com.myproject.movie_ticket.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DummyDataInit implements CommandLineRunner {

    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;
    private final SeatRepository seatRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
//        if (userRepository.count() >= 100) {
//            return;
//        }
//
//        List<String> tokens = new ArrayList<>();
//        for (int i = 1; i <= 100; i++) {
//            String uniquePhoneNumber = String.format("010-0000-%04d", i);
//            User dummyUser = User.builder()
//                    .email("testUser"+ i + "@test.com")
//                    .password("password123!")
//                    .name("회원" + i)
//                    .phoneNumber(uniquePhoneNumber)
//                    .role(UserRole.USER)
//                    .status(UserStatus.ACTIVE)
//                    .build();
//            userRepository.save(dummyUser); // 실제 DB에 저장!
//
//            // 토큰 발급
//            String token = jwtProvider.createAccessToken(dummyUser.getId(), "USER");
//            tokens.add(token);
//        }
//
//        // 콘솔에 JSON 형태로 출력
//        String jsonOutput = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tokens);
//        System.out.println("========== [아래 JSON 텍스트를 복사하여 tokens.json에 덮어쓰세요] ==========");
//        System.out.println(jsonOutput);
//        System.out.println("=================================================");

        // DB에 영화 데이터가 하나도 없을 때만 더미 데이터를 삽입합니다.
//        if (movieRepository.count() == 0) {
        if (theaterRepository.count() == 0) {

//            log.info("영화 더미 데이터를 초기화합니다...");
//
//            Movie movie1 = new Movie(
//                    "0",
//                    "오디세이",
//                    135,
//                    "12세",
//                    "https://image.tmdb.org/t/p/w1280/8ze9OcVuFiy94s6FFPvsn4oC2e1.jpg",
//                    "미지의 우주를 탐험하며 인류의 새로운 희망을 찾아 나서는 우주 비행사들의 장엄한 어드벤처. 새로운 우주 시대의 서막이 오른다."
//            );
//
//            Movie movie2 = new Movie(
//                    "1",
//                    "스파이더맨: 브랜드 뉴 데이",
//                    142,
//                    "12세",
//                    "https://image.tmdb.org/t/p/w1280/8mLepBa5l591xFidRpn65xV7hb4.jpg",
//                    "멀티버스의 충돌 이후, 새로운 위협에 맞서기 위해 다시 한번 수트를 입게 된 피터 파커의 고군분투 액션 영웅담."
//            );
//
//            Movie movie3 = new Movie(
//                    "2",
//                    "사랑의 하츄핑: 고래보석의 전설",
//                    86,
//                    "전체",
//                    "https://image.tmdb.org/t/p/w1280/tmV1b9s0zKtUx06pkYaRtBAz065.jpg",
//                    "신비한 마법의 바다에서 잃어버린 전설의 고래보석을 찾기 위해 미지의 섬으로 모험을 떠나는 하츄핑과 친구들의 따뜻한 이야기."
//            );
//
//            Movie movie4 = new Movie(
//                    "3",
//                    "호프",
//                    128,
//                    "15세",
//                    "https://image.tmdb.org/t/p/w1280/ptqlN6mDIAqJjVA86J5eOoG82zc.jpg",
//                    "고립된 항구마을에 나타난 미지의 존재. 마을을 지키기 위해 처절한 사투를 벌이는 사람들을 그린 미스터리 스릴러."
//            );
//
//            Movie movie5 = new Movie(
//                    "4",
//                    "오케이 마담 2",
//                    110,
//                    "15세",
//                    "https://image.tmdb.org/t/p/w1280/xDbrWXB9yb5aEdt4peVy8OWNP2Y.jpg",
//                    "평범한 꽈배기 맛집 사장님이 우연히 또 다른 국제 범죄 조직에 얽히게 되면서 숨겨왔던 액션 본능을 다시 한번 뽐내는 코믹 액션."
//            );
//
//            movieRepository.saveAll(List.of(movie1, movie2, movie3, movie4, movie5));
//            log.info("영화 더미 데이터 5건이 성공적으로 저장되었습니다.");

            Theater theater1 = new Theater("1관 (IMAX)", 608);
            Theater theater2 = new Theater("2관 (DOLBY)", 264);
            Theater theater3 = new Theater("3관 (리클라이너)", 70);
            Theater theater4 = new Theater("4관 (2D)", 169);
            Theater theater5 = new Theater("5관 (2D)", 120);
            theaterRepository.saveAll(List.of(theater1, theater2, theater3, theater4, theater5));

            List<Seat> seats = new ArrayList<>();

            //IMAX
            char[] rows1 = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
            'L', 'M', 'N', 'O', 'P'};
            for (char row : rows1) {
                for (int col = 1; col <= 38; col++) {
                    seats.add(new Seat(theater1, String.valueOf(row), col));
                }
            }

            //DOLBY
            char[] rows2 = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K'};
            for (char row : rows2) {
                for (int col = 1; col <= 24; col++) {
                    seats.add(new Seat(theater2, String.valueOf(row), col));
                }
            }

            //리클라이너
            char[] rows3 = {'A', 'B', 'C', 'D', 'E', 'F', 'G'};
            for (char row : rows3) {
                for (int col = 1; col <= 10; col++) {
                    seats.add(new Seat(theater3, String.valueOf(row), col));
                }
            }

            //2D
            char[] rows4 = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
                    'L', 'M'};
            for (char row : rows4) {
                for (int col = 1; col <= 13; col++) {
                    seats.add(new Seat(theater4, String.valueOf(row), col));
                }
            }

            //2D
            char[] rows5 = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
            for (char row : rows5) {
                for (int col = 1; col <= 12; col++) {
                    seats.add(new Seat(theater5, String.valueOf(row), col));
                }
            }

            seatRepository.saveAll(seats);
            log.info("상영관 및 좌석이 저장되었습니다.");


//            LocalDate today = LocalDate.now();
//            List<Schedule> schedules = new ArrayList<>();
//
//            // 3일치 스케줄을 만들기 위한 반복문 (오늘, 내일, 모레)
//            for (int dayOffset = 0; dayOffset < 3; dayOffset++) {
//                LocalDate scheduleDate = today.plusDays(dayOffset);
//
//                // 🎬 [1관 IMAX] - 블록버스터 교차 상영 (오디세이, 스파이더맨)
//                LocalTime t1Time = LocalTime.of(9, 0); // 아침 9시 시작
//                List<Movie> t1Movies = List.of(movie1, movie2, movie1, movie2);
//                for (Movie m : t1Movies) {
//                    LocalDateTime start = LocalDateTime.of(scheduleDate, t1Time);
//                    LocalDateTime end = start.plusMinutes(m.getDuration());
//                    schedules.add(new Schedule(m, theater1, start, end));
//                    t1Time = end.plusMinutes(20).toLocalTime(); // 청소 및 입장 준비 20분
//                }
//
//                // 🎬 [2관 DOLBY] - 액션/스릴러 중심 (스파이더맨, 호프)
//                LocalTime t2Time = LocalTime.of(9, 30);
//                List<Movie> t2Movies = List.of(movie2, movie4, movie2, movie4);
//                for (Movie m : t2Movies) {
//                    LocalDateTime start = LocalDateTime.of(scheduleDate, t2Time);
//                    LocalDateTime end = start.plusMinutes(m.getDuration());
//                    schedules.add(new Schedule(m, theater2, start, end));
//                    t2Time = end.plusMinutes(15).toLocalTime();
//                }
//
//                // 🎬 [3관 리클라이너] - 긴 영화 및 코미디 (호프, 오케이 마담 2, 오디세이)
//                LocalTime t3Time = LocalTime.of(10, 0);
//                List<Movie> t3Movies = List.of(movie4, movie5, movie1, movie5);
//                for (Movie m : t3Movies) {
//                    LocalDateTime start = LocalDateTime.of(scheduleDate, t3Time);
//                    LocalDateTime end = start.plusMinutes(m.getDuration());
//                    schedules.add(new Schedule(m, theater3, start, end));
//                    t3Time = end.plusMinutes(20).toLocalTime();
//                }
//
//                // 🎬 [4관 2D] - ⭐️ 하츄핑(아이들) 오전/오후 집중 상영 + 저녁 코미디
//                LocalTime t4Time = LocalTime.of(9, 15);
//                List<Movie> t4Movies = List.of(movie3, movie3, movie3, movie5, movie5); // 하츄핑 3연속 상영
//                for (Movie m : t4Movies) {
//                    LocalDateTime start = LocalDateTime.of(scheduleDate, t4Time);
//                    LocalDateTime end = start.plusMinutes(m.getDuration());
//                    schedules.add(new Schedule(m, theater4, start, end));
//                    t4Time = end.plusMinutes(15).toLocalTime();
//                }
//
//                // 🎬 [5관 2D] - ⭐️ 하츄핑 교차 상영 + 다양한 장르
//                LocalTime t5Time = LocalTime.of(10, 30);
//                List<Movie> t5Movies = List.of(movie5, movie3, movie3, movie4); // 하츄핑 2회 상영
//                for (Movie m : t5Movies) {
//                    LocalDateTime start = LocalDateTime.of(scheduleDate, t5Time);
//                    LocalDateTime end = start.plusMinutes(m.getDuration());
//                    schedules.add(new Schedule(m, theater5, start, end));
//                    t5Time = end.plusMinutes(15).toLocalTime();
//                }
//            }
//
//            scheduleRepository.saveAll(schedules);
//            log.info("영화, 상영관, 좌석, 3일치 스케줄 더미 데이터가 성공적으로 셋업되었습니다!");
        }

    }
}
