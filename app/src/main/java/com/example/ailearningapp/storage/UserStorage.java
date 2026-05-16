package com.example.ailearningapp.storage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.example.ailearningapp.model.UserAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
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

    @Nullable
    public UserAccount getUserByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        for (UserAccount user : readUsers()) {
            if (normalizeEmail(user.email).equals(normalizedEmail)) {
                return user;
            }
        }
        return null;
    }

    public boolean updateUser(UserAccount updatedUser) {
        List<UserAccount> users = readUsers();
        String normalizedEmail = normalizeEmail(updatedUser.email);
        for (int index = 0; index < users.size(); index++) {
            if (normalizeEmail(users.get(index).email).equals(normalizedEmail)) {
                users.set(index, updatedUser);
                return persistUsers(users);
            }
        }
        return false;
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
                user.accountTier = item.optString("accountTier", "Free");
                user.interests = parseInterests(item.optJSONArray("interests"));
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
                item.put("accountTier", user.accountTier == null ? "Free" : user.accountTier);
                item.put("interests", new JSONArray(user.interests == null ? new ArrayList<>() : user.interests));
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

    private List<String> parseInterests(@Nullable JSONArray interestArray) {
        if (interestArray == null || interestArray.length() == 0) {
            return new ArrayList<>(Arrays.asList("Algorithms", "Testing"));
        }

        List<String> interests = new ArrayList<>();
        for (int i = 0; i < interestArray.length(); i++) {
            String interest = interestArray.optString(i, "").trim();
            if (!interest.isEmpty()) {
                interests.add(interest);
            }
        }

        if (interests.isEmpty()) {
            interests.add("Algorithms");
            interests.add("Testing");
        }
        return interests;
    }
}
