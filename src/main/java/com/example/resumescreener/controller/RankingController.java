package com.example.resumescreener.controller;

import com.example.resumescreener.dto.RankingResponse;
import com.example.resumescreener.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for resume ranking.
 * 
 * Endpoints:
 * - GET /ranking/{jobId} - Get ranked list of resumes for a job
 */
@RestController
@RequestMapping("/ranking")
public class RankingController {

    private final RankingService rankingService;

    @Autowired
    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    /**
     * Get ranked list of resumes for a job posting.
     * 
     * @param jobId Job ID from path variable
     * @return Ranked list of resumes with HTTP 200 status
     */
    @GetMapping("/{jobId}")
    public ResponseEntity<List<RankingResponse>> getRankedResumes(@PathVariable Long jobId) {
        List<RankingResponse> rankings = rankingService.getRankedResumes(jobId);
        return ResponseEntity.ok(rankings);
    }
}
