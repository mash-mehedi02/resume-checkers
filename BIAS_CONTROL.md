# Bias Control Measures - Documentation

## Overview

The Resume Screening System is designed with bias control measures to ensure fair, objective candidate evaluation based solely on technical qualifications. This document explains the bias control mechanisms implemented in the system.

---

## Core Principles

### 1. **Skill-First Scoring**
- **Skills are weighted at 50%** (highest weight) in final score calculation
- Technical competence is the primary evaluation factor
- Prevents bias based on personal characteristics
- Ensures candidates are evaluated on their ability to perform the job

### 2. **No Name/Gender Usage**
- Candidate names are **NOT used** in scoring or ranking algorithms
- Names are stored only for display/identification purposes
- Gender information is **never extracted or used**
- All evaluation is based on technical qualifications only

### 3. **Configurable Weights**
- Scoring weights are configurable via `application.yml`
- Organizations can adjust weights while maintaining skill-first approach
- Default weights ensure skills are always most important:
  - Skills: 50% (most important)
  - Experience: 30%
  - Education: 10%
  - Projects: 10%

### 4. **Objective Metrics Only**
All scoring is based on objective, measurable criteria:
- **Skill Matching**: Exact/partial match algorithms (no subjective evaluation)
- **Experience Years**: Numeric comparison (years of experience)
- **Education Level**: Hierarchy-based (PhD > Master > Bachelor)
- **Project Relevance**: Keyword-based matching

---

## Implementation Details

### Data Extraction

**What IS Extracted:**
- ✅ Technical skills (Java, Spring Boot, MySQL, etc.)
- ✅ Years of experience (numeric)
- ✅ Education level (Bachelor, Master, PhD)
- ✅ Education field (Computer Science, Engineering, etc.)
- ✅ Project information (descriptions, technologies used)

**What is NOT Extracted:**
- ❌ Gender information
- ❌ Age/date of birth
- ❌ Ethnicity/race
- ❌ Location (beyond what's in resume text)
- ❌ Personal photos/images
- ❌ Marital status
- ❌ Religion
- ❌ Political affiliation

### Candidate Name Handling

- **Storage**: Candidate name is optional and stored in database
- **Usage**: Name is **NOT used** in:
  - Scoring calculations
  - Ranking algorithms
  - Tie-breaking logic
  - Any evaluation process
- **Purpose**: Name is included in API responses only for:
  - Display purposes
  - User identification
  - Administrative tracking

### Scoring Algorithm

**Final Score Formula:**
```
Final Score = (Skill Score × 50%) + 
              (Experience Score × 30%) + 
              (Education Score × 10%) + 
              (Project Score × 10%)
```

**Bias Control in Formula:**
- Skills weighted highest (50%) ensures technical competence is primary factor
- Experience (30%) focuses on years of practice, not personal characteristics
- Education (10%) considers degree level, not institution name or location
- Projects (10%) evaluates technical work, not personal background

### Ranking Algorithm

**Tie-Breaking Order:**
1. Final score (descending)
2. Skill score (descending)
3. Experience score (descending)
4. Number of matched skills (descending)
5. Resume ID (ascending - first uploaded wins)

**Bias Control:**
- No personal information used in tie-breaking
- All criteria are objective and technical
- Resume ID used only as final tie-breaker (first-come basis)

---

## Code-Level Bias Control

### ScoringService.java
```java
/**
 * BIAS CONTROL MEASURES:
 * 1. SKILL-FIRST SCORING: Skills weighted at 50% (highest)
 * 2. NO NAME/GENDER USAGE: Names not used in scoring
 * 3. CONFIGURABLE WEIGHTS: Adjustable via application.yml
 * 4. OBJECTIVE METRICS: All scoring based on measurable criteria
 */
```

### CandidateResume.java
```java
// BIAS CONTROL: Candidate name is optional and NOT used in scoring or ranking.
// It is stored only for display purposes.
@Column(name = "candidate_name", length = 255)
private String candidateName;
```

### RankingService.java
```java
/**
 * BIAS CONTROL MEASURES:
 * 1. NO NAME/GENDER IN RANKING: Names not used in ranking algorithms
 * 2. OBJECTIVE TIE-BREAKING: Only technical metrics used
 * 3. SCORE-BASED SORTING: Pure score-based ranking
 */
```

### SkillMatcher.java
```java
/**
 * BIAS CONTROL: Skill-first matching engine.
 * Evaluates candidates purely based on technical skills without
 * considering any personal information.
 */
```

---

## Configuration

Bias control weights are configurable in `application.yml`:

```yaml
app:
  scoring:
    skill-weight: 0.50      # 50% - Skills (most important)
    experience-weight: 0.30 # 30% - Experience
    education-weight: 0.10  # 10% - Education
    project-weight: 0.10    # 10% - Projects
```

**Note**: While weights are configurable, the system is designed to maintain skill-first approach. Organizations should ensure skills remain the highest weighted factor.

---

## Best Practices

### For Organizations

1. **Maintain Skill-First Approach**
   - Keep skill weight at 50% or higher
   - Don't reduce skill weight below other factors

2. **Review Scoring Weights**
   - Regularly review and adjust weights based on job requirements
   - Ensure weights reflect job performance factors

3. **Monitor for Bias**
   - Review ranking results for patterns
   - Ensure diverse candidate pools are being evaluated fairly

4. **Transparency**
   - Communicate scoring criteria to candidates
   - Explain how weights are determined

### For Developers

1. **Never Extract Personal Information**
   - Don't add parsers for gender, age, ethnicity
   - Don't use names in scoring algorithms

2. **Keep Scoring Objective**
   - All scoring should be based on measurable criteria
   - Avoid subjective evaluation factors

3. **Document Bias Control**
   - Add comments explaining bias control measures
   - Update this document when adding new features

---

## Compliance

This system is designed to support:
- **Equal Employment Opportunity (EEO)** compliance
- **Fair hiring practices**
- **Objective candidate evaluation**
- **Diversity and inclusion** goals

---

## Limitations

While the system implements bias control measures, it's important to note:

1. **Algorithmic Bias**: The system may still reflect biases present in:
   - Skill keyword databases
   - Education level hierarchies
   - Experience year requirements

2. **Data Quality**: Bias control depends on:
   - Accurate resume parsing
   - Complete skill extraction
   - Proper experience calculation

3. **Human Review**: Automated screening should be combined with:
   - Human review of top candidates
   - Diverse interview panels
   - Structured interview processes

---

## Future Enhancements

Potential improvements for enhanced bias control:

1. **Blind Review Mode**: Option to hide candidate names completely
2. **Skill Synonym Expansion**: More comprehensive skill matching
3. **Bias Auditing**: Tools to detect potential bias in results
4. **Diversity Metrics**: Track diversity in candidate pools (anonymized)
5. **Custom Weight Profiles**: Different weight sets for different job types

---

## Conclusion

The Resume Screening System prioritizes fair, objective candidate evaluation through:
- Skill-first scoring approach
- Exclusion of personal information from evaluation
- Objective, measurable criteria
- Configurable weights for organizational needs

These measures ensure candidates are evaluated based on their qualifications and ability to perform the job, not on personal characteristics.
