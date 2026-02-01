# Next Steps - Project Setup & Running Guide

## এখন যা করতে হবে (What to Do Now)

আপনার Resume Screening System তৈরি হয়ে গেছে! এখন এটিকে run করতে এবং test করতে হবে।

---

## Step 1: Database Setup (MySQL)

### 1.1 MySQL Install & Start
```bash
# MySQL install করুন (যদি না থাকে)
# Windows: MySQL installer download করুন
# Linux: sudo apt-get install mysql-server
# Mac: brew install mysql

# MySQL start করুন
# Windows: Services → MySQL start
# Linux/Mac: sudo systemctl start mysql
```

### 1.2 Database Configuration
`src/main/resources/application.yml` file এ credentials check করুন:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/resume_screener_db?createDatabaseIfNotExist=true
    username: root          # আপনার MySQL username
    password: root           # আপনার MySQL password (change করুন)
```

**Important:** আপনার MySQL username/password দিয়ে update করুন!

---

## Step 2: Build & Run the Application

### 2.1 Maven Build
```bash
# Project directory তে যান
cd "D:\Programming\Project\Resume Checkers by JAVA"

# Maven dependencies download করুন
mvn clean install

# বা শুধু compile
mvn compile
```

### 2.2 Run Application
```bash
# Spring Boot application run করুন
mvn spring-boot:run
```

**Success হলে দেখবেন:**
```
Started ResumeScreenerApplication in X.XXX seconds
```

Application run হবে: `http://localhost:8080/api`

---

## Step 3: Test with Postman

### 3.1 Postman Setup
1. Postman install করুন (যদি না থাকে)
2. `POSTMAN_COLLECTION.json` file টি import করুন Postman এ
3. Collection ready হবে!

### 3.2 Basic Testing Workflow

**1. Create Job Posting:**
```
POST http://localhost:8080/api/jobs
Content-Type: application/json

{
  "title": "Senior Java Developer",
  "description": "We need an experienced Java developer",
  "requiredSkills": "Java,Spring Boot,MySQL,Hibernate",
  "minExperienceYears": 5,
  "educationLevel": "Bachelor",
  "jobType": "Backend"
}
```

**2. Upload Resume:**
```
POST http://localhost:8080/api/resumes/upload
Content-Type: multipart/form-data

file: [Select a PDF resume file]
candidateName: "John Doe" (optional)
```

**3. Get Rankings:**
```
GET http://localhost:8080/api/ranking/1
```
(1 = job ID যা আপনি create করেছেন)

---

## Step 4: Verify Everything Works

### Checklist:
- [ ] Application starts without errors
- [ ] Database connection successful
- [ ] Can create job posting
- [ ] Can upload resume (PDF/DOCX)
- [ ] Resume text extracted successfully
- [ ] Resume parsed (skills, experience extracted)
- [ ] Rankings calculated correctly
- [ ] Scores look reasonable

---

## Step 5: Common Issues & Solutions

### Issue 1: Database Connection Error
```
Error: Access denied for user 'root'@'localhost'
```
**Solution:**
- MySQL credentials check করুন `application.yml` এ
- MySQL running আছে কিনা check করুন
- Database manually create করতে পারেন:
  ```sql
  CREATE DATABASE resume_screener_db;
  ```

### Issue 2: Port Already in Use
```
Error: Port 8080 is already in use
```
**Solution:**
- `application.yml` এ port change করুন:
  ```yaml
  server:
    port: 8081  # বা অন্য কোনো port
  ```

### Issue 3: Maven Dependencies Error
```
Error: Could not resolve dependencies
```
**Solution:**
```bash
# Maven clean করুন
mvn clean

# Dependencies আবার download করুন
mvn dependency:resolve
```

### Issue 4: File Upload Fails
```
Error: File type not allowed
```
**Solution:**
- শুধু PDF, DOCX, DOC files upload করুন
- File size 10MB এর নিচে রাখুন

---

## Step 6: Test Complete Workflow

### Full Test Scenario:

1. **Create Job:**
   ```
   POST /api/jobs
   → Save job ID (e.g., 1)
   ```

2. **Upload 3 Resumes:**
   ```
   POST /api/resumes/upload (Resume 1 - Strong candidate)
   POST /api/resumes/upload (Resume 2 - Average candidate)
   POST /api/resumes/upload (Resume 3 - Weak candidate)
   ```

