package com.example.istream.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlaylistDao {
    @Insert
    void insert(PlaylistItem item);

    @Query("SELECT * FROM playlist_items WHERE user_id = :userId ORDER BY id DESC")
    List<PlaylistItem> getPlaylistForUser(int userId);
}
