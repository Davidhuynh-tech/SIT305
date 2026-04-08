package com.example.task_41c_quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ResultsActivity extends AppCompatActivity {
    static final String EXTRA_SCORE = "extra_score";
    static final String EXTRA_TOTAL = "extra_total";
    static final String EXTRA_NAME = "extra_name";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_results);

        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int total = getIntent().getIntExtra(EXTRA_TOTAL, 0);
        String playerName = getIntent().getStringExtra(EXTRA_NAME);
        if (playerName == null) playerName = "";

        TextView resultsTitle = findViewById(R.id.resultsTitle);
        TextView scoreText = findViewById(R.id.scoreText);
        MaterialButton newQuizButton = findViewById(R.id.newQuizButton);
        MaterialButton finishButton = findViewById(R.id.finishButton);

        resultsTitle.setText("Congratulations " + playerName + "!");
        scoreText.setText("YOUR SCORE: " + score + "/" + total);

        newQuizButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        finishButton.setOnClickListener(v -> finishAffinity());
    }
}
