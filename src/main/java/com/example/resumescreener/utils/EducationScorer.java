package com.example.resumescreener.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for calculating education match score.
 * 
 * Scoring Logic:
 * - Matches education level from resume with job requirement
 * - Higher education levels get higher scores
 * - Field relevance provides bonus points
 * 
 * Assumptions:
 * - Education levels: PhD > Master > Bachelor > Diploma > Certificate
 * - Field match (e.g., Computer Science) provides bonus
 * - If no requirement, consider it a match (100 points)
 */
@Component
public class EducationScorer {

    // Education level hierarchy (higher number = higher level)
    private static final Map<String, Integer> EDUCATION_HIERARCHY = new HashMap<>();
    
    static {
        EDUCATION_HIERARCHY.put("phd", 5);
        EDUCATION_HIERARCHY.put("master", 4);
        EDUCATION_HIERARCHY.put("bachelor", 3);
        EDUCATION_HIERARCHY.put("diploma", 2);
        EDUCATION_HIERARCHY.put("certificate", 1);
        EDUCATION_HIERARCHY.put("associate", 2);
    }

    /**
     * Calculate education match score (0-100).
     * 
     * @param resumeEducationLevel Education level from resume (can be null)
     * @param resumeEducationField Education field from resume (can be null)
     * @param requiredEducationLevel Required education level from job (can be null)
     * @param requiredEducationField Required education field from job (can be null)
     * @return Education score (0-100)
     */
    public BigDecimal calculateEducationScore(
            String resumeEducationLevel,
            String resumeEducationField,
            String requiredEducationLevel,
            String requiredEducationField) {

        // If no requirement, consider it a match
        if (requiredEducationLevel == null || requiredEducationLevel.trim().isEmpty()) {
            return BigDecimal.valueOf(100.0);
        }

        // If no education in resume, return 0
        if (resumeEducationLevel == null || resumeEducationLevel.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Calculate level match score
        double levelScore = calculateLevelScore(resumeEducationLevel, requiredEducationLevel);

        // Calculate field relevance bonus
        double fieldBonus = calculateFieldBonus(resumeEducationField, requiredEducationField);

        // Combine: Level score (80%) + Field bonus (20%)
        // But cap field bonus so total doesn't exceed 100
        double totalScore = levelScore * 0.80 + fieldBonus * 0.20;

        // Ensure score is between 0 and 100
        totalScore = Math.max(0.0, Math.min(100.0, totalScore));

        return BigDecimal.valueOf(totalScore)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate score based on education level match.
     * 
     * @param resumeLevel Education level from resume
     * @param requiredLevel Required education level
     * @return Level match score (0-100)
     */
    private double calculateLevelScore(String resumeLevel, String requiredLevel) {
        String normalizedResume = normalizeLevel(resumeLevel);
        String normalizedRequired = normalizeLevel(requiredLevel);

        Integer resumeRank = EDUCATION_HIERARCHY.get(normalizedResume);
        Integer requiredRank = EDUCATION_HIERARCHY.get(normalizedRequired);

        // If levels not recognized, return 50 (neutral)
        if (resumeRank == null || requiredRank == null) {
            // Try partial match
            if (normalizedResume.contains(normalizedRequired) || normalizedRequired.contains(normalizedResume)) {
                return 80.0; // Partial match
            }
            return 50.0; // Unknown levels
        }

        // Exact match: 100 points
        if (resumeRank.equals(requiredRank)) {
            return 100.0;
        }

        // Higher than required: 100 points (over-qualified is good)
        if (resumeRank > requiredRank) {
            return 100.0;
        }

        // Lower than required: Proportional score
        // Calculate ratio and apply penalty
        double ratio = (double) resumeRank / requiredRank;
        
        if (ratio >= 0.8) {
            // Close to required (e.g., Bachelor vs Master)
            return 70.0;
        } else if (ratio >= 0.6) {
            // Moderate gap (e.g., Diploma vs Bachelor)
            return 50.0;
        } else {
            // Large gap (e.g., Certificate vs Bachelor)
            return 30.0;
        }
    }

    /**
     * Calculate field relevance bonus.
     * 
     * @param resumeField Education field from resume
     * @param requiredField Required education field
     * @return Field bonus (0-20)
     */
    private double calculateFieldBonus(String resumeField, String requiredField) {
        // If no field requirement, no bonus/penalty
        if (requiredField == null || requiredField.trim().isEmpty()) {
            return 0.0;
        }

        // If no field in resume, no bonus
        if (resumeField == null || resumeField.trim().isEmpty()) {
            return 0.0;
        }

        String normalizedResume = normalizeField(resumeField);
        String normalizedRequired = normalizeField(requiredField);

        // Exact match: Full bonus
        if (normalizedResume.equals(normalizedRequired)) {
            return 20.0;
        }

        // Partial match (contains): Partial bonus
        if (normalizedResume.contains(normalizedRequired) || normalizedRequired.contains(normalizedResume)) {
            return 15.0;
        }

        // Related fields (e.g., Computer Science vs Software Engineering)
        if (areRelatedFields(normalizedResume, normalizedRequired)) {
            return 10.0;
        }

        return 0.0;
    }

    /**
     * Check if two education fields are related.
     * 
     * @param field1 First field
     * @param field2 Second field
     * @return true if fields are related
     */
    private boolean areRelatedFields(String field1, String field2) {
        // Computer Science related
        if ((field1.contains("computer") || field1.contains("cs")) &&
            (field2.contains("computer") || field2.contains("software") || field2.contains("it"))) {
            return true;
        }

        // Engineering related
        if ((field1.contains("engineering") || field1.contains("engineer")) &&
            (field2.contains("engineering") || field2.contains("engineer"))) {
            return true;
        }

        return false;
    }

    /**
     * Normalize education level for comparison.
     */
    private String normalizeLevel(String level) {
        if (level == null) {
            return "";
        }
        return level.toLowerCase().trim();
    }

    /**
     * Normalize education field for comparison.
     */
    private String normalizeField(String field) {
        if (field == null) {
            return "";
        }
        return field.toLowerCase().trim();
    }
}
