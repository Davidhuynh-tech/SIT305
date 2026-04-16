package com.example.mediacontentapp.sports.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BookmarkStore {
    private static final String PREFS_NAME = "sports_bookmarks";
    private static final String KEY_IDS = "bookmark_ids";

    private final SharedPreferences preferences;

    public BookmarkStore(@NonNull Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void toggleBookmark(int newsId) {
        Set<String> ids = new HashSet<>(getBookmarks());
        String key = String.valueOf(newsId);
        if (ids.contains(key)) {
            ids.remove(key);
        } else {
            ids.add(key);
        }
        preferences.edit().putStringSet(KEY_IDS, ids).apply();
    }

    public boolean isBookmarked(int newsId) {
        return getBookmarks().contains(String.valueOf(newsId));
    }

    @NonNull
    public Set<String> getBookmarks() {
        Set<String> saved = preferences.getStringSet(KEY_IDS, Collections.emptySet());
        return saved == null ? Collections.emptySet() : saved;
    }
}
