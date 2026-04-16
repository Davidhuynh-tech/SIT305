package com.example.istream.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "istream_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void login(int userId, String username) {
        preferences.edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    public void logout() {
        preferences.edit().clear().apply();
    }

    public int getUserId() {
        return preferences.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return preferences.getString(KEY_USERNAME, "");
    }

    public boolean isLoggedIn() {
        return getUserId() != -1;
    }
}
