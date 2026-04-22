package com.example.ailearningapp.storage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.example.ailearningapp.model.UserAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserStorage {
    private static final String PREFS_NAME = "user_accounts_prefs";
    private static final String KEY_USERS = "users_json";

    private final SharedPreferences sharedPreferences;

    public UserStorage(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean saveUser(UserAccount userAccount) {
        List<UserAccount> users = readUsers();
        String normalizedEmail = normalizeEmail(userAccount.email);

        for (UserAccount existing : users) {
            if (normalizeEmail(existing.email).equals(normalizedEmail)) {
                return false;
            }
        }

        users.add(userAccount);
        return persistUsers(users);
    }

    @Nullable
    public UserAccount login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        for (UserAccount user : readUsers()) {
            if (normalizeEmail(user.email).equals(normalizedEmail) && user.password.equals(password)) {
                return user;
            }
        }
        return null;
    }

    private List<UserAccount> readUsers() {
        List<UserAccount> users = new ArrayList<>();
        String rawJson = sharedPreferences.getString(KEY_USERS, "[]");
        try {
            JSONArray jsonArray = new JSONArray(rawJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.optJSONObject(i);
                if (item == null) {
                    continue;
                }

                UserAccount user = new UserAccount();
                user.username = item.optString("username", "");
                user.email = item.optString("email", "");
                user.password = item.optString("password", "");
                user.phoneNumber = item.optString("phoneNumber", "");
                users.add(user);
            }
        } catch (JSONException ignored) {
            // Keep empty list if the stored payload is invalid.
        }
        return users;
    }

    private boolean persistUsers(List<UserAccount> users) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (UserAccount user : users) {
                JSONObject item = new JSONObject();
                item.put("username", user.username);
                item.put("email", normalizeEmail(user.email));
                item.put("password", user.password);
                item.put("phoneNumber", user.phoneNumber);
                jsonArray.put(item);
            }
        } catch (JSONException e) {
            return false;
        }

        return sharedPreferences.edit().putString(KEY_USERS, jsonArray.toString()).commit();
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.US);
    }
}
