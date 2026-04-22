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
import com.example.ailearningapp.repository.LearningRepository;
import com.example.ailearningapp.state.SessionStore;

import java.util.List;

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
