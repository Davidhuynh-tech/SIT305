package com.example.scamguard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.scamguard.backend.AuthManager;
import com.example.scamguard.backend.HistoryStore;
import com.example.scamguard.databinding.FragmentSecondBinding;

import java.util.List;

public class HistoryFragment extends Fragment {

    private FragmentSecondBinding binding;
    private AuthManager authManager;
    private HistoryStore historyStore;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authManager = new AuthManager(requireContext());
        historyStore = new HistoryStore(requireContext());

        binding.buttonSecond.setOnClickListener(v ->
                NavHostFragment.findNavController(HistoryFragment.this)
                        .navigate(R.id.action_historyFragment_to_analyzeFragment)
        );

        binding.buttonClearHistory.setOnClickListener(v -> {
            String user = authManager.getCurrentUser();
            if (user == null) {
                Toast.makeText(requireContext(), getString(R.string.history_login_required), Toast.LENGTH_SHORT).show();
                return;
            }
            historyStore.clearHistory(user);
            Toast.makeText(requireContext(), getString(R.string.history_cleared), Toast.LENGTH_SHORT).show();
            renderHistory();
        });

        renderHistory();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) {
            renderHistory();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void renderHistory() {
        String user = authManager.getCurrentUser();
        if (user == null) {
            binding.textUserHistory.setText(getString(R.string.no_user));
            binding.historyContainer.removeAllViews();
            binding.textHistoryEmpty.setVisibility(View.VISIBLE);
            binding.buttonClearHistory.setEnabled(false);
            return;
        }

        binding.textUserHistory.setText(getString(R.string.current_user, user));
        binding.buttonClearHistory.setEnabled(true);

        List<HistoryStore.HistoryEntry> entries = historyStore.getEntries(user);
        binding.historyContainer.removeAllViews();

        if (entries.isEmpty()) {
            binding.textHistoryEmpty.setVisibility(View.VISIBLE);
            return;
        }

        binding.textHistoryEmpty.setVisibility(View.GONE);
        for (HistoryStore.HistoryEntry entry : entries) {
            TextView card = new TextView(requireContext());
            card.setText(
                    entry.timestamp + "\n"
                            + "Message: " + entry.message + "\n"
                            + "Scam Score: " + entry.scamScore + "%\n"
                            + "Risk: " + entry.riskLevel + "\n"
                            + "Reasoning: " + entry.reasoning + "\n"
                            + "Action: " + entry.suggestedAction
            );
            card.setTextSize(14f);
            card.setPadding(20, 20, 20, 20);
            card.setBackgroundResource(R.drawable.bg_message_chip);
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = 16;
            card.setLayoutParams(params);
            binding.historyContainer.addView(card);
        }
    }
}
