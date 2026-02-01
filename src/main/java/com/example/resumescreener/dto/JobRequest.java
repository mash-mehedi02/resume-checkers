package com.example.resumescreener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating/updating a job posting.
 * Used in POST /jobs request.
 */
public class JobRequest {

    @NotBlank(message = "Job title is required")
    @Size(max = 255, message = "Job title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Job description is required")
    private String description;

    @NotBlank(message = "Required skills are mandatory")
    private String requiredSkills;

    private String preferredSkills;

    @NotNull(message = "Minimum experience years is required")
    @Min(value = 0, message = "Minimum experience years cannot be negative")
    private Integer minExperienceYears;

    @Size(max = 50, message = "Education level must not exceed 50 characters")
    private String educationLevel;

    @Size(max = 50, message = "Job type must not exceed 50 characters")
    private String jobType;

    // Constructors
    public JobRequest() {
    }

    public JobRequest(String title, String description, String requiredSkills, Integer minExperienceYears) {
        this.title = title;
        this.description = description;
        this.requiredSkills = requiredSkills;
        this.minExperienceYears = minExperienceYears;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public String getPreferredSkills() {
        return preferredSkills;
    }

    public void setPreferredSkills(String preferredSkills) {
        this.preferredSkills = preferredSkills;
    }

    public Integer getMinExperienceYears() {
        return minExperienceYears;
    }

    public void setMinExperienceYears(Integer minExperienceYears) {
        this.minExperienceYears = minExperienceYears;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }
}
