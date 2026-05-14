package com.example.lostandfound.storage;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LostFoundItemDao {

    @Insert
    long insertItem(LostFoundItem item);

    @Query("SELECT * FROM items ORDER BY createdAt DESC")
    List<LostFoundItem> getAllItems();

    @Query("SELECT * FROM items WHERE category = :category ORDER BY createdAt DESC")
    List<LostFoundItem> getItemsByCategory(String category);

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    LostFoundItem getItemById(long id);

    @Query("DELETE FROM items WHERE id = :id")
    int deleteItemById(long id);
}
