const API_BASE = '/api';

// Show/Hide sections
function showSection(sectionId) {
    document.querySelectorAll('.section').forEach(section => {
        section.classList.remove('active');
    });
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    document.getElementById(sectionId).classList.add('active');
    event.target.classList.add('active');
    
    // Load data when section is shown
    if (sectionId === 'dashboard') {
        loadDashboard();
    } else if (sectionId === 'jobs') {
        loadJobs();
    } else if (sectionId === 'resumes') {
        loadResumes();
    } else if (sectionId === 'rankings') {
        loadJobSelect();
    }
}

// Show loading overlay
function showLoading() {
    document.getElementById('loadingOverlay').style.display = 'flex';
}

function hideLoading() {
    document.getElementById('loadingOverlay').style.display = 'none';
}

// Show toast notification
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} show`;
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// API Calls
async function apiCall(endpoint, method = 'GET', body = null) {
    try {
        const options = {
            method,
            headers: {}
        };
        
        if (body instanceof FormData) {
            options.body = body;
        } else if (body) {
            options.headers['Content-Type'] = 'application/json';
            options.body = JSON.stringify(body);
        }
        
        const response = await fetch(`${API_BASE}${endpoint}`, options);
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || 'An error occurred');
        }
        
        return data;
    } catch (error) {
        showToast(error.message, 'error');
        throw error;
    }
}

// Dashboard
async function loadDashboard() {
    showLoading();
    try {
        const jobs = await apiCall('/jobs');
        const resumes = await apiCall('/resumes');
        
        document.getElementById('totalJobs').textContent = jobs.length || 0;
        document.getElementById('totalResumes').textContent = resumes.length || 0;
        
        // Calculate average score (if rankings exist)
        let totalScore = 0;
        let count = 0;
        if (jobs.length > 0) {
            for (const job of jobs) {
                try {
                    const rankings = await apiCall(`/ranking/${job.id}`);
                    rankings.forEach(r => {
                        totalScore += parseFloat(r.finalScore || 0);
                        count++;
                    });
                } catch (e) {
                    // Ignore errors
                }
            }
        }
        
        const avgScore = count > 0 ? (totalScore / count).toFixed(1) : 0;
        document.getElementById('avgScore').textContent = avgScore;
        
        // Load recent activity
        loadActivity();
    } catch (error) {
        console.error('Error loading dashboard:', error);
    } finally {
        hideLoading();
    }
}

function loadActivity() {
    const activityList = document.getElementById('activityList');
    activityList.innerHTML = '<p style="color: var(--text-secondary); text-align: center; padding: 20px;">No recent activity</p>';
    // Can be enhanced to show actual activity
}

// Jobs
function showCreateJobForm() {
    document.getElementById('createJobForm').style.display = 'block';
    document.getElementById('jobForm').reset();
}

function hideCreateJobForm() {
    document.getElementById('createJobForm').style.display = 'none';
}

async function createJob(event) {
    event.preventDefault();
    showLoading();
    
    try {
        const jobData = {
            title: document.getElementById('jobTitle').value,
            description: document.getElementById('jobDescription').value,
            requiredSkills: document.getElementById('requiredSkills').value,
            preferredSkills: document.getElementById('preferredSkills').value,
            minExperienceYears: parseInt(document.getElementById('minExperience').value),
            educationLevel: document.getElementById('educationLevel').value || null,
            jobType: document.getElementById('jobType').value || null
        };
        
        await apiCall('/jobs', 'POST', jobData);
        showToast('Job created successfully!');
        hideCreateJobForm();
        loadJobs();
        loadDashboard();
    } catch (error) {
        console.error('Error creating job:', error);
    } finally {
        hideLoading();
    }
}

async function loadJobs() {
    showLoading();
    try {
        const jobs = await apiCall('/jobs');
        const jobsList = document.getElementById('jobsList');
        
        if (jobs.length === 0) {
            jobsList.innerHTML = '<div class="form-container"><p style="text-align: center; color: var(--text-secondary);">No jobs created yet. Create your first job!</p></div>';
            return;
        }
        
        jobsList.innerHTML = jobs.map(job => `
            <div class="job-card">
                <h3>${job.title}</h3>
                <p>${job.description}</p>
                <div class="skills">
                    ${job.requiredSkills.split(',').map(skill => `<span class="skill-tag">${skill.trim()}</span>`).join('')}
                </div>
                <div class="meta">
                    <span>📅 ${new Date(job.createdAt).toLocaleDateString()}</span>
                    <span>💼 ${job.minExperienceYears} years exp</span>
                    ${job.educationLevel ? `<span>🎓 ${job.educationLevel}</span>` : ''}
                    ${job.jobType ? `<span>🔧 ${job.jobType}</span>` : ''}
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading jobs:', error);
    } finally {
        hideLoading();
    }
}

// Resumes
function showUploadForm() {
    document.getElementById('uploadForm').style.display = 'block';
    document.getElementById('resumeForm').reset();
}

function hideUploadForm() {
    document.getElementById('uploadForm').style.display = 'none';
}

