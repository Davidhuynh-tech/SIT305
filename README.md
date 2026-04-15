# SIT305 — Android Developer Quiz

Native Android app (**Java**) built with Gradle. The launcher experience is the **Task 4.1C Quiz** flow: enter a name, take a short quiz, then view results.

## Requirements

- **Android Studio** (recommended: version aligned with AGP **9.0.1** in this repo)
- **JDK 11** (matches `compileOptions` in `app/build.gradle.kts`)
- **Android SDK**: `compileSdk` / `targetSdk` **36**, `minSdk` **24**

## Open and run

1. Open the project folder in Android Studio (`File → Open` → select this directory).
2. Let Gradle sync finish.
3. Choose a device or emulator (API 24+).
4. Click **Run** (or **Shift+F10**).

The app entry point declared in `AndroidManifest.xml` is `com.example.task_41c_quizapp.MainActivity`.

## What’s in the app (quiz)

| File | Role |
|------|------|
| `MainActivity.java` | Collects the user’s name (saved in `SharedPreferences`) and starts the quiz. |
| `QuizActivity.java` | Presents questions and records the score. |
| `ResultsActivity.java` | Shows the outcome after the quiz. |
| `Question.java` | Model for quiz items. |

**Package:** `com.example.task_41c_quizapp`  
**App label:** **Android Developer Quiz** (`res/values/strings.xml`).

## What’s in the app (Task 4P — events)

Event-related code lives under `app/src/main/java/Task_4P/` as **Java** (`.java`) sources. It targets a separate **events** feature (Room entity, list UI, navigation shell). It is **not** the launcher flow in the current manifest unless you point `AndroidManifest.xml` at `Task_4P.com.MainActivity` and align Gradle resources/deps.

| File | Role |
|------|------|
| `Task_4P/com/MainActivity.java` | Host activity: `NavHostFragment` + bottom navigation wired to the nav graph. |
| `Task_4P/com/data/Event.java` | Room `@Entity` for stored events (title, category, location, date/time). |
| `Task_4P/com/data/AppDatabase.java` | Room database singleton (`events_db`) exposing the DAO. |
| `Task_4P/com/ui/EventListFragment.java` | Fragment that shows the event list (RecyclerView, dialogs, `EventViewModel`). |
| `Task_4P/com/ui/EventAdapter.java` | `RecyclerView.Adapter` for event rows (edit/delete actions). |

**Package roots:** `Task_4P.com`, `Task_4P.com.data`, `Task_4P.com.ui`.

## Project metadata

| Item | Value |
|------|--------|
| Gradle root name | `Task_4.1C_QuizApp` (`settings.gradle.kts`) |
| Application ID | `com.example.task_41c_quizapp` |
| Version | `1.0` (`versionName`), `versionCode` **1** |

---

*Course / submission: SIT305.*
