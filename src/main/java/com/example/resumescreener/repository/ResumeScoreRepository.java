package com.example.resumescreener.repository;

import com.example.resumescreener.model.ResumeScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ResumeScore entity.
 * Provides CRUD operations and custom queries for score management.
 */
@Repository
public interface ResumeScoreRepository extends JpaRepository<ResumeScore, Long> {
    
    /**
     * Find score by job ID and resume ID.
     * 
     * @param jobId Job ID
     * @param resumeId Resume ID
     * @return Optional ResumeScore
     */
    Optional<ResumeScore> findByJobPostIdAndCandidateResumeId(Long jobId, Long resumeId);
    
    /**
     * Find all scores for a specific job, ordered by final score descending.
     * 
     * @param jobId Job ID
     * @return List of ResumeScore ordered by final score
     */
    @Query("SELECT rs FROM ResumeScore rs WHERE rs.jobPost.id = :jobId ORDER BY rs.finalScore DESC")
    List<ResumeScore> findByJobPostIdOrderByFinalScoreDesc(@Param("jobId") Long jobId);
    
    /**
     * Find all scores for a specific resume.
     * 
     * @param resumeId Resume ID
     * @return List of ResumeScore
     */
    List<ResumeScore> findByCandidateResumeId(Long resumeId);
    
    /**
     * Delete all scores for a specific job.
     * 
     * @param jobId Job ID
     */
    void deleteByJobPostId(Long jobId);
}
