package com.example.task_41c_quizapp;

import androidx.annotation.NonNull;

public final class Question {
    @NonNull public final String questionText;
    @NonNull public final String[] options; // length 4
    public final int correctIndex; // 0..3

    public Question(@NonNull String questionText, @NonNull String[] options, int correctIndex) {
        if (options.length != 4) {
            throw new IllegalArgumentException("options must be length 4");
        }
        if (correctIndex < 0 || correctIndex > 3) {
            throw new IllegalArgumentException("correctIndex must be 0..3");
        }
        this.questionText = questionText;
        this.options = options;
        this.correctIndex = correctIndex;
    }
}
