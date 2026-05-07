package com.example.aichat.network;

import androidx.annotation.NonNull;

import com.example.aichat.BuildConfig;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatbotService {
    public interface ChatCallback {
        void onSuccess(String reply);

        void onError(String errorMessage);
    }

    private static final String CHAT_ENDPOINT = "/chat";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final OkHttpClient client = new OkHttpClient();

    public void getReply(@NonNull String username, @NonNull String userMessage, @NonNull ChatCallback callback) {
        networkExecutor.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("username", username);
                payload.put("message", userMessage);

                Request request = new Request.Builder()
                        .url(normalizeBaseUrl(BuildConfig.BACKEND_BASE_URL) + CHAT_ENDPOINT)
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(payload.toString(), JSON))
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        callback.onError("Backend API failed. Code: " + response.code());
                        return;
                    }

                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    String reply = json.optString("reply", "").trim();
                    if (reply.isEmpty()) {
                        callback.onError("Backend response missing 'reply' field.");
                        return;
                    }

                    callback.onSuccess(reply);
                }
            } catch (IOException ioException) {
                callback.onError("Network error: " + ioException.getMessage());
            } catch (Exception exception) {
                callback.onError("Error parsing backend response: " + exception.getMessage());
            }
        });
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return "http://10.0.2.2:8080";
        }
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
