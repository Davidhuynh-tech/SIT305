package com.example.ailearningapp.state;

import com.example.ailearningapp.model.GeneratedTaskResponse;
import com.example.ailearningapp.model.SubmittedAnswer;

import java.util.ArrayList;
import java.util.List;

public final class SessionStore {
    public static String studentName = "Your Name";
    public static final List<String> interests = new ArrayList<>();
    public static GeneratedTaskResponse currentTask;
    public static List<SubmittedAnswer> latestAnswers = new ArrayList<>();

    private SessionStore() {
    }

    public static void clearSession() {
        studentName = "Your Name";
        interests.clear();
        currentTask = null;
        latestAnswers = new ArrayList<>();
    }
}
