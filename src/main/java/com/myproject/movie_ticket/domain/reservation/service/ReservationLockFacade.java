package com.myproject.movie_ticket.domain.reservation.service;

import com.myproject.movie_ticket.domain.reservation.dto.ReservationCreateRequestDto;
import com.myproject.movie_ticket.domain.reservation.dto.ReservationResponseDto;
import lombok.RequiredArgsConstructor;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReservationLockFacade {

    private final RedissonClient redissonClient;
    private final ReservationService reservationService;

    public ReservationResponseDto reserve(Long userId, ReservationCreateRequestDto requestDto) {
        //요청한 모든 좌석에 대한 고유 락(Lock) 객체들을 생성
        List<RLock> locks = requestDto.getSeatIds().stream()
                .map(seatId -> redissonClient.getLock("lock:schedule:" + requestDto.getScheduleId() + ":seat:" + seatId))
                .toList();

        //다중 좌석을 한 번에 락킹할 수 있는 MultiLock 생성 (데드락 방지)
        RedissonMultiLock multiLock = new RedissonMultiLock(locks.toArray(new RLock[0]));

        try {
            // 누군가 이미 락을 쥐고 있다면 1초도 기다리지 않고 즉시 false 반환
            boolean isLocked = multiLock.tryLock(0, 10, TimeUnit.SECONDS);

            if (!isLocked) {
                throw new IllegalArgumentException("이미 다른 사용자가 예매중인 좌석입니다.");
            }
            return reservationService.createReservation(userId, requestDto);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("락 획득 중 인터럽트가 발생했습니다.");
        } finally {
            try {
                multiLock.unlock();
            } catch (IllegalMonitorStateException e){

            }
        }
    }
}
