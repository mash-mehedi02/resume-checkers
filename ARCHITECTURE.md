# Intelligent Resume Screening System - Architecture

## System Overview

This is a Java-based REST API application that automates resume screening by:
1. Accepting job postings with required skills and qualifications
2. Processing uploaded resume files (PDF, DOCX, etc.)
3. Extracting and analyzing resume content
4. Scoring resumes against job requirements
5. Ranking candidates by match quality

---

## Component Architecture

### 1. **Controller Layer** (`controller/`)
**Purpose**: HTTP request/response handling

- **JobController**: Manages job posting CRUD operations
  - `POST /jobs` - Create new job posting
  - `GET /jobs/{id}` - Retrieve job details
  - `GET /jobs` - List all jobs

- **ResumeController**: Handles resume uploads and processing
  - `POST /resumes/upload` - Upload resume file
  - `GET /resumes/{id}` - Get resume details

- **RankingController**: Provides ranking and scoring results
  - `GET /ranking/{jobId}` - Get ranked list of candidates for a job

### 2. **Service Layer** (`service/`)
**Purpose**: Business logic implementation

- **JobService**: Job posting management logic
- **ResumeService**: Resume processing orchestration
- **ResumeParserService**: Extracts structured data from resume text
- **ScoringService**: Calculates match scores between resume and job
- **RankingService**: Sorts and ranks candidates

### 3. **Repository Layer** (`repository/`)
**Purpose**: Database access (Spring Data JPA)

- **JobRepository**: Job entity persistence
- **ResumeRepository**: Resume entity persistence
- **ResumeScoreRepository**: Score entity persistence

### 4. **Model Layer** (`model/`)
**Purpose**: JPA entities representing database tables

- **JobPost**: Job posting with requirements
- **CandidateResume**: Uploaded resume with extracted text
- **ResumeScore**: Calculated scores for resume-job pairs

### 5. **DTO Layer** (`dto/`)
**Purpose**: Data transfer objects for API communication

- Request DTOs: JobRequest, ResumeUploadRequest
- Response DTOs: JobResponse, ResumeResponse, RankingResponse

### 6. **Utils Layer** (`utils/`)
**Purpose**: Reusable utility classes

- **TikaExtractor**: Apache Tika wrapper for text extraction
- **SkillMatcher**: Skill matching algorithms
- **ExperienceCalculator**: Experience years extraction
- **EducationExtractor**: Education level detection

### 7. **Config Layer** (`config/`)
**Purpose**: Spring configuration

- Database configuration
- File upload configuration
- CORS configuration (if needed)

### 8. **Exception Layer** (`exception/`)
**Purpose**: Custom exceptions and global error handling

- Custom exceptions (InvalidFileException, ParsingException, etc.)
- Global exception handler with @ControllerAdvice

---

## System Flow: Resume Upload & Processing

### Flow 1: Job Posting Creation
```
1. Client sends POST /jobs with job details (title, description, required skills, experience years)
2. JobController receives request
3. JobService validates and processes
4. JobRepository saves to database
5. JobResponse returned to client
```

### Flow 2: Resume Upload & Text Extraction
```
1. Client sends POST /resumes/upload with multipart file (PDF/DOCX)
2. ResumeController receives file
3. ResumeService validates file type and size
4. TikaExtractor (Apache Tika) extracts raw text from file
5. Extracted text stored in CandidateResume entity
6. ResumeResponse with resume ID returned
```

### Flow 3: Resume Parsing (Structured Data Extraction)
```
1. ResumeParserService processes extracted text
2. Uses Java Regex patterns to identify:
   - Skills: Keywords like "Java", "Spring Boot", "MySQL"
   - Experience: Years mentioned (e.g., "5 years", "3+ years")
   - Education: Degree keywords ("Bachelor", "Master", "PhD")
   - Projects: Project-related sections
3. Structured data stored/updated in CandidateResume
```

### Flow 4: Scoring & Ranking
```
1. Client requests GET /ranking/{jobId}
2. RankingController receives request
3. RankingService fetches:
   - Job requirements (JobPost)
   - All resumes (CandidateResume)
4. For each resume:
   a. ScoringService calculates:
      - Skill Score (50%): Matches required skills
      - Experience Score (30%): Years of experience match
      - Education Score (10%): Education level match
      - Project Score (10%): Relevant project experience
   b. Final Score = weighted sum of all scores
5. Resumes sorted by Final Score (descending)
6. RankingResponse with ranked list returned
```

---

## Scoring & Ranking Logic

### Skill Matching Engine
- **Exact Match**: Required skill found in resume → Full points
- **Partial Match**: Synonym match (e.g., "Spring" matches "Spring Boot") → Partial points
- **Missing Skill**: Required skill not found → 0 points
- **Formula**: (Matched Skills / Total Required Skills) × 100

### Experience Relevance Scoring
- **Role Match**: Backend role matches backend experience → Higher score
- **Years Match**: Required years vs actual years → Proportional scoring
- **Formula**: Based on role relevance + years alignment

### Education Scoring
- **Degree Level**: Bachelor, Master, PhD → Different weights
- **Relevance**: Degree field relevance to job → Bonus points

### Project Scoring
- **Relevance**: Projects matching job domain → Points
- **Complexity**: Number and depth of projects → Additional points

### Final Score Calculation
```
Final Score = (Skill Score × 0.50) + 
              (Experience Score × 0.30) + 
              (Education Score × 0.10) + 
              (Project Score × 0.10)
```

### Ranking Logic
1. Sort by Final Score (descending)
2. Tie-breaking: If scores equal, prioritize:
   - Higher skill match count
   - More experience years
   - Higher education level

---

## Data Flow Diagram

```
┌─────────┐
│ Client  │
└────┬────┘
     │
     │ HTTP Request
     ▼
┌─────────────────┐
│  Controller     │ ← REST API Endpoints
└────┬────────────┘
     │
     │ Business Logic
     ▼
┌─────────────────┐
│   Service       │ ← Core Processing Logic
└────┬────────────┘
     │
     │ Data Access
     ▼
┌─────────────────┐
│  Repository     │ ← Spring Data JPA
└────┬────────────┘
     │
     │ SQL Queries
     ▼
┌─────────────────┐
│    MySQL DB     │ ← Persistent Storage
└─────────────────┘
```

---

## Key Design Decisions

### 1. **Pure Java Implementation**
- Apache Tika for text extraction (Java library)
- Java Regex for parsing
- No external ML services or Python scripts
- All logic self-contained in Java

### 2. **Layered Architecture**
- Clear separation of concerns
- Easy to test and maintain
- Follows Spring Boot best practices

### 3. **Stateless API**
- Each request is independent
- No session management required
- Scalable and RESTful

### 4. **Bias Control**
- No name/gender extraction
- Skill-first scoring approach
- Configurable weights for fairness

---

## Technology Stack Summary

- **Language**: Java 17+
- **Framework**: Spring Boot (Web, Data JPA, Security optional)
- **Database**: MySQL
- **Text Extraction**: Apache Tika
- **Build Tool**: Maven
- **API Style**: REST JSON

---

## Next Steps

After understanding this architecture, we will:
1. Design database schema (Step 2)
2. Set up Spring Boot project (Step 3)
3. Implement each component step by step (Steps 4-13)
