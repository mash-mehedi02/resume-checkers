# Testing Guide - Resume Screening System

## Overview

This guide provides comprehensive testing instructions for the Resume Screening System using Postman or similar API testing tools. It includes request examples, expected responses, and edge cases to test.

---

## Prerequisites

1. **Start the Application**
   ```bash
   mvn spring-boot:run
   ```
   Application runs on: `http://localhost:8080/api`

2. **Database Setup**
   - MySQL should be running
   - Database `resume_screener_db` will be auto-created
   - Tables will be auto-created on first run

3. **Postman Collection**
   - Import the requests below into Postman
   - Or use curl commands provided

---

## API Endpoints Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/jobs` | Create job posting |
| GET | `/jobs/{id}` | Get job by ID |
| GET | `/jobs` | Get all jobs |
| POST | `/resumes/upload` | Upload resume |
| GET | `/resumes/{id}` | Get resume by ID |
| POST | `/resumes/{id}/parse` | Parse resume |
| GET | `/ranking/{jobId}` | Get ranked resumes |

---

## 1. Job Management APIs

### 1.1 Create Job Posting

**Request:**
```
POST http://localhost:8080/api/jobs
Content-Type: application/json

{
  "title": "Senior Java Developer",
  "description": "We are looking for an experienced Java developer with Spring Boot expertise to join our backend team.",
  "requiredSkills": "Java,Spring Boot,MySQL,Hibernate,REST API",
  "preferredSkills": "Docker,Kubernetes,Microservices",
  "minExperienceYears": 5,
  "educationLevel": "Bachelor",
  "jobType": "Backend"
}
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "title": "Senior Java Developer",
  "description": "We are looking for an experienced Java developer...",
  "requiredSkills": "Java,Spring Boot,MySQL,Hibernate,REST API",
  "preferredSkills": "Docker,Kubernetes,Microservices",
  "minExperienceYears": 5,
  "educationLevel": "Bachelor",
  "jobType": "Backend",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Edge Cases to Test:**
1. **Empty Title** - Should return 400 Bad Request
   ```json
   {
     "title": "",
     "description": "Test",
     "requiredSkills": "Java",
     "minExperienceYears": 5
   }
   ```

2. **Negative Experience Years** - Should return 400 Bad Request
   ```json
   {
     "title": "Java Developer",
     "description": "Test",
     "requiredSkills": "Java",
     "minExperienceYears": -1
   }
   ```

3. **Missing Required Fields** - Should return 400 Bad Request
   ```json
   {
     "title": "Java Developer"
   }
   ```

4. **Very Long Title** - Should return 400 Bad Request (exceeds 255 chars)

### 1.2 Get Job by ID

**Request:**
```
GET http://localhost:8080/api/jobs/1
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "title": "Senior Java Developer",
  ...
}
```

**Edge Cases to Test:**
1. **Non-existent ID** - Should return 404 Not Found
   ```
   GET http://localhost:8080/api/jobs/99999
   ```

2. **Invalid ID Format** - Should return 400 Bad Request
   ```
   GET http://localhost:8080/api/jobs/abc
   ```

3. **Negative ID** - Should return 400 Bad Request
   ```
   GET http://localhost:8080/api/jobs/-1
   ```

### 1.3 Get All Jobs

**Request:**
```
GET http://localhost:8080/api/jobs
```

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "title": "Senior Java Developer",
    ...
  },
  {
    "id": 2,
    "title": "Frontend Developer",
    ...
  }
]
```

**Edge Cases to Test:**
1. **Empty Database** - Should return empty array `[]`

---

## 2. Resume Upload & Text Extraction

### 2.1 Upload Resume

**Request:**
```
POST http://localhost:8080/api/resumes/upload
Content-Type: multipart/form-data

file: [Select PDF/DOCX file]
candidateName: "John Doe" (optional)
```

**Using curl:**
```bash
curl -X POST http://localhost:8080/api/resumes/upload \
  -F "file=@/path/to/resume.pdf" \
  -F "candidateName=John Doe"
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "candidateName": "John Doe",
  "fileName": "resume.pdf",
  "fileType": "application/pdf",
  "fileSize": 245678,
  "extractedText": "John Doe\nSenior Java Developer...",
  "parsedSkills": "java,spring boot,mysql",
  "experienceYears": 5,
  "educationLevel": "Bachelor",
  "educationField": "Computer Science",
  "projectsSummary": "E-commerce platform...",
  "uploadedAt": "2024-01-15T10:35:00",
  "parsedAt": "2024-01-15T10:35:00"
}
```

**Edge Cases to Test:**
1. **Empty File** - Should return 400 Bad Request
   ```
   file: [empty file]
   ```

2. **Invalid File Type** - Should return 400 Bad Request
   ```
   file: [image.png or text.txt]
   ```

3. **File Too Large** - Should return 413 Payload Too Large
   ```
   file: [file > 10MB]
   ```

4. **Corrupted PDF** - Should return 422 Unprocessable Entity
   ```
   file: [corrupted PDF file]
   ```

