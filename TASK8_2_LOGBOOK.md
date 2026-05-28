# Task 8.2 Logbook (12/05/2026 - 26/05/2026)

Student: David Huynh  
Project: ScamGuard  
Unit: SIT305 Task 8.2 HD

---

## 12/05/2026

### Goals
- Confirm scope from approved proposal.
- Define MVP features for scam detection workflow.

### Work Completed
- Re-read proposal and converted it into implementation checklist.
- Identified core flow: input -> analysis -> risk + reasoning + action.

### Challenges
- Need to balance proposal goals (privacy, offline support, AI reasoning) with practical prototype constraints.

### Decisions and Trade-offs
- Chose hybrid architecture first for faster implementation and easier Llama demonstration.
- Kept offline heuristic logic as fallback to preserve usability.

### Evidence
- Planning notes created for architecture and feature scope.

### Next Steps
- Set up Android project baseline and initial UI structure.

---

## 14/05/2026

### Goals
- Build first functional Android prototype screen.

### Work Completed
- Created analyzer-first UI structure in Android app.
- Defined display areas for risk level, reasoning, and AI response.

### Challenges
- Default template UI was not aligned with proposal design.

### Decisions and Trade-offs
- Replaced template interaction flow with custom analysis flow.
- Prioritized clarity and simple navigation over feature-heavy UI.

### Evidence
- Initial analyzer screen build completed.

### Next Steps
- Implement local scam pattern analysis logic.

---

## 16/05/2026

### Goals
- Implement local rule-based scam detection.

### Work Completed
- Added heuristic pattern detection engine for suspicious language and links.
- Added initial risk categorization logic.

### Challenges
- Needed stable rule thresholds that avoid over-flagging normal messages.

### Decisions and Trade-offs
- Started with weighted keyword groups to keep logic explainable for presentation.

### Evidence
- `AnalysisEngine` baseline logic implemented.

### Next Steps
- Add Llama integration and backend API layer.

---

## 18/05/2026

### Goals
- Add Llama2 integration and backend request pipeline.

### Work Completed
- Added Llama request flow via Ollama endpoint.
- Added local backend API endpoint pattern (`/api/analyze`) for clean separation from UI.

### Challenges
- Android networking and endpoint reliability issues.

### Decisions and Trade-offs
- Used local backend + retry/fallback strategy to keep results available even when Llama is slow/unavailable.

### Evidence
- Prompt -> response pipeline connected end-to-end.

### Next Steps
- Improve safety and privacy handling in UI and backend.

---

## 20/05/2026

### Goals
- Align implementation with Task 8.2 rubric.

### Work Completed
- Updated app structure to satisfy required user-facing AI integration.
- Added README with setup, compatibility, and demo guidance.
- Created checklist and weekly progress template files.

### Challenges
- Ensuring rubric wording and implementation details stayed consistent.

### Decisions and Trade-offs
- Chose explicit documentation for API level testing and back-navigation compatibility evidence.

### Evidence
- `README.md`, `TASK8_2_CHECKLIST.md`, and progress template created.

### Next Steps
- Run build/lint checks and fix issues.

---

## 21/05/2026

### Goals
- Stabilize build and resource integrity.

### Work Completed
- Fixed resource linking error and re-ran build/test/lint checks.
- Confirmed successful `assembleDebug`, unit tests, and lint.

### Challenges
- Environment setup issue (`JAVA_HOME`) and missing resource reference.

### Decisions and Trade-offs
- Used Android Studio JBR path for consistent local builds.

### Evidence
- Clean local build pipeline validated.

### Next Steps
- Implement user account and history features.

---

## 22/05/2026

### Goals
- Add user login/register and history persistence.

### Work Completed
- Implemented local auth manager (register/login/logout).
- Implemented per-user history storage and history screen.
- Linked clear-history action to account-specific data.

### Challenges
- Needed to keep analysis available to guests while only persisting history for logged-in users.

### Decisions and Trade-offs
- Analysis allowed without login; login only required for persistence and account history.

### Evidence
- Auth and history flow functioning in app.

### Next Steps
- Refine UI according to wireframe/mockups.

---

## 23/05/2026

### Goals
- Match UI closely to design sketches and improve interaction flow.

### Work Completed
- Redesigned layout states: start, input, and result.
- Adjusted spacing, button positioning, and panel appearance.
- Replaced top-right control with profile icon and moved auth actions into profile dialog.

### Challenges
- Responsive behavior: button position and content visibility across state changes.

### Decisions and Trade-offs
- Used state-specific visibility and fixed spacers to keep visual consistency.

### Evidence
- Updated `AnalyzeFragment` state handling and corresponding XML layout.

### Next Steps
- Improve scoring consistency and risk/action mapping.

---

## 24/05/2026

### Goals
- Improve scoring consistency and backend reliability.

### Work Completed
- Fixed risk label normalization and score mapping issues.
- Updated risk score scale:
  - 90: This is a Scam
  - 65: Likely a Scam
  - 45: Suspicious
  - 30: Should keep on Monitor
  - 20: Safe
- Added retry + fallback logic so analysis still succeeds if local API call fails.
- Added `scamScore` to saved history and display cards.

### Challenges
- Inconsistent score when risk labels differed by wording.

### Decisions and Trade-offs
- Added normalization layer to map label variants to canonical risk categories.

### Evidence
- Updated `AnalyzeFragment`, `AnalysisEngine`, and `HistoryStore`.

### Next Steps
- Finalize secure data sync architecture (no direct DB access from app).

---

## 25/05/2026

### Goals
- Connect app data persistence to backend service and MongoDB.

### Work Completed
- Built Node/Express backend API (`backend-api`) with endpoints:
  - `/api/health`
  - `/api/sync/user`
  - `/api/sync/history`
  - `/api/sync/history/clear`
- Configured backend to create collections if absent:
  - `Users`
  - `Histories`
- Updated mobile app sync model to remove direct DB access.

### Challenges
- Android compatibility issues when attempting direct Mongo driver integration.

### Decisions and Trade-offs
- Removed direct Mongo access from mobile for security and architecture correctness.
- Adopted backend bridge pattern (mobile -> backend API -> MongoDB).

### Evidence
- `backend-api` folder created and validated with local startup and API tests.

### Next Steps
- Validate end-to-end sync and finalize presentation content.

---

## 26/05/2026

### Goals
- Final validation and presentation preparation.

### Work Completed
- Verified app build stability after latest architecture changes.
- Confirmed clear-history flow also deletes account-associated remote history.
- Prepared presentation script aligned to Task 8.2 sections (A/B/C/D + compatibility slide).

### Challenges
- Ensure rubric language matches implemented architecture (hybrid Llama + offline local fallback).

### Decisions and Trade-offs
- Clearly documented privacy/data movement:
  - Local heuristics stay on device.
  - Llama requests and optional sync go through configured endpoints.

### Evidence
- Updated docs and demonstration flow ready for recording.

### Next Steps
- Record 10-minute presentation and app demo video.
- Capture API 35 and API 36 test evidence screenshots.

---

## Summary of Progress

- Functional Android prototype implemented.
- Llama-enhanced analysis integrated (hybrid mode).
- Offline-capable local analysis and fallback behavior implemented.
- Auth + per-user history completed.
- Secure architecture direction improved: no direct mobile DB access; backend API bridge used.
- Project now in final submission and presentation stage.
