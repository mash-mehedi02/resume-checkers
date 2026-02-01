# Final Score Calculation - Documentation

## Overview

The final score is a weighted combination of four components:
- **Skills**: 50% weight
- **Experience**: 30% weight
- **Education**: 10% weight
- **Projects**: 10% weight

**Total**: 100%

---

## Formula

```
Final Score = (Skill Score × 0.50) + 
              (Experience Score × 0.30) + 
              (Education Score × 0.10) + 
              (Project Score × 0.10)
```

All component scores are on a scale of 0-100.

---

## Example Calculation

### Scenario: Strong Candidate

**Resume Data:**
- Skills: "Java, Spring Boot, MySQL, Hibernate" (4 skills)
- Experience: 5 years, Backend Developer
- Education: Bachelor in Computer Science
- Projects: 3 projects mentioning Java and Spring Boot

**Job Requirements:**
- Required Skills: "Java, Spring Boot, MySQL, Hibernate" (4 skills)
- Required Experience: 5 years, Backend Developer
- Required Education: Bachelor
- (No specific project requirement)

**Component Scores:**
1. **Skill Score**: 100.0 (4/4 skills matched)
2. **Experience Score**: 84.0 (exact match + role match)
3. **Education Score**: 100.0 (exact match + field match)
4. **Project Score**: 80.0 (projects present + relevance)

**Final Score Calculation:**
```
Final Score = (100.0 × 0.50) + (84.0 × 0.30) + (100.0 × 0.10) + (80.0 × 0.10)
            = 50.0 + 25.2 + 10.0 + 8.0
            = 93.20
```

---

### Scenario: Average Candidate

**Resume Data:**
- Skills: "Java, Spring Boot, MySQL" (3 skills)
- Experience: 3 years, Backend Developer
- Education: Bachelor in Computer Science
- Projects: 1 project mentioning Java

**Job Requirements:**
- Required Skills: "Java, Spring Boot, MySQL, Hibernate" (4 skills)
- Required Experience: 5 years, Backend Developer
- Required Education: Bachelor
- (No specific project requirement)

**Component Scores:**
1. **Skill Score**: 75.0 (3/4 skills matched)
2. **Experience Score**: 48.0 (under-qualified: 3/5 years)
3. **Education Score**: 100.0 (exact match)
4. **Project Score**: 60.0 (projects present, limited relevance)

**Final Score Calculation:**
```
Final Score = (75.0 × 0.50) + (48.0 × 0.30) + (100.0 × 0.10) + (60.0 × 0.10)
            = 37.5 + 14.4 + 10.0 + 6.0
            = 67.90
```

---

### Scenario: Weak Candidate

**Resume Data:**
- Skills: "Java, MySQL" (2 skills)
- Experience: 1 year, Frontend Developer
- Education: Diploma
- Projects: No projects mentioned

**Job Requirements:**
- Required Skills: "Java, Spring Boot, MySQL, Hibernate" (4 skills)
- Required Experience: 5 years, Backend Developer
- Required Education: Bachelor
- (No specific project requirement)

**Component Scores:**
1. **Skill Score**: 50.0 (2/4 skills matched)
2. **Experience Score**: 12.0 (severely under-qualified: 1/5 years, role mismatch)
3. **Education Score**: 30.0 (Diploma vs Bachelor requirement)
4. **Project Score**: 0.0 (no projects)

**Final Score Calculation:**
```
Final Score = (50.0 × 0.50) + (12.0 × 0.30) + (30.0 × 0.10) + (0.0 × 0.10)
            = 25.0 + 3.6 + 3.0 + 0.0
            = 31.60
```

---

## Java Implementation

### Method Signature

```java
public BigDecimal calculateFinalScore(CandidateResume resume, JobPost job)
```

### Code Flow

1. Calculate individual component scores:
   - `calculateSkillScore(resume, job)`
   - `calculateExperienceScore(resume, job)`
   - `calculateEducationScore(resume, job)`
   - `calculateProjectScore(resume, job)`

2. Apply weights:
   - Skill: 50%
   - Experience: 30%
   - Education: 10%
   - Projects: 10%

3. Sum weighted scores

4. Clamp to 0-100 range

5. Round to 2 decimal places

### Example Usage

```java
@Autowired
private ScoringService scoringService;

// Calculate final score
BigDecimal finalScore = scoringService.calculateFinalScore(resume, job);
// Returns: 93.20 (for strong candidate example)
```

---

## Weight Configuration

Weights are configurable in `application.yml`:

```yaml
app:
  scoring:
    skill-weight: 0.50      # 50%
    experience-weight: 0.30 # 30%
    education-weight: 0.10  # 10%
    project-weight: 0.10    # 10%
```

**Note**: Weights should sum to 1.0 (100%) for accurate scoring.

---

## Score Interpretation

### Score Ranges

- **90-100**: Excellent match - Highly recommended
- **80-89**: Very good match - Strong candidate
- **70-79**: Good match - Qualified candidate
- **60-69**: Average match - Consider if other factors are strong
- **50-59**: Below average - May need additional screening
- **0-49**: Poor match - Likely not suitable

### Component Impact

- **Skills (50%)**: Most important - Directly impacts job performance
- **Experience (30%)**: Very important - Years of practice matter
- **Education (10%)**: Important but less critical - Can be compensated
- **Projects (10%)**: Bonus factor - Shows practical experience

---

## Edge Cases

### Missing Data

- **No skills in resume**: Skill score = 0
- **No experience data**: Experience score = 0
- **No education data**: Education score = 0
- **No projects**: Project score = 0

### No Requirements

- **No required skills**: Skill score = 100 (perfect match)
- **No experience requirement**: Experience score = 100
- **No education requirement**: Education score = 100

### Null Handling

All methods handle null values gracefully:
- Null skills → 0 score
- Null experience → 0 score
- Null education → 0 score (unless no requirement, then 100)
- Null projects → 0 score

---

## Precision

- All scores use `BigDecimal` for precision
- Final score rounded to 2 decimal places
- Uses `HALF_UP` rounding mode
- Example: 93.195 → 93.20

---

## Integration

The final score calculation is used in:
- **Ranking Service**: To sort candidates
- **Resume Score Entity**: To store calculated scores
- **API Responses**: To return match scores to clients

---

## Future Enhancements

Potential improvements:
1. Dynamic weight adjustment based on job type
2. Industry-specific scoring
3. Certification scoring
4. Language proficiency scoring
5. Custom scoring rules per job posting
