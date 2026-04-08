package com.example.task_41c_quizapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {
    static final String EXTRA_NAME = "extra_name";
    private static final int OPTION_COUNT = 4;

    private final List<Question> questions = new ArrayList<>();
    private int currentIndex = 0;
    private int score = 0;
    private boolean answered = false;
    private int selectedIndex = -1;
    private String playerName = "";

    private ProgressBar progressBar;
    private TextView progressText;
    private TextView questionText;
    private MaterialButton[] optionButtons;
    private MaterialButton submitButton;
    private MaterialButton nextButton;

    private ColorStateList defaultOptionTextColors;
    private ColorStateList defaultOptionBackgroundTint;
    @ColorInt private int correctColor;
    @ColorInt private int incorrectColor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        playerName = getIntent().getStringExtra(EXTRA_NAME);
        if (playerName == null) playerName = "";
        setContentView(R.layout.activity_quiz);

        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        TextView welcomeText = findViewById(R.id.welcomeText);
        if (playerName.isEmpty()) {
            welcomeText.setText("Welcome your name!");
        } else {
            welcomeText.setText("Welcome " + playerName + "!");
        }
        questionText = findViewById(R.id.questionText);
        submitButton = findViewById(R.id.submitButton);
        nextButton = findViewById(R.id.nextButton);

        optionButtons = new MaterialButton[] {
                findViewById(R.id.option1),
                findViewById(R.id.option2),
                findViewById(R.id.option3),
                findViewById(R.id.option4)
        };

        defaultOptionBackgroundTint = optionButtons[0].getBackgroundTintList();
        defaultOptionTextColors = optionButtons[0].getTextColors();
        correctColor = ContextCompat.getColor(this, R.color.quiz_correct);
        incorrectColor = ContextCompat.getColor(this, R.color.quiz_incorrect);

        seedQuestions();
        progressBar.setMax(questions.size());

        loadQuestion();

        submitButton.setOnClickListener(v -> submitAnswer());
        nextButton.setOnClickListener(v -> goNext());

        // Track selection via button taps (before submit).
        for (int i = 0; i < optionButtons.length; i++) {
            final int index = i;
            optionButtons[i].setOnClickListener(v -> {
                if (answered) return; // lock after submit
                selectedIndex = index;
            });
        }
    }

    private void seedQuestions() {
        questions.add(new Question(
                "Which language is primarily used for native Android development?",
                new String[] {"Swift", "Kotlin", "JavaScript", "Ruby"},
                1
        ));
        questions.add(new Question(
                "What does APK stand for?",
                new String[] {"Android Package", "Application Kernel", "Advanced Package Kit", "Android Program Key"},
                0
        ));
        questions.add(new Question(
                "Which UI component is best for choosing one option from many?",
                new String[] {"CheckBox", "RadioButton", "ImageView", "WebView"},
                1
        ));
        questions.add(new Question(
                "Which file declares Android app components (activities, services)?",
                new String[] {"AndroidManifest.xml", "build.gradle", "settings.gradle", "proguard-rules.pro"},
                0
        ));
        questions.add(new Question(
                "What is the minimum SDK in this project (minSdk)?",
                new String[] {"21", "23", "24", "26"},
                2
        ));
    }

    private void loadQuestion() {
        answered = false;
        selectedIndex = -1;
        submitButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.GONE);

        for (MaterialButton b : optionButtons) {
            b.setEnabled(true);
            b.setBackgroundTintList(defaultOptionBackgroundTint);
            b.setTextColor(defaultOptionTextColors);
        }

        Question q = questions.get(currentIndex);
        questionText.setText(q.questionText);
        for (int i = 0; i < OPTION_COUNT; i++) {
            optionButtons[i].setText(q.options[i]);
        }

        updateProgress();
    }

    private void updateProgress() {
        int total = questions.size();
        int currentOneBased = currentIndex + 1;
        progressBar.setProgress(currentOneBased);
        progressText.setText(currentOneBased + "/" + total);
    }

    private void submitAnswer() {
        if (selectedIndex < 0) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        Question q = questions.get(currentIndex);
        // Always show correct answer in green; selected (if wrong) turns red.
        optionButtons[q.correctIndex].setBackgroundTintList(ColorStateList.valueOf(correctColor));

        if (selectedIndex == q.correctIndex) {
            score++;
        } else if (selectedIndex >= 0) {
            // If incorrect, show chosen answer in red.
            optionButtons[selectedIndex].setBackgroundTintList(ColorStateList.valueOf(incorrectColor));
        }

        // Prevent changing answer after submission for this question.
        for (MaterialButton b : optionButtons) {
            b.setEnabled(false);
        }

        answered = true;
        boolean isLast = currentIndex == questions.size() - 1;
        submitButton.setVisibility(View.GONE);
        nextButton.setText("Next");
        nextButton.setVisibility(View.VISIBLE);
    }

    private void goNext() {
        if (currentIndex == questions.size() - 1) {
            Intent intent = new Intent(this, ResultsActivity.class);
            intent.putExtra(ResultsActivity.EXTRA_SCORE, score);
            intent.putExtra(ResultsActivity.EXTRA_TOTAL, questions.size());
            intent.putExtra(ResultsActivity.EXTRA_NAME, playerName);
            startActivity(intent);
            finish();
            return;
        }

        currentIndex++;
        loadQuestion();
    }
}
