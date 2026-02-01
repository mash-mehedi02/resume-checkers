package com.example.resumescreener.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core skill matching engine.
 * Matches resume skills against job requirements and calculates scores.
 * 
 * Features:
 * - Exact match scoring
 * - Partial match using synonym map
 * - Missing skill handling
 * - Returns numeric skill score (0-100)
 * 
 * BIAS CONTROL: This is a skill-first matching engine. It evaluates candidates
 * purely based on technical skills without considering any personal information
 * (name, gender, age, ethnicity, etc.). All matching is objective and based on
 * skill keywords and synonyms, ensuring fair evaluation.
 */
@Component
public class SkillMatcher {

    private final SkillExtractor skillExtractor;

    // Skill synonym map for partial matching
    // Key: skill variant, Value: canonical skill name
    private static final Map<String, Set<String>> SKILL_SYNONYMS = new HashMap<>();
    
    static {
        // Spring family
        addSynonymGroup("spring", "spring boot", "spring mvc", "spring security", "spring framework");
        
        // Node.js variants
        addSynonymGroup("node.js", "nodejs", "node");
        
        // Database variants
        addSynonymGroup("mysql", "mariadb");
        addSynonymGroup("postgresql", "postgres");
        addSynonymGroup("sql server", "mssql", "microsoft sql server");
        addSynonymGroup("mongodb", "mongo");
        
        // .NET variants
        addSynonymGroup(".net", "dotnet", "asp.net", "aspnet");
        
        // JavaScript frameworks
        addSynonymGroup("react", "reactjs", "react.js");
        addSynonymGroup("angular", "angularjs", "angular.js");
        addSynonymGroup("vue", "vuejs", "vue.js");
        
        // Testing frameworks
        addSynonymGroup("junit", "junit5", "junit 5");
        addSynonymGroup("testng", "test ng");
        
        // Cloud platforms
        addSynonymGroup("aws", "amazon web services");
        addSynonymGroup("gcp", "google cloud platform", "google cloud");
        
        // Version control
        addSynonymGroup("git", "gitlab", "github");
        
        // Build tools
        addSynonymGroup("maven", "mvn");
        addSynonymGroup("gradle", "gradle build");
        
        // API types
        addSynonymGroup("rest api", "rest", "restful api", "restful");
        addSynonymGroup("graphql", "graph ql");
        
        // Microservices
        addSynonymGroup("microservices", "microservice", "micro services");
        
        // Message queues
        addSynonymGroup("rabbitmq", "rabbit mq");
        addSynonymGroup("apache kafka", "kafka");
    }

    /**
     * Helper method to add synonym groups.
     * All skills in a group are considered equivalent.
     */
    private static void addSynonymGroup(String... skills) {
        Set<String> group = new HashSet<>(Arrays.asList(skills));
        for (String skill : skills) {
            SKILL_SYNONYMS.put(normalizeSkillStatic(skill), group);
        }
    }

    /**
     * Static version of normalizeSkill for use in static initializer.
     */
    private static String normalizeSkillStatic(String skill) {
        if (skill == null) {
            return "";
        }
        return skill.toLowerCase()
                .trim()
                .replaceAll("\\s+", " ");
    }

    @Autowired
    public SkillMatcher(SkillExtractor skillExtractor) {
        this.skillExtractor = skillExtractor;
    }

    /**
     * Calculate skill match score between resume skills and job requirements.
     * 
     * @param resumeSkills Comma-separated skills from resume
     * @param requiredSkills Comma-separated required skills from job
     * @return Skill score (0-100)
     */
    public double calculateSkillScore(String resumeSkills, String requiredSkills) {
        if (requiredSkills == null || requiredSkills.trim().isEmpty()) {
            return 100.0; // If no requirements, consider it a perfect match
        }

        if (resumeSkills == null || resumeSkills.trim().isEmpty()) {
            return 0.0; // No skills in resume
        }

        Set<String> resumeSkillSet = parseSkills(resumeSkills);
        Set<String> requiredSkillSet = parseSkills(requiredSkills);

        if (requiredSkillSet.isEmpty()) {
            return 100.0;
        }

        if (resumeSkillSet.isEmpty()) {
            return 0.0;
        }

        // Count matched skills
        int matchedCount = countMatchedSkills(resumeSkillSet, requiredSkillSet);
        
        // Calculate score: (matched skills / total required skills) * 100
        double score = (double) matchedCount / requiredSkillSet.size() * 100.0;
        
        // Ensure score is between 0 and 100
        return Math.max(0.0, Math.min(100.0, score));
    }

    /**
     * Count how many required skills are matched in resume skills.
     * Uses exact match and synonym-based partial matching.
     * 
     * @param resumeSkillSet Set of skills from resume
     * @param requiredSkillSet Set of required skills from job
     * @return Number of matched skills
     */
    private int countMatchedSkills(Set<String> resumeSkillSet, Set<String> requiredSkillSet) {
        int matchedCount = 0;

        for (String requiredSkill : requiredSkillSet) {
            String normalizedRequired = normalizeSkill(requiredSkill);
            
            // Check exact match
            if (resumeSkillSet.contains(normalizedRequired)) {
                matchedCount++;
                continue;
            }

            // Check synonym-based partial match
            if (hasSynonymMatch(normalizedRequired, resumeSkillSet)) {
                matchedCount++;
            }
        }

        return matchedCount;
    }

