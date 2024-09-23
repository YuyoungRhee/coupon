package com.example.coupon.repository;

import com.example.coupon.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 추가 메서드 필요 시 여기에 작성
}