package com.example.task_41c_quizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {
    static final String PREFS = "quiz_prefs";
    static final String KEY_NAME = "user_name";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_main);

        TextInputEditText nameEditText = findViewById(R.id.nameEditText);
        MaterialButton startQuizButton = findViewById(R.id.startQuizButton);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String existingName = prefs.getString(KEY_NAME, "");
        if (existingName != null && !existingName.isEmpty()) {
            nameEditText.setText(existingName);
            nameEditText.setSelection(existingName.length());
        }

        startQuizButton.setOnClickListener(v -> {
            String name = nameEditText.getText() == null ? "" : nameEditText.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit().putString(KEY_NAME, name).apply();

            Intent intent = new Intent(this, QuizActivity.class);
            intent.putExtra(QuizActivity.EXTRA_NAME, name);
            startActivity(intent);
        });
    }
}
