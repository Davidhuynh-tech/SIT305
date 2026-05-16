package com.example.ailearningapp.repository;

import androidx.annotation.NonNull;

import com.example.ailearningapp.model.ExplainResponse;
import com.example.ailearningapp.model.ExplanationItem;
import com.example.ailearningapp.model.GeneratedTaskResponse;
import com.example.ailearningapp.model.HintResponse;
import com.example.ailearningapp.model.QuestionItem;
import com.example.ailearningapp.model.SubmittedAnswer;
import com.example.ailearningapp.network.ApiClient;
import com.example.ailearningapp.network.dto.ExplainAnswersRequest;
import com.example.ailearningapp.network.dto.GenerateTaskRequest;
import com.example.ailearningapp.network.dto.HintRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LearningRepository {
    public interface CallbackResult<T> {
        void onSuccess(T data);

        void onError(String message);
    }

    public void generateTask(String studentName, List<String> interests, CallbackResult<GeneratedTaskResponse> callback) {
        GenerateTaskRequest request = new GenerateTaskRequest();
        request.studentName = studentName;
        request.interests = interests;

        ApiClient.getLearningApiService().generateTask(request).enqueue(new Callback<GeneratedTaskResponse>() {
            @Override
            public void onResponse(@NonNull Call<GeneratedTaskResponse> call, @NonNull Response<GeneratedTaskResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().questions != null && !response.body().questions.isEmpty()) {
                    callback.onSuccess(response.body());
                    return;
                }
                callback.onError("Backend returned an invalid task payload.");
            }

            @Override
            public void onFailure(@NonNull Call<GeneratedTaskResponse> call, @NonNull Throwable t) {
                callback.onError(t.getMessage() == null ? "Request failed." : t.getMessage());
            }
        });
    }

    public void generateHint(String question, String selectedAnswer, CallbackResult<HintResponse> callback) {
        HintRequest request = new HintRequest();
        request.question = question;
        request.selectedAnswer = selectedAnswer;

        ApiClient.getLearningApiService().generateHint(request).enqueue(new Callback<HintResponse>() {
            @Override
            public void onResponse(@NonNull Call<HintResponse> call, @NonNull Response<HintResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().response != null) {
                    callback.onSuccess(response.body());
                    return;
                }
                callback.onError("Backend did not return a hint.");
            }

            @Override
            public void onFailure(@NonNull Call<HintResponse> call, @NonNull Throwable t) {
                callback.onError(t.getMessage() == null ? "Hint request failed." : t.getMessage());
            }
        });
    }

    public void explainAnswers(List<SubmittedAnswer> answers, CallbackResult<ExplainResponse> callback) {
        ExplainAnswersRequest request = new ExplainAnswersRequest();
        request.answers = answers;

        ApiClient.getLearningApiService().explainAnswers(request).enqueue(new Callback<ExplainResponse>() {
            @Override
            public void onResponse(@NonNull Call<ExplainResponse> call, @NonNull Response<ExplainResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().explanations != null && !response.body().explanations.isEmpty()) {
                    callback.onSuccess(response.body());
                    return;
                }
                callback.onSuccess(createFallbackExplanation(answers));
            }

            @Override
            public void onFailure(@NonNull Call<ExplainResponse> call, @NonNull Throwable t) {
                callback.onSuccess(createFallbackExplanation(answers));
            }
        });
    }

    public GeneratedTaskResponse createFallbackTask(List<String> interests) {
        GeneratedTaskResponse response = new GeneratedTaskResponse();
        response.promptUsed = "Create two revision MCQs for: " + String.join(", ", interests) + ".";
        response.questions = new ArrayList<>();

        QuestionItem q1 = new QuestionItem();
        q1.id = "q1";
        q1.question = "What is the main purpose of a stack data structure?";
        q1.options = Arrays.asList(
                "To process items in random order",
                "To store data with Last-In First-Out access",
                "To always keep values sorted",
                "To represent tree edges"
        );
        q1.correctIndex = 1;

        QuestionItem q2 = new QuestionItem();
        q2.id = "q2";
        q2.question = "Which testing type validates a full user workflow?";
        q2.options = Arrays.asList(
                "Unit testing",
                "Static code analysis",
                "Integration and UI flow testing",
                "Memory profiling"
        );
        q2.correctIndex = 2;

        response.questions.add(q1);
        response.questions.add(q2);
        return response;
    }

    private ExplainResponse createFallbackExplanation(List<SubmittedAnswer> answers) {
        ExplainResponse response = new ExplainResponse();
        response.promptUsed = "Explain each student answer in simple teaching language.";
        response.explanations = new ArrayList<>();

        for (SubmittedAnswer answer : answers) {
            ExplanationItem item = new ExplanationItem();
            item.question = answer.questionText;
            item.selectedAnswer = answer.selectedAnswer;
            item.correct = answer.selectedAnswer != null && answer.selectedAnswer.equals(answer.correctAnswer);
            if (item.correct) {
                item.explanation = "Correct. Your selected answer matches the concept expected for this question.";
            } else {
                item.explanation = "Not quite. Review this concept and compare your choice with the expected answer: " + answer.correctAnswer;
            }
            response.explanations.add(item);
        }
        return response;
    }
}
