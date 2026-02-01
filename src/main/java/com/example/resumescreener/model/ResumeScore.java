package com.example.resumescreener.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity representing calculated scores for a resume-job pair.
 * Maps to the 'resume_score' table in MySQL.
 */
@Entity
@Table(name = "resume_score", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "resume_id"}),
       indexes = {
           @Index(name = "idx_job_id", columnList = "job_id"),
           @Index(name = "idx_resume_id", columnList = "resume_id"),
           @Index(name = "idx_final_score", columnList = "final_score")
       })
public class ResumeScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobPost jobPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private CandidateResume candidateResume;

    @Column(name = "skill_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal skillScore = BigDecimal.ZERO;

    @Column(name = "experience_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal experienceScore = BigDecimal.ZERO;

    @Column(name = "education_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal educationScore = BigDecimal.ZERO;

    @Column(name = "project_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal projectScore = BigDecimal.ZERO;

    @Column(name = "final_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal finalScore = BigDecimal.ZERO;

    @Column(name = "calculated_at", nullable = false, updatable = false)
    private LocalDateTime calculatedAt;

    // JPA Lifecycle Callbacks
    @PrePersist
    protected void onCreate() {
        calculatedAt = LocalDateTime.now();
    }

    // Constructors
    public ResumeScore() {
    }

    public ResumeScore(JobPost jobPost, CandidateResume candidateResume) {
        this.jobPost = jobPost;
        this.candidateResume = candidateResume;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JobPost getJobPost() {
        return jobPost;
    }

    public void setJobPost(JobPost jobPost) {
        this.jobPost = jobPost;
    }

    public CandidateResume getCandidateResume() {
        return candidateResume;
    }

    public void setCandidateResume(CandidateResume candidateResume) {
        this.candidateResume = candidateResume;
    }

    public BigDecimal getSkillScore() {
        return skillScore;
    }

    public void setSkillScore(BigDecimal skillScore) {
        this.skillScore = skillScore;
    }

    public BigDecimal getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(BigDecimal experienceScore) {
        this.experienceScore = experienceScore;
    }

    public BigDecimal getEducationScore() {
        return educationScore;
    }

    public void setEducationScore(BigDecimal educationScore) {
        this.educationScore = educationScore;
    }

    public BigDecimal getProjectScore() {
        return projectScore;
    }

    public void setProjectScore(BigDecimal projectScore) {
        this.projectScore = projectScore;
    }

    public BigDecimal getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(BigDecimal finalScore) {
        this.finalScore = finalScore;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
}
