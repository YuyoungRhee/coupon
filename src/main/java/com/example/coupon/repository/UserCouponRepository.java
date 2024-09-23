package com.example.coupon.repository;

import com.example.coupon.domain.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    // 사용자와 쿠폰 ID를 통해 발급 내역을 확인할 수 있는 메서드
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);
}


