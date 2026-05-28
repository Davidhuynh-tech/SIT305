# ScamGuard Backend API

This backend receives sync data from the Android app and writes it to MongoDB.

Collections used:

- `Users`
- `Histories`

If these collections do not exist, the backend creates them on startup.

## Endpoints

- `GET /api/health`
- `POST /api/sync/user`
- `POST /api/sync/history`
- `POST /api/sync/history/clear`

## Setup

1. Copy `.env.example` to `.env`
2. Fill in your MongoDB settings in `.env`
3. Install dependencies:
   - `npm install`
4. Start server:
   - `npm run dev`

## Example payloads

`POST /api/sync/user`

```json
{
  "username": "david",
  "passwordHash": "..."
}
```

`POST /api/sync/history`

```json
{
  "username": "david",
  "message": "Please transfer now...",
  "riskLevel": "Likely a Scam",
  "scamScore": 65,
  "reasoning": "Urgency language detected.",
  "suggestedAction": "Verify through official channels."
}
```

## Connect from Android app

Set your sync base URL in app to this backend host.

- Emulator + local backend: `http://10.0.2.2:3000`
- Physical phone + local backend on same Wi-Fi: `http://<your-lan-ip>:3000`
