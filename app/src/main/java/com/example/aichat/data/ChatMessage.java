package com.example.aichat.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessage {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String username;
    public String content;
    public boolean isUserMessage;
    public long timestamp;

    public ChatMessage(String username, String content, boolean isUserMessage, long timestamp) {
        this.username = username;
        this.content = content;
        this.isUserMessage = isUserMessage;
        this.timestamp = timestamp;
    }
}
