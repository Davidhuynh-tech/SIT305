package com.example.ailearningapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ailearningapp.databinding.FragmentResultsBinding;
import com.example.ailearningapp.model.ExplainResponse;
import com.example.ailearningapp.model.ExplanationItem;
import com.example.ailearningapp.model.HistoryEntry;
import com.example.ailearningapp.network.ApiClient;
import com.example.ailearningapp.network.dto.HistoryAppendRequest;
import com.example.ailearningapp.repository.LearningRepository;
import com.example.ailearningapp.state.SessionStore;
import com.example.ailearningapp.storage.HistoryStorage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResultsFragment extends Fragment {
    private FragmentResultsBinding binding;
    private final LearningRepository repository = new LearningRepository();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentResultsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.continueButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_resultsFragment_to_homeFragment));
        loadExplanations();
    }

    private void loadExplanations() {
        setLoadingState(true);
        repository.explainAnswers(SessionStore.latestAnswers, new LearningRepository.CallbackResult<ExplainResponse>() {
            @Override
            public void onSuccess(ExplainResponse data) {
                if (!isAdded()) {
                    return;
                }
                setLoadingState(false);
                binding.explanationsPromptText.setVisibility(View.VISIBLE);
                binding.explanationsPromptText.setText(getString(R.string.llm_prompt_label) + " " + data.promptUsed);
                renderExplanations(data.explanations);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                setLoadingState(false);
                binding.explanationsErrorText.setVisibility(View.VISIBLE);
                binding.explanationsErrorText.setText(getString(R.string.error_loading_explanations));
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderExplanations(List<ExplanationItem> explanations) {
        binding.explanationsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (ExplanationItem explanation : explanations) {
            View itemView = inflater.inflate(R.layout.item_explanation, binding.explanationsContainer, false);
            TextView questionText = itemView.findViewById(R.id.resultQuestionText);
            TextView answerText = itemView.findViewById(R.id.resultAnswerText);
            TextView responseText = itemView.findViewById(R.id.resultResponseText);

            questionText.setText(explanation.question);
            String correctness = explanation.correct ? "Correct" : "Incorrect";
            answerText.setText(getString(R.string.answer_label) + ": " + explanation.selectedAnswer + " (" + correctness + ")");
            responseText.setText(getString(R.string.llm_response_label) + " " + explanation.explanation);

            binding.explanationsContainer.addView(itemView);
        }

        saveHistory(explanations);
    }

    private void setLoadingState(boolean isLoading) {
        binding.explanationsProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.explanationsLoadingText.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.continueButton.setEnabled(!isLoading);
        if (isLoading) {
            binding.explanationsErrorText.setVisibility(View.GONE);
            binding.explanationsPromptText.setVisibility(View.GONE);
            binding.explanationsContainer.removeAllViews();
        }
    }

    private void saveHistory(List<ExplanationItem> explanations) {
        int correctCount = 0;
        for (ExplanationItem explanation : explanations) {
            if (explanation.correct) {
                correctCount++;
            }
        }

        HistoryEntry entry = new HistoryEntry();
        entry.timestamp = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());
        entry.taskPrompt = SessionStore.currentTask != null && SessionStore.currentTask.promptUsed != null
                ? SessionStore.currentTask.promptUsed
                : "Generated revision task";
        entry.totalQuestions = explanations.size();
        entry.correctCount = correctCount;
        entry.topicSummary = "Topics: " + (SessionStore.interests.isEmpty() ? "Algorithms, Testing" : String.join(", ", SessionStore.interests));

        HistoryStorage historyStorage = new HistoryStorage(requireContext());
        historyStorage.appendHistory(SessionStore.currentUserEmail, entry);

        if (!SessionStore.currentUserEmail.isEmpty()) {
            HistoryAppendRequest request = new HistoryAppendRequest();
            request.email = SessionStore.currentUserEmail;
            request.entry = entry;
            ApiClient.getLearningApiService().appendHistory(request).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                    // Silent sync only.
                }

                @Override
                public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                    // Local history already persisted.
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
