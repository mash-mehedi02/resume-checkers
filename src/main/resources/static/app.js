const API_BASE = '/api';

// Section Management
function showSection(sectionId, button) {
    const sections = document.querySelectorAll('.section');
    const navItems = document.querySelectorAll('.nav-item');

    sections.forEach(section => section.classList.remove('active'));
    navItems.forEach(item => item.classList.remove('active'));

    const targetSection = document.getElementById(sectionId);
    if (targetSection) targetSection.classList.add('active');

    if (button && button.classList.contains('nav-item')) {
        button.classList.add('active');
    } else {
        const navItem = document.querySelector(`.nav-item[onclick*="'${sectionId}'"]`);
        if (navItem) navItem.classList.add('active');
    }

    // Load data based on current section
    switch (sectionId) {
        case 'dashboard': loadDashboard(); break;
        case 'jobs': loadJobs(); break;
        case 'resumes': loadResumes(); break;
        case 'rankings': loadJobSelect(); break;
    }

    // Close mobile sidebar if open
    document.getElementById('sidebar').classList.remove('active');
}

// Global UI State
function toggleLoading(show) {
    document.getElementById('loadingOverlay').style.display = show ? 'flex' : 'none';
}

function showToast(message, type = 'success') {
    const container = document.getElementById('toast');
    const toast = document.createElement('div');
    toast.className = `toast-msg ${type}`;
    toast.textContent = message;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        setTimeout(() => toast.remove(), 400);
    }, 4000);
}

// API Integration
async function apiCall(endpoint, method = 'GET', body = null) {
    try {
        const options = { method, headers: {} };
        if (body instanceof FormData) {
            options.body = body;
        } else if (body) {
            options.headers['Content-Type'] = 'application/json';
            options.body = JSON.stringify(body);
        }

        const response = await fetch(`${API_BASE}${endpoint}`, options);
        if (response.status === 204) return null;

        const contentType = response.headers.get('content-type') || '';
        let data = null;
        if (contentType.includes('application/json')) {
            data = await response.json();
        }

        if (!response.ok) {
            throw new Error(data?.message || 'Execution failed');
        }
        return data;
    } catch (error) {
        showToast(error.message, 'error');
        throw error;
    }
}

// Dashboard Logic
async function loadDashboard() {
    toggleLoading(true);
    try {
        const [jobs, resumes] = await Promise.all([
            apiCall('/jobs'),
            apiCall('/resumes')
        ]);

        document.getElementById('totalJobs').textContent = jobs?.length || 0;
        document.getElementById('totalResumes').textContent = resumes?.length || 0;

        let totalScore = 0;
        let count = 0;
        let maxRankCount = 0;

        if (jobs?.length > 0) {
            for (const job of jobs) {
                try {
                    const rankings = await apiCall(`/ranking/${job.id}`);
                    if (rankings) {
                        rankings.forEach(r => {
                            totalScore += parseFloat(r.finalScore || 0);
                            count++;
                        });
                        maxRankCount = Math.max(maxRankCount, rankings.length);
                    }
                } catch (e) { }
            }
        }

        document.getElementById('avgScore').textContent = count > 0 ? (totalScore / count).toFixed(0) : 0;
        document.getElementById('topRanked').textContent = maxRankCount;

        renderActivity(jobs, resumes);
    } catch (e) {
        console.error(e);
    } finally {
        toggleLoading(false);
    }
}

function renderActivity(jobs = [], resumes = []) {
    const list = document.getElementById('activityList');
    const items = [];

    jobs?.slice(0, 3).forEach(j => items.push({
        type: 'job',
        title: j.title,
        desc: `New opening created • ${formatDate(j.createdAt)}`,
        icon: '💼'
    }));

    resumes?.slice(0, 3).forEach(r => items.push({
        type: 'resume',
        title: r.candidateName || r.fileName,
        desc: `Resume parsed and indexed • ${formatDate(r.uploadedAt)}`,
        icon: '📄'
    }));

    if (items.length === 0) {
        list.innerHTML = '<div class="empty-state">No operations logged yet</div>';
        return;
    }

    list.innerHTML = items.map(item => `
        <div class="activity-item">
            <div class="activity-icon">${item.icon}</div>
            <div class="activity-content">
                <h4>${item.title}</h4>
                <p>${item.desc}</p>
            </div>
        </div>
    `).join('');
}

// Job Management
function showCreateJobForm() {
    document.getElementById('createJobForm').style.display = 'flex';
}

function hideCreateJobForm() {
    document.getElementById('createJobForm').style.display = 'none';
}

async function createJob(event) {
    event.preventDefault();
    toggleLoading(true);
    try {
        const data = {
            title: document.getElementById('jobTitle').value,
            description: document.getElementById('jobDescription').value,
            requiredSkills: document.getElementById('requiredSkills').value,
            preferredSkills: document.getElementById('preferredSkills').value,
            minExperienceYears: parseInt(document.getElementById('minExperience').value),
            educationLevel: document.getElementById('educationLevel').value || null
        };
        await apiCall('/jobs', 'POST', data);
        showToast('Role created successfully');
        hideCreateJobForm();
        loadJobs();
    } finally {
        toggleLoading(false);
    }
}

