package com.example.scamguard.backend;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

public class AuthManager {

    private static final String PREF_NAME = "scamguard_auth";
    private static final String KEY_USERS = "users_json";
    private static final String KEY_CURRENT_USER = "current_user";

    private final SharedPreferences prefs;
    private final RemoteSyncClient remoteSyncClient;

    public AuthManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.remoteSyncClient = RemoteSyncClient.getInstance(context);
    }

    public synchronized boolean register(String username, String password) {
        try {
            JSONObject users = getUsers();
            if (users.has(username)) {
                return false;
            }
            users.put(username, password);
            prefs.edit()
                    .putString(KEY_USERS, users.toString())
                    .putString(KEY_CURRENT_USER, username)
                    .apply();
            remoteSyncClient.syncUserCredential(username, password);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public synchronized boolean login(String username, String password) {
        try {
            JSONObject users = getUsers();
            String storedPassword = users.optString(username, null);
            if (storedPassword == null || !storedPassword.equals(password)) {
                return false;
            }
            prefs.edit().putString(KEY_CURRENT_USER, username).apply();
            remoteSyncClient.syncUserCredential(username, password);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public synchronized void logout() {
        prefs.edit().remove(KEY_CURRENT_USER).apply();
    }

    public synchronized String getCurrentUser() {
        return prefs.getString(KEY_CURRENT_USER, null);
    }

    private JSONObject getUsers() {
        try {
            String usersJson = prefs.getString(KEY_USERS, "{}");
            return new JSONObject(usersJson == null ? "{}" : usersJson);
        } catch (Exception exception) {
            return new JSONObject();
        }
    }
}
