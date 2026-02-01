package com.example.resumescreener.utils;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for extracting skills from resume text.
 * Uses advanced keyword-based matching with normalization and context
 * awareness.
 * Includes a comprehensive database of modern tech skills.
 */
@Component
public class SkillExtractor {

    // Extended technical skills database (Categorized for maintenance, but merged
    // for search)
    private static final Set<String> TECHNICAL_SKILLS = new HashSet<>();

    static {
        // --- Programming Languages ---
        TECHNICAL_SKILLS.addAll(Arrays.asList(
                "java", "python", "javascript", "typescript", "c++", "c#", "go", "golang", "rust", "kotlin", "scala",
                "php", "ruby", "swift", "dart", "r", "matlab", "perl", "bash", "shell scripting", "powershell",
                "html", "html5", "css", "css3", "sass", "less", "sql", "nosql", "pl/sql", "assembly"));

        // --- Frameworks & Libraries (Frontend/Backend/Mobile) ---
        TECHNICAL_SKILLS.addAll(Arrays.asList(
                "spring boot", "spring framework", "spring mvc", "spring security", "hibernate", "jpa", "jakarta ee",
                "react", "react.js", "angular", "angularjs", "vue", "vue.js", "next.js", "nuxt.js", "svelte",
                "node.js", "express.js", "nestjs", "django", "flask", "fastapi", "ruby on rails", "laravel", "symfony",
                "asp.net", "asp.net core", "entity framework", "jquery", "bootstrap", "tailwind css", "material ui",
                "flutter", "react native", "ionic", "xamarin", "swing", "javafx"));

        // --- Databases & Storage ---
        TECHNICAL_SKILLS.addAll(Arrays.asList(
                "mysql", "postgresql", "postgres", "mongodb", "redis", "oracle db", "microsoft sql server", "mssql",
                "sqlite", "mariadb", "cassandra", "elasticsearch", "dynamodb", "neo4j", "couchbase", "firebase",
                "cloud firestore", "realm", "h2"));

        // --- Cloud, DevOps & Infrastructure ---
        TECHNICAL_SKILLS.addAll(Arrays.asList(
                "aws", "amazon web services", "azure", "google cloud platform", "gcp", "heroku", "digitalocean",
                "docker", "kubernetes", "k8s", "openshift", "jenkins", "gitlab ci", "github actions", "circleci",
                "travis ci", "terraform", "ansible", "chef", "puppet", "vagrant", "prometheus", "grafana", "elk stack",
                "nginx", "apache tomcat", "linux", "unix", "ubuntu", "centos"));

        // --- Tools, Testing & Methodologies ---
        TECHNICAL_SKILLS.addAll(Arrays.asList(
                "git", "github", "gitlab", "bitbucket", "jira", "confluence", "trello", "asana", "slack",
                "maven", "gradle", "ant", "npm", "yarn", "webpack", "babel",
                "junit", "testng", "mockito", "selenium", "cypress", "jest", "mocha", "cucumber", "postman", "swagger",
                "rest api", "restful api", "graphql", "soap", "json", "xml", "microservices", "agile", "scrum",
                "kanban",
                "tdd", "bdd", "ci/cd", "oop", "design patterns", "clean code", "solid principles"));

        // --- Big Data, AI & ML ---
        TECHNICAL_SKILLS.addAll(Arrays.asList(
                "machine learning", "deep learning", "artificial intelligence", "data science", "nlp",
                "computer vision",
                "tensorflow", "pytorch", "keras", "scikit-learn", "pandas", "numpy", "matplotlib", "opencv",
                "apache spark", "hadoop", "apache kafka", "airflow", "tableau", "power bi"));
    }

    // Skill synonyms map for normalization during extraction
    private static final Map<String, String> SKILL_SYNONYMS = new HashMap<>();

    static {
        SKILL_SYNONYMS.put("js", "javascript");
        SKILL_SYNONYMS.put("ts", "typescript");
        SKILL_SYNONYMS.put("golang", "go");
        SKILL_SYNONYMS.put("reactjs", "react");
        SKILL_SYNONYMS.put("vuejs", "vue");
        SKILL_SYNONYMS.put("nodejs", "node.js");
        SKILL_SYNONYMS.put("expressjs", "express.js");
        SKILL_SYNONYMS.put("mssql", "sql server");
        SKILL_SYNONYMS.put("postgres", "postgresql");
        SKILL_SYNONYMS.put("aws", "amazon web services");
        SKILL_SYNONYMS.put("gcp", "google cloud platform");
        SKILL_SYNONYMS.put("k8s", "kubernetes");
        SKILL_SYNONYMS.put("repo", "git");
    }

