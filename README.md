# ScamGuard - SIT305 Task 8.2 HD Prototype

ScamGuard is an Android prototype that helps users detect scam messages, emails, and links.
It combines:

- On-device heuristic scam scoring (always available, offline).
- Optional Llama2 explanation using an Ollama server (`llama2`) for prompt -> response AI output.
- A local backend API server running inside the app on `127.0.0.1:8787`.

## Requirements Coverage (Task 8.2)

- **User-facing LLM capability:** contextual classification + explanation generation.
- **Prompt -> response pipeline:** implemented in the analyzer screen (`AnalyzeFragment`).
- **Separate backend layer:** frontend calls local API endpoint `/api/analyze` instead of direct in-UI logic.
- **Privacy statement:** shown in app UI (what stays on device vs what leaves device).
- **Basic safety handling:** sensitive requests are refused with a warning message.
- **Modern target SDK:** `compileSdk 36.1`, `targetSdk 36`.
- **Navigation compatibility:** AndroidX Navigation + app bar navigation (predictive back compatible flow).

## Project Setup

1. Open in Android Studio (stable channel).
2. Sync Gradle.
3. Run on:
   - API 35 emulator/device
   - API 36 emulator/device

## Llama2 Integration (Ollama Hybrid Mode)

The app always runs local analysis through the local backend API.  
If **Use Llama2 (Ollama server)** is enabled, the local backend calls:

- Endpoint: `http://10.0.2.2:11434/api/generate`
- Model: `llama2`

### Start Ollama

On your computer:

```bash
ollama run llama2
```

For Android emulator networking, `10.0.2.2` maps to your host machine localhost.

## Local Backend API

ScamGuard starts an in-app backend server when the app launches:

- `GET /api/health`
- `POST /api/analyze`

Request JSON:

```json
{
  "content": "Suspicious message text here",
  "useLlama": true
}
```

Response JSON:

```json
{
  "riskLevel": "Suspicious",
  "reasoning": "Urgency language detected. External link detected.",
  "llmResponse": "..."
}
```

## Remote Sync (MongoDb access)

hosync endpoints:

- `POST /api/sync/user`
- `POST /api/sync/history`



