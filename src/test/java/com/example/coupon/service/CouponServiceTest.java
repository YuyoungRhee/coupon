package com.example.coupon.service;

import com.example.coupon.domain.Coupon;
import com.example.coupon.domain.User;
import com.example.coupon.domain.UserCoupon;
import com.example.coupon.repository.CouponRepository;
import com.example.coupon.repository.UserCouponRepository;
import com.example.coupon.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class CouponServiceTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @DisplayName("쿠폰이 발급되면 수량이 1 줄어든다.")
    @Test
    @Transactional
    void issueCoupon() {
        // given
        User user = User.builder()
                .name("userA")
                .password("password")
                .build();
        userRepository.save(user);

        Coupon coupon = Coupon.builder()
                .discountRate(10)
                .expirationDate(LocalDate.of(2024,10,01))
                .name("coupon1")
                .quantity(10)
                .build();
        couponRepository.save(coupon);

        // when
        String response = couponService.issueCoupon(user.getId(),coupon.getId());

        // then
        assertThat(response).isEqualTo("쿠폰 발급 완료!");
        assertThat(couponRepository.findById(coupon.getId()).get().getQuantity()).isEqualTo(9);

        // 발급 내역이 UserCoupon에 저장되었는지 확인
        assertThat(userCouponRepository.existsByUserIdAndCouponId(user.getId(), coupon.getId())).isTrue();

        // 실제 UserCoupon 데이터를 조회하여 검증
        UserCoupon userCoupon = userCouponRepository.findAll().get(0);
        assertThat(userCoupon.getUserId()).isEqualTo(user.getId());
        assertThat(userCoupon.getCoupon().getId()).isEqualTo(coupon.getId());
    }

    @DisplayName("쿠폰 수량이 0일 때 쿠폰 발급 시도 시 예외 발생")
    @Test
    @Transactional
    void issueCoupon_failed() {
        // given
        User user = User.builder()
                .name("userA")
                .password("password")
                .build();
        userRepository.save(user);

        Coupon coupon = Coupon.builder()
                .discountRate(10)
                .expirationDate(LocalDate.of(2024,10,01))
                .name("coupon1")
                .quantity(0)
                .build();
        couponRepository.save(coupon);

        // when & then
        assertThatThrownBy(
                () -> couponService.issueCoupon(user.getId(), coupon.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("쿠폰 수량이 부족합니다.");
    }

}