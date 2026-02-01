package com.example.resumescreener.repository;

import com.example.resumescreener.model.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for JobPost entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface JobRepository extends JpaRepository<JobPost, Long> {
    
    // Spring Data JPA automatically provides:
    // - save(JobPost entity)
    // - findById(Long id)
    // - findAll()
    // - deleteById(Long id)
    // - count()
    // etc.
}
