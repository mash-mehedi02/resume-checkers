package com.example.resumescreener.utils;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for extracting project information from resume text.
 * Uses keyword-based matching to identify project sections.
 */
@Component
public class ProjectExtractor {

    // Project-related keywords
    private static final Set<String> PROJECT_KEYWORDS = new HashSet<>(Arrays.asList(
            "project", "projects", "personal project", "side project", "academic project",
            "portfolio", "work project", "development project", "software project"
    ));

    /**
     * Extract project summary from resume text.
     * 
     * @param text Extracted resume text
     * @return Project summary or null
     */
    public String extractProjectsSummary(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        String normalizedText = normalizeText(text);
        String projectsSection = extractProjectsSection(normalizedText);

        if (projectsSection != null && !projectsSection.trim().isEmpty()) {
            // Clean up the section
            projectsSection = projectsSection.trim();
            
            // Limit length to avoid storing too much data
            if (projectsSection.length() > 1000) {
                projectsSection = projectsSection.substring(0, 1000) + "...";
            }
            
            return projectsSection;
        }

        return null;
    }

    /**
     * Count number of projects mentioned in resume.
     * 
     * @param text Extracted resume text
     * @return Number of projects found
     */
    public int countProjects(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        String normalizedText = normalizeText(text);
        String projectsSection = extractProjectsSection(normalizedText);

        if (projectsSection == null) {
            return 0;
        }

        // Count project entries (usually separated by newlines or numbers)
        int count = 0;
        
        // Pattern to find project entries (numbered or bulleted)
        Pattern projectEntryPattern = Pattern.compile(
                "(?i)(?:^|\\n)\\s*(?:\\d+\\.|[-*•]|project\\s+\\d+[:]?)\\s+",
                Pattern.MULTILINE
        );

        Matcher matcher = projectEntryPattern.matcher(projectsSection);
        while (matcher.find()) {
            count++;
        }

        // If no numbered entries, count by "Project:" headers
        if (count == 0) {
            Pattern projectHeaderPattern = Pattern.compile(
                    "(?i)(?:^|\\n)\\s*project\\s*[:]?\\s*",
                    Pattern.MULTILINE
            );
            Matcher headerMatcher = projectHeaderPattern.matcher(projectsSection);
            while (headerMatcher.find()) {
                count++;
            }
        }

        return Math.max(1, count); // At least 1 if section exists
    }

    /**
     * Extract projects section from resume text.
     * 
     * @param text Normalized resume text
     * @return Projects section text or null
     */
    private String extractProjectsSection(String text) {
        // Pattern to find projects section
        Pattern projectsSectionPattern = Pattern.compile(
                "(?i)(?:projects?|personal\\s+projects?|side\\s+projects?|academic\\s+projects?|portfolio)[:;]?\\s*([^\\n]+(?:\\n[^\\n]+)*?)(?=\\n\\n|\\n[A-Z]|$)",
                Pattern.MULTILINE
        );

        Matcher matcher = projectsSectionPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // Also try to find project mentions in experience section
        return extractProjectsFromExperience(text);
    }

    /**
     * Extract project mentions from experience section.
     * 
     * @param text Normalized resume text
     * @return Project-related text or null
     */
    private String extractProjectsFromExperience(String text) {
        // Look for project mentions in experience/work section
        Pattern experiencePattern = Pattern.compile(
                "(?i)(?:experience|work\\s+experience)[:;]?\\s*([^\\n]+(?:\\n[^\\n]+)*?)(?=\\n\\n|\\n[A-Z]|$)",
                Pattern.MULTILINE
        );

        Matcher matcher = experiencePattern.matcher(text);
        if (matcher.find()) {
            String experienceSection = matcher.group(1);
            
            // Check if it contains project keywords
            for (String keyword : PROJECT_KEYWORDS) {
                if (experienceSection.toLowerCase().contains(keyword)) {
                    // Extract sentences containing project keywords
                    String[] sentences = experienceSection.split("[.!?]");
                    StringBuilder projectText = new StringBuilder();
                    
                    for (String sentence : sentences) {
                        if (sentence.toLowerCase().contains("project")) {
                            projectText.append(sentence.trim()).append(". ");
                        }
                    }
                    
                    if (projectText.length() > 0) {
                        return projectText.toString().trim();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Normalize text for better matching.
     * 
     * @param text Original text
     * @return Normalized text
     */
    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        
        String normalized = text.replaceAll("\\s+", " ");
        normalized = normalized.replaceAll("\\n+", "\n");
        
        return normalized;
    }
}
