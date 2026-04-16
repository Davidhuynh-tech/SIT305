package com.example.mediacontentapp.sports.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public class NewsItem {
    private final int id;
    private final String title;
    private final String description;
    private final String category;
    private final boolean featured;
    private final int imageResId;

    public NewsItem(int id, @NonNull String title, @NonNull String description,
                    @NonNull String category, boolean featured, @DrawableRes int imageResId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.featured = featured;
        this.imageResId = imageResId;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    public boolean isFeatured() {
        return featured;
    }

    @DrawableRes
    public int getImageResId() {
        return imageResId;
    }
}
