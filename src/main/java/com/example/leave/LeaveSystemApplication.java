package com.example.leave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LeaveSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeaveSystemApplication.class, args);
    }
}
