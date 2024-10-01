package com.example.coupon.concurrency;

import com.example.coupon.domain.Coupon;
import com.example.coupon.domain.User;
import com.example.coupon.domain.UserCoupon;
import com.example.coupon.repository.CouponRepository;
import com.example.coupon.repository.UserCouponRepository;
import com.example.coupon.repository.UserRepository;
import com.example.coupon.service.CouponService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CouponServiceConcurrencyTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private User user1;
    private Coupon coupon;

    @BeforeEach
    public void setUp() {
        user1 = User.builder()
                .name("User1")
                .password("password1")  // 패스워드 추가
                .build();

        userRepository.save(user1);

        // 빌더 패턴을 사용하여 쿠폰 생성 (초기 수량 10개)
        coupon = Coupon.builder()
                .name("Discount Coupon")
                .quantity(1000)
                .discountRate(10)  // 할인율 추가
                .expirationDate(LocalDate.now().plusDays(10))  // 유효기간 10일 추가
                .build();

        couponRepository.save(coupon);
    }

    @Test
    @DisplayName("1000개 스레드가 동시에 쿠폰을 발급받으면 동시성 문제가 발생한다.")
    public void testConcurrentCouponIssueUnderHighLoad() throws InterruptedException {
        // 1000개의 스레드를 실행하는 스레드 풀을 생성
        int numberOfThreads = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(8);

        // Callable 리스트 생성, 스레드가 실행할 task를 정의
        Callable<String> task = () -> transactionTemplate.execute(status -> {
            couponService.issueCoupon(user1.getId(), coupon.getId());
            return "Success";
        });

        // 1000개의 트랜잭션 작업을 수행할 수 있도록 task 리스트를 준비
        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            tasks.add(task);
        }

        // 1000개의 작업을 8개의 스레드 풀에서 동시에 실행
        List<Future<String>> results = executorService.invokeAll(tasks);

        // 스레드 풀을 종료, 스레드가 모두 끝날 때까지 최대 5초 대기
        executorService.shutdown();
        boolean finished = executorService.awaitTermination(10, TimeUnit.SECONDS);

        if (!finished) {
            // 작업이 시간 내에 종료되지 않으면 예외를 던지거나, 로그를 남긴다.
            throw new RuntimeException("작업이 시간 내에 완료되지 않았습니다.");
        }

        // 결과 확인
        Coupon updatedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        System.out.println("남은 쿠폰 수량: " + updatedCoupon.getQuantity());

        // 동시성 제어가 제대로 이루어지지 않으면 쿠폰 수량이 0이 아니거나, 예상과 다른 값이 될 수 있음
        assertThat(updatedCoupon.getQuantity()).isNotEqualTo(0);
    }


    @Test
    @DisplayName("비관적 락으로 동시성 문제를 해결한다.")
    public void PessimisticWriteTest() throws InterruptedException {
        // 1000개의 스레드를 실행하는 스레드 풀을 생성
        int numberOfThreads = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(8);

        // Callable 리스트 생성, 스레드가 실행할 task를 정의
        Callable<String> task = () -> transactionTemplate.execute(status -> {
            couponService.issueCoupon(user1.getId(), coupon.getId());
            return "Success";
        });

        // 1000개의 트랜잭션 작업을 수행할 수 있도록 task 리스트를 준비
        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            tasks.add(task);
        }

        // 1000개의 작업을 8개의 스레드 풀에서 동시에 실행
        List<Future<String>> results = executorService.invokeAll(tasks);

        // 스레드 풀을 종료, 스레드가 모두 끝날 때까지 최대 5초 대기
        executorService.shutdown();
        boolean finished = executorService.awaitTermination(5, TimeUnit.SECONDS);

        if (!finished) {
            // 작업이 시간 내에 종료되지 않으면 예외를 던지거나, 로그를 남긴다.
            throw new RuntimeException("작업이 시간 내에 완료되지 않았습니다.");
        }

        // 결과 확인
        Coupon updatedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        System.out.println("남은 쿠폰 수량: " + updatedCoupon.getQuantity());

        assertThat(updatedCoupon.getQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("낙관적 락으로 동시성 문제를 해결한다.")
    public void OptimisticWriteTest() throws InterruptedException {
        // 1000개의 스레드를 실행하는 스레드 풀을 생성
        int numberOfThreads = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(8);

        // Callable 리스트 생성, 스레드가 실행할 task를 정의
        Callable<String> task = () -> transactionTemplate.execute(status -> {
            try {
                System.out.println("쿠폰 발급 시작: " + Thread.currentThread().getName());
                couponService.issueCoupon(user1.getId(), coupon.getId());
                System.out.println("쿠폰 발급 완료: " + Thread.currentThread().getName());
            } catch (Exception e) {
                System.err.println("예외 발생: " + e.getMessage());
            }
            return "Success";
        });

        // 1000개의 트랜잭션 작업을 수행할 수 있도록 task 리스트를 준비
        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            tasks.add(task);
        }

        // 1000개의 작업을 8개의 스레드 풀에서 동시에 실행
        List<Future<String>> results = executorService.invokeAll(tasks);

        // 스레드 풀을 종료, 스레드가 모두 끝날 때까지 최대 5초 대기
        executorService.shutdown();
        boolean finished = executorService.awaitTermination(5, TimeUnit.SECONDS);

        if (!finished) {
            // 작업이 시간 내에 종료되지 않으면 예외를 던지거나, 로그를 남긴다.
            throw new RuntimeException("작업이 시간 내에 완료되지 않았습니다.");
        }

        // 결과 확인
        Coupon updatedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        System.out.println("남은 쿠폰 수량: " + updatedCoupon.getQuantity());

        // 동시성 제어가 제대로 이루어지지 않으면 쿠폰 수량이 0이 아니거나, 예상과 다른 값이 될 수 있음
        assertThat(updatedCoupon.getQuantity()).isEqualTo(0);
    }
}