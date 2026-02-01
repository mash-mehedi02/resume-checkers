package com.example.resumescreener.service;

import com.example.resumescreener.dto.RankingResponse;
import com.example.resumescreener.exception.ResourceNotFoundException;
import com.example.resumescreener.model.CandidateResume;
import com.example.resumescreener.model.JobPost;
import com.example.resumescreener.model.ResumeScore;
import com.example.resumescreener.repository.JobRepository;
import com.example.resumescreener.repository.ResumeRepository;
import com.example.resumescreener.repository.ResumeScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Service for ranking resumes against a job posting.
 * Calculates scores, sorts by final score, and handles tie-breaking.
 * 
 * BIAS CONTROL MEASURES:
 * =====================
 * 1. NO NAME/GENDER IN RANKING: Candidate names are NOT used in ranking algorithms.
 *    Ranking is based solely on calculated scores (skills, experience, education, projects).
 *    Names are included in response only for display/identification purposes.
 * 
 * 2. OBJECTIVE TIE-BREAKING: When scores are tied, tie-breaking uses only objective
 *    technical metrics (skill score, experience score, matched skills count). Never
 *    uses personal information like name, gender, or demographic data.
 * 
 * 3. SCORE-BASED SORTING: All ranking is done purely on calculated scores. No human
 *    bias or subjective factors are introduced into the ranking process.
 */
@Service
@Transactional
public class RankingService {

    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeScoreRepository scoreRepository;
    private final ScoringService scoringService;

    @Autowired
    public RankingService(
            JobRepository jobRepository,
            ResumeRepository resumeRepository,
            ResumeScoreRepository scoreRepository,
            ScoringService scoringService) {
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.scoreRepository = scoreRepository;
        this.scoringService = scoringService;
    }

