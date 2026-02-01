package com.example.resumescreener.utils;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for extracting skills from resume text.
 * Uses keyword-based matching with normalization.
 */
@Component
public class SkillExtractor {

    // Common technical skills database (can be expanded)
    private static final Set<String> TECHNICAL_SKILLS = new HashSet<>(Arrays.asList(
            // Programming Languages
            "java", "python", "javascript", "typescript", "c++", "c#", "go", "rust", "kotlin", "scala",
            "php", "ruby", "swift", "dart", "r", "matlab",
            
            // Frameworks & Libraries
            "spring boot", "spring", "spring mvc", "spring security", "hibernate", "jpa",
            "react", "angular", "vue", "node.js", "express", "django", "flask", "fastapi",
            "laravel", "symfony", "asp.net", ".net", "rails", "gin", "echo",
            
            // Databases
            "mysql", "postgresql", "mongodb", "redis", "oracle", "sql server", "sqlite",
            "cassandra", "elasticsearch", "dynamodb", "neo4j",
            
            // Cloud & DevOps
            "aws", "azure", "gcp", "docker", "kubernetes", "jenkins", "gitlab ci", "github actions",
            "terraform", "ansible", "chef", "puppet",
            
            // Tools & Technologies
            "git", "maven", "gradle", "ant", "junit", "testng", "mockito", "selenium",
            "rest api", "graphql", "soap", "microservices", "rabbitmq", "kafka",
            "apache spark", "hadoop", "apache kafka", "apache storm"
    ));

    // Skill synonyms map (for partial matching)
    private static final Map<String, String> SKILL_SYNONYMS = new HashMap<>();
    
    static {
        SKILL_SYNONYMS.put("spring boot", "spring");
        SKILL_SYNONYMS.put("spring mvc", "spring");
        SKILL_SYNONYMS.put("spring security", "spring");
        SKILL_SYNONYMS.put("node.js", "nodejs");
        SKILL_SYNONYMS.put("nodejs", "node.js");
        SKILL_SYNONYMS.put("sql server", "mssql");
        SKILL_SYNONYMS.put("mssql", "sql server");
        SKILL_SYNONYMS.put(".net", "dotnet");
        SKILL_SYNONYMS.put("dotnet", ".net");
    }

    /**
     * Extract skills from resume text.
     * 
     * @param text Extracted resume text
     * @return Comma-separated string of extracted skills
     */
    public String extractSkills(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        String normalizedText = normalizeText(text);
        Set<String> foundSkills = new LinkedHashSet<>();

        // Extract skills using keyword matching
        for (String skill : TECHNICAL_SKILLS) {
            if (containsSkill(normalizedText, skill)) {
                foundSkills.add(skill);
            }
        }

        // Also check for skills mentioned in "Skills" or "Technical Skills" section
        foundSkills.addAll(extractSkillsFromSection(normalizedText));

        return foundSkills.stream()
                .sorted()
                .collect(Collectors.joining(","));
    }

    /**
     * Check if text contains a skill (case-insensitive, word boundary aware).
     * 
     * @param text Resume text
     * @param skill Skill to search for
     * @return true if skill is found
     */
    private boolean containsSkill(String text, String skill) {
        // Use word boundaries to avoid partial matches
        String pattern = "\\b" + Pattern.quote(skill.toLowerCase()) + "\\b";
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text).find();
    }

    /**
     * Extract skills from dedicated "Skills" section.
     * Looks for sections like "Skills:", "Technical Skills:", etc.
     * 
     * @param text Normalized resume text
     * @return Set of skills found in skills section
     */
    private Set<String> extractSkillsFromSection(String text) {
        Set<String> skills = new HashSet<>();
        
        // Pattern to find skills section
        Pattern skillsSectionPattern = Pattern.compile(
                "(?i)(?:skills|technical\\s+skills|technologies|tools|expertise)[:;]?\\s*([^\\n]+(?:\\n[^\\n]+)*?)(?=\\n\\n|\\n[A-Z]|$)",
                Pattern.MULTILINE
        );

        java.util.regex.Matcher matcher = skillsSectionPattern.matcher(text);
        if (matcher.find()) {
            String skillsSection = matcher.group(1);
            // Extract individual skills (comma, semicolon, or newline separated)
            String[] potentialSkills = skillsSection.split("[,;\\n]");
            
            for (String potentialSkill : potentialSkills) {
                String trimmed = potentialSkill.trim().toLowerCase();
                // Check if it's a known skill or looks like a skill
                if (!trimmed.isEmpty() && (TECHNICAL_SKILLS.contains(trimmed) || isValidSkillFormat(trimmed))) {
                    skills.add(trimmed);
                }
            }
        }

        return skills;
    }

    /**
     * Check if a string looks like a valid skill format.
     * 
     * @param skill String to check
     * @return true if it looks like a skill
     */
    private boolean isValidSkillFormat(String skill) {
        // Skills are usually 2-30 characters, alphanumeric with common separators
        return skill.matches("^[a-z0-9\\s.+#-]{2,30}$") && 
               !skill.matches("^\\d+$"); // Not just numbers
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
        
        // Convert to lowercase
        String normalized = text.toLowerCase();
        
        // Replace common variations
        normalized = normalized.replaceAll("\\s+", " "); // Multiple spaces to single
        normalized = normalized.replaceAll("\\n+", "\n"); // Multiple newlines to single
        
        return normalized;
    }

    /**
     * Get list of extracted skills as a Set.
     * 
     * @param skillsString Comma-separated skills string
     * @return Set of skills
     */
    public Set<String> parseSkillsString(String skillsString) {
        if (skillsString == null || skillsString.trim().isEmpty()) {
            return new HashSet<>();
        }
        
        return Arrays.stream(skillsString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}
