# AiChat - Task 8.1 LLM ChatBot

This Android app implements the Credit Task 8.1 requirements:

- Username login screen
- Chat interface after login
- LLM chatbot integration
- Message timestamps on each chat bubble
- Persistent chat history using Room (SQLite)

## Tech Stack

- Language: Java
- UI: XML + ViewBinding + RecyclerView
- Database: Room 
- Architecture: Android App -> Backend API -> LLM API (OpenAI)
- Networking: OkHttp

## Setup

1. Open the project in Android Studio.
2. Configure your backend base URL in `~/.gradle/gradle.properties`:

```
BACKEND_BASE_URL=http://10.0.2.2:8080
```

3. Build and run on emulator/device.

## Backend Setup (Required)

1. Go to backend folder:

```bash
cd backend
```

2. Install dependencies:

```bash
npm install
```

3. Create/update root `.env` in project root:

```env
OPENAI_API_KEY=your_openai_api_key
OPENAI_MODEL=gpt-4o-mini
PORT=8080
```

4. Start backend server:

```bash
npm start
```

5. Verify backend health:

```bash
curl http://localhost:8080/health
```

Your backend should expose:

- `POST /chat`
- Request body:

```json
{
  "username": "student",
  "message": "Hello"
}
```

- Response body:

```json
{
  "reply": "Hi! How can I help?"
}
```

## How It Works

- User enters username on login screen.
- After login, chat UI is shown.
- Username session is stored locally in `SharedPreferences` file `user_session_prefs`.
- Sent and received messages are stored in Room database table `chat_messages`.
- Existing chat messages are loaded on startup.
- Each message bubble shows its timestamp.

## Project Structure

- `app/src/main/java/com/example/aichat/MainActivity.java` - NavHost activity
- `app/src/main/java/com/example/aichat/FirstFragment.java` - login screen
- `app/src/main/java/com/example/aichat/SecondFragment.java` - chat screen
- `app/src/main/java/com/example/aichat/data/` - Room Entity, DAO, Database
- `app/src/main/java/com/example/aichat/network/ChatbotService.java` - backend API calls
- `app/src/main/java/com/example/aichat/ui/ChatAdapter.java` - RecyclerView adapter
- `app/src/main/res/layout/` - login/chat and message item layouts
- `app/src/main/res/navigation/nav_graph.xml` - fragment navigation flow
- `backend/src/server.js` - Express backend + OpenAI integration
