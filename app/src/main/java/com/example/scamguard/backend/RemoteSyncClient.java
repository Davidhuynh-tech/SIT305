package com.example.scamguard.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RemoteSyncClient {

    private static final String TAG = "RemoteSyncClient";
    private static final String PREF_NAME = "scamguard_sync";
    private static final String KEY_BASE_URL = "sync_base_url";
    private static final String DEFAULT_BASE_URL = "http://10.0.2.2:3000";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private static RemoteSyncClient instance;

    private final SharedPreferences prefs;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final OkHttpClient httpClient = new OkHttpClient();

    private RemoteSyncClient(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized RemoteSyncClient getInstance(Context context) {
        if (instance == null) {
            instance = new RemoteSyncClient(context.getApplicationContext());
        }
        return instance;
    }

    public void setBaseUrl(String baseUrl) {
        if (baseUrl == null) {
            prefs.edit().remove(KEY_BASE_URL).apply();
            return;
        }
        prefs.edit().putString(KEY_BASE_URL, baseUrl.trim()).apply();
    }

    public String getBaseUrl() {
        String value = prefs.getString(KEY_BASE_URL, "");
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? DEFAULT_BASE_URL : trimmed;
    }

    public boolean isEnabled() {
        return !getBaseUrl().isEmpty();
    }

    public void syncUserCredential(String username, String rawPassword) {
        if (!isEnabled()) {
            return;
        }
        executorService.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("username", username);
                payload.put("passwordHash", sha256(rawPassword));
                postJson("/api/sync/user", payload);
            } catch (Exception exception) {
                Log.w(TAG, "Failed syncing user credential", exception);
            }
        });
    }

    public void syncHistoryEntry(String username, String message, String riskLevel, int scamScore, String reasoning, String suggestedAction) {
        if (!isEnabled()) {
            return;
        }
        executorService.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("username", username);
                payload.put("message", message);
                payload.put("riskLevel", riskLevel);
                payload.put("scamScore", scamScore);
                payload.put("reasoning", reasoning);
                payload.put("suggestedAction", suggestedAction);
                postJson("/api/sync/history", payload);
            } catch (Exception exception) {
                Log.w(TAG, "Failed syncing history entry", exception);
            }
        });
    }

    public void clearHistoryForUser(String username) {
        if (!isEnabled()) {
            return;
        }
        executorService.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("username", username);
                postJson("/api/sync/history/clear", payload);
            } catch (Exception exception) {
                Log.w(TAG, "Failed clearing remote history", exception);
            }
        });
    }

    private void postJson(String path, JSONObject payload) throws Exception {
        String base = getBaseUrl();
        if (base.isEmpty()) {
            return;
        }
        String url = base.endsWith("/") ? base.substring(0, base.length() - 1) + path : base + path;
        RequestBody requestBody = RequestBody.create(payload.toString(), JSON_MEDIA_TYPE);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException("Remote sync HTTP " + response.code());
            }
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception exception) {
            return "";
        }
    }
}
