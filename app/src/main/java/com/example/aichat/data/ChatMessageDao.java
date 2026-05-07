package com.example.aichat.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatMessageDao {
    @Insert
    long insert(ChatMessage message);

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC, id ASC")
    List<ChatMessage> getAllMessages();
}
