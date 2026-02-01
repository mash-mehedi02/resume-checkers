# Application Run করুন - 3 Simple Steps

## VS Code এ Run করুন (সবচেয়ে সহজ)

### Step 1: Java Extension Install করুন
1. VS Code → Extensions (Ctrl+Shift+X)
2. "Java Extension Pack" search করুন
3. Install করুন (Microsoft এর official)

### Step 2: Application Run করুন
1. `ResumeScreenerApplication.java` file open করুন
2. File এর উপরে **"Run"** button (▶️) click করুন
3. VS Code automatically সব কিছু handle করবে

### Step 3: Check করুন
- Browser এ যান: `http://localhost:8080/api/jobs`
- বা Postman এ test করুন

---

## Alternative: Maven Wrapper Use করুন

PowerShell এ:
```powershell
cd "D:\Programming\Project\Resume Checkers by JAVA"
.\mvnw.cmd spring-boot:run
```

(প্রথমবার Maven download হবে, তারপর run হবে)

---

## Quick Fix

VS Code Java Extension সবচেয়ে সহজ - install করুন এবং Run button click করুন!
