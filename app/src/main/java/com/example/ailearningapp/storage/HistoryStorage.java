package com.example.ailearningapp.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.ailearningapp.model.HistoryEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HistoryStorage {
    private static final String PREFS_NAME = "learning_history_prefs";
    private static final String KEY_PREFIX = "history_";
    private final SharedPreferences sharedPreferences;

    public HistoryStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void appendHistory(String email, HistoryEntry entry) {
        List<HistoryEntry> entries = getHistory(email);
        entries.add(0, entry);
        if (entries.size() > 30) {
            entries = entries.subList(0, 30);
        }
        persist(email, entries);
    }

    public List<HistoryEntry> getHistory(String email) {
        List<HistoryEntry> entries = new ArrayList<>();
        String raw = sharedPreferences.getString(key(email), "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                HistoryEntry entry = new HistoryEntry();
                entry.timestamp = object.optString("timestamp", "");
                entry.taskPrompt = object.optString("taskPrompt", "");
                entry.totalQuestions = object.optInt("totalQuestions", 0);
                entry.correctCount = object.optInt("correctCount", 0);
                entry.topicSummary = object.optString("topicSummary", "");
                entries.add(entry);
            }
        } catch (JSONException ignored) {
        }
        return entries;
    }

    private void persist(String email, List<HistoryEntry> entries) {
        JSONArray array = new JSONArray();
        for (HistoryEntry entry : entries) {
            JSONObject object = new JSONObject();
            try {
                object.put("timestamp", entry.timestamp);
                object.put("taskPrompt", entry.taskPrompt);
                object.put("totalQuestions", entry.totalQuestions);
                object.put("correctCount", entry.correctCount);
                object.put("topicSummary", entry.topicSummary);
                array.put(object);
            } catch (JSONException ignored) {
            }
        }
        sharedPreferences.edit().putString(key(email), array.toString()).apply();
    }

    private String key(String email) {
        return KEY_PREFIX + (email == null ? "guest" : email.toLowerCase().trim());
    }
}