5. **No File** - Should return 400 Bad Request
   ```
   POST without file parameter
   ```

6. **Valid PDF with No Text** - Should return 422 Unprocessable Entity
   ```
   file: [PDF with only images, no extractable text]
   ```

### 2.2 Get Resume by ID

**Request:**
```
GET http://localhost:8080/api/resumes/1
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "candidateName": "John Doe",
  "fileName": "resume.pdf",
  ...
}
```

**Edge Cases to Test:**
1. **Non-existent ID** - Should return 404 Not Found
2. **Invalid ID** - Should return 400 Bad Request

### 2.3 Parse Resume

**Request:**
```
POST http://localhost:8080/api/resumes/1/parse
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "parsedSkills": "java,spring boot,mysql,hibernate",
  "experienceYears": 5,
  "educationLevel": "Bachelor",
  "educationField": "Computer Science",
  "projectsSummary": "...",
  "parsedAt": "2024-01-15T10:40:00"
}
```

**Edge Cases to Test:**
1. **Resume Not Found** - Should return 404 Not Found
2. **Resume with No Extracted Text** - Should return 422 Unprocessable Entity
3. **Resume Already Parsed** - Should re-parse and update timestamp

---

## 3. Ranking API

### 3.1 Get Ranked Resumes

**Request:**
```
GET http://localhost:8080/api/ranking/1
```

**Expected Response (200 OK):**
```json
[
  {
    "resumeId": 3,
    "candidateName": "John Doe",
    "fileName": "john_resume.pdf",
    "skillScore": 100.00,
    "experienceScore": 84.00,
    "educationScore": 100.00,
    "projectScore": 80.00,
    "finalScore": 93.20,
    "matchedSkills": ["Java", "Spring Boot", "MySQL", "Hibernate"],
    "missingSkills": [],
    "rank": 1
  },
  {
    "resumeId": 1,
    "candidateName": "Jane Smith",
    "fileName": "jane_resume.pdf",
    "skillScore": 75.00,
    "experienceScore": 48.00,
    "educationScore": 100.00,
    "projectScore": 60.00,
    "finalScore": 67.90,
    "matchedSkills": ["Java", "Spring Boot", "MySQL"],
    "missingSkills": ["Hibernate"],
    "rank": 2
  }
]
```

**Edge Cases to Test:**
1. **Job Not Found** - Should return 404 Not Found
   ```
   GET http://localhost:8080/api/ranking/99999
   ```

2. **No Resumes Uploaded** - Should return empty array `[]`

3. **Job with No Requirements** - Should return all resumes with high scores

4. **Tied Scores** - Should apply tie-breaking logic correctly
   - Create multiple resumes with same final score
   - Verify tie-breaking by skill score, then experience, etc.

5. **Resumes Not Parsed** - Should still calculate scores (may be less accurate)

---

## 4. Complete Testing Workflow

### Step-by-Step Test Scenario

1. **Create Job Posting**
   ```
   POST /api/jobs
   Save job ID (e.g., jobId = 1)
   ```

2. **Upload Multiple Resumes**
   ```
   POST /api/resumes/upload (Resume 1 - Strong candidate)
   POST /api/resumes/upload (Resume 2 - Average candidate)
   POST /api/resumes/upload (Resume 3 - Weak candidate)
   Save resume IDs
   ```

3. **Verify Parsing**
   ```
   GET /api/resumes/{id} (for each resume)
   Check parsedSkills, experienceYears, etc.
   ```

4. **Get Rankings**
   ```
   GET /api/ranking/1
   Verify:
   - Resumes sorted by final score (descending)
   - Ranks assigned correctly
   - Scores calculated correctly
   ```

5. **Verify Score Calculation**
   ```
   Check final score = (skill × 0.50) + (exp × 0.30) + (edu × 0.10) + (proj × 0.10)
   ```

---

## 5. Edge Cases Summary

### Validation Edge Cases
- ✅ Empty/null required fields
- ✅ Negative numbers (experience years)
- ✅ Exceeding max length (title, description)
- ✅ Invalid data types
- ✅ Missing request body

### File Upload Edge Cases
- ✅ Empty file
- ✅ Invalid file type (not PDF/DOCX/DOC)
- ✅ File too large (>10MB)
- ✅ Corrupted file
- ✅ File with no extractable text
- ✅ Multiple file uploads (should handle one at a time)

### Scoring Edge Cases
- ✅ Resume with no skills
- ✅ Resume with all required skills
- ✅ Resume with partial skills
- ✅ Resume with no experience data
- ✅ Resume with over-qualified experience
- ✅ Resume with under-qualified experience
- ✅ Resume with no education data
- ✅ Resume with no projects
- ✅ Job with no requirements (should score all high)

### Ranking Edge Cases
- ✅ Empty resume list
- ✅ Single resume
- ✅ All resumes with same score (tie-breaking)
- ✅ Job with no requirements
- ✅ Resumes not parsed yet
- ✅ Very large number of resumes (performance test)

