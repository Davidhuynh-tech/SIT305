# MediaContentApp - Sports News Feed (SIT305 Task 5.1C)

This project currently implements **Subtask 1: Sports News Feed App** from SIT305 Task 5.1C.

## Overview

The app is built with a **Single Activity Architecture** and uses **Fragments** for navigation.
It demonstrates:

- Horizontal and vertical `RecyclerView` lists
- Detail screen with related stories
- Search/filter by sport category
- Local bookmarks storage

## Implemented Features

- **Home screen (`HomeFragment`)**
  - Horizontal list: Featured Matches
  - Vertical list: Latest Sports News
  - Search bar for filtering by sport/title
  - Button to open bookmarks
W
- **Detail screen (`NewsDetailFragment`)**
  - Story image, title, descriptionW
  - Related stories list
  - Bookmark/Remove Bookmark toggle

- **Bookmarks screen (`BookmarksFragment`)**
  - Displays saved stories
  - Tap a bookmarked item to open its detail screen

## Tech Stack

- Java
- Android Fragments
- RecyclerView
- Material Components
- SharedPreferences (for bookmark persistence)

## Project Structure

`app/src/main/java/com/example/mediacontentapp/`

- `MainActivity.java` - Fragment host and navigation
- `sports/model/NewsItem.java` - Data model
- `sports/data/SportsNewsRepository.java` - Dummy sports data source
- `sports/data/BookmarkStore.java` - Local bookmark storage
- `sports/ui/HomeFragment.java` - Home UI + filtering
- `sports/ui/NewsDetailFragment.java` - News detail + related list
- `sports/ui/BookmarksFragment.java` - Bookmarked news UI
- `sports/ui/adapter/NewsAdapter.java` - RecyclerView adapter

## Dummy Data

The app uses the provided 8-record sports dataset:

- Football, Basketball, Cricket entries
- Match details include date, opponent, score, venue, and status

## How to Run

1. Open the project in Android Studio.
2. Let Gradle sync complete.
3. Run on emulator/device (API 24+).

