package com.example.resumescreener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.resumescreener.repository")
@EntityScan(basePackages = "com.example.resumescreener.model")
public class ResumeScreenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResumeScreenerApplication.class, args);
    }
}
