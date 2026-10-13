package com.myproject.movie_ticket.domain.asset;

import com.myproject.movie_ticket.domain.user.User;
import com.myproject.movie_ticket.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //지갑:회원
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer balance = 0;

    @Builder
    public Wallet(User user, Integer balance) {
        this.user = user;
        this.balance = (balance != null) ? balance : 0;
    }

    // 2. DDD 실무 표준: Setter 대신 의도가 명확한 비즈니스 메서드를 엔티티 안에 직접 만듭니다.
    public void charge(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0원보다 커야 합니다.");
        }
        this.balance += amount;
    }

    public void deduct(int amount) {
        if (this.balance < amount) {
            throw new IllegalArgumentException("잔액이 부족합니다."); // 향후 Custom Exception으로 교체
        }
        this.balance -= amount;
    }

    public void clearBalance() {
        this.balance = 0;
    }
}
