package com.example.resumescreener.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for calculating project relevance score.
 * 
 * Scoring Logic:
 * - Presence of projects: Base score
 * - Project count: More projects = higher score
 * - Project relevance: Matches job domain/technologies
 * 
 * Assumptions:
 * - Having projects is better than no projects
 * - More projects indicate better experience
 * - Project descriptions are analyzed for relevance
 */
@Component
public class ProjectScorer {

    /**
     * Calculate project relevance score (0-100).
     * 
     * @param projectsSummary Project summary from resume (can be null)
     * @param requiredSkills Required skills from job (for relevance check)
     * @return Project score (0-100)
     */
    public BigDecimal calculateProjectScore(String projectsSummary, String requiredSkills) {
        // If no projects, return 0
        if (projectsSummary == null || projectsSummary.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Base score for having projects
        double baseScore = 50.0;

        // Count projects (rough estimate based on length and structure)
        int projectCount = estimateProjectCount(projectsSummary);
        double countBonus = Math.min(30.0, projectCount * 5.0); // Max 30 points

        // Relevance bonus (check if projects mention required skills)
        double relevanceBonus = calculateRelevanceBonus(projectsSummary, requiredSkills);

        // Total score: Base + Count bonus + Relevance bonus
        double totalScore = baseScore + countBonus + relevanceBonus;

        // Ensure score is between 0 and 100
        totalScore = Math.max(0.0, Math.min(100.0, totalScore));

        return BigDecimal.valueOf(totalScore)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Estimate number of projects from summary text.
     * 
     * @param projectsSummary Project summary text
     * @return Estimated project count
     */
    private int estimateProjectCount(String projectsSummary) {
        if (projectsSummary == null || projectsSummary.isEmpty()) {
            return 0;
        }

        // Count project indicators
        int count = 0;

        // Count numbered projects (1., 2., etc.)
        String[] lines = projectsSummary.split("\n");
        for (String line : lines) {
            if (line.trim().matches("^\\d+[.)]\\s+.*")) {
                count++;
            }
        }

        // Count "Project:" headers
        String lowerSummary = projectsSummary.toLowerCase();
        long projectHeaders = lowerSummary.split("project\\s*:").length - 1;
        count += projectHeaders;

        // If no clear indicators, estimate by length
        if (count == 0) {
            // Rough estimate: ~200 characters per project
            count = Math.max(1, projectsSummary.length() / 200);
        }

        return Math.min(count, 10); // Cap at 10 projects
    }

    /**
     * Calculate relevance bonus based on required skills mentioned in projects.
     * 
     * @param projectsSummary Project summary text
     * @param requiredSkills Required skills (comma-separated)
     * @return Relevance bonus (0-20)
     */
    private double calculateRelevanceBonus(String projectsSummary, String requiredSkills) {
        if (requiredSkills == null || requiredSkills.trim().isEmpty()) {
            return 0.0; // No requirements, no relevance check
        }

        if (projectsSummary == null || projectsSummary.isEmpty()) {
            return 0.0;
        }

        String lowerSummary = projectsSummary.toLowerCase();
        String[] required = requiredSkills.toLowerCase().split(",");
        
        int matchedSkills = 0;
        for (String skill : required) {
            String trimmedSkill = skill.trim();
            if (!trimmedSkill.isEmpty() && lowerSummary.contains(trimmedSkill)) {
                matchedSkills++;
            }
        }

        // Bonus: 5 points per matched skill, max 20 points
        double bonus = Math.min(20.0, matchedSkills * 5.0);

        return bonus;
    }
}
