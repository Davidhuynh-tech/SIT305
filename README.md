# AiLearningApp - SIT305 6.1D

Android app for a "LLM-Enhanced Learning Assistant" flow:

- Login
- Register
- Interests
- Home
- Generated Task (quiz)
- Results

The app is built so **LLM calls happen only on the backend** (never directly from Android).

## Tech Stack

- Java (Android)
- Navigation Component (single-activity, multi-fragment)
- ViewBinding
- Retrofit + Gson + OkHttp Logging

## Project Structure (Key Files)

- `app/src/main/java/com/example/ailearningapp/MainActivity.java` - host activity containing the nav host fragment
- `app/src/main/res/navigation/nav_graph.xml` - app navigation flow
- `app/src/main/java/com/example/ailearningapp/network/` - backend API client/service
- `app/src/main/java/com/example/ailearningapp/repository/LearningRepository.java` - data/retry/fallback logic
- `app/src/main/java/com/example/ailearningapp/state/SessionStore.java` - lightweight in-memory session state
- `app/src/main/res/layout/fragment_*.xml` - all UI scenes
- `backend/server.js` - Express backend with OpenAI (ChatGPT) integration

## Backend API Contract

Base URL is currently configured in:

- `app/build.gradle.kts`
- `BuildConfig.BACKEND_BASE_URL = "http://10.0.2.2:8080/"`

`10.0.2.2` is Android Emulator's alias to your local machine (`localhost`).

### 1) Generate Task

- `POST /api/learning/generate-task`
- Request:

```json
{
  "studentName": "Alex",
  "interests": ["Algorithms", "Testing"]
}
```

- Response:

```json
{
  "prompt": "Create two MCQs for Algorithms and Testing.",
  "questions": [
    {
      "id": "q1",
      "question": "What is a stack?",
      "options": ["...", "...", "...", "..."],
      "correctIndex": 1
    }
  ]
}
```

### 2) Generate Hint

- `POST /api/learning/generate-hint`
- Request:

```json
{
  "question": "What is a stack?",
  "selectedAnswer": "A FIFO structure"
}
```

- Response:

```json
{
  "prompt": "Give a short hint without revealing answer.",
  "response": "Think about insertion and removal at the same end."
}
```

### 3) Explain Answers

- `POST /api/learning/explain-answers`
- Request:

```json
{
  "answers": [
    {
      "questionId": "q1",
      "questionText": "What is a stack?",
      "selectedAnswer": "A FIFO structure",
      "correctAnswer": "A LIFO structure"
    }
  ]
}
```

- Response:

```json
{
  "prompt": "Explain why each answer is correct or incorrect.",
  "explanations": [
    {
      "question": "What is a stack?",
      "selectedAnswer": "A FIFO structure",
      "explanation": "Stacks are LIFO, not FIFO.",
      "correct": false
    }
  ]
}
```

## Prerequisites

- Android Studio (latest stable)
- Android SDK installed
- JDK 11+ configured
- Node.js 18+ installed
- OpenAI API key
- Running backend server exposing the 3 endpoints above

## Backend Setup (ChatGPT Provider)

This project now includes a backend provider using **OpenAI (ChatGPT)**.

1. Go to backend folder:

```powershell
cd backend
```

2. Copy `.env.example` to `.env` and set your key:

```powershell
copy .env.example .env
```

Edit `.env`:

```env
OPENAI_API_KEY=your_real_openai_key
OPENAI_MODEL=gpt-4o-mini
PORT=8080
```

3. Start backend:

```powershell
npm install
npm start
```

4. Verify backend is live:
- Open [http://localhost:8080/health](http://localhost:8080/health)
- Expected JSON includes:
  - `"provider": "openai"`
  - `"model": "gpt-4o-mini"` (or your chosen model)

## How To Run

1. Open project in Android Studio.
2. Ensure JDK is configured:
   - Android Studio: `File -> Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> Gradle JDK`
3. Sync Gradle.
4. Start backend server (`cd backend && npm start`) on port `8080` (or update base URL).
5. Run app on emulator/device.

### If Gradle says `JAVA_HOME is not set`

On Windows PowerShell, set it (example path):

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

Then run:

```powershell
.\gradlew.bat assembleDebug
```

## How To Test (Manual Test Plan)

### A) Navigation and Screens

1. Launch app -> verify **Login** screen appears.
2. Tap `Need an Account?` -> verify **Register**.
3. Tap `Create new Account` -> verify **Interests**.
4. Select interests and tap `Next` -> verify **Home**.
5. Tap `Open Task` -> verify **Generated Task** screen.
6. Submit answers -> verify **Results**.
7. Tap `Continue` -> returns to **Home**.

### B) Backend-only LLM behavior

1. Open Logcat and verify network calls hit your backend endpoints.
2. Confirm app never calls OpenAI/Gemini directly from Android code.
3. Confirm backend `/health` reports OpenAI provider and model.
4. In quiz screen:
   - Tap `Generate hint` -> check loading -> prompt+response shown.
5. In results screen:
   - Check loading -> prompt+response explanations shown.

### C) Loading + Failure states (assignment requirement)

1. Start app with backend running -> verify loading states show briefly then content.
2. Stop backend and retry:
   - Quiz task should show error/retry UI (or fallback behavior where implemented).
   - Hint request should show failure message.
   - Result explanations should still show safe fallback explanation text.

### D) Accessibility/basic UX checks

1. Text readable (no tiny text).
2. Buttons have clear labels.
3. Back button works between fragments and no dead-end screens.

## Notes

- For physical devices, `10.0.2.2` will not work. Replace with your PC LAN IP:
  - Example: `http://192.168.1.20:8080/`
- Keep backend LLM key/logic server-side only.