### Data Consistency Edge Cases
- ✅ Delete job after scores calculated
- ✅ Delete resume after scores calculated
- ✅ Update job requirements (scores should be recalculated)
- ✅ Re-parse resume (should update scores)

---

## 6. Postman Collection Structure

### Collection: Resume Screener API

#### Folder: Jobs
- Create Job
- Get Job by ID
- Get All Jobs

#### Folder: Resumes
- Upload Resume
- Get Resume by ID
- Parse Resume

#### Folder: Ranking
- Get Ranked Resumes

#### Folder: Error Cases
- Invalid Job Creation
- Invalid File Upload
- Non-existent Resources

---

## 7. Sample Test Data

### Sample Job Postings

**Job 1: Senior Java Developer**
```json
{
  "title": "Senior Java Developer",
  "description": "Backend development with Spring Boot",
  "requiredSkills": "Java,Spring Boot,MySQL,Hibernate",
  "minExperienceYears": 5,
  "educationLevel": "Bachelor",
  "jobType": "Backend"
}
```

**Job 2: Frontend Developer**
```json
{
  "title": "Frontend Developer",
  "description": "React and TypeScript development",
  "requiredSkills": "React,TypeScript,JavaScript,HTML,CSS",
  "minExperienceYears": 3,
  "educationLevel": "Bachelor",
  "jobType": "Frontend"
}
```

### Sample Resume Scenarios

**Resume 1: Perfect Match**
- Skills: Java, Spring Boot, MySQL, Hibernate
- Experience: 5 years
- Education: Bachelor in Computer Science
- Projects: E-commerce platform with Spring Boot

**Resume 2: Partial Match**
- Skills: Java, Spring Boot, MySQL (missing Hibernate)
- Experience: 3 years (under-qualified)
- Education: Bachelor in Computer Science
- Projects: Basic CRUD application

**Resume 3: Weak Match**
- Skills: Java, MySQL (missing Spring Boot, Hibernate)
- Experience: 1 year (severely under-qualified)
- Education: Diploma
- Projects: None

---

## 8. Performance Testing

### Test Scenarios

1. **Upload 100 Resumes**
   - Measure upload time
   - Measure parsing time
   - Check database performance

2. **Rank 100 Resumes**
   - Measure ranking calculation time
   - Check response time

3. **Concurrent Requests**
   - Multiple simultaneous uploads
   - Multiple simultaneous ranking requests

---

## 9. Error Response Testing

### Expected Error Formats

**400 Bad Request:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "title: Job title is required",
  "path": "/api/jobs"
}
```

**404 Not Found:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Resource Not Found",
  "message": "Job not found with id: 999",
  "path": "/api/jobs/999"
}
```

**413 Payload Too Large:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 413,
  "error": "File Too Large",
  "message": "File size exceeds maximum allowed size. Maximum size: 10 MB",
  "path": "/api/resumes/upload"
}
```

---

## 10. Testing Checklist

### Functional Testing
- [ ] Create job posting with all fields
- [ ] Create job posting with minimal fields
- [ ] Get job by ID (existing)
- [ ] Get job by ID (non-existent)
- [ ] Get all jobs
- [ ] Upload PDF resume
- [ ] Upload DOCX resume
- [ ] Upload invalid file type
- [ ] Upload file too large
- [ ] Get resume by ID
- [ ] Parse resume
- [ ] Get ranked resumes
- [ ] Verify score calculation
- [ ] Verify tie-breaking

### Validation Testing
- [ ] Empty title validation
- [ ] Negative experience validation
- [ ] Missing required fields
- [ ] Invalid file type
- [ ] File size limit

### Edge Case Testing
- [ ] Empty database scenarios
- [ ] Tied scores
- [ ] Missing data (no skills, no experience)
- [ ] Over-qualified candidates
- [ ] Under-qualified candidates

### Integration Testing
- [ ] Complete workflow (create job → upload resumes → rank)
- [ ] Multiple jobs with multiple resumes
- [ ] Score recalculation after job update

---

## 11. Troubleshooting

### Common Issues

1. **Database Connection Error**
   - Check MySQL is running
   - Verify credentials in `application.yml`
   - Check database exists

2. **File Upload Fails**
   - Check file size (<10MB)
   - Verify file type (PDF/DOCX/DOC)
   - Check file is not corrupted

3. **Parsing Returns Empty**
   - Verify file has extractable text
   - Check Apache Tika is working
   - Try different file format

4. **Scores Not Calculating**
   - Ensure resume is parsed first
   - Check job requirements are set
   - Verify resume has extracted text

---

## 12. Next Steps

After manual testing:
1. Review all test results
2. Document any issues found
3. Verify edge cases are handled
4. Check performance metrics
5. Validate score calculations manually
6. Test with real resume files

---

## Conclusion

This testing guide provides comprehensive coverage of all API endpoints, edge cases, and error scenarios. Use this guide to ensure the Resume Screening System works correctly before deployment.
