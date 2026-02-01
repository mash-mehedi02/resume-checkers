# Intelligent Resume Screening System

A Java-based REST API application for automated resume screening and candidate ranking.

## 🚀 Features

- **Resume Upload & Text Extraction**: Upload PDF/DOCX resumes and extract text using Apache Tika
- **Resume Parsing**: Automatically extract skills, experience, education, and projects
- **Skill Matching**: Match resume skills against job requirements with synonym support
- **Intelligent Scoring**: Calculate match scores based on skills (50%), experience (30%), education (10%), and projects (10%)
- **Candidate Ranking**: Rank candidates by final score with tie-breaking logic
- **Bias Control**: Skill-first scoring with no name/gender usage

## 🛠️ Tech Stack

- **Language**: Java 17+
- **Framework**: Spring Boot 3.2.0
- **ORM**: Spring Data JPA (Hibernate)
- **Database**: MySQL
- **Text Extraction**: Apache Tika
- **Build Tool**: Maven
- **API Style**: REST

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Postman (for testing)

## 🔧 Setup Instructions

### 1. Clone/Download Project
```bash
cd "D:\Programming\Project\Resume Checkers by JAVA"
```

### 2. Configure Database
Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/resume_screener_db?createDatabaseIfNotExist=true
    username: your_username
    password: your_password
```

### 3. Build Project
```bash
mvn clean install
```

### 4. Run Application
```bash
mvn spring-boot:run
```

Application will start on: `http://localhost:8080/api`

## 📚 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/jobs` | Create job posting |
| GET | `/api/jobs/{id}` | Get job by ID |
| GET | `/api/jobs` | Get all jobs |
| POST | `/api/resumes/upload` | Upload resume |
| GET | `/api/resumes/{id}` | Get resume by ID |
| POST | `/api/resumes/{id}/parse` | Parse resume |
| GET | `/api/ranking/{jobId}` | Get ranked resumes |

## 🧪 Testing

### Using Postman
1. Import `POSTMAN_COLLECTION.json` into Postman
2. Follow the testing guide in `TESTING_GUIDE.md`

### Quick Test
```bash
# 1. Create Job
POST http://localhost:8080/api/jobs
{
  "title": "Senior Java Developer",
  "description": "Backend development role",
  "requiredSkills": "Java,Spring Boot,MySQL",
  "minExperienceYears": 5
}

# 2. Upload Resume
POST http://localhost:8080/api/resumes/upload
Content-Type: multipart/form-data
file: [your-resume.pdf]

# 3. Get Rankings
GET http://localhost:8080/api/ranking/1
```

## 📖 Documentation

- **ARCHITECTURE.md**: System architecture and design
- **DATABASE_DESIGN.md**: Database schema and relationships
- **TESTING_GUIDE.md**: Comprehensive testing instructions
- **BIAS_CONTROL.md**: Bias control measures
- **EXPERIENCE_SCORING.md**: Experience scoring algorithm
- **FINAL_SCORE_CALCULATION.md**: Score calculation formula
- **EXCEPTION_HANDLING.md**: Error handling documentation

## 🏗️ Project Structure

```
src/main/java/com/example/resumescreener/
├── controller/      # REST API endpoints
├── service/         # Business logic
├── repository/      # Database access
├── model/          # JPA entities
├── dto/            # Data transfer objects
├── utils/          # Utility classes
├── exception/      # Exception handling
└── config/          # Configuration
```

## 🎯 Key Features Explained

### Resume Parsing
- Extracts skills using keyword matching
- Calculates experience years using regex patterns
- Identifies education level and field
- Extracts project information

### Skill Matching
- Exact match scoring
- Partial match using synonym map (e.g., Spring ≈ Spring Boot)
- Returns numeric score (0-100)

### Scoring Algorithm
```
Final Score = (Skill Score × 50%) + 
              (Experience Score × 30%) + 
              (Education Score × 10%) + 
              (Project Score × 10%)
```

### Ranking
- Sorts by final score (descending)
- Tie-breaking: Skill score → Experience → Matched skills → Resume ID
- Assigns ranks (1, 2, 3, ...)

## 🔒 Bias Control

- **Skill-First Scoring**: Skills weighted at 50% (highest)
- **No Name/Gender Usage**: Names not used in scoring/ranking
- **Objective Metrics**: All scoring based on measurable criteria
- **Configurable Weights**: Adjustable via `application.yml`

## 🐛 Troubleshooting

### Database Connection Error
- Verify MySQL is running
- Check credentials in `application.yml`
- Ensure database exists or auto-create is enabled

### Port Already in Use
- Change port in `application.yml`:
  ```yaml
  server:
    port: 8081
  ```

### File Upload Fails
- Ensure file is PDF, DOCX, or DOC
- Check file size is under 10MB
- Verify file is not corrupted

## 📝 License

This project is for educational and interview purposes.

## 👨‍💻 Author

Resume Screening System - Java Backend Implementation

## 📞 Support

For issues or questions, refer to:
- `TESTING_GUIDE.md` for testing help
- `NEXT_STEPS.md` for setup instructions
- Application logs for error details

---

**Built with ❤️ using Java & Spring Boot**