    /**
     * Check if a required skill has a synonym match in resume skills.
     * 
     * @param requiredSkill Normalized required skill
     * @param resumeSkillSet Set of resume skills
     * @return true if synonym match found
     */
    private boolean hasSynonymMatch(String requiredSkill, Set<String> resumeSkillSet) {
        // Get synonyms for required skill
        Set<String> requiredSynonyms = SKILL_SYNONYMS.get(requiredSkill);
        if (requiredSynonyms == null) {
            return false;
        }

        // Check if any resume skill matches any synonym
        for (String resumeSkill : resumeSkillSet) {
            String normalizedResume = normalizeSkill(resumeSkill);
            
            // Direct synonym match
            if (requiredSynonyms.contains(normalizedResume)) {
                return true;
            }

            // Check if resume skill has synonyms that overlap
            Set<String> resumeSynonyms = SKILL_SYNONYMS.get(normalizedResume);
            if (resumeSynonyms != null) {
                // Check for intersection
                Set<String> intersection = new HashSet<>(requiredSynonyms);
                intersection.retainAll(resumeSynonyms);
                if (!intersection.isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check for partial word match (e.g., "spring" matches "spring boot").
     * 
     * @param requiredSkill Required skill
     * @param resumeSkillSet Resume skills
     * @return true if partial match found
     */
    private boolean hasPartialMatch(String requiredSkill, Set<String> resumeSkillSet) {
        String normalizedRequired = normalizeSkill(requiredSkill);

        for (String resumeSkill : resumeSkillSet) {
            String normalizedResume = normalizeSkill(resumeSkill);

            // Check if required skill is contained in resume skill
            if (normalizedResume.contains(normalizedRequired) && 
                normalizedResume.length() > normalizedRequired.length()) {
                return true;
            }

            // Check if resume skill is contained in required skill
            if (normalizedRequired.contains(normalizedResume) && 
                normalizedRequired.length() > normalizedResume.length()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get list of missing skills (required but not found in resume).
     * 
     * @param resumeSkills Comma-separated resume skills
     * @param requiredSkills Comma-separated required skills
     * @return Set of missing skills
     */
    public Set<String> getMissingSkills(String resumeSkills, String requiredSkills) {
        if (requiredSkills == null || requiredSkills.trim().isEmpty()) {
            return new HashSet<>();
        }

        if (resumeSkills == null || resumeSkills.trim().isEmpty()) {
            return parseSkills(requiredSkills);
        }

        Set<String> resumeSkillSet = parseSkills(resumeSkills);
        Set<String> requiredSkillSet = parseSkills(requiredSkills);
        Set<String> missingSkills = new HashSet<>();

        for (String requiredSkill : requiredSkillSet) {
            String normalizedRequired = normalizeSkill(requiredSkill);
            
            // Check if skill is missing (no exact or synonym match)
            if (!resumeSkillSet.contains(normalizedRequired) && 
                !hasSynonymMatch(normalizedRequired, resumeSkillSet) &&
                !hasPartialMatch(normalizedRequired, resumeSkillSet)) {
                missingSkills.add(requiredSkill);
            }
        }

        return missingSkills;
    }

    /**
     * Get list of matched skills (found in resume).
     * 
     * @param resumeSkills Comma-separated resume skills
     * @param requiredSkills Comma-separated required skills
     * @return Set of matched skills
     */
    public Set<String> getMatchedSkills(String resumeSkills, String requiredSkills) {
        if (requiredSkills == null || requiredSkills.trim().isEmpty()) {
            return new HashSet<>();
        }

        if (resumeSkills == null || resumeSkills.trim().isEmpty()) {
            return new HashSet<>();
        }

        Set<String> resumeSkillSet = parseSkills(resumeSkills);
        Set<String> requiredSkillSet = parseSkills(requiredSkills);
        Set<String> matchedSkills = new HashSet<>();

        for (String requiredSkill : requiredSkillSet) {
            String normalizedRequired = normalizeSkill(requiredSkill);
            
            // Check if skill is matched (exact, synonym, or partial)
            if (resumeSkillSet.contains(normalizedRequired) || 
                hasSynonymMatch(normalizedRequired, resumeSkillSet) ||
                hasPartialMatch(normalizedRequired, resumeSkillSet)) {
                matchedSkills.add(requiredSkill);
            }
        }

        return matchedSkills;
    }

    /**
     * Parse comma-separated skills string into a Set.
     * 
     * @param skillsString Comma-separated skills
     * @return Set of normalized skills
     */
    private Set<String> parseSkills(String skillsString) {
        if (skillsString == null || skillsString.trim().isEmpty()) {
            return new HashSet<>();
        }

        return Arrays.stream(skillsString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::normalizeSkill)
                .collect(Collectors.toSet());
    }

    /**
     * Normalize a skill name for comparison.
     * 
     * @param skill Skill name
     * @return Normalized skill name
     */
    private String normalizeSkill(String skill) {
        if (skill == null) {
            return "";
        }
        
        return skill.toLowerCase()
                .trim()
                .replaceAll("\\s+", " "); // Multiple spaces to single space
    }
}
