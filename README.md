# LostAndFound Android App

A fragment-based Android app built for the SIT305 Lost and Found task.  
Users can post lost/found items, upload an image, filter posts by category, and remove posts after items are recovered.

## Features

- Home screen with navigation to:
  - `Create a New Advert`
  - `Show All Lost & Found Items`
- Add post flow:
  - Post type: Lost or Found
  - Required fields: Name, Phone, Date & Time, Location
  - Optional field: Description
  - Category selection
  - Required image upload from device storage
- Date/time handling:
  - Date picker then time picker
  - Past date/time is rejected (future time only)
- Phone validation:
  - Exactly 10 digits only (`0-9`)
- List screen:
  - Displays saved posts with relative posting time
  - Category filter (All, Electronics, Pets, Wallets, Keys, Documents, Other)
- Remove screen:
  - Shows post details and image
  - Allows deleting a selected post
- Local persistence:
  - Room Database (no `SQLiteOpenHelper`)

## Tech Stack

- Java
- AndroidX Fragments + Navigation Component
- Room Database
- ViewBinding
- Material Components

## Project Structure

- `app/src/main/java/com/example/lostandfound/`
  - `MainActivity.java` (host activity)
  - `HomeFragment.java`
  - `AddItemFragment.java`
  - `ItemListFragment.java`
  - `RemoveItemFragment.java`
- `app/src/main/java/com/example/lostandfound/storage/`
  - `LostFoundItem.java` (`@Entity`)
  - `LostFoundItemDao.java` (`@Dao`)
  - `LostFoundDatabase.java` (`@Database`)
- `app/src/main/res/navigation/nav_graph.xml`

## Navigation Flow

1. Home -> Create a New Advert
2. Home -> Show All Lost & Found Items
3. List item click -> Remove Item screen
4. Save post -> Returns to Home

## How to Run

1. Open project in Android Studio.
2. Let Gradle sync.
3. Start an emulator or connect a physical device.
4. Run the `app` module.

## Testing Checklist

- Create a post with all required fields and image -> post saves.
- Try saving with missing required fields -> validation message appears.
- Try picking a past time -> blocked.
- Enter non-10-digit phone -> blocked.
- Filter list by category -> list updates correctly.
- Open a post from list -> details and image are shown.
- Remove a post -> item disappears from list.

