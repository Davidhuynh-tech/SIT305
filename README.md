# AiLearningApp - SIT305 Tasks 6.1D + 10.1D

Android app for an **LLM-Enhanced Learning Assistant** with backend-only AI, account flow, history, sharing, and upgrade features.

## Features Implemented

- Core learning flow: `Login -> Register -> Interests -> Home -> Quiz -> Results`
- LLM utilities (backend API only):
  - Generate task questions
  - Generate hint for a question
  - Explain answer correctness
- Account features:
  - Signup/login validation
  - Local user persistence
  - Logout
- Task 10.1D upgrades:
  - **Profile Page**
  - **History Page**
  - **Upgrade Account Page**
  - **Share Profile** (public share link)
  - **Purchase Upgrade** (Free -> Pro)
- Public share webpage:
  - `GET /public/:shareId`
  - shows profile + history (email not displayed)

## Architecture

- Android app never calls OpenAI directly.
- Android -> Backend REST API (Retrofit)
- Backend -> OpenAI API
- Backend -> MongoDB Atlas (with in-memory fallback if Mongo is unavailable)

## Tech Stack

- Android (Java, Navigation, ViewBinding)
- Retrofit + Gson + OkHttp logging
- Node.js + Express backend
- OpenAI API (`gpt-4o-mini` default)
- MongoDB Atlas

## Key Paths

- Android nav host/activity: `app/src/main/java/com/example/ailearningapp/MainActivity.java`
- Navigation graph: `app/src/main/res/navigation/nav_graph.xml`
- Android API layer: `app/src/main/java/com/example/ailearningapp/network/`
- Backend server: `backend/server.js`
- Backend env template: `backend/.env.example`

## Backend Setup

### 1) Configure environment

```powershell
cd backend
copy .env.example .env
```

Edit `backend/.env`:

```env
OPENAI_API_KEY=your_openai_api_key
OPENAI_MODEL=gpt-4o-mini
PORT=8080
MONGODB_URI=mongodb+srv://<user>:<password>@<cluster>.mongodb.net/?retryWrites=true&w=majority
MONGODB_DB=ai_learning_app
PUBLIC_BASE_URL=http://localhost:8080
```

### 2) Install dependencies

```powershell
npm install
```

### 3) Start backend

```powershell
npm start
```

### 4) Verify backend

Open [http://localhost:8080/health](http://localhost:8080/health)

Expected keys include:
- `ok: true`
- `provider: "openai"`
- `storage: "mongodb"` (or `"memory"` fallback)

## Android Setup

1. Open project in Android Studio
2. Set Gradle JDK (`File -> Settings -> Build Tools -> Gradle`)
3. Sync project
4. Start backend (`npm start` in `backend`)
5. Run app on emulator/device

The app base URL is configured in:
- `app/build.gradle.kts`
- `BuildConfig.BACKEND_BASE_URL = "http://10.0.2.2:8080/"`

`10.0.2.2` is emulator alias to your machine localhost.

## API Endpoints

### Learning endpoints

- `POST /api/learning/generate-task`
- `POST /api/learning/generate-hint`
- `POST /api/learning/explain-answers`

### User/profile/history/share/purchase endpoints

- `POST /api/users/upsert`
- `GET /api/profile/:email`
- `POST /api/history/append`
- `GET /api/history/:email`
- `POST /api/purchase/upgrade`
- `POST /api/share/create-link`
- `GET /public/:shareId`

## MongoDB Collections

Recommended DB: `ai_learning_app`

Collections used:
- `users`
- `histories`
- `shares`

## Manual Test Plan

### A) Core flow

1. Register account
2. Select interests
3. Open generated task
4. Generate hint
5. Submit answers
6. Review AI explanations

### B) Task 10.1D screens

1. From Home open `Profile`, `History`, `Upgrade`
2. Verify back button on each page
3. Upgrade to Pro and confirm plan updates

### C) Sharing

1. Open Profile -> Share Profile
2. Backend returns share link
3. Open shared URL (`/public/:shareId`)
4. Verify profile + history visible and email hidden

### D) History persistence

1. Complete quiz and view results
2. Open History page
3. Confirm new history entry appears

## Troubleshooting

- `CLEARTEXT not permitted`:
  - already handled using network security config for local dev.
- `No connected devices`:
  - start emulator or connect physical device.
- `bad auth` Mongo:
  - check Atlas DB user/password, project, and IP allowlist.

## Security Note

Keep secrets out of source control:
- `OPENAI_API_KEY`
- `MONGODB_URI`

If credentials were exposed, rotate them in OpenAI/Atlas immediately.

