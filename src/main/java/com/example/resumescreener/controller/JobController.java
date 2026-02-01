package com.example.resumescreener.controller;

import com.example.resumescreener.dto.JobRequest;
import com.example.resumescreener.dto.JobResponse;
import com.example.resumescreener.service.JobService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for job posting management.
 * 
 * Endpoints:
 * - POST /jobs - Create a new job posting
 * - GET /jobs/{id} - Get job by ID
 * - GET /jobs - Get all jobs
 */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    @Autowired
    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    /**
     * Create a new job posting.
     * 
     * @param jobRequest Job details from request body
     * @return Created job with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest jobRequest) {
        JobResponse jobResponse = jobService.createJob(jobRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(jobResponse);
    }

    /**
     * Get a job posting by ID.
     * 
     * @param id Job ID from path variable
     * @return Job details with HTTP 200 status
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        JobResponse jobResponse = jobService.getJobById(id);
        return ResponseEntity.ok(jobResponse);
    }

    /**
     * Get all job postings.
     * 
     * @return List of all jobs with HTTP 200 status
     */
    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs() {
        List<JobResponse> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }
}
