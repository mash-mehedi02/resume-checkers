# Database Design - Resume Screening System

## Database Schema Overview

The system uses **MySQL** database with three main tables:
1. `job_post` - Stores job postings with requirements
2. `candidate_resume` - Stores uploaded resumes and extracted data
3. `resume_score` - Stores calculated scores for resume-job pairs

---

## Table 1: `job_post`

**Purpose**: Store job postings with their requirements and criteria.

### Fields:

| Field Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique job identifier |
| `title` | VARCHAR(255) | NOT NULL | Job title (e.g., "Senior Java Developer") |
| `description` | TEXT | NOT NULL | Full job description |
| `required_skills` | TEXT | NOT NULL | Comma-separated required skills (e.g., "Java,Spring Boot,MySQL") |
| `preferred_skills` | TEXT | NULL | Optional preferred skills |
| `min_experience_years` | INT | NOT NULL, DEFAULT 0 | Minimum years of experience required |
| `education_level` | VARCHAR(50) | NULL | Required education (e.g., "Bachelor", "Master") |
| `job_type` | VARCHAR(50) | NULL | Job type (e.g., "Backend", "Full Stack") |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Job creation timestamp |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

### Relationships:
- One-to-Many with `resume_score` (one job can have many scores)

---

## Table 2: `candidate_resume`

**Purpose**: Store uploaded resume files and extracted/parsed data.

### Fields:

| Field Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique resume identifier |
| `candidate_name` | VARCHAR(255) | NULL | Candidate name (optional, for bias control) |
| `file_name` | VARCHAR(255) | NOT NULL | Original uploaded file name |
| `file_type` | VARCHAR(50) | NOT NULL | File MIME type (e.g., "application/pdf") |
| `file_size` | BIGINT | NOT NULL | File size in bytes |
| `extracted_text` | LONGTEXT | NULL | Raw text extracted by Apache Tika |
| `parsed_skills` | TEXT | NULL | Comma-separated extracted skills |
| `experience_years` | INT | NULL | Extracted years of experience |
| `education_level` | VARCHAR(50) | NULL | Extracted education level |
| `education_field` | VARCHAR(255) | NULL | Extracted education field |
| `projects_summary` | TEXT | NULL | Extracted project information |
| `uploaded_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Resume upload timestamp |
| `parsed_at` | TIMESTAMP | NULL | Timestamp when parsing completed |

### Relationships:
- One-to-Many with `resume_score` (one resume can be scored against many jobs)

### Notes:
- `candidate_name` is optional to support bias-free screening
- `extracted_text` stores the full raw text for re-parsing if needed
- Parsed fields are extracted using Java regex and keyword matching

---

## Table 3: `resume_score`

**Purpose**: Store calculated scores for each resume-job pair.

### Fields:

| Field Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique score identifier |
| `job_id` | BIGINT | NOT NULL, FOREIGN KEY | Reference to job_post.id |
| `resume_id` | BIGINT | NOT NULL, FOREIGN KEY | Reference to candidate_resume.id |
| `skill_score` | DECIMAL(5,2) | NOT NULL, DEFAULT 0.00 | Skill match score (0-100) |
| `experience_score` | DECIMAL(5,2) | NOT NULL, DEFAULT 0.00 | Experience relevance score (0-100) |
| `education_score` | DECIMAL(5,2) | NOT NULL, DEFAULT 0.00 | Education match score (0-100) |
| `project_score` | DECIMAL(5,2) | NOT NULL, DEFAULT 0.00 | Project relevance score (0-100) |
| `final_score` | DECIMAL(5,2) | NOT NULL, DEFAULT 0.00 | Weighted final score (0-100) |
| `calculated_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Score calculation timestamp |

### Relationships:
- Many-to-One with `job_post` (many scores belong to one job)
- Many-to-One with `candidate_resume` (many scores belong to one resume)

### Unique Constraint:
- `UNIQUE(job_id, resume_id)` - One score per resume-job pair

### Indexes:
- `INDEX idx_job_id` on `job_id` (for fast job-based queries)
- `INDEX idx_resume_id` on `resume_id` (for fast resume-based queries)
- `INDEX idx_final_score` on `final_score` (for ranking queries)

---

## Entity Relationship Diagram (ERD)

```
┌──────────────┐
│  job_post    │
├──────────────┤
│ id (PK)      │
│ title        │
│ description  │
│ required_    │
│   skills     │
│ min_exp_yrs  │
│ ...          │
└──────┬───────┘
       │
       │ 1
       │
       │ N
┌──────▼──────────┐
│  resume_score   │
├─────────────────┤
│ id (PK)         │
│ job_id (FK)     │◄──┐
│ resume_id (FK)  │   │
│ skill_score     │   │
│ experience_     │   │
│   score         │   │
│ education_score │   │
│ project_score   │   │
│ final_score     │   │
└──────┬──────────┘   │
       │              │
       │ N            │
       │              │
       │ 1            │
┌──────▼──────────┐   │
│candidate_resume │   │
├─────────────────┤   │
│ id (PK)         │───┘
│ candidate_name  │
│ file_name       │
│ extracted_text  │
│ parsed_skills   │
│ experience_yrs  │
│ ...
└─────────────────┘
```

---

## Database Design Decisions

### 1. **Separate Score Table**
- Allows storing scores for multiple jobs per resume
- Enables historical tracking of score calculations
- Supports re-calculation without losing history

### 2. **TEXT Fields for Skills**
- Skills stored as comma-separated strings
- Simple and flexible for Java parsing
- Can be normalized later if needed

### 3. **DECIMAL for Scores**
- `DECIMAL(5,2)` allows scores from 0.00 to 999.99
- Precise enough for percentage calculations
- Better than FLOAT for financial/score calculations

### 4. **Timestamps**
- Track creation and update times
- Useful for auditing and debugging
- Support for future features (e.g., resume updates)

### 5. **Optional Candidate Name**
- Supports bias-free screening
- Name extraction is optional
- Can be used for display purposes only

---

## JPA Entity Classes

The following section shows the JPA entity skeletons with annotations.
See the `model/` package for full implementation.
