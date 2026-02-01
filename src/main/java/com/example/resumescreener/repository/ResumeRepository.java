package com.example.resumescreener.repository;

import com.example.resumescreener.model.CandidateResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for CandidateResume entity.
 * Provides CRUD operations for resume management.
 */
@Repository
public interface ResumeRepository extends JpaRepository<CandidateResume, Long> {
    
    // Spring Data JPA automatically provides:
    // - save(CandidateResume entity)
    // - findById(Long id)
    // - findAll()
    // - deleteById(Long id)
    // etc.
}
