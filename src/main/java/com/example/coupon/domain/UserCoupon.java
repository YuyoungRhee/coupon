package com.example.coupon.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AccessLevel;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "couponId", nullable = false)
    private Coupon coupon;

    @Column(nullable = false)
    private Long userId;

    @Builder
    public UserCoupon(Coupon coupon, Long userId) {
        this.coupon = coupon;
        this.userId = userId;
    }
}

