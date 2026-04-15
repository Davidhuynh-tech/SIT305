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

| Piece | Role |
|--------|------|
| `MainActivity` | Collects the user’s name (saved in `SharedPreferences`) and starts the quiz. |
| `QuizActivity` | Presents questions and records the score. |
| `ResultsActivity` | Shows the outcome after the quiz. |
| `Question` | Model for quiz items. |

Package: `com.example.task_41c_quizapp`  
App label (launcher name): **Android Developer Quiz** (`res/values/strings.xml`).

## Other source in this repo

Under `app/src/main/java/Task_4P/` there is additional **event-related** Java (e.g. `Event`, UI pieces). That code uses a **different package** from the quiz app’s `applicationId` / manifest launcher. Treat it as a separate task or work-in-progress unless you wire it into the manifest, Gradle dependencies, and navigation yourself.

## Tests

- Unit tests: `app/src/test/java/`
- Instrumented tests: `app/src/androidTest/java/`

Run tests from Android Studio (**Run tests** on a package/class) or with Gradle test tasks for the `app` module.

## Project metadata

| Item | Value |
|------|--------|
| Gradle root name | `Task_4.1C_QuizApp` (`settings.gradle.kts`) |
| Application ID | `com.example.task_41c_quizapp` |
| Version | `1.0` (`versionName`), `versionCode` **1** |

---

*Course / submission: SIT305.*
