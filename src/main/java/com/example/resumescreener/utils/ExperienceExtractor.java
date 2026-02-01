package com.example.resumescreener.utils;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for extracting years of experience from resume text.
 * Uses regex patterns to find experience mentions.
 */
@Component
public class ExperienceExtractor {

    // Patterns to match experience years
    private static final List<Pattern> EXPERIENCE_PATTERNS = Arrays.asList(
            // "5 years", "5+ years", "5-7 years"
            Pattern.compile("(\\d+)\\s*[+-]?\\s*(?:years?|yrs?|yr)", Pattern.CASE_INSENSITIVE),
            
            // "5 years of experience", "5+ years experience"
            Pattern.compile("(\\d+)\\s*[+-]?\\s*years?\\s+(?:of\\s+)?experience", Pattern.CASE_INSENSITIVE),
            
            // "Experience: 5 years"
            Pattern.compile("experience[\\s:]+(\\d+)\\s*[+-]?\\s*years?", Pattern.CASE_INSENSITIVE),
            
            // "5+ years in Java", "5 years in backend development"
            Pattern.compile("(\\d+)\\s*[+-]?\\s*years?\\s+in", Pattern.CASE_INSENSITIVE),
            
            // Date range calculation (e.g., "2019 - 2024" = 5 years)
            Pattern.compile("(\\d{4})\\s*[-–]\\s*(\\d{4})", Pattern.CASE_INSENSITIVE)
    );

    /**
     * Extract years of experience from resume text.
     * 
     * @param text Extracted resume text
     * @return Integer representing years of experience, or null if not found
     */
    public Integer extractExperienceYears(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        String normalizedText = normalizeText(text);
        List<Integer> foundYears = new ArrayList<>();

        // Try each pattern
        for (Pattern pattern : EXPERIENCE_PATTERNS) {
            Matcher matcher = pattern.matcher(normalizedText);
            while (matcher.find()) {
                if (matcher.groupCount() >= 1) {
                    try {
                        int years = Integer.parseInt(matcher.group(1));
                        // Validate reasonable range (0-50 years)
                        if (years >= 0 && years <= 50) {
                            foundYears.add(years);
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid numbers
                    }
                }
                
                // Handle date range pattern (group 1 and 2)
                if (matcher.groupCount() >= 2 && matcher.group(2) != null) {
                    try {
                        int startYear = Integer.parseInt(matcher.group(1));
                        int endYear = Integer.parseInt(matcher.group(2));
                        int calculatedYears = endYear - startYear;
                        if (calculatedYears >= 0 && calculatedYears <= 50) {
                            foundYears.add(calculatedYears);
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid dates
                    }
                }
            }
        }

        // Also try to extract from experience section
        Integer sectionYears = extractFromExperienceSection(normalizedText);
        if (sectionYears != null) {
            foundYears.add(sectionYears);
        }

        // Return the maximum found (most likely accurate)
        if (foundYears.isEmpty()) {
            return null;
        }

        return Collections.max(foundYears);
    }

    /**
     * Extract experience from dedicated "Experience" or "Work Experience" section.
     * 
     * @param text Normalized resume text
     * @return Years of experience or null
     */
    private Integer extractFromExperienceSection(String text) {
        // Find experience section
        Pattern experienceSectionPattern = Pattern.compile(
                "(?i)(?:work\\s+)?experience[:]?\\s*([^\\n]+(?:\\n[^\\n]+)*?)(?=\\n\\n|\\n[A-Z]|$)",
                Pattern.MULTILINE
        );

        Matcher sectionMatcher = experienceSectionPattern.matcher(text);
        if (sectionMatcher.find()) {
            String experienceSection = sectionMatcher.group(1);
            
            // Look for date ranges in the section
            Pattern dateRangePattern = Pattern.compile("(\\d{4})\\s*[-–]\\s*(\\d{4}|present|current)", Pattern.CASE_INSENSITIVE);
            Matcher dateMatcher = dateRangePattern.matcher(experienceSection);
            
            List<Integer> durations = new ArrayList<>();
            while (dateMatcher.find()) {
                try {
                    int startYear = Integer.parseInt(dateMatcher.group(1));
                    String endStr = dateMatcher.group(2).toLowerCase();
                    
                    int endYear;
                    if (endStr.equals("present") || endStr.equals("current")) {
                        endYear = Calendar.getInstance().get(Calendar.YEAR);
                    } else {
                        endYear = Integer.parseInt(endStr);
                    }
                    
                    int years = endYear - startYear;
                    if (years >= 0 && years <= 50) {
                        durations.add(years);
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid dates
                }
            }
            
            // Sum up all durations (total experience)
            if (!durations.isEmpty()) {
                return durations.stream().mapToInt(Integer::intValue).sum();
            }
        }

        return null;
    }

    /**
     * Normalize text for better pattern matching.
     * 
     * @param text Original text
     * @return Normalized text
     */
    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        
        // Replace common variations
        String normalized = text.replaceAll("\\s+", " "); // Multiple spaces to single
        normalized = normalized.replaceAll("\\n+", "\n"); // Multiple newlines to single
        
        return normalized;
    }
}