3. **Check Parsing:**
   ```
   GET /api/resumes/1
   GET /api/resumes/2
   GET /api/resumes/3
   → Verify parsedSkills, experienceYears, etc.
   ```

4. **Get Rankings:**
   ```
   GET /api/ranking/1
   → Verify:
     - Resumes sorted by final score
     - Ranks assigned (1, 2, 3)
     - Scores calculated correctly
   ```

---

## Step 7: Production Deployment (Optional)

### 7.1 Build JAR File
```bash
mvn clean package
```
JAR file তৈরি হবে: `target/resume-screener-1.0.0.jar`

### 7.2 Run JAR
```bash
java -jar target/resume-screener-1.0.0.jar
```

### 7.3 Production Configuration
`application-prod.yml` create করুন:
```yaml
spring:
  datasource:
    url: jdbc:mysql://your-production-db:3306/resume_screener_db
    username: your_prod_username
    password: your_prod_password

server:
  port: 8080
```

---

## Step 8: Project Structure Review

আপনার project structure:
```
Resume Checkers by JAVA/
├── src/main/java/com/example/resumescreener/
│   ├── controller/      (REST APIs)
│   ├── service/         (Business logic)
│   ├── repository/      (Database access)
│   ├── model/           (JPA entities)
│   ├── dto/             (Request/Response objects)
│   ├── utils/           (Utilities: Tika, Parsers, Scorers)
│   ├── exception/       (Exception handling)
│   └── config/          (Configuration)
├── src/main/resources/
│   └── application.yml  (Configuration)
├── pom.xml              (Maven dependencies)
├── ARCHITECTURE.md      (System design)
├── DATABASE_DESIGN.md   (Database schema)
├── TESTING_GUIDE.md     (Testing instructions)
├── BIAS_CONTROL.md      (Bias control measures)
└── POSTMAN_COLLECTION.json
```

---

## Step 9: Interview Preparation

### What You Can Explain:

1. **Architecture:**
   - Layered architecture (Controller → Service → Repository)
   - REST API design
   - Database design

2. **Key Features:**
   - Resume text extraction (Apache Tika)
   - Resume parsing (Java Regex)
   - Skill matching (exact/partial with synonyms)
   - Scoring algorithm (weighted: 50/30/10/10)
   - Ranking with tie-breaking

3. **Technical Skills:**
   - Spring Boot
   - JPA/Hibernate
   - MySQL
   - Apache Tika
   - Java Collections & Streams
   - Exception handling

4. **Bias Control:**
   - Skill-first scoring
   - No name/gender usage
   - Objective metrics only

---

## Step 10: Next Enhancements (Optional)

আপনি চাইলে add করতে পারেন:

1. **Authentication & Authorization:**
   - Spring Security + JWT
   - User roles (Admin, HR, Recruiter)

2. **Frontend:**
   - React/Angular frontend
   - File upload UI
   - Dashboard for rankings

3. **Advanced Features:**
   - Email notifications
   - Resume comparison
   - Analytics dashboard
   - Export rankings to PDF/Excel

4. **Testing:**
   - Unit tests (JUnit)
   - Integration tests
   - API tests

---

## Quick Start Commands

```bash
# 1. Database setup (MySQL running)
# 2. Update application.yml credentials

# 3. Build & Run
mvn clean install
mvn spring-boot:run

# 4. Test in Postman
# Import POSTMAN_COLLECTION.json
# Run requests

# 5. Check logs
# Application logs দেখবেন console এ
```

---

## Summary

**এখন যা করতে হবে:**

1. ✅ MySQL setup করুন
2. ✅ `application.yml` এ credentials update করুন
3. ✅ `mvn spring-boot:run` দিয়ে application start করুন
4. ✅ Postman এ test করুন
5. ✅ Complete workflow test করুন (create job → upload resume → get rankings)

**সব কিছু working হলে:**
- ✅ Project ready for interview!
- ✅ GitHub এ push করতে পারেন
- ✅ Resume এ mention করতে পারেন

---

## Need Help?

যদি কোনো problem হয়:
1. Error messages check করুন
2. `TESTING_GUIDE.md` দেখুন
3. Logs check করুন
4. Database connection verify করুন

**Good Luck! 🚀**
