package com.example.ailearningapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ailearningapp.databinding.FragmentHistoryBinding;
import com.example.ailearningapp.model.HistoryEntry;
import com.example.ailearningapp.model.HistoryResponse;
import com.example.ailearningapp.network.ApiClient;
import com.example.ailearningapp.state.SessionStore;
import com.example.ailearningapp.storage.HistoryStorage;

import java.util.List;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        renderHistory();
        binding.backFromHistoryButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
    }

    private void renderHistory() {
        HistoryStorage historyStorage = new HistoryStorage(requireContext());
        List<HistoryEntry> entries = historyStorage.getHistory(SessionStore.currentUserEmail);
        renderEntries(entries);

        if (!SessionStore.currentUserEmail.isEmpty()) {
            ApiClient.getLearningApiService().getHistory(SessionStore.currentUserEmail).enqueue(new retrofit2.Callback<HistoryResponse>() {
                @Override
                public void onResponse(retrofit2.Call<HistoryResponse> call, retrofit2.Response<HistoryResponse> response) {
                    if (!isAdded() || response.body() == null || response.body().history == null) {
                        return;
                    }
                    renderEntries(response.body().history);
                }

                @Override
                public void onFailure(retrofit2.Call<HistoryResponse> call, Throwable t) {
                    // Keep local fallback entries.
                }
            });
        }
    }

    private void renderEntries(List<HistoryEntry> entries) {
        binding.historyContainer.removeAllViews();
        if (entries.isEmpty()) {
            binding.historyEmptyText.setVisibility(View.VISIBLE);
            return;
        }

        binding.historyEmptyText.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (HistoryEntry entry : entries) {
            View item = inflater.inflate(R.layout.item_history, binding.historyContainer, false);
            TextView timestamp = item.findViewById(R.id.historyTimestampText);
            TextView prompt = item.findViewById(R.id.historyPromptText);
            TextView score = item.findViewById(R.id.historyScoreText);
            TextView topics = item.findViewById(R.id.historyTopicsText);

            timestamp.setText(getString(R.string.history_timestamp_format, entry.timestamp));
            prompt.setText(entry.taskPrompt);
            score.setText(getString(R.string.history_score_format, entry.correctCount, entry.totalQuestions));
            topics.setText(entry.topicSummary);
            binding.historyContainer.addView(item);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
