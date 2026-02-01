package com.example.resumescreener.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for calculating experience relevance score.
 * 
 * Scoring Logic:
 * 1. Years Match Score: How well resume experience matches required experience
 * 2. Role Relevance: Bonus for matching job type (backend/frontend/fullstack)
 * 
 * Assumptions:
 * - Experience years are extracted from resume (can be null)
 * - Job requires minimum experience years
 * - Job type (backend/frontend/fullstack) is optional but helps with relevance
 * - Perfect match (exact years) = 100 points
 * - Over-qualified (more years) = 100 points (bonus)
 * - Under-qualified (less years) = Proportional score with penalty
 */
@Component
public class ExperienceScorer {

    // Maximum experience years considered (beyond this, no additional benefit)
    private static final int MAX_EXPERIENCE_YEARS = 20;

    /**
     * Calculate experience relevance score (0-100).
     * 
     * @param resumeExperienceYears Years of experience from resume (can be null)
     * @param requiredMinYears Minimum years required by job
     * @param jobType Job type (backend, frontend, fullstack, etc.) - optional
     * @param resumeJobType Job type from resume (if extracted) - optional
     * @return Experience score (0-100)
     */
    public BigDecimal calculateExperienceScore(
            Integer resumeExperienceYears,
            Integer requiredMinYears,
            String jobType,
            String resumeJobType) {

        // If no experience data in resume, return 0
        if (resumeExperienceYears == null) {
            return BigDecimal.ZERO;
        }

        // If no requirement, consider it a match (return 100)
        if (requiredMinYears == null || requiredMinYears <= 0) {
            return BigDecimal.valueOf(100.0);
        }

        // Calculate years match score
        double yearsScore = calculateYearsMatchScore(resumeExperienceYears, requiredMinYears);

        // Calculate role relevance bonus (if job type information available)
        double roleRelevanceBonus = calculateRoleRelevanceBonus(jobType, resumeJobType);

        // Combine scores: Years score (80%) + Role relevance bonus (20%)
        // But cap role bonus so total doesn't exceed 100
        double totalScore = yearsScore * 0.80 + roleRelevanceBonus * 0.20;

        // Ensure score is between 0 and 100
        totalScore = Math.max(0.0, Math.min(100.0, totalScore));

        return BigDecimal.valueOf(totalScore)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate score based on years of experience match.
     * 
     * Formula:
     * - If resume years >= required years: 100 points (meets or exceeds)
     * - If resume years < required years: (resume years / required years) × 100
     *   with a penalty for being significantly under-qualified
     * 
     * @param resumeYears Years from resume
     * @param requiredYears Required years
     * @return Years match score (0-100)
     */
    private double calculateYearsMatchScore(int resumeYears, int requiredYears) {
        // Meets or exceeds requirement: Full score
        if (resumeYears >= requiredYears) {
            // Bonus for being over-qualified (but cap at reasonable level)
            if (resumeYears > requiredYears) {
                int excessYears = resumeYears - requiredYears;
                // Small bonus for 1-2 extra years, but not too much
                if (excessYears <= 2) {
                    return 100.0; // Perfect match with slight over-qualification
                } else if (excessYears <= 5) {
                    return 100.0; // Still good, but might be over-qualified
                } else {
                    // Too over-qualified might not be ideal, but still give high score
                    return 95.0;
                }
            }
            return 100.0; // Exact match
        }

        // Under-qualified: Proportional score with penalty
        double ratio = (double) resumeYears / requiredYears;
        
        // Apply penalty for being significantly under-qualified
        // If less than 50% of required, apply additional penalty
        if (ratio < 0.5) {
            // Heavy penalty: score = ratio × 60 (max 30 points)
            return ratio * 60.0;
        } else if (ratio < 0.75) {
            // Moderate penalty: score = ratio × 80 (max 60 points)
            return ratio * 80.0;
        } else {
            // Light penalty: score = ratio × 90 (max 67.5 points)
            return ratio * 90.0;
        }
    }

    /**
     * Calculate role relevance bonus based on job type match.
     * 
     * Logic:
     * - If job type matches resume job type: +20 points
     * - If job type is "fullstack" and resume has either backend/frontend: +10 points
     * - If job type is backend/frontend and resume is fullstack: +15 points
     * - No match: 0 points
     * 
     * @param jobType Job type from job posting (backend, frontend, fullstack, etc.)
     * @param resumeJobType Job type from resume (if extracted)
     * @return Role relevance bonus (0-20)
     */
    private double calculateRoleRelevanceBonus(String jobType, String resumeJobType) {
        // If no job type information, return 0 (no bonus, no penalty)
        if (jobType == null || jobType.trim().isEmpty()) {
            return 0.0;
        }

        if (resumeJobType == null || resumeJobType.trim().isEmpty()) {
            return 0.0;
        }

        String normalizedJobType = normalizeJobType(jobType);
        String normalizedResumeType = normalizeJobType(resumeJobType);

        // Exact match: Full bonus
        if (normalizedJobType.equals(normalizedResumeType)) {
            return 20.0;
        }

        // Fullstack compatibility
        if (normalizedJobType.contains("fullstack") || normalizedJobType.contains("full stack")) {
            // Fullstack job accepts backend or frontend experience
            if (normalizedResumeType.contains("backend") || normalizedResumeType.contains("frontend")) {
                return 10.0;
            }
        }

        if (normalizedResumeType.contains("fullstack") || normalizedResumeType.contains("full stack")) {
            // Fullstack experience is valuable for backend/frontend roles
            if (normalizedJobType.contains("backend") || normalizedJobType.contains("frontend")) {
                return 15.0;
            }
        }

        // Partial match (e.g., "backend developer" matches "backend engineer")
        if (normalizedJobType.contains("backend") && normalizedResumeType.contains("backend")) {
            return 15.0;
        }

        if (normalizedJobType.contains("frontend") && normalizedResumeType.contains("frontend")) {
            return 15.0;
        }

        // No match
        return 0.0;
    }

    /**
     * Normalize job type string for comparison.
     * 
     * @param jobType Job type string
     * @return Normalized job type
     */
    private String normalizeJobType(String jobType) {
        if (jobType == null) {
            return "";
        }

        return jobType.toLowerCase()
                .trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[^a-z\\s]", ""); // Remove special characters
    }

    /**
     * Calculate experience score with only years (no role relevance).
     * Simplified version for cases where job type is not available.
     * 
     * @param resumeExperienceYears Years from resume
     * @param requiredMinYears Required years
     * @return Experience score (0-100)
     */
    public BigDecimal calculateExperienceScore(Integer resumeExperienceYears, Integer requiredMinYears) {
        return calculateExperienceScore(resumeExperienceYears, requiredMinYears, null, null);
    }
}
