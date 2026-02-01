# Experience Relevance Scoring - Documentation

## Overview

The Experience Relevance Scoring system calculates how well a candidate's experience matches job requirements. It considers both years of experience and role relevance (backend/frontend/fullstack).

---

## Scoring Components

### 1. Years Match Score (80% weight)

**Formula:**
- **Meets or Exceeds Requirement**: 100 points
  - Exact match: 100 points
  - 1-2 years over: 100 points (slight over-qualification is good)
  - 3-5 years over: 100 points (still acceptable)
  - 6+ years over: 95 points (might be over-qualified)

- **Under-Qualified**: Proportional score with penalty
  - **Less than 50% of required**: `(resume years / required years) × 60`
    - Example: 2 years vs 5 required = (2/5) × 60 = 24 points
  - **50-75% of required**: `(resume years / required years) × 80`
    - Example: 3 years vs 5 required = (3/5) × 80 = 48 points
  - **75-100% of required**: `(resume years / required years) × 90`
    - Example: 4 years vs 5 required = (4/5) × 90 = 72 points

### 2. Role Relevance Bonus (20% weight)

**Logic:**
- **Exact Match**: +20 points
  - Job: "Backend Developer" → Resume: "Backend Engineer" = +20 points
  
- **Fullstack Compatibility**:
  - Job: "Fullstack Developer" → Resume: "Backend Developer" = +10 points
  - Job: "Fullstack Developer" → Resume: "Frontend Developer" = +10 points
  
- **Fullstack Experience Value**:
  - Job: "Backend Developer" → Resume: "Fullstack Developer" = +15 points
  - Job: "Frontend Developer" → Resume: "Fullstack Developer" = +15 points
  
- **Partial Match**: +15 points
  - Job: "Backend Developer" → Resume: "Backend Engineer" = +15 points
  
- **No Match**: 0 points

**Final Score Calculation:**
```
Total Score = (Years Match Score × 0.80) + (Role Relevance Bonus × 0.20)
```

---

## Assumptions

### 1. **Experience Years Extraction**
- Experience years are extracted from resume using regex patterns
- Can be null if not found (returns 0 score)
- Assumes accurate extraction (validated range: 0-50 years)

### 2. **Over-Qualification Handling**
- Slight over-qualification (1-2 years) is considered ideal
- Moderate over-qualification (3-5 years) is acceptable
- Significant over-qualification (6+ years) gets slight penalty (95 points)
- Rationale: Over-qualified candidates might be expensive or get bored

### 3. **Under-Qualification Penalties**
- Heavy penalty for <50% of required experience
- Moderate penalty for 50-75% of required experience
- Light penalty for 75-100% of required experience
- Rationale: Experience is critical for job performance

### 4. **Role Relevance**
- Job type matching is optional (bonus, not requirement)
- Fullstack experience is valuable for specialized roles
- Specialized experience is valuable for fullstack roles
- No penalty for missing job type information

### 5. **Score Range**
- All scores are clamped to 0-100 range
- Uses BigDecimal for precision (2 decimal places)
- Rounds using HALF_UP rounding mode

---

## Examples

### Example 1: Perfect Match
```
Resume: 5 years experience, Backend Developer
Job: 5 years required, Backend Developer

Years Score: 100 (exact match)
Role Bonus: 20 (exact match)
Final: (100 × 0.80) + (20 × 0.20) = 80 + 4 = 84 points
```

### Example 2: Over-Qualified
```
Resume: 8 years experience, Backend Developer
Job: 5 years required, Backend Developer

Years Score: 100 (exceeds requirement)
Role Bonus: 20 (exact match)
Final: (100 × 0.80) + (20 × 0.20) = 84 points
```

### Example 3: Under-Qualified
```
Resume: 2 years experience, Backend Developer
Job: 5 years required, Backend Developer

Years Score: (2/5) × 60 = 24 points (heavy penalty)
Role Bonus: 20 (exact match)
Final: (24 × 0.80) + (20 × 0.20) = 19.2 + 4 = 23.2 points
```

### Example 4: Role Mismatch
```
Resume: 5 years experience, Frontend Developer
Job: 5 years required, Backend Developer

Years Score: 100 (exact match)
Role Bonus: 0 (no match)
Final: (100 × 0.80) + (0 × 0.20) = 80 points
```

### Example 5: Fullstack Compatibility
```
Resume: 5 years experience, Fullstack Developer
Job: 5 years required, Backend Developer

Years Score: 100 (exact match)
Role Bonus: 15 (fullstack → backend)
Final: (100 × 0.80) + (15 × 0.20) = 80 + 3 = 83 points
```

---

## Implementation Notes

### Java Implementation
- **Class**: `ExperienceScorer.java`
- **Method**: `calculateExperienceScore()`
- **Returns**: `BigDecimal` (0-100, 2 decimal places)

### Integration
- Used by `ScoringService.calculateExperienceScore()`
- Weight: 30% of final score (configurable in `application.yml`)
- Can be called independently or as part of overall scoring

### Future Enhancements
- Extract job type from resume text automatically
- Consider industry-specific experience
- Add domain expertise scoring (e.g., fintech, healthcare)
- Consider company size and type matching

---

## Configuration

Scoring weights are configurable in `application.yml`:
```yaml
app:
  scoring:
    experience-weight: 0.30  # 30% of final score
```
