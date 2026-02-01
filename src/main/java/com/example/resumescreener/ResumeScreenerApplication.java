package com.example.resumescreener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot Application Class
 * 
 * This is the entry point for the Resume Screening Application.
 * 
 * Features:
 * - REST API for job posting management
 * - Resume upload and text extraction using Apache Tika
 * - Resume parsing and structured data extraction
 * - Skill matching and scoring engine
 * - Candidate ranking system
 * 
 * @author Resume Screener Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.resumescreener.repository")
@EntityScan(basePackages = "com.example.resumescreener.model")
public class ResumeScreenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResumeScreenerApplication.class, args);
    }
}
