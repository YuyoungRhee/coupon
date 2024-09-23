package com.example.coupon.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class CouponTest {

    @DisplayName("쿠폰 수량이 1 이상일 때, 정상적으로 발급되고 수량이 1 감소된다.")
    @Test
    void issueCoupon_success() {
        // given
        Coupon coupon = Coupon.builder()
                .name("coupon1")
                .quantity(10)
                .discountRate(10)
                .expirationDate(LocalDate.now().plusDays(10))
                .build();
        // when
        boolean result = coupon.issueCoupon();
        // then
        assertThat(result).isTrue();
        assertThat(coupon.getQuantity()).isEqualTo(9);
    }

}