async function loadJobs() {
    toggleLoading(true);
    try {
        const jobs = await apiCall('/jobs') || [];
        const container = document.getElementById('jobsList');

        if (jobs.length === 0) {
            container.innerHTML = '<div class="empty-state" style="grid-column: 1/-1">No active openings found</div>';
            return;
        }

        container.innerHTML = jobs.map(j => `
            <div class="job-card">
                <h3>${j.title}</h3>
                <p>${j.description}</p>
                <div class="skills-row">
                    ${j.requiredSkills.split(',').map(s => `<span class="skill-tag">${s.trim()}</span>`).join('')}
                </div>
                <div class="meta-row">
                    <span>📅 ${formatDate(j.createdAt)}</span>
                    <span>📍 ${j.minExperienceYears}+ Years</span>
                </div>
            </div>
        `).join('');
    } finally {
        toggleLoading(false);
    }
}

// Resume Management
function showUploadForm() {
    document.getElementById('uploadForm').style.display = 'flex';
}

function hideUploadForm() {
    document.getElementById('uploadForm').style.display = 'none';
}

async function uploadResume(event) {
    event.preventDefault();
    const fileInput = document.getElementById('resumeFile');
    if (!fileInput.files[0]) return showToast('Please select a file', 'error');

    toggleLoading(true);
    try {
        const formData = new FormData();
        formData.append('file', fileInput.files[0]);
        const name = document.getElementById('candidateName').value;
        if (name) formData.append('candidateName', name);

        await apiCall('/resumes/upload', 'POST', formData);
        showToast('Profile indexed successfully');
        hideUploadForm();
        loadResumes();
    } finally {
        toggleLoading(false);
    }
}

async function loadResumes() {
    toggleLoading(true);
    try {
        const resumes = await apiCall('/resumes') || [];
        const container = document.getElementById('resumesList');

        if (resumes.length === 0) {
            container.innerHTML = '<div class="empty-state" style="grid-column: 1/-1">Database is empty</div>';
            return;
        }

        container.innerHTML = resumes.map(r => `
            <div class="resume-card">
                <h3>${r.candidateName || r.fileName}</h3>
                <div class="skills-row">
                    ${r.parsedSkills ? r.parsedSkills.split(',').map(s => `<span class="skill-tag">${s.trim()}</span>`).join('') : '<span class="skill-tag">Parsing...</span>'}
                </div>
                <div class="meta-row">
                    <span>📄 ${r.fileName}</span>
                    <span>📏 ${(r.fileSize / 1024).toFixed(0)}KB</span>
                </div>
            </div>
        `).join('');
    } finally {
        toggleLoading(false);
    }
}

// Ranking Engine
async function loadJobSelect() {
    try {
        const jobs = await apiCall('/jobs') || [];
        const select = document.getElementById('jobSelect');
        select.innerHTML = '<option value="">Select an active role...</option>' +
            jobs.map(j => `<option value="${j.id}">${j.title}</option>`).join('');
    } catch (e) { }
}

async function loadRankings() {
    const jobId = document.getElementById('jobSelect').value;
    if (!jobId) {
        document.getElementById('rankingsList').innerHTML = '<div class="empty-state-large"><h3>No job selected</h3><p>Select a job opening above to see matching candidates.</p></div>';
        return;
    }

    toggleLoading(true);
    try {
        const rankings = await apiCall(`/ranking/${jobId}`) || [];
        const container = document.getElementById('rankingsList');

        if (rankings.length === 0) {
            container.innerHTML = '<div class="empty-state-large"><h3>No matches found</h3><p>Try uploading more resumes or broadening job criteria.</p></div>';
            return;
        }

        container.innerHTML = rankings.map((r, i) => {
            const rankClass = (i === 0) ? 'gold' : (i === 1) ? 'silver' : (i === 2) ? 'bronze' : 'other';
            return `
                <div class="ranking-card">
                    <div class="rank-badge ${rankClass}">${i + 1}</div>
                    <div class="ranking-info">
                        <h3>${r.candidateName || r.fileName}</h3>
                        <p>${r.matchedSkills?.join(', ') || 'No skill overlap detected'}</p>
                    </div>
                    <div class="scores-wrap">
                        <div class="score-node"><span>Skills</span><strong>${parseFloat(r.skillScore).toFixed(0)}</strong></div>
                        <div class="score-node"><span>Experience</span><strong>${parseFloat(r.experienceScore).toFixed(0)}</strong></div>
                        <div class="score-node"><span>Education</span><strong>${parseFloat(r.educationScore).toFixed(0)}</strong></div>
                    </div>
                    <div class="final-wrap">
                        <span>Relevancy</span>
                        <h2>${parseFloat(r.finalScore).toFixed(0)}%</h2>
                    </div>
                </div>
            `;
        }).join('');
    } finally {
        toggleLoading(false);
    }
}

// Helpers
function formatDate(val) {
    if (!val) return 'Recently';
    const date = new Date(val);
    return isNaN(date.getTime()) ? 'Recently' : date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

async function updateStatus() {
    try {
        await fetch('/');
        document.getElementById('apiStatusText').textContent = 'Live & Ready';
    } catch (e) {
        document.getElementById('apiStatusText').textContent = 'Connection Issue';
    }
}

// Init
document.addEventListener('DOMContentLoaded', () => {
    loadDashboard();
    updateStatus();
});
