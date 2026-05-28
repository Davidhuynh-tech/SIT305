package com.example.scamguard;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.scamguard.backend.AnalysisEngine;
import com.example.scamguard.backend.AuthManager;
import com.example.scamguard.backend.HistoryStore;
import com.example.scamguard.backend.LocalBackendServer;
import com.example.scamguard.databinding.FragmentFirstBinding;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AnalyzeFragment extends Fragment {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final String LOCAL_BACKEND_ENDPOINT = "http://127.0.0.1:" + LocalBackendServer.PORT + "/api/analyze";

    private FragmentFirstBinding binding;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final OkHttpClient httpClient = new OkHttpClient();
    private final AnalysisEngine analysisEngine = new AnalysisEngine();
    private boolean useLlama = true;
    private AuthManager authManager;
    private HistoryStore historyStore;

    private enum UiState {
        START,
        INPUT,
        RESULT
    }

    private UiState uiState = UiState.START;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authManager = new AuthManager(requireContext());
        historyStore = new HistoryStore(requireContext());
        binding.inputContent.setText("");
        applyUiState(UiState.START);
        refreshAuthUi();

        binding.buttonMainImport.setOnClickListener(v -> {
            if (uiState == UiState.START) {
                applyUiState(UiState.INPUT);
                return;
            }
            if (uiState == UiState.INPUT) {
                runAnalysis();
                return;
            }
            applyUiState(UiState.INPUT);
        });

        binding.buttonUpload.setOnClickListener(v ->
                Toast.makeText(requireContext(), getString(R.string.upload_not_ready), Toast.LENGTH_SHORT).show());

        binding.buttonMenu.setOnClickListener(v ->
                NavHostFragment.findNavController(AnalyzeFragment.this)
                        .navigate(R.id.action_analyzeFragment_to_historyFragment));

        binding.buttonSettings.setOnClickListener(v -> showProfileMenu());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }

    private void runAnalysis() {
        String userInput = binding.inputContent.getText() == null
                ? ""
                : binding.inputContent.getText().toString().trim();

        if (userInput.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.paste_message_prompt), Toast.LENGTH_SHORT).show();
            return;
        }

        binding.textImportedMessage.setText(userInput);
        binding.textScore.setText("Scam Score: ...");
        binding.textRisk.setText("Risk Level: Analyzing...");
        binding.textReasons.setText("Reasoning why it's a scam");
        binding.textSuggestedAction.setText("Suggested Action");
        applyUiState(UiState.RESULT);

        executorService.execute(() -> {
            try {
                JSONObject result = requestBackendWithRetry(userInput, useLlama);
                String riskLevel = result.optString("riskLevel", "Unknown");
                String reasoning = result.optString("reasoning", "No reasoning returned.");
                String llmResponse = result.optString("llmResponse", "No LLM output returned.");
                int score = getScoreForRisk(riskLevel);
                String suggestedAction = buildSuggestedAction(riskLevel, llmResponse);

                requireActivity().runOnUiThread(() -> {
                    binding.textScore.setText("Scam Score: " + score + "%");
                    binding.textRisk.setText("Risk Level: " + riskLevel);
                    binding.textReasons.setText(reasoning);
                    binding.textSuggestedAction.setText(suggestedAction);
                    binding.textLlm.setText(llmResponse);
                });

                String currentUser = authManager.getCurrentUser();
                if (currentUser != null) {
                    historyStore.addEntry(currentUser, userInput, riskLevel, score, reasoning, suggestedAction);
                }
            } catch (Exception exception) {
                // Fallback: if localhost API fails, run analysis directly on-device so user still gets a result.
                AnalysisEngine.AnalysisResult fallback = analysisEngine.analyze(userInput, useLlama);
                int score = getScoreForRisk(fallback.riskLevel);
                String suggestedAction = buildSuggestedAction(fallback.riskLevel, fallback.llmResponse);

                requireActivity().runOnUiThread(() -> {
                    binding.textScore.setText("Scam Score: " + score + "%");
                    binding.textRisk.setText("Risk Level: " + fallback.riskLevel);
                    binding.textReasons.setText(fallback.reasoning);
                    binding.textSuggestedAction.setText(suggestedAction);
                    binding.textLlm.setText(fallback.llmResponse);
                });

                String currentUser = authManager.getCurrentUser();
                if (currentUser != null) {
                    historyStore.addEntry(currentUser, userInput, fallback.riskLevel, score, fallback.reasoning, suggestedAction);
                }
            }
        });
    }

    private JSONObject requestBackendWithRetry(String userInput, boolean useLlama) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("content", userInput);
                payload.put("useLlama", useLlama);

                RequestBody requestBody = RequestBody.create(payload.toString(), JSON_MEDIA_TYPE);
                Request request = new Request.Builder()
                        .url(LOCAL_BACKEND_ENDPOINT)
                        .post(requestBody)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IllegalStateException("Backend returned HTTP " + response.code());
                    }
                    if (response.body() == null) {
                        throw new IllegalStateException("Backend response body is empty.");
                    }
                    return new JSONObject(response.body().string());
                }
            } catch (Exception exception) {
                lastException = exception;
                if (attempt < 2) {
                    Thread.sleep(250);
                }
            }
        }

        throw lastException == null ? new IllegalStateException("Unknown backend error.") : lastException;
    }

    private void handleLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.auth_missing_fields), Toast.LENGTH_SHORT).show();
            return;
        }
        boolean success = authManager.login(username, password);
        Toast.makeText(requireContext(),
                getString(success ? R.string.auth_login_success : R.string.auth_login_failed),
                Toast.LENGTH_SHORT).show();
        if (success) {
            refreshAuthUi();
        }
    }

    private void handleRegister(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.auth_missing_fields), Toast.LENGTH_SHORT).show();
            return;
        }
        boolean success = authManager.register(username, password);
        Toast.makeText(requireContext(),
                getString(success ? R.string.auth_register_success : R.string.auth_register_exists),
                Toast.LENGTH_SHORT).show();
        if (success) {
            refreshAuthUi();
        }
    }

    private void handleLogout() {
        authManager.logout();
        Toast.makeText(requireContext(), getString(R.string.auth_logout_success), Toast.LENGTH_SHORT).show();
        applyUiState(UiState.START);
        refreshAuthUi();
    }

    private void refreshAuthUi() {
        String currentUser = authManager.getCurrentUser();
        binding.buttonSettings.setContentDescription(currentUser == null
                ? getString(R.string.profile_not_logged_in)
                : getString(R.string.profile_logged_in_as, currentUser));
    }

    private String getTrimmed(String value) {
        return value == null ? "" : value.trim();
    }

    private void showProfileMenu() {
        String currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            showLoginRegisterDialog();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.profile_title))
                .setMessage(getString(R.string.profile_logged_in_as, currentUser))
                .setPositiveButton(getString(R.string.profile_logout), (dialog, which) -> handleLogout())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showLoginRegisterDialog() {
        LinearLayout form = new LinearLayout(requireContext());
        form.setOrientation(LinearLayout.VERTICAL);
        int padding = 32;
        form.setPadding(padding, padding, padding, padding);

        EditText usernameInput = new EditText(requireContext());
        usernameInput.setHint(getString(R.string.username_hint));
        form.addView(usernameInput);

        EditText passwordInput = new EditText(requireContext());
        passwordInput.setHint(getString(R.string.password_hint));
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        form.addView(passwordInput);

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.profile_login_register))
                .setView(form)
                .setPositiveButton(getString(R.string.login_button), (dialog, which) -> {
                    String username = getTrimmed(usernameInput.getText() == null ? "" : usernameInput.getText().toString());
                    String password = getTrimmed(passwordInput.getText() == null ? "" : passwordInput.getText().toString());
                    handleLogin(username, password);
                })
                .setNeutralButton(getString(R.string.register_button), (dialog, which) -> {
                    String username = getTrimmed(usernameInput.getText() == null ? "" : usernameInput.getText().toString());
                    String password = getTrimmed(passwordInput.getText() == null ? "" : passwordInput.getText().toString());
                    handleRegister(username, password);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void applyUiState(UiState state) {
        uiState = state;
        binding.textStartPrompt.setVisibility(state == UiState.START ? View.VISIBLE : View.GONE);
        binding.panelInput.setVisibility(state == UiState.INPUT ? View.VISIBLE : View.GONE);
        binding.panelResult.setVisibility(state == UiState.RESULT ? View.VISIBLE : View.GONE);
        binding.viewHomeSpacer.setVisibility(state == UiState.START ? View.VISIBLE : View.GONE);
    }

    private int getScoreForRisk(String riskLevel) {
        String normalized = normalizeRiskLevel(riskLevel);
        if ("This is a Scam".equals(normalized)) {
            return 90;
        }
        if ("Likely a Scam".equals(normalized)) {
            return 65;
        }
        if ("Suspicious".equals(normalized)) {
            return 45;
        }
        if ("Should keep on Monitor".equals(normalized)) {
            return 30;
        }
        return 0;
    }

    private String buildSuggestedAction(String riskLevel, String llmResponse) {
        String normalized = normalizeRiskLevel(riskLevel);
        if ("This is a Scam".equals(normalized)) {
            return "Do not reply. Block sender and report the message.";
        }
        if ("Likely a Scam".equals(normalized)) {
            return "Verify through official channels before taking any action.";
        }
        if ("Suspicious".equals(normalized)) {
            return "Treat with caution and verify sender identity before proceeding.";
        }
        if ("Should keep on Monitor".equals(normalized)) {
            return "Keep monitoring this conversation and avoid sharing sensitive details.";
        }
        if (llmResponse != null && !llmResponse.isEmpty() && !"No LLM output returned.".equals(llmResponse)) {
            return "Looks low risk. Continue carefully and avoid sharing private info.";
        }
        return "No immediate red flags. Stay cautious with links and personal details.";
    }

    private String normalizeRiskLevel(String riskLevel) {
        if (riskLevel == null) {
            return "Safe";
        }
        String lower = riskLevel.trim().toLowerCase();
        if (lower.contains("this is a scam") || lower.equals("scam") || lower.contains("refused")) {
            return "This is a Scam";
        }
        if (lower.contains("likely a scam") || lower.contains("likely scam")) {
            return "Likely a Scam";
        }
        if (lower.contains("suspicious")) {
            return "Suspicious";
        }
        if (lower.contains("monitor")) {
            return "Should keep on Monitor";
        }
        if (lower.contains("safe")) {
            return "Safe";
        }
        return "Safe";
    }
}
