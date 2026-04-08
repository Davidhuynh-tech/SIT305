# Event Planner (SIT305 Task 4.1P)

Personal **Event Planner** Android app that lets users create, view, update, and delete upcoming events.  
Events are stored locally using **Room** so they persist after the app is closed or the device restarts.

## Features (meets Task 4.1P requirements)

- **Create**: Add an event with **Title**, **Category**, **Location**, and **Date/Time**
- **Read**: Dashboard list of all events, **sorted automatically by date/time**
- **Update**: Edit an existing event and save changes
- **Delete**: Delete with a **confirmation dialog**
- **Persistence**: **Room database** (local storage)
- **Navigation**: **Jetpack Navigation Component** with **Bottom Navigation**
- **Validation & feedback**
  - Title is required (inline field error)
  - Date/time is required and must be in the future
  - Toasts/dialogs for save/update/delete feedback

## Tech Stack

- Language: **Java**
- UI: **Fragments**, Material Components
- Navigation: **Jetpack Navigation Component**
- Local DB: **Room**
- Architecture: basic **Repository + ViewModel + LiveData**

## How to Run

1. Open the project in **Android Studio**
2. Let Gradle sync finish
3. Run on an emulator/device (minimum SDK 24)

## How to Use

- Tap **Add Event** in the bottom navigation
  - Enter Title, Category, Location
  - Tap **Select Date & Time**
  - Tap **Save Event**
- Tap **Events** to view all upcoming events (sorted by date/time)
- Tap **Edit** to modify an event
  - Save will return you to the Events list
  - The top bar shows a back button while editing
- Tap **Delete** to remove an event (confirmation required)

## Project Structure (key files)

### UI / Navigation

- `app/src/main/java/Task_4P/com/MainActivity.java`
  - Hosts toolbar + bottom navigation + nav host
- `app/src/main/res/layout/activity_main.xml`
  - `MaterialToolbar` (top bar), `NavHostFragment`, `BottomNavigationView`
- `app/src/main/res/navigation/nav_graph.xml`
  - Fragment destinations (`EventListFragment`, `AddEditEventFragment`)
- `app/src/main/java/Task_4P/com/ui/EventListFragment.java`
  - Displays events list, edit navigation, delete confirmation
- `app/src/main/java/Task_4P/com/ui/AddEditEventFragment.java`
  - Add/edit form, date/time pickers, validation, save/update logic
- `app/src/main/java/Task_4P/com/ui/EventAdapter.java`
  - RecyclerView adapter for event items

### Room Database (Persistence)

- `app/src/main/java/Task_4P/com/data/Event.java`
  - Room `@Entity` table: `events`
- `app/src/main/java/Task_4P/com/data/EventDao.java`
  - CRUD operations and sorted query
- `app/src/main/java/Task_4P/com/data/AppDatabase.java`
  - Room database singleton (`events_db`)
- `app/src/main/java/Task_4P/com/data/EventRepository.java`
  - Runs DAO operations on a background thread
- `app/src/main/java/Task_4P/com/ui/EventViewModel.java`
  - Exposes `LiveData<List<Event>>` to the UI


