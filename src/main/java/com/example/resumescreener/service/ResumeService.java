package com.example.resumescreener.service;

import com.example.resumescreener.dto.ResumeResponse;
import com.example.resumescreener.exception.InvalidFileException;
import com.example.resumescreener.exception.ParsingException;
import com.example.resumescreener.exception.ResourceNotFoundException;
import com.example.resumescreener.model.CandidateResume;
import com.example.resumescreener.repository.ResumeRepository;
import com.example.resumescreener.utils.TikaExtractor;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Service layer for resume upload and text extraction.
 * Handles file validation, text extraction using Apache Tika, and persistence.
 */
@Service
@Transactional
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final TikaExtractor tikaExtractor;
    private final ResumeParserService resumeParserService;

    @Value("${app.resume.allowed-file-types}")
    private String allowedFileTypes;

    @Value("${app.resume.max-file-size-mb}")
    private long maxFileSizeMB;

    @Autowired
    public ResumeService(ResumeRepository resumeRepository, TikaExtractor tikaExtractor, ResumeParserService resumeParserService) {
        this.resumeRepository = resumeRepository;
        this.tikaExtractor = tikaExtractor;
        this.resumeParserService = resumeParserService;
    }

    /**
     * Upload and process a resume file.
     * Validates file, extracts text using Apache Tika, and saves to database.
     * 
     * @param file Multipart file (PDF, DOCX, DOC)
     * @param candidateName Optional candidate name
     * @return ResumeResponse with uploaded resume details
     * @throws InvalidFileException if file is invalid
     * @throws ParsingException if text extraction fails
     */
    public ResumeResponse uploadResume(MultipartFile file, String candidateName) {
        // Validate file
        validateFile(file);

        // Extract text using Apache Tika
        String extractedText;
        try {
            extractedText = tikaExtractor.extractText(file);
            if (extractedText == null || extractedText.trim().isEmpty()) {
                throw new ParsingException("Failed to extract text from file. File may be empty or corrupted.");
            }
        } catch (IOException e) {
            throw new ParsingException("Error reading file: " + e.getMessage(), e);
        } catch (TikaException e) {
            throw new ParsingException("Text extraction failed: " + e.getMessage(), e);
        }

        // Detect MIME type
        String detectedMimeType;
        try {
            detectedMimeType = tikaExtractor.detectMimeType(file);
        } catch (IOException e) {
            detectedMimeType = file.getContentType();
        }

        // Create and save resume entity
        CandidateResume resume = new CandidateResume();
        resume.setFileName(file.getOriginalFilename());
        resume.setFileType(detectedMimeType != null ? detectedMimeType : file.getContentType());
        resume.setFileSize(file.getSize());
        resume.setExtractedText(extractedText);
        resume.setCandidateName(candidateName);

        CandidateResume savedResume = resumeRepository.save(resume);
        
        // Automatically parse the resume after upload
        try {
            savedResume = resumeParserService.parseResume(savedResume.getId());
        } catch (Exception e) {
            // Log error but don't fail the upload
            // Parsing can be retried later
            System.err.println("Warning: Failed to parse resume after upload: " + e.getMessage());
        }
        
        return convertToResponse(savedResume);
    }

    /**
     * Retrieve all resumes.
     * 
     * @return List of all resumes
     */
    @Transactional(readOnly = true)
    public List<ResumeResponse> getAllResumes() {
        return resumeRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Retrieve a resume by ID.
     * 
     * @param id Resume ID
     * @return ResumeResponse with resume details
     * @throws ResourceNotFoundException if resume not found
     */
    @Transactional(readOnly = true)
    public ResumeResponse getResumeById(Long id) {
        CandidateResume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", id));
        return convertToResponse(resume);
    }

    /**
     * Validate uploaded file.
     * 
     * @param file Multipart file to validate
     * @throws InvalidFileException if validation fails
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is required and cannot be empty");
        }

        // Check file size
        long maxSizeBytes = maxFileSizeMB * 1024 * 1024; // Convert MB to bytes
        if (file.getSize() > maxSizeBytes) {
            throw new InvalidFileException(
                    "File size exceeds maximum allowed size of %d MB".formatted(maxFileSizeMB)
            );
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new InvalidFileException("Cannot determine file type");
        }

        List<String> allowedTypes = Arrays.asList(allowedFileTypes.split(","));
        boolean isAllowed = allowedTypes.stream()
                .anyMatch(allowedType -> contentType.equals(allowedType.trim()));

        if (!isAllowed) {
            throw new InvalidFileException(
                    "File type '%s' is not allowed. Allowed types: %s".formatted(
                            contentType, allowedFileTypes)
            );
        }
    }

    /**
     * Convert CandidateResume entity to ResumeResponse DTO.
     * 
     * @param resume Entity to convert
     * @return ResumeResponse DTO
     */
    private ResumeResponse convertToResponse(CandidateResume resume) {
        ResumeResponse response = new ResumeResponse();
        response.setId(resume.getId());
        response.setCandidateName(resume.getCandidateName());
        response.setFileName(resume.getFileName());
        response.setFileType(resume.getFileType());
        response.setFileSize(resume.getFileSize());
        response.setExtractedText(resume.getExtractedText());
        response.setParsedSkills(resume.getParsedSkills());
        response.setExperienceYears(resume.getExperienceYears());
        response.setEducationLevel(resume.getEducationLevel());
        response.setEducationField(resume.getEducationField());
        response.setProjectsSummary(resume.getProjectsSummary());
        response.setUploadedAt(resume.getUploadedAt());
        response.setParsedAt(resume.getParsedAt());
        return response;
    }
}
