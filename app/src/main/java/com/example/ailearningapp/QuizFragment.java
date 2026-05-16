package com.example.ailearningapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ailearningapp.databinding.FragmentQuizBinding;
import com.example.ailearningapp.model.GeneratedTaskResponse;
import com.example.ailearningapp.model.HintResponse;
import com.example.ailearningapp.model.QuestionItem;
import com.example.ailearningapp.model.SubmittedAnswer;
import com.example.ailearningapp.repository.LearningRepository;
import com.example.ailearningapp.state.SessionStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizFragment extends Fragment {
    private FragmentQuizBinding binding;
    private final LearningRepository repository = new LearningRepository();
    private final Map<String, Integer> selectedIndexes = new HashMap<>();
    private GeneratedTaskResponse currentTask;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQuizBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.retryTaskButton.setOnClickListener(v -> loadTask());
        binding.submitButton.setOnClickListener(v -> submitAnswers());
        loadTask();
    }

    private void loadTask() {
        setTaskLoadingState(true);
        repository.generateTask(SessionStore.studentName, SessionStore.interests, new LearningRepository.CallbackResult<GeneratedTaskResponse>() {
            @Override
            public void onSuccess(GeneratedTaskResponse data) {
                if (!isAdded()) {
                    return;
                }
                currentTask = data;
                SessionStore.currentTask = data;
                renderTask(false, null);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                currentTask = repository.createFallbackTask(SessionStore.interests);
                SessionStore.currentTask = currentTask;
                renderTask(true, message);
            }
        });
    }

    private void renderTask(boolean showError, @Nullable String message) {
        setTaskLoadingState(false);
        if (showError) {
            binding.taskErrorText.setText(getString(R.string.error_loading_task));
            binding.taskErrorText.setVisibility(View.VISIBLE);
            binding.retryTaskButton.setVisibility(View.VISIBLE);
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        } else {
            binding.taskErrorText.setVisibility(View.GONE);
            binding.retryTaskButton.setVisibility(View.GONE);
        }

        binding.taskPromptText.setVisibility(View.VISIBLE);
        binding.taskPromptText.setText(getString(R.string.llm_prompt_label) + " " + currentTask.promptUsed);
        renderQuestions(currentTask.questions);
    }

    private void renderQuestions(List<QuestionItem> questions) {
        selectedIndexes.clear();
        binding.questionsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (QuestionItem questionItem : questions) {
            View questionView = inflater.inflate(R.layout.item_question, binding.questionsContainer, false);
            TextView questionText = questionView.findViewById(R.id.questionText);
            RadioGroup optionsGroup = questionView.findViewById(R.id.optionsGroup);
            View hintButton = questionView.findViewById(R.id.hintButton);
            View hintProgress = questionView.findViewById(R.id.hintProgressBar);
            TextView hintStatus = questionView.findViewById(R.id.hintStatusText);
            TextView hintPrompt = questionView.findViewById(R.id.hintPromptText);
            TextView hintResponse = questionView.findViewById(R.id.hintResponseText);

            questionText.setText(questionItem.question);
            for (int optionIndex = 0; optionIndex < questionItem.options.size(); optionIndex++) {
                RadioButton radioButton = new RadioButton(requireContext());
                radioButton.setId(View.generateViewId());
                radioButton.setText(questionItem.options.get(optionIndex));
                radioButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_light));
                radioButton.setTag(optionIndex);
                optionsGroup.addView(radioButton);
            }

            optionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
                View selectedView = group.findViewById(checkedId);
                if (selectedView != null && selectedView.getTag() instanceof Integer) {
                    selectedIndexes.put(questionItem.id, (Integer) selectedView.getTag());
                }
            });

            hintButton.setOnClickListener(v -> {
                hintProgress.setVisibility(View.VISIBLE);
                hintStatus.setVisibility(View.VISIBLE);
                hintStatus.setText(getString(R.string.loading_hint));
                hintPrompt.setVisibility(View.GONE);
                hintResponse.setVisibility(View.GONE);

                int selectedOptionIndex = selectedIndexes.getOrDefault(questionItem.id, -1);
                String selectedAnswer = selectedOptionIndex >= 0 && selectedOptionIndex < questionItem.options.size()
                        ? questionItem.options.get(selectedOptionIndex)
                        : "";

                repository.generateHint(questionItem.question, selectedAnswer, new LearningRepository.CallbackResult<HintResponse>() {
                    @Override
                    public void onSuccess(HintResponse data) {
                        if (!isAdded()) {
                            return;
                        }
                        hintProgress.setVisibility(View.GONE);
                        hintStatus.setVisibility(View.GONE);
                        hintPrompt.setVisibility(View.VISIBLE);
                        hintResponse.setVisibility(View.VISIBLE);
                        hintPrompt.setText(getString(R.string.llm_prompt_label) + " " + data.promptUsed);
                        hintResponse.setText(getString(R.string.llm_response_label) + " " + data.response);
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded()) {
                            return;
                        }
                        hintProgress.setVisibility(View.GONE);
                        hintStatus.setVisibility(View.VISIBLE);
                        hintStatus.setText(getString(R.string.error_loading_hint));
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            });

            binding.questionsContainer.addView(questionView);
        }
    }

    private void submitAnswers() {
        if (currentTask == null || currentTask.questions == null || currentTask.questions.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_loading_task, Toast.LENGTH_SHORT).show();
            return;
        }

        List<SubmittedAnswer> answers = new ArrayList<>();
        for (QuestionItem questionItem : currentTask.questions) {
            Integer selectedIndex = selectedIndexes.get(questionItem.id);
            if (selectedIndex == null || selectedIndex < 0 || selectedIndex >= questionItem.options.size()) {
                Toast.makeText(requireContext(), R.string.select_option_warning, Toast.LENGTH_SHORT).show();
                return;
            }

            SubmittedAnswer answer = new SubmittedAnswer();
            answer.questionId = questionItem.id;
            answer.questionText = questionItem.question;
            answer.selectedAnswer = questionItem.options.get(selectedIndex);
            answer.correctAnswer = questionItem.options.get(questionItem.correctIndex);
            answers.add(answer);
        }

        SessionStore.latestAnswers = answers;
        NavHostFragment.findNavController(this).navigate(R.id.action_quizFragment_to_resultsFragment);
    }

    private void setTaskLoadingState(boolean loading) {
        binding.taskProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.taskLoadingText.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.submitButton.setEnabled(!loading);
        if (loading) {
            binding.taskErrorText.setVisibility(View.GONE);
            binding.retryTaskButton.setVisibility(View.GONE);
            binding.taskPromptText.setVisibility(View.GONE);
            binding.questionsContainer.removeAllViews();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
