# Global Exception Handling - Documentation

## Overview

The application uses a centralized exception handling mechanism via `@ControllerAdvice`. All exceptions are caught and converted to standardized error responses.

---

## Exception Handler

**Class**: `GlobalExceptionHandler.java`  
**Annotation**: `@ControllerAdvice`

This class handles all exceptions thrown by controllers and services, providing consistent error responses across the entire API.

---

## Standard Error Response

All error responses follow this structure:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Resource Not Found",
  "message": "Job not found with id: 123",
  "path": "/api/jobs/123"
}
```

### Fields:
- **timestamp**: When the error occurred (ISO 8601 format)
- **status**: HTTP status code
- **error**: Error type/category
- **message**: Detailed error message
- **path**: API endpoint where error occurred

---

## Handled Exceptions

### 1. ResourceNotFoundException
- **HTTP Status**: 404 Not Found
- **Error**: "Resource Not Found"
- **When**: Requested resource (Job, Resume) doesn't exist
- **Example**:
  ```json
  {
    "timestamp": "2024-01-15T10:30:00",
    "status": 404,
    "error": "Resource Not Found",
    "message": "Job not found with id: 123",
    "path": "/api/jobs/123"
  }
  ```

### 2. InvalidFileException
- **HTTP Status**: 400 Bad Request
- **Error**: "Invalid File"
- **When**: File upload validation fails (wrong type, empty file, etc.)
- **Example**:
  ```json
  {
    "timestamp": "2024-01-15T10:30:00",
    "status": 400,
    "error": "Invalid File",
    "message": "File type 'image/png' is not allowed. Allowed types: application/pdf,application/msword",
    "path": "/api/resumes/upload"
  }
  ```

### 3. ParsingException
- **HTTP Status**: 422 Unprocessable Entity
- **Error**: "Parsing Error"
- **When**: Text extraction or parsing fails
- **Example**:
  ```json
  {
    "timestamp": "2024-01-15T10:30:00",
    "status": 422,
    "error": "Parsing Error",
    "message": "Text extraction failed: File may be corrupted",
    "path": "/api/resumes/upload"
  }
  ```

### 4. MethodArgumentNotValidException
- **HTTP Status**: 400 Bad Request
- **Error**: "Validation Failed"
- **When**: Request body validation fails (@Valid annotation)
- **Example**:
  ```json
  {
    "timestamp": "2024-01-15T10:30:00",
    "status": 400,
    "error": "Validation Failed",
    "message": "title: Job title is required, minExperienceYears: Minimum experience years cannot be negative",
    "path": "/api/jobs"
  }
  ```

### 5. ConstraintViolationException
- **HTTP Status**: 400 Bad Request
- **Error**: "Constraint Violation"
- **When**: Path variable or query parameter validation fails
- **Example**:
  ```json
  {
    "timestamp": "2024-01-15T10:30:00",
    "status": 400,
    "error": "Constraint Violation",
    "message": "Job ID must be positive",
    "path": "/api/jobs/-1"
  }
  ```

### 6. MaxUploadSizeExceededException
- **HTTP Status**: 413 Payload Too Large
- **Error**: "File Too Large"
- **When**: Uploaded file exceeds maximum size limit
- **Example**:
  ```json
  {
    "timestamp": "2024-01-15T10:30:00",
    "status": 413,
    "error": "File Too Large",
    "message": "File size exceeds maximum allowed size. Maximum size: 10 MB",
    "path": "/api/resumes/upload"
  }
  ```

### 7. IllegalArgumentException
- **HTTP Status**: 400 Bad Request
- **Error**: "Invalid Argument"
- **When**: Invalid method argument provided
- **Example**:
  ```json
  {
    "timestamp": "2024-01-15T10:30:00",
    "status": 400,
    "error": "Invalid Argument",
    "message": "File is null or empty",
    "path": "/api/resumes/upload"
  }
  ```

### 8. IllegalStateException
- **HTTP Status**: 422 Unprocessable Entity
- **Error**: "Invalid State"
- **When**: Operation cannot be performed in current state
- **Example**:
  ```json
  {
    "timestamp": "2024-01-15T10:30:00",
    "status": 422,
    "error": "Invalid State",
    "message": "Resume text is empty. Please upload resume first.",
    "path": "/api/resumes/1/parse"
  }
  ```

### 9. Generic Exception (All Others)
- **HTTP Status**: 500 Internal Server Error
- **Error**: "Internal Server Error"
- **When**: Any unhandled exception occurs
- **Example**:
  ```json
  {
    "timestamp": "2024-01-15T10:30:00",
    "status": 500,
    "error": "Internal Server Error",
    "message": "An unexpected error occurred. Please try again later.",
    "path": "/api/jobs"
  }
  ```

---

## HTTP Status Codes Used

| Status Code | Meaning | Used For |
|------------|---------|----------|
| 400 | Bad Request | Validation errors, invalid arguments |
| 404 | Not Found | Resource not found |
| 413 | Payload Too Large | File size exceeded |
| 422 | Unprocessable Entity | Parsing errors, invalid state |
| 500 | Internal Server Error | Unexpected errors |

---

## Best Practices

### 1. **Use Specific Exceptions**
- Prefer custom exceptions (ResourceNotFoundException, InvalidFileException) over generic ones
- Provides clearer error messages and appropriate HTTP status codes

### 2. **Meaningful Error Messages**
- Error messages should be clear and actionable
- Include relevant details (e.g., resource ID, file type)

### 3. **Consistent Error Format**
- All errors use the same ErrorResponse structure
- Makes it easier for API consumers to handle errors

### 4. **Security Considerations**
- Generic exception handler doesn't expose internal details
- Stack traces are logged server-side, not sent to client

---

## Example Usage

### Throwing Custom Exceptions

```java
// In Service layer
public JobResponse getJobById(Long id) {
    JobPost job = jobRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job", id));
    return convertToResponse(job);
}
```

### Client Receives:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Resource Not Found",
  "message": "Job not found with id: 123",
  "path": "/api/jobs/123"
}
```

---

## Testing Exception Handling

### Using Postman/curl:

1. **Test 404**:
   ```
   GET /api/jobs/99999
   ```

2. **Test Validation**:
   ```
   POST /api/jobs
   {
     "title": "",  // Empty title
     "minExperienceYears": -1  // Negative value
   }
   ```

3. **Test File Upload**:
   ```
   POST /api/resumes/upload
   Content-Type: multipart/form-data
   file: [invalid file type]
   ```

---

## Future Enhancements

Potential improvements:
1. Add logging framework (SLF4J/Logback) for proper error logging
2. Add error codes for programmatic error handling
3. Add request ID for error tracking
4. Add stack trace in development mode only
5. Add internationalization (i18n) for error messages
