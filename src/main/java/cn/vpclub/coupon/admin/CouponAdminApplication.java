package cn.vpclub.coupon.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CouponAdminApplication {

    public static void main(String[] args) {

        SpringApplication.run(CouponAdminApplication.class, args);
    }
}
