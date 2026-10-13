package com.myproject.movie_ticket.domain.payment;

import com.myproject.movie_ticket.domain.coupon.UserCoupon;
import com.myproject.movie_ticket.domain.reservation.Reservation;
import com.myproject.movie_ticket.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @Column(nullable = false)
    private Integer finalAmount; //최종 실제 결제액

    @Column(nullable = false)
    private Integer pointAmount; // 사용한 포인트 금액

    @Column(nullable = false)
    private Integer couponAmount; //쿠폰으로 할인받은 금액

    @Column(nullable = false, length = 20)
    private PayMethod payMethod; //절제 수단

    private String pgTid; //결제 고유 번호

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    //결제:보유쿠폰
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_coupon_id")
    private UserCoupon userCoupon;

    @Builder
    public Payment(Reservation reservation, Integer finalAmount, Integer pointAmount,
                   Integer couponAmount, PayMethod payMethod, String pgTid,
                   PaymentStatus status, UserCoupon userCoupon) {
        this.reservation = reservation;
        this.finalAmount = finalAmount;
        this.pointAmount = (pointAmount != null) ? pointAmount : 0;
        this.couponAmount = (couponAmount != null) ? couponAmount : 0;
        this.payMethod = payMethod;
        this.pgTid = pgTid;
        this.status = (status != null) ? status : PaymentStatus.SUCCESS;
        this.userCoupon = userCoupon;
    }

    public void fail() {
        this.status = PaymentStatus.FAILED; // 결제 진행 중 중단
    }

    public void refund() {
        if (this.status != PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("결제 완료 상태에서만 환불할 수 있습니다.");
        }
        this.status = PaymentStatus.REFUNDED;
    }
}
