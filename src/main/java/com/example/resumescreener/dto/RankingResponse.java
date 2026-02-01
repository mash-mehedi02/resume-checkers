package com.example.resumescreener.dto;

import java.math.BigDecimal;
import java.util.Set;

/**
 * DTO for ranking response.
 * Contains resume details with calculated scores.
 */
public class RankingResponse {

    private Long resumeId;
    private String candidateName;
    private String fileName;
    private BigDecimal skillScore;
    private BigDecimal experienceScore;
    private BigDecimal educationScore;
    private BigDecimal projectScore;
    private BigDecimal finalScore;
    private Set<String> matchedSkills;
    private Set<String> missingSkills;
    private Integer rank;

    // Constructors
    public RankingResponse() {
    }

    public RankingResponse(Long resumeId, BigDecimal finalScore) {
        this.resumeId = resumeId;
        this.finalScore = finalScore;
    }

    // Getters and Setters
    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public BigDecimal getSkillScore() {
        return skillScore;
    }

    public void setSkillScore(BigDecimal skillScore) {
        this.skillScore = skillScore;
    }

    public BigDecimal getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(BigDecimal experienceScore) {
        this.experienceScore = experienceScore;
    }

    public BigDecimal getEducationScore() {
        return educationScore;
    }

    public void setEducationScore(BigDecimal educationScore) {
        this.educationScore = educationScore;
    }

    public BigDecimal getProjectScore() {
        return projectScore;
    }

    public void setProjectScore(BigDecimal projectScore) {
        this.projectScore = projectScore;
    }

    public BigDecimal getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(BigDecimal finalScore) {
        this.finalScore = finalScore;
    }

    public Set<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(Set<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public Set<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(Set<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
