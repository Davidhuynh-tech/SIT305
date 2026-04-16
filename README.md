# iStream - Personal Video Playlist App

Android app for SIT305 Task 5.1C (Subtask 2).  
iStream allows users to sign up, log in, play YouTube videos from URL input, and save personal playlists locally.

## Features

- User authentication with local Room database
  - Sign Up: full name, username, password, confirm password
  - Login: username and password validation
- Input validation on sign up
  - Username must be at least 2 characters
  - Password must include at least 1 uppercase letter and 1 number
  - Confirm password must match
- Session persistence using SharedPreferences
  - Logged-in user remains signed in until logout
- Home screen video playback
  - Paste YouTube URL and play via iFrame embed in WebView
  - Invalid URL handling with user feedback
- Playlist support per user
  - Add current video URL to playlist
  - View playlist URLs in RecyclerView
  - Click playlist item to load video back on home screen
- Logout from Home and Playlist screens

## Tech Stack

- Language: Java
- UI: XML layouts + Fragments
- Architecture: Single Activity + Fragment transactions managed by `MainActivity`
- Local storage: Room (`User`, `PlaylistItem`)
- Session: SharedPreferences (`SessionManager`)
- Media: YouTube iFrame embed rendered in WebView

## Project Structure

- `app/src/main/java/com/example/istream/MainActivity.java`  
  Hosts and manages fragment navigation.
- `app/src/main/java/com/example/istream/LoginFragment.java`  
  Login screen and credential check.
- `app/src/main/java/com/example/istream/SignupFragment.java`  
  Sign-up form and validation rules.
- `app/src/main/java/com/example/istream/HomeFragment.java`  
  YouTube URL input, play, add to playlist, logout.
- `app/src/main/java/com/example/istream/PlaylistFragment.java`  
  Lists saved URLs for current user and supports item selection.
- `app/src/main/java/com/example/istream/data/`  
  Room entities, DAOs, database singleton, session helper.
- `app/src/main/java/com/example/istream/util/YoutubeUtils.java`  
  YouTube URL parsing and embed HTML helper.

## Database Design

### `User`
- `id` (PK, auto-generated)
- `full_name`
- `username` (unique)
- `password`

### `PlaylistItem`
- `id` (PK, auto-generated)
- `user_id` (FK -> `User.id`)
- `video_url`

This ensures each user's playlist is isolated and not visible to other users.

## How to Run

1. Clone this repository.
2. Open the project in Android Studio.
3. Let Gradle sync dependencies.
4. Run the app on emulator or physical device (internet required for video playback).

## Usage Flow

1. Create an account on Sign Up screen.
2. Log in with your credentials.
3. Paste a YouTube URL on Home and tap **Play**.
4. Tap **Add to Playlist** to save URL.
5. Open **My Playlist** and tap any URL to load it back on Home.
6. Tap **Logout** to clear session.

## Notes
- Some YouTube videos may block embedded playback depending on owner restrictions.
- Internet permission is required and already included in `AndroidManifest.xml`.


