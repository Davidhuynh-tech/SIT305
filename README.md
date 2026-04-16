# Android Developer Quiz - SIT305 Task

This project implements the **Android Developer Quiz** app using native Android (**Java**) and Gradle.

## Overview

The app follows a simple quiz flow:

- Enter user name
- Start quiz
- Answer questions
- View result summary

It demonstrates core Android development with Activities, model classes, and local preference storage.

## Implemented Features

- **Main screen (`MainActivity`)**
  - Captures user name
  - Stores name using `SharedPreferences`
  - Navigates to quiz screen

- **Quiz screen (`QuizActivity`)**
  - Displays quiz questions
  - Accepts user answers
  - Calculates score

- **Result screen (`ResultsActivity`)**
  - Displays final score/result to the user

## Tech Stack

- Java
- Android SDK
- Gradle (Kotlin DSL)
- SharedPreferences

## Project Structure

`app/src/main/java/com/example/task_41c_quizapp/`

- `MainActivity.java` - Name input + start quiz
- `QuizActivity.java` - Question display + scoring logic
- `ResultsActivity.java` - Final result UI
- `Question.java` - Quiz data model

## Requirements

- Android Studio (compatible with AGP `9.0.1`)
- JDK 11
- Android SDK:
  - `compileSdk` 36
  - `targetSdk` 36
  - `minSdk` 24

## How to Run

1. Open the project in Android Studio.
2. Wait for Gradle sync to complete.
3. Select an emulator/device (API 24+).
4. Click **Run** (`Shift+F10`).
