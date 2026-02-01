package com.example.resumescreener.controller;

import com.example.resumescreener.dto.ResumeResponse;
import com.example.resumescreener.service.ResumeParserService;
import com.example.resumescreener.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for resume upload and management.
 * 
 * Endpoints:
 * - POST /resumes/upload - Upload and extract text from resume
 * - GET /resumes/{id} - Get resume by ID
 * - POST /resumes/{id}/parse - Parse resume and extract structured data
 */
@RestController
@RequestMapping("/resumes")
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeParserService resumeParserService;

    @Autowired
    public ResumeController(ResumeService resumeService, ResumeParserService resumeParserService) {
        this.resumeService = resumeService;
        this.resumeParserService = resumeParserService;
    }

    /**
     * Upload a resume file and extract text.
     * 
     * @param file Multipart file (PDF, DOCX, DOC)
     * @param candidateName Optional candidate name (for bias control)
     * @return Uploaded resume details with HTTP 201 status
     */
    @PostMapping("/upload")
    public ResponseEntity<ResumeResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "candidateName", required = false) String candidateName) {
        
        ResumeResponse resumeResponse = resumeService.uploadResume(file, candidateName);
        return ResponseEntity.status(HttpStatus.CREATED).body(resumeResponse);
    }

    /**
     * Get a resume by ID.
     * 
     * @param id Resume ID from path variable
     * @return Resume details with HTTP 200 status
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getResumeById(@PathVariable Long id) {
        ResumeResponse resumeResponse = resumeService.getResumeById(id);
        return ResponseEntity.ok(resumeResponse);
    }

    /**
     * Parse a resume and extract structured data (skills, experience, education, projects).
     * 
     * @param id Resume ID from path variable
     * @return Updated resume details with HTTP 200 status
     */
    @PostMapping("/{id}/parse")
    public ResponseEntity<ResumeResponse> parseResume(@PathVariable Long id) {
        resumeParserService.parseResume(id);
        ResumeResponse resumeResponse = resumeService.getResumeById(id);
        return ResponseEntity.ok(resumeResponse);
    }
}
