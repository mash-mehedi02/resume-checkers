package com.example.resumescreener.service;

import com.example.resumescreener.model.CandidateResume;
import com.example.resumescreener.model.JobPost;
import com.example.resumescreener.utils.EducationScorer;
import com.example.resumescreener.utils.ExperienceScorer;
import com.example.resumescreener.utils.ProjectScorer;
import com.example.resumescreener.utils.SkillMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service for calculating match scores between resumes and jobs.
 * Implements skill matching, experience scoring, education scoring, project scoring,
 * and final weighted score calculation.
 * 
 * BIAS CONTROL MEASURES:
 * =====================
 * 1. SKILL-FIRST SCORING: Skills are weighted at 50% (highest weight), ensuring that
 *    technical competence is the primary factor in candidate evaluation. This prevents
 *    bias based on personal characteristics.
 * 
 * 2. NO NAME/GENDER USAGE: Candidate names are NOT used in scoring or ranking algorithms.
 *    The candidateName field is optional and stored only for display purposes. All scoring
 *    is based purely on technical qualifications (skills, experience, education, projects).
 * 
 * 3. CONFIGURABLE WEIGHTS: Scoring weights are configurable via application.yml, allowing
 *    organizations to adjust the importance of different factors based on their needs while
 *    maintaining skill-first approach. Default weights:
 *    - Skills: 50% (most important - technical competence)
 *    - Experience: 30% (years of practice)
 *    - Education: 10% (degree level - can be compensated)
 *    - Projects: 10% (practical experience - bonus factor)
 * 
 * 4. OBJECTIVE METRICS: All scoring is based on objective, measurable criteria:
 *    - Skill matching (exact/partial match algorithms)
 *    - Experience years (numeric comparison)
 *    - Education level (hierarchy-based)
 *    - Project relevance (keyword-based)
 * 
 * 5. NO DEMOGRAPHIC DATA: The system does not extract, store, or use:
 *    - Gender information
 *    - Age/date of birth
 *    - Ethnicity/race
 *    - Location (beyond what's in resume text)
 *    - Photo/images
 * 
 * These measures ensure fair, unbiased candidate evaluation based solely on qualifications.
 */
@Service
public class ScoringService {

    private final SkillMatcher skillMatcher;
    private final ExperienceScorer experienceScorer;
    private final EducationScorer educationScorer;
    private final ProjectScorer projectScorer;

    // BIAS CONTROL: Configurable weights allow organizations to adjust scoring while
    // maintaining skill-first approach. Skills are always weighted highest (50% default)
    // to ensure technical competence is the primary evaluation factor.
    @Value("${app.scoring.skill-weight:0.50}")
    private double skillWeight;

    @Value("${app.scoring.experience-weight:0.30}")
    private double experienceWeight;

    @Value("${app.scoring.education-weight:0.10}")
    private double educationWeight;

    @Value("${app.scoring.project-weight:0.10}")
    private double projectWeight;

    @Autowired
    public ScoringService(
            SkillMatcher skillMatcher,
            ExperienceScorer experienceScorer,
            EducationScorer educationScorer,
            ProjectScorer projectScorer) {
        this.skillMatcher = skillMatcher;
        this.experienceScorer = experienceScorer;
        this.educationScorer = educationScorer;
        this.projectScorer = projectScorer;
    }

    /**
     * Calculate skill match score between resume and job.
     * 
     * @param resume Resume entity
     * @param job Job posting entity
     * @return Skill score (0-100)
     */
    public BigDecimal calculateSkillScore(CandidateResume resume, JobPost job) {
        String resumeSkills = resume.getParsedSkills();
        String requiredSkills = job.getRequiredSkills();

        double score = skillMatcher.calculateSkillScore(resumeSkills, requiredSkills);
        
        return BigDecimal.valueOf(score)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get missing skills for a resume-job pair.
     * 
     * @param resume Resume entity
     * @param job Job posting entity
     * @return Set of missing skills
     */
    public java.util.Set<String> getMissingSkills(CandidateResume resume, JobPost job) {
        String resumeSkills = resume.getParsedSkills();
        String requiredSkills = job.getRequiredSkills();

        return skillMatcher.getMissingSkills(resumeSkills, requiredSkills);
    }

    /**
     * Get matched skills for a resume-job pair.
     * 
     * @param resume Resume entity
     * @param job Job posting entity
     * @return Set of matched skills
     */
    public java.util.Set<String> getMatchedSkills(CandidateResume resume, JobPost job) {
        String resumeSkills = resume.getParsedSkills();
        String requiredSkills = job.getRequiredSkills();

        return skillMatcher.getMatchedSkills(resumeSkills, requiredSkills);
    }

    /**
     * Calculate experience relevance score between resume and job.
     * 
     * @param resume Resume entity
     * @param job Job posting entity
     * @return Experience score (0-100)
     */
    public BigDecimal calculateExperienceScore(CandidateResume resume, JobPost job) {
        Integer resumeExperienceYears = resume.getExperienceYears();
        Integer requiredMinYears = job.getMinExperienceYears();
        String jobType = job.getJobType();
        
        // Try to extract job type from resume (could be in projects or experience)
        // For now, we'll use null as resume job type (can be enhanced later)
        String resumeJobType = null; // Could be extracted from resume text in future

        return experienceScorer.calculateExperienceScore(
                resumeExperienceYears,
                requiredMinYears,
                jobType,
                resumeJobType
        );
    }

    /**
     * Calculate education match score between resume and job.
     * 
     * @param resume Resume entity
     * @param job Job posting entity
     * @return Education score (0-100)
     */
    public BigDecimal calculateEducationScore(CandidateResume resume, JobPost job) {
        String resumeEducationLevel = resume.getEducationLevel();
        String resumeEducationField = resume.getEducationField();
        String requiredEducationLevel = job.getEducationLevel();
        String requiredEducationField = null; // Job entity doesn't have education field yet

        return educationScorer.calculateEducationScore(
                resumeEducationLevel,
                resumeEducationField,
                requiredEducationLevel,
                requiredEducationField
        );
    }

    /**
     * Calculate project relevance score between resume and job.
     * 
     * @param resume Resume entity
     * @param job Job posting entity
     * @return Project score (0-100)
     */
    public BigDecimal calculateProjectScore(CandidateResume resume, JobPost job) {
        String projectsSummary = resume.getProjectsSummary();
        String requiredSkills = job.getRequiredSkills();

        return projectScorer.calculateProjectScore(projectsSummary, requiredSkills);
    }

    /**
     * Calculate final weighted score combining all components.
     * 
     * Formula:
     * Final Score = (Skill Score × 0.50) + 
     *               (Experience Score × 0.30) + 
     *               (Education Score × 0.10) + 
     *               (Project Score × 0.10)
     * 
     * BIAS CONTROL: Skills are weighted at 50% (highest), ensuring skill-first evaluation.
     * This means technical competence is the primary factor, preventing bias based on
     * personal characteristics, names, or demographic information.
     * 
     * @param resume Resume entity
     * @param job Job posting entity
     * @return Final weighted score (0-100)
     */
    public BigDecimal calculateFinalScore(CandidateResume resume, JobPost job) {
        // Calculate individual component scores
        BigDecimal skillScore = calculateSkillScore(resume, job);
        BigDecimal experienceScore = calculateExperienceScore(resume, job);
        BigDecimal educationScore = calculateEducationScore(resume, job);
        BigDecimal projectScore = calculateProjectScore(resume, job);

        // Calculate weighted sum
        double finalScore = skillScore.doubleValue() * skillWeight +
                           experienceScore.doubleValue() * experienceWeight +
                           educationScore.doubleValue() * educationWeight +
                           projectScore.doubleValue() * projectWeight;

        // Ensure score is between 0 and 100
        finalScore = Math.max(0.0, Math.min(100.0, finalScore));

        return BigDecimal.valueOf(finalScore)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Getters for weights (used in final score calculation)
    public double getSkillWeight() {
        return skillWeight;
    }

    public double getExperienceWeight() {
        return experienceWeight;
    }

    public double getEducationWeight() {
        return educationWeight;
    }

    public double getProjectWeight() {
        return projectWeight;
    }
}
