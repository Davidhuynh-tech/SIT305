package com.example.ailearningapp.network;

import android.text.TextUtils;

import com.example.ailearningapp.network.dto.UserUpsertRequest;
import com.example.ailearningapp.state.SessionStore;

public final class BackendSyncHelper {
    private BackendSyncHelper() {
    }

    public static void syncCurrentUserSilently() {
        if (TextUtils.isEmpty(SessionStore.currentUserEmail)) {
            return;
        }

        UserUpsertRequest request = new UserUpsertRequest();
        request.email = SessionStore.currentUserEmail;
        request.username = SessionStore.studentName;
        request.accountTier = SessionStore.accountTier;
        request.interests = SessionStore.interests;
        request.phoneNumber = "";

        ApiClient.getLearningApiService().upsertUser(request).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                // No-op: silent sync for demo app.
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                // No-op: fallback storage still available.
            }
        });
    }
}
