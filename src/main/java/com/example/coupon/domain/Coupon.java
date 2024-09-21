package com.example.coupon.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;
import lombok.AccessLevel;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private Long discountRate;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Builder
    public Coupon(Long quantity, String name, Long discountRate, LocalDate expirationDate) {
        this.quantity = quantity;
        this.name = name;
        this.discountRate = discountRate;
        this.expirationDate = expirationDate;
    }

    // 쿠폰 발급 가능 여부를 체크하고 수량 감소
    public boolean issueCoupon() {
        if (this.quantity > 0) {
            this.quantity--;
            return true;
        } else {
            throw new RuntimeException("쿠폰 수량이 부족합니다.");
        }
    }
}

