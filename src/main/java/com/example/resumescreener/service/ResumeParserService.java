package com.example.resumescreener.service;

import com.example.resumescreener.exception.ResourceNotFoundException;
import com.example.resumescreener.model.CandidateResume;
import com.example.resumescreener.repository.ResumeRepository;
import com.example.resumescreener.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for parsing resume text and extracting structured data.
 * Uses utility classes to extract skills, experience, education, and projects.
 * 
 * BIAS CONTROL: This parser extracts ONLY technical qualifications:
 * - Skills (technical keywords)
 * - Experience years (numeric)
 * - Education level and field
 * - Project information
 * 
 * It does NOT extract or use:
 * - Candidate name (optional, stored separately for display only)
 * - Gender information
 * - Age/date of birth
 * - Ethnicity/race
 * - Personal photos
 * 
 * All extracted data is objective and qualification-based, ensuring fair evaluation.
 */
@Service
@Transactional
public class ResumeParserService {

    private final ResumeRepository resumeRepository;
    private final SkillExtractor skillExtractor;
    private final ExperienceExtractor experienceExtractor;
    private final EducationExtractor educationExtractor;
    private final ProjectExtractor projectExtractor;

    @Autowired
    public ResumeParserService(
            ResumeRepository resumeRepository,
            SkillExtractor skillExtractor,
            ExperienceExtractor experienceExtractor,
            EducationExtractor educationExtractor,
            ProjectExtractor projectExtractor) {
        this.resumeRepository = resumeRepository;
        this.skillExtractor = skillExtractor;
        this.experienceExtractor = experienceExtractor;
        this.educationExtractor = educationExtractor;
        this.projectExtractor = projectExtractor;
    }

    /**
     * Parse a resume and extract structured data.
     * 
     * @param resumeId ID of the resume to parse
     * @return Updated CandidateResume entity
     */
    public CandidateResume parseResume(Long resumeId) {
        CandidateResume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", resumeId));

        String extractedText = resume.getExtractedText();
        if (extractedText == null || extractedText.trim().isEmpty()) {
            throw new IllegalStateException("Resume text is empty. Please upload resume first.");
        }

        // Extract skills
        String skills = skillExtractor.extractSkills(extractedText);
        resume.setParsedSkills(skills);

        // Extract experience years
        Integer experienceYears = experienceExtractor.extractExperienceYears(extractedText);
        resume.setExperienceYears(experienceYears);

        // Extract education level
        String educationLevel = educationExtractor.extractEducationLevel(extractedText);
        resume.setEducationLevel(educationLevel);

        // Extract education field
        String educationField = educationExtractor.extractEducationField(extractedText);
        resume.setEducationField(educationField);

        // Extract projects summary
        String projectsSummary = projectExtractor.extractProjectsSummary(extractedText);
        resume.setProjectsSummary(projectsSummary);

        // Mark as parsed
        resume.setParsedAt(LocalDateTime.now());

        return resumeRepository.save(resume);
    }

    /**
     * Re-parse a resume (useful if parsing logic is updated).
     * 
     * @param resumeId ID of the resume to re-parse
     * @return Updated CandidateResume entity
     */
    public CandidateResume reparseResume(Long resumeId) {
        return parseResume(resumeId);
    }
}
