package com.example.lostandfound.storage;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {LostFoundItem.class}, version = 2, exportSchema = false)
public abstract class LostFoundDatabase extends RoomDatabase {

    private static volatile LostFoundDatabase instance;

    public abstract LostFoundItemDao lostFoundItemDao();

    public static LostFoundDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (LostFoundDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    LostFoundDatabase.class,
                                    "lost_found_room.db"
                            )
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return instance;
    }
}
