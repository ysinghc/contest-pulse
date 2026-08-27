# ACM ContestPulse — IIITU ACM Student Chapter

<p align="center">
  <img src="app/src/main/res/drawable/ic_acm_logo.png" alt="IIITU ACM Logo" width="160"/>
</p>

<p align="center">
  <b>Official Competitive Programming Contest Tracker & Alarm Android App</b><br/>
  <i>Developed under IIITU ACM Student Chapter (Indian Institute of Information Technology Una)</i>
</p>

<p align="center">
  <a href="#features"><img src="https://img.shields.io/badge/Platform-Android_8.0+-0085C7?style=for-the-badge&logo=android" alt="Android"/></a>
  <a href="#architecture"><img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin" alt="Kotlin"/></a>
  <a href="#architecture"><img src="https://img.shields.io/badge/Database-Room_SQLite-0085C7?style=for-the-badge&logo=sqlite" alt="Room SQLite"/></a>
  <a href="#building--running"><img src="https://img.shields.io/badge/Build-Gradle_8.7-02303A?style=for-the-badge&logo=gradle" alt="Gradle"/></a>
</p>

---

## Features

- **Multi-Platform Live Tracking**: Auto-fetches live upcoming contests from Codeforces, LeetCode, and CodeChef.
- **Monday-First Weekly Overview**: Displays upcoming contests mapped to the current week starting on Monday.
- **Offline-First SQLite Storage**: All fetched and custom contest data is stored locally in Room SQLite (`ContestDatabase`), allowing full offline access when disconnected from the internet.
- **Native Homescreen App Widget**:
  - Weekly overview widget layout (`IIITU ACM Contests`).
  - **Manual Refresh Button**: Interactive widget button to trigger instant API polling and SQLite DB update directly from the phone homescreen.
- **15-Minute Loud Emergency Alarm**:
  - `AlarmManager` sets exact alarms 15 minutes before contest start times.
  - Automatically boosts `AudioManager.STREAM_ALARM` to maximum volume, loops alarm ringtone audio, and triggers continuous high-intensity vibration patterns.
  - Full-screen emergency alarm screen (`LoudAlarmActivity`) over lockscreen with countdown, Snooze (5 min), and Dismiss options.
- **Custom Weekly Reminders**: Easily set custom recurring weekly contest schedules (Mon–Sun, time picker, title) saved in SQLite DB.
- **Instant Alarm Test Mode**: Built-in toolbar button to instantly test volume gain, ringtone synthesizer, vibration, and full-screen alarm screen.

---

## Architecture & Technology Stack

```
org.iiitu.acm.contestpulse/
├── data/
│   ├── local/          # Room SQLite Database (ContestDao, ContestDatabase)
│   ├── model/          # Contest Entity Data Model
│   └── repository/     # ContestRepository (Sync manager & Alarm coordinator)
├── network/            # Codeforces, LeetCode, and CodeChef API fetchers
├── worker/             # WorkManager for weekly background polling & widget sync
├── alarm/              # AlarmScheduler, AlarmReceiver, LoudAlarmService, LoudAlarmActivity
├── widget/             # AppWidgetProvider & WidgetRefreshReceiver
└── ui/                 # MainActivity, ContestAdapter, CustomScheduleDialog, AboutAcmDialog
```

- **Language**: Kotlin 1.9
- **Build System**: Android Gradle Plugin 8.4.1 (Java 17/21)
- **UI Components**: Material 3, ViewBinding, SwipeRefreshLayout, RecyclerView
- **Asynchronous Operations**: Kotlin Coroutines & Flow
- **Background Operations**: WorkManager & Foreground Services

---

## Building & Running Locally

### Prerequisites
- JDK 17 or JDK 21
- Android SDK 35 (Build Tools 34.0.0+)

### Step 1: Clone Repository
```bash
git clone https://github.com/IIITU-ACM/ACM-ContestPulse.git
cd ACM-ContestPulse
```

### Step 2: Configure `local.properties`
Create `local.properties` in the root directory:
```properties
sdk.dir=/path/to/your/Android/Sdk
```

### Step 3: Compile Debug & Release APKs
```bash
JAVA_HOME=/path/to/jdk-21 gradle assembleDebug assembleRelease
```

Generated APKs will be located at:
- **Debug APK**: `app/build/outputs/apk/debug/IIITU-ACM-ContestPulse-v1.0-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/IIITU-ACM-ContestPulse-v1.0-release.apk`

---

## Credits & Organization

Built by **IIITU ACM Student Chapter**  
*Indian Institute of Information Technology Una, Himachal Pradesh, India*

- **Website**: [acmiiitu.in](https://acmiiitu.in)