    /**
     * Extract skills from resume text.
     * 
     * @param text Extracted resume text
     * @return Comma-separated string of extracted skills on lowercase
     */
    public String extractSkills(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        String normalizedText = normalizeText(text);
        Set<String> foundSkills = new LinkedHashSet<>();

        // 1. Check for skills in specific "Skills" sections (High Confidence)
        foundSkills.addAll(extractSkillsFromSection(normalizedText));

        // 2. Scan entire text for keywords (Broader Search)
        // We iterate specifically over our defined tech skills to avoid false positives
        for (String skill : TECHNICAL_SKILLS) {
            // Optimization: Only regex search if simple contains check passes
            if (normalizedText.contains(skill) && containsSkillCorrectly(normalizedText, skill)) {
                foundSkills.add(skill);
            }
        }

        return foundSkills.stream()
                .sorted()
                .collect(Collectors.joining(","));
    }

    /**
     * Check if text contains a skill using word boundaries.
     * Prevents "java" matching inside "javascript" or "go" matching "google".
     */
    private boolean containsSkillCorrectly(String text, String skill) {
        // Escape special chars in skill name for regex (e.g., c++, .net)
        String regexSkill = Pattern.quote(skill);
        // Look for word boundaries. Special handling for skills ending in symbols like
        // C++
        String pattern = "\\b" + regexSkill + "(?!\\w)";
        // Note: \\b matches start/end of word chars. For C++, the + are not word chars,
        // so standard \\b at end might fail.
        // Let's use a robust boundary check:
        // Start: bound or start of line. End: bound or punctuation or whitespace.

        // Simplified approach that works well for most tech terms:
        // 1. If skill has special chars (c++, .net, node.js), simple boundary check
        // logic is custom
        if (skill.matches(".*[^a-z0-9].*")) {
            return text.contains(skill); // Fallback to simple contains for complex terms to avoid regex issues,
                                         // strictness handled by list
        }

        return Pattern.compile("\\b" + regexSkill + "\\b").matcher(text).find();
    }

    /**
     * Extract skills specifically from a "Skills" header section.
     */
    private Set<String> extractSkillsFromSection(String text) {
        Set<String> skills = new HashSet<>();

        // Advanced Regex to capture content under common Skill headers
        // Captures text until double newline or next capitalized header
        Pattern skillsSectionPattern = Pattern.compile(
                "(?im)(?:^|\\n)\\s*(?:technical\\s+skills|core\\s+competencies|technologies|tech\\s+stack|programming\\s+languages|skills)\\s*[:|-]?\\s*\\n?([^\\n]+(?:\\n(?!\\s*[A-Z][a-z]+:)[^\\n]+)*)",
                Pattern.MULTILINE);

        java.util.regex.Matcher matcher = skillsSectionPattern.matcher(text);
        while (matcher.find()) {
            String sectionContent = matcher.group(1);
            // Split by common delimiters: comma, bullet points, pipes
            String[] tokens = sectionContent.split("[,;|•·\\n/]");

            for (String token : tokens) {
                String cleanToken = token.trim().toLowerCase();
                // Normalize using synonym map
                cleanToken = SKILL_SYNONYMS.getOrDefault(cleanToken, cleanToken);

                if (TECHNICAL_SKILLS.contains(cleanToken)) {
                    skills.add(cleanToken);
                } else {
                    // Startswith check for versioned skills like "java 8" -> "java"
                    for (String tech : TECHNICAL_SKILLS) {
                        if (cleanToken.startsWith(tech + " ")) {
                            skills.add(tech);
                        }
                    }
                }
            }
        }

        return skills;
    }

    private String normalizeText(String text) {
        if (text == null)
            return "";
        return text.toLowerCase().replaceAll("\\s+", " ").trim();
    }

    public Set<String> parseSkillsString(String skillsString) {
        if (skillsString == null || skillsString.isEmpty())
            return new HashSet<>();
        return Arrays.stream(skillsString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
