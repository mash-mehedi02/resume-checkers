package com.example.resumescreener.utils;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for extracting education information from resume text.
 * Uses keyword-based matching to identify degree levels and fields.
 */
@Component
public class EducationExtractor {

    // Education level keywords (ordered by level)
    private static final Map<String, String> EDUCATION_LEVELS = new LinkedHashMap<>();
    
    static {
        EDUCATION_LEVELS.put("phd", "PhD");
        EDUCATION_LEVELS.put("ph.d", "PhD");
        EDUCATION_LEVELS.put("doctorate", "PhD");
        EDUCATION_LEVELS.put("doctoral", "PhD");
        EDUCATION_LEVELS.put("d.phil", "PhD");
        
        EDUCATION_LEVELS.put("master", "Master");
        EDUCATION_LEVELS.put("masters", "Master");
        EDUCATION_LEVELS.put("m.sc", "Master");
        EDUCATION_LEVELS.put("m.s", "Master");
        EDUCATION_LEVELS.put("mba", "Master");
        EDUCATION_LEVELS.put("m.tech", "Master");
        EDUCATION_LEVELS.put("m.eng", "Master");
        EDUCATION_LEVELS.put("ms", "Master");
        EDUCATION_LEVELS.put("m.sc", "Master");
        
        EDUCATION_LEVELS.put("bachelor", "Bachelor");
        EDUCATION_LEVELS.put("bachelors", "Bachelor");
        EDUCATION_LEVELS.put("b.sc", "Bachelor");
        EDUCATION_LEVELS.put("b.s", "Bachelor");
        EDUCATION_LEVELS.put("b.tech", "Bachelor");
        EDUCATION_LEVELS.put("b.eng", "Bachelor");
        EDUCATION_LEVELS.put("b.e", "Bachelor");
        EDUCATION_LEVELS.put("bs", "Bachelor");
        EDUCATION_LEVELS.put("ba", "Bachelor");
        EDUCATION_LEVELS.put("b.com", "Bachelor");
        
        EDUCATION_LEVELS.put("diploma", "Diploma");
        EDUCATION_LEVELS.put("associate", "Associate");
        EDUCATION_LEVELS.put("certificate", "Certificate");
    }

    // Common education fields
    private static final Set<String> EDUCATION_FIELDS = new HashSet<>(Arrays.asList(
            "computer science", "cs", "software engineering", "se", "information technology", "it",
            "electrical engineering", "ee", "mechanical engineering", "me", "civil engineering",
            "electronics", "telecommunications", "data science", "artificial intelligence", "ai",
            "machine learning", "ml", "business administration", "mba", "management",
            "mathematics", "physics", "chemistry", "engineering"
    ));

    /**
     * Extract education level from resume text.
     * 
     * @param text Extracted resume text
     * @return Education level (PhD, Master, Bachelor, etc.) or null
     */
    public String extractEducationLevel(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        String normalizedText = normalizeText(text);
        String highestLevel = null;

        // Check for education section first
        String educationSection = extractEducationSection(normalizedText);
        if (educationSection != null) {
            highestLevel = findEducationLevel(educationSection);
        }

        // If not found in section, search entire text
        if (highestLevel == null) {
            highestLevel = findEducationLevel(normalizedText);
        }

        return highestLevel;
    }

    /**
     * Extract education field from resume text.
     * 
     * @param text Extracted resume text
     * @return Education field or null
     */
    public String extractEducationField(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        String normalizedText = normalizeText(text);
        String field = null;

        // Check education section first
        String educationSection = extractEducationSection(normalizedText);
        if (educationSection != null) {
            field = findEducationField(educationSection);
        }

        // If not found, search entire text
        if (field == null) {
            field = findEducationField(normalizedText);
        }

        return field;
    }

    /**
     * Find education level in text.
     * 
     * @param text Text to search
     * @return Highest education level found
     */
    private String findEducationLevel(String text) {
        String highestLevel = null;
        int highestPriority = Integer.MAX_VALUE;

        int priority = 0;
        for (Map.Entry<String, String> entry : EDUCATION_LEVELS.entrySet()) {
            String keyword = entry.getKey();
            String level = entry.getValue();
            
            // Use word boundary to avoid partial matches
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b", Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(text).find()) {
                // PhD has highest priority (0), then Master, then Bachelor
                if (highestLevel == null || priority < highestPriority) {
                    highestLevel = level;
                    highestPriority = priority;
                }
            }
            priority++;
        }

        return highestLevel;
    }

    /**
     * Find education field in text.
     * 
     * @param text Text to search
     * @return Education field found
     */
    private String findEducationField(String text) {
        for (String field : EDUCATION_FIELDS) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(field) + "\\b", Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(text).find()) {
                // Return full form (e.g., "computer science" instead of "cs")
                if (field.length() > 2) {
                    return capitalizeWords(field);
                }
            }
        }
        return null;
    }

    /**
     * Extract education section from resume text.
     * 
     * @param text Normalized resume text
     * @return Education section text or null
     */
    private String extractEducationSection(String text) {
        Pattern educationSectionPattern = Pattern.compile(
                "(?i)(?:education|academic\\s+background|qualifications)[:;]?\\s*([^\\n]+(?:\\n[^\\n]+)*?)(?=\\n\\n|\\n[A-Z]|$)",
                Pattern.MULTILINE
        );

        Matcher matcher = educationSectionPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
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
        
        String normalized = text.toLowerCase();
        normalized = normalized.replaceAll("\\s+", " ");
        normalized = normalized.replaceAll("\\n+", "\n");
        
        return normalized;
    }

    /**
     * Capitalize words in a string.
     * 
     * @param text Text to capitalize
     * @return Capitalized text
     */
    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (result.length() > 0) {
                result.append(" ");
            }
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1));
                }
            }
        }
        
        return result.toString();
    }
}
