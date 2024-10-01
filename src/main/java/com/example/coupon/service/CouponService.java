package com.example.coupon.service;

import com.example.coupon.domain.Coupon;
import com.example.coupon.domain.User;
import com.example.coupon.domain.UserCoupon;
import com.example.coupon.repository.CouponRepository;
import com.example.coupon.repository.UserCouponRepository;
import com.example.coupon.repository.UserRepository;
import jakarta.persistence.LockModeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;

    @Autowired
    public CouponService(CouponRepository couponRepository, UserCouponRepository userCouponRepository, UserRepository userRepository) {
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public String issueCoupon(Long userId, Long couponId) {

        // 1. 유저와 쿠폰을 가져온다.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
//        Coupon coupon = couponRepository.findById(couponId)
//                .orElseThrow(() -> new RuntimeException("쿠폰을 찾을 수 없습니다."));
        Coupon coupon = couponRepository.findByIdWithPessimisticLock(couponId)
                .orElseThrow(() -> new RuntimeException("쿠폰을 찾을 수 없습니다."));


        // 2. 해당 사용자가 이미 해당 쿠폰을 발급받았는지 확인한다.
//        if (userCouponRepository.existsByUserIdAndCouponId(userId, couponId)) {
//            throw new RuntimeException("이미 이 쿠폰을 발급받았습니다.");
//        }

        // 3. 쿠폰 수량 감소
        coupon.issueCoupon();

        // 4. UserCoupon 객체 생성 및 저장
        UserCoupon userCoupon = UserCoupon.builder()
                .coupon(coupon)
                .userId(user.getId())
                .build();
        userCouponRepository.save(userCoupon);

        // 5. 쿠폰 업데이트
        couponRepository.save(coupon);

        return "쿠폰 발급 완료!";
    }
}


