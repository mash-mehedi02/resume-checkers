# Quick Start Guide - Resume Screening System

## 🚀 3-Step Quick Start

### Step 1: Update Database Credentials
Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    username: root        # আপনার MySQL username
    password: root         # আপনার MySQL password
```

### Step 2: Run Application
```bash
mvn spring-boot:run
```

### Step 3: Test in Postman
1. Import `POSTMAN_COLLECTION.json` in Postman
2. Create Job → Upload Resume → Get Rankings

---

## ✅ Verification Checklist

- [ ] MySQL is running
- [ ] Database credentials updated in `application.yml`
- [ ] Application starts without errors
- [ ] Can access: `http://localhost:8080/api/jobs`

---

## 📝 First Test

**1. Create Job:**
```
POST http://localhost:8080/api/jobs
{
  "title": "Java Developer",
  "description": "Backend developer needed",
  "requiredSkills": "Java,Spring Boot",
  "minExperienceYears": 3
}
```

**2. Upload Resume:**
```
POST http://localhost:8080/api/resumes/upload
file: [your-resume.pdf]
```

**3. Get Rankings:**
```
GET http://localhost:8080/api/ranking/1
```

---

## 🆘 Quick Troubleshooting

**Port 8080 in use?**
- Change port in `application.yml`: `server.port: 8081`

**Database error?**
- Check MySQL is running
- Verify credentials
- Database auto-creates on first run

**File upload fails?**
- Use PDF/DOCX only
- File size < 10MB

---

**Ready to go! 🎉**
