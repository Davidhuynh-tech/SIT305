package com.example.scamguard.backend;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryStore {

    private static final String PREF_NAME = "scamguard_history";

    private final SharedPreferences prefs;
    private final RemoteSyncClient remoteSyncClient;

    public HistoryStore(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.remoteSyncClient = RemoteSyncClient.getInstance(context);
    }

    public synchronized void addEntry(String username, String message, String riskLevel, int scamScore, String reasoning, String suggestedAction) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        try {
            JSONArray entries = getEntriesArray(username);
            JSONObject item = new JSONObject();
            item.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            item.put("message", message);
            item.put("riskLevel", riskLevel);
            item.put("scamScore", scamScore);
            item.put("reasoning", reasoning);
            item.put("suggestedAction", suggestedAction);
            entries.put(item);
            prefs.edit().putString(getUserKey(username), entries.toString()).apply();
            remoteSyncClient.syncHistoryEntry(username, message, riskLevel, scamScore, reasoning, suggestedAction);
        } catch (Exception ignored) {
            // Keep the app resilient even if history serialization fails.
        }
    }

    public synchronized List<HistoryEntry> getEntries(String username) {
        List<HistoryEntry> output = new ArrayList<>();
        if (username == null || username.trim().isEmpty()) {
            return output;
        }
        try {
            JSONArray entries = getEntriesArray(username);
            for (int i = entries.length() - 1; i >= 0; i--) {
                JSONObject item = entries.getJSONObject(i);
                output.add(new HistoryEntry(
                        item.optString("timestamp", ""),
                        item.optString("message", ""),
                        item.optString("riskLevel", ""),
                        item.optInt("scamScore", 0),
                        item.optString("reasoning", ""),
                        item.optString("suggestedAction", "")
                ));
            }
        } catch (Exception ignored) {
            // Return what we have.
        }
        return output;
    }

    public synchronized void clearHistory(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        prefs.edit().remove(getUserKey(username)).apply();
        remoteSyncClient.clearHistoryForUser(username);
    }

    private JSONArray getEntriesArray(String username) {
        try {
            String raw = prefs.getString(getUserKey(username), "[]");
            return new JSONArray(raw == null ? "[]" : raw);
        } catch (Exception exception) {
            return new JSONArray();
        }
    }

    private String getUserKey(String username) {
        return "history_" + username;
    }

    public static class HistoryEntry {
        public final String timestamp;
        public final String message;
        public final String riskLevel;
        public final int scamScore;
        public final String reasoning;
        public final String suggestedAction;

        public HistoryEntry(String timestamp, String message, String riskLevel, int scamScore, String reasoning, String suggestedAction) {
            this.timestamp = timestamp;
            this.message = message;
            this.riskLevel = riskLevel;
            this.scamScore = scamScore;
            this.reasoning = reasoning;
            this.suggestedAction = suggestedAction;
        }
    }
}
