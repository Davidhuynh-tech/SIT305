package com.example.lostandfound.storage;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "items")
public class LostFoundItem {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @NonNull
    private String postType;
    @NonNull
    private String name;
    @NonNull
    private String phone;
    @NonNull
    private String description;
    @NonNull
    private String date;
    @NonNull
    private String category;
    @NonNull
    private String imageUri;
    @NonNull
    private String location;
    private long createdAt;

    public LostFoundItem(
            @NonNull String postType,
            @NonNull String name,
            @NonNull String phone,
            @NonNull String description,
            @NonNull String date,
            @NonNull String category,
            @NonNull String imageUri,
            @NonNull String location,
            long createdAt
    ) {
        this.postType = postType;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.date = date;
        this.category = category;
        this.imageUri = imageUri;
        this.location = location;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getPostType() {
        return postType;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getPhone() {
        return phone;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    @NonNull
    public String getImageUri() {
        return imageUri;
    }

    @NonNull
    public String getLocation() {
        return location;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