async function uploadResume(event) {
    event.preventDefault();
    showLoading();
    
    try {
        const formData = new FormData();
        const fileInput = document.getElementById('resumeFile');
        const candidateName = document.getElementById('candidateName').value;
        
        if (!fileInput.files[0]) {
            showToast('Please select a file', 'error');
            hideLoading();
            return;
        }
        
        formData.append('file', fileInput.files[0]);
        if (candidateName) {
            formData.append('candidateName', candidateName);
        }
        
        const response = await fetch(`${API_BASE}/resumes/upload`, {
            method: 'POST',
            body: formData
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Upload failed');
        }
        
        const data = await response.json();
        showToast('Resume uploaded and parsed successfully!');
        hideUploadForm();
        loadResumes();
        loadDashboard();
    } catch (error) {
        showToast(error.message, 'error');
    } finally {
        hideLoading();
    }
}

async function loadResumes() {
    showLoading();
    try {
        const resumes = await apiCall('/resumes');
        const resumesList = document.getElementById('resumesList');
        
        if (resumes.length === 0) {
            resumesList.innerHTML = '<div class="form-container"><p style="text-align: center; color: var(--text-secondary);">No resumes uploaded yet. Upload your first resume!</p></div>';
            return;
        }
        
        resumesList.innerHTML = resumes.map(resume => `
            <div class="resume-card">
                <h3>${resume.candidateName || resume.fileName || 'Resume #' + resume.id}</h3>
                <p>📄 ${resume.fileName}</p>
                ${resume.parsedSkills ? `
                    <div class="skills" style="margin-top: 15px;">
                        ${resume.parsedSkills.split(',').map(skill => `<span class="skill-tag">${skill.trim()}</span>`).join('')}
                    </div>
                ` : ''}
                <div class="meta">
                    <span>📅 ${new Date(resume.uploadedAt).toLocaleDateString()}</span>
                    ${resume.experienceYears ? `<span>💼 ${resume.experienceYears} years exp</span>` : ''}
                    ${resume.educationLevel ? `<span>🎓 ${resume.educationLevel}</span>` : ''}
                    ${resume.fileSize ? `<span>📦 ${(resume.fileSize / 1024).toFixed(1)} KB</span>` : ''}
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading resumes:', error);
        const resumesList = document.getElementById('resumesList');
        resumesList.innerHTML = '<div class="form-container"><p style="text-align: center; color: var(--text-secondary);">Error loading resumes. Please try again.</p></div>';
    } finally {
        hideLoading();
    }
}

// Rankings
async function loadJobSelect() {
    showLoading();
    try {
        const jobs = await apiCall('/jobs');
        const jobSelect = document.getElementById('jobSelect');
        
        jobSelect.innerHTML = '<option value="">-- Select Job --</option>' +
            jobs.map(job => `<option value="${job.id}">${job.title}</option>`).join('');
        
        if (jobs.length === 0) {
            document.getElementById('rankingsList').innerHTML = 
                '<div class="form-container"><p style="text-align: center; color: var(--text-secondary);">No jobs available. Create a job first!</p></div>';
        }
    } catch (error) {
        console.error('Error loading jobs:', error);
    } finally {
        hideLoading();
    }
}

async function loadRankings() {
    const jobId = document.getElementById('jobSelect').value;
    if (!jobId) {
        document.getElementById('rankingsList').innerHTML = '';
        return;
    }
    
    showLoading();
    try {
        const rankings = await apiCall(`/ranking/${jobId}`);
        const rankingsList = document.getElementById('rankingsList');
        
        if (rankings.length === 0) {
            rankingsList.innerHTML = '<div class="form-container"><p style="text-align: center; color: var(--text-secondary);">No resumes ranked yet. Upload resumes first!</p></div>';
            return;
        }
        
        rankingsList.innerHTML = rankings.map((ranking, index) => {
            const rankClass = ranking.rank === 1 ? 'gold' : ranking.rank === 2 ? 'silver' : ranking.rank === 3 ? 'bronze' : 'other';
            
            return `
                <div class="ranking-card">
                    <div class="rank-badge ${rankClass}">${ranking.rank}</div>
                    <div class="ranking-info">
                        <h3>${ranking.candidateName || ranking.fileName || 'Candidate'}</h3>
                        <p>📄 ${ranking.fileName}</p>
                        <div class="score-breakdown">
                            <div class="score-item">
                                <div class="label">Skills</div>
                                <div class="value">${parseFloat(ranking.skillScore).toFixed(1)}</div>
                            </div>
                            <div class="score-item">
                                <div class="label">Experience</div>
                                <div class="value">${parseFloat(ranking.experienceScore).toFixed(1)}</div>
                            </div>
                            <div class="score-item">
                                <div class="label">Education</div>
                                <div class="value">${parseFloat(ranking.educationScore).toFixed(1)}</div>
                            </div>
                            <div class="score-item">
                                <div class="label">Projects</div>
                                <div class="value">${parseFloat(ranking.projectScore).toFixed(1)}</div>
                            </div>
                        </div>
                        ${ranking.matchedSkills && ranking.matchedSkills.length > 0 ? `
                            <div class="skills" style="margin-top: 10px;">
                                ${Array.from(ranking.matchedSkills).map(skill => `<span class="skill-tag">✓ ${skill}</span>`).join('')}
                            </div>
                        ` : ''}
                    </div>
                    <div class="final-score">
                        <div class="label">Final Score</div>
                        <div class="value">${parseFloat(ranking.finalScore).toFixed(1)}</div>
                    </div>
                </div>
            `;
        }).join('');
    } catch (error) {
        showToast(error.message, 'error');
        console.error('Error loading rankings:', error);
    } finally {
        hideLoading();
    }
}

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    loadDashboard();
});
