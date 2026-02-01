package com.example.resumescreener.dto;

import org.springframework.web.multipart.MultipartFile;

/**
 * DTO for resume upload request.
 * Note: This is used for documentation purposes.
 * Actual file upload is handled via @RequestParam in controller.
 */
public class ResumeUploadRequest {
    
    private MultipartFile file;
    private String candidateName; // Optional, for bias control

    public ResumeUploadRequest() {
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }
}
