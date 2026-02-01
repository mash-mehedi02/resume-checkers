package com.example.resumescreener.service;

import com.example.resumescreener.dto.JobRequest;
import com.example.resumescreener.dto.JobResponse;
import com.example.resumescreener.exception.ResourceNotFoundException;
import com.example.resumescreener.model.JobPost;
import com.example.resumescreener.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for job posting management.
 * Contains business logic for job operations.
 */
@Service
@Transactional
public class JobService {

    private final JobRepository jobRepository;

    @Autowired
    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /**
     * Create a new job posting.
     * 
     * @param jobRequest DTO containing job details
     * @return JobResponse with created job data
     */
    public JobResponse createJob(JobRequest jobRequest) {
        JobPost jobPost = new JobPost();
        jobPost.setTitle(jobRequest.getTitle());
        jobPost.setDescription(jobRequest.getDescription());
        jobPost.setRequiredSkills(jobRequest.getRequiredSkills());
        jobPost.setPreferredSkills(jobRequest.getPreferredSkills());
        jobPost.setMinExperienceYears(jobRequest.getMinExperienceYears());
        jobPost.setEducationLevel(jobRequest.getEducationLevel());
        jobPost.setJobType(jobRequest.getJobType());

        JobPost savedJob = jobRepository.save(jobPost);
        return convertToResponse(savedJob);
    }

    /**
     * Retrieve a job posting by ID.
     * 
     * @param id Job ID
     * @return JobResponse with job details
     * @throws ResourceNotFoundException if job not found
     */
    @Transactional(readOnly = true)
    public JobResponse getJobById(Long id) {
        JobPost jobPost = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", id));
        return convertToResponse(jobPost);
    }

    /**
     * Retrieve all job postings.
     * 
     * @return List of JobResponse objects
     */
    @Transactional(readOnly = true)
    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert JobPost entity to JobResponse DTO.
     * 
     * @param jobPost Entity to convert
     * @return JobResponse DTO
     */
    private JobResponse convertToResponse(JobPost jobPost) {
        JobResponse response = new JobResponse();
        response.setId(jobPost.getId());
        response.setTitle(jobPost.getTitle());
        response.setDescription(jobPost.getDescription());
        response.setRequiredSkills(jobPost.getRequiredSkills());
        response.setPreferredSkills(jobPost.getPreferredSkills());
        response.setMinExperienceYears(jobPost.getMinExperienceYears());
        response.setEducationLevel(jobPost.getEducationLevel());
        response.setJobType(jobPost.getJobType());
        response.setCreatedAt(jobPost.getCreatedAt());
        response.setUpdatedAt(jobPost.getUpdatedAt());
        return response;
    }
}
