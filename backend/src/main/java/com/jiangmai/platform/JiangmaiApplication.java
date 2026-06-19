package com.jiangmai.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class JiangmaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JiangmaiApplication.class, args);
    }

    @GetMapping("/api/health")
    public String healthCheck() {
        return "Backend API is up and running!";
    }
}
