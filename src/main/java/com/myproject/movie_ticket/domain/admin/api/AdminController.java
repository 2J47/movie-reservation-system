package com.myproject.movie_ticket.domain.admin.api;

import com.myproject.movie_ticket.domain.admin.service.AdminCouponService;
import com.myproject.movie_ticket.domain.admin.service.MovieSyncService;
import com.myproject.movie_ticket.domain.admin.service.ScheduleSyncService;
import com.myproject.movie_ticket.domain.coupon.dto.CouponCreateRequestDto;

import com.myproject.movie_ticket.domain.movie.MovieRepository;
import com.myproject.movie_ticket.domain.movie.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminCouponService adminCouponService;
    private final MovieSyncService movieSyncService;
    private final ScheduleSyncService scheduleSyncService;
    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;

    //쿠폰 발행
    @PostMapping("/coupons")
    public ResponseEntity<Void> createCoupon(@RequestBody CouponCreateRequestDto requestDto) {
        System.out.println("프론트에서 넘어온 이름: " + requestDto.getName());
        System.out.println("프론트에서 온 값: " + requestDto.getDiscountType());
        adminCouponService.createCoupon(requestDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/coupons/{couponId}")
    public ResponseEntity<Void> issueCouponToUser(
            @PathVariable Long userId,
            @PathVariable Long couponId) {
        adminCouponService.issueCouponToUser(couponId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/movies/sync")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> syncMovies(
            @RequestParam @DateTimeFormat(pattern = "yyyyMMdd") LocalDate targetDate) {

        String formattedDate = targetDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        movieSyncService.syncBoxOffice(formattedDate);
        return ResponseEntity.ok("영화 DB 동기화가 완료되었습니다.");
    }

    @PostMapping("/schedules/auto-generate")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> generateSchedules(
            @RequestParam List<Long> movieIds,
            @RequestParam Long theaterId,
            @RequestParam @DateTimeFormat(pattern = "yyyyMMdd") LocalDate targetDate) {

        scheduleSyncService.generateSchedules(movieIds, theaterId, targetDate);
        return ResponseEntity.ok("스케줄 배정이 완료되었습니다.");
    }

    // 스케줄 배정 시 사용할 영화 목록 조회
    @GetMapping("/movies/options")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<OptionDto>> getMovieOptions() {
        List<OptionDto> options = movieRepository.findAll().stream()
                .map(m -> new OptionDto(m.getId(), m.getTitle(), m.getDuration()))
                .toList();
        return ResponseEntity.ok(options);
    }

    // 스케줄 배정 시 사용할 상영관 목록 조회
    @GetMapping("/theaters/options")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<TheaterOptionDto>> getTheaterOptions() {
        List<TheaterOptionDto> options = theaterRepository.findAll().stream()
                .map(t -> new TheaterOptionDto(t.getId(), t.getName()))
                .toList();
        return ResponseEntity.ok(options);
    }
    // 드롭다운 옵션 반환용 전용 DTO
    public record OptionDto(Long id, String name, Integer duration) {}
    public record TheaterOptionDto(Long id, String name) {}
}
