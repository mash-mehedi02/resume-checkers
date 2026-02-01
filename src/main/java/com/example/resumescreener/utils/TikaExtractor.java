package com.example.resumescreener.utils;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for extracting text from documents using Apache Tika.
 * Supports PDF, DOCX, DOC, and other document formats.
 */
@Component
public class TikaExtractor {

    private final Tika tika;

    public TikaExtractor() {
        this.tika = new Tika();
    }

    /**
     * Extract text content from a multipart file.
     * 
     * @param file Multipart file (PDF, DOCX, DOC, etc.)
     * @return Extracted text as String
     * @throws IOException if file reading fails
     * @throws TikaException if text extraction fails
     */
    public String extractText(MultipartFile file) throws IOException, TikaException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is null or empty");
        }

        try (InputStream inputStream = file.getInputStream()) {
            String extractedText = tika.parseToString(inputStream);
            return extractedText != null ? extractedText.trim() : "";
        }
    }

    /**
     * Detect MIME type of the file.
     * 
     * @param file Multipart file
     * @return MIME type (e.g., "application/pdf")
     * @throws IOException if file reading fails
     */
    public String detectMimeType(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is null or empty");
        }

        try (InputStream inputStream = file.getInputStream()) {
            return tika.detect(inputStream, file.getOriginalFilename());
        }
    }
}
