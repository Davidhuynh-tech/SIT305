package com.example.ailearningapp.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthSessionStorage {
    private static final String PREFS_NAME = "auth_session_prefs";
    private static final String KEY_LOGGED_IN_EMAIL = "logged_in_email";

    private final SharedPreferences sharedPreferences;

    public AuthSessionStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setLoggedInEmail(String email) {
        sharedPreferences.edit().putString(KEY_LOGGED_IN_EMAIL, email == null ? "" : email.trim()).apply();
    }

    public String getLoggedInEmail() {
        return sharedPreferences.getString(KEY_LOGGED_IN_EMAIL, "");
    }

    public void clear() {
        sharedPreferences.edit().remove(KEY_LOGGED_IN_EMAIL).apply();
    }
}
