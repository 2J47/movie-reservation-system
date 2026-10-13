package com.myproject.movie_ticket.domain.asset.service;

import com.myproject.movie_ticket.domain.asset.Wallet;
import com.myproject.movie_ticket.domain.asset.WalletRepository;
import com.myproject.movie_ticket.domain.asset.dto.WalletChargeRequestDto;
import com.myproject.movie_ticket.domain.asset.dto.WalletResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;

    @Transactional
    public WalletResponseDto chargeBalance(Long userId, WalletChargeRequestDto requestDto) {
        //유저 지갑을 db에서 가져옴
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("지갑 정보를 찾을 수 없습니다."));

        //금액 충전
        wallet.charge(requestDto.getAmount());

        //더티 체킹에 의해 트랜잭션이 끝날 때 UPDATE 쿼리가 자동 실행되므로, 변경된 잔액을 반환하기만 하면 됩니다.
        return new WalletResponseDto(wallet);
    }

    @Transactional(readOnly = true)
    public Integer getBalance(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(()-> new IllegalArgumentException("지갑을 찾을 수 없습니다."))
                .getBalance();
    }
}