    /**
     * Get ranked list of resumes for a job.
     * Calculates scores if not already calculated, sorts by final score, and applies tie-breaking.
     * 
     * @param jobId Job ID
     * @return List of RankingResponse sorted by final score (descending)
     */
    public List<RankingResponse> getRankedResumes(Long jobId) {
        // Fetch job
        JobPost job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));

        // Fetch all resumes
        List<CandidateResume> resumes = resumeRepository.findAll();

        if (resumes.isEmpty()) {
            return new ArrayList<>();
        }

        // Calculate or retrieve scores for each resume
        List<RankingResponse> rankingResponses = new ArrayList<>();

        for (CandidateResume resume : resumes) {
            // Get or calculate score
            ResumeScore resumeScore = getOrCalculateScore(job, resume);

            // Create ranking response
            RankingResponse response = createRankingResponse(resume, resumeScore, job);
            rankingResponses.add(response);
        }

        // Sort by final score (descending)
        rankingResponses.sort((r1, r2) -> r2.getFinalScore().compareTo(r1.getFinalScore()));

        // Apply tie-breaking and assign ranks
        applyTieBreakingAndRanks(rankingResponses);

        return rankingResponses;
    }

    /**
     * Get existing score or calculate new score for resume-job pair.
     * 
     * @param job Job posting
     * @param resume Resume
     * @return ResumeScore entity
     */
    private ResumeScore getOrCalculateScore(JobPost job, CandidateResume resume) {
        // Check if score already exists
        Optional<ResumeScore> existingScore = scoreRepository.findByJobPostIdAndCandidateResumeId(
                job.getId(), resume.getId());

        if (existingScore.isPresent()) {
            return existingScore.get();
        }

        // Calculate new scores
        BigDecimal skillScore = scoringService.calculateSkillScore(resume, job);
        BigDecimal experienceScore = scoringService.calculateExperienceScore(resume, job);
        BigDecimal educationScore = scoringService.calculateEducationScore(resume, job);
        BigDecimal projectScore = scoringService.calculateProjectScore(resume, job);
        BigDecimal finalScore = scoringService.calculateFinalScore(resume, job);

        // Create and save score entity
        ResumeScore resumeScore = new ResumeScore(job, resume);
        resumeScore.setSkillScore(skillScore);
        resumeScore.setExperienceScore(experienceScore);
        resumeScore.setEducationScore(educationScore);
        resumeScore.setProjectScore(projectScore);
        resumeScore.setFinalScore(finalScore);

        return scoreRepository.save(resumeScore);
    }

    /**
     * Create RankingResponse from resume and score data.
     * 
     * @param resume Resume entity
     * @param resumeScore Score entity
     * @param job Job posting
     * @return RankingResponse DTO
     */
    private RankingResponse createRankingResponse(CandidateResume resume, ResumeScore resumeScore, JobPost job) {
        RankingResponse response = new RankingResponse();
        response.setResumeId(resume.getId());
        response.setCandidateName(resume.getCandidateName());
        response.setFileName(resume.getFileName());
        response.setSkillScore(resumeScore.getSkillScore());
        response.setExperienceScore(resumeScore.getExperienceScore());
        response.setEducationScore(resumeScore.getEducationScore());
        response.setProjectScore(resumeScore.getProjectScore());
        response.setFinalScore(resumeScore.getFinalScore());
        response.setMatchedSkills(scoringService.getMatchedSkills(resume, job));
        response.setMissingSkills(scoringService.getMissingSkills(resume, job));
        return response;
    }

    /**
     * Apply tie-breaking logic and assign ranks.
     * 
     * Tie-breaking order:
     * 1. Final score (already sorted)
     * 2. Skill score (higher is better)
     * 3. Experience score (higher is better)
     * 4. Number of matched skills (more is better)
     * 5. Resume ID (lower is better - first uploaded)
     * 
     * @param responses List of ranking responses (already sorted by final score)
     */
    private void applyTieBreakingAndRanks(List<RankingResponse> responses) {
        if (responses.isEmpty()) {
            return;
        }

        // Apply tie-breaking sort to entire list
        responses.sort((r1, r2) -> {
            // 1. Compare final score (descending)
            int finalScoreCompare = r2.getFinalScore().compareTo(r1.getFinalScore());
            if (finalScoreCompare != 0) {
                return finalScoreCompare;
            }

            // 2. Compare skill score (descending)
            int skillCompare = r2.getSkillScore().compareTo(r1.getSkillScore());
            if (skillCompare != 0) {
                return skillCompare;
            }

            // 3. Compare experience score (descending)
            int expCompare = r2.getExperienceScore().compareTo(r1.getExperienceScore());
            if (expCompare != 0) {
                return expCompare;
            }

            // 4. Compare number of matched skills (descending)
            int matchedSkillsCompare = Integer.compare(
                    r2.getMatchedSkills() != null ? r2.getMatchedSkills().size() : 0,
                    r1.getMatchedSkills() != null ? r1.getMatchedSkills().size() : 0);
            if (matchedSkillsCompare != 0) {
                return matchedSkillsCompare;
            }

            // 5. Compare resume ID (ascending - first uploaded wins)
            return Long.compare(r1.getResumeId(), r2.getResumeId());
        });

        // Assign ranks (same rank for identical scores after tie-breaking)
        int currentRank = 1;
        for (int i = 0; i < responses.size(); i++) {
            RankingResponse response = responses.get(i);
            
            // If not first item, check if it's different from previous
            if (i > 0) {
                RankingResponse previous = responses.get(i - 1);
                boolean isDifferent = !response.getFinalScore().equals(previous.getFinalScore()) ||
                                     !response.getSkillScore().equals(previous.getSkillScore()) ||
                                     !response.getExperienceScore().equals(previous.getExperienceScore()) ||
                                     !Objects.equals(
                                         response.getMatchedSkills() != null ? response.getMatchedSkills().size() : 0,
                                         previous.getMatchedSkills() != null ? previous.getMatchedSkills().size() : 0);
                
                if (isDifferent) {
                    currentRank = i + 1;
                }
            }
            
            response.setRank(currentRank);
        }
    }


    /**
     * Recalculate all scores for a job (useful if scoring logic is updated).
     * 
     * @param jobId Job ID
     * @return List of updated RankingResponse
     */
    public List<RankingResponse> recalculateRankings(Long jobId) {
        // Delete existing scores
        scoreRepository.deleteByJobPostId(jobId);
        
        // Recalculate
        return getRankedResumes(jobId);
    }
}
