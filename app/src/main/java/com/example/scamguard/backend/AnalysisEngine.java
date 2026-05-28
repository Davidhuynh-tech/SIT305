package com.example.scamguard.backend;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AnalysisEngine {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final String OLLAMA_ENDPOINT = "http://10.0.2.2:11434/api/generate";
    private static final String OLLAMA_MODEL = "llama2";

    private final OkHttpClient httpClient = new OkHttpClient();

    public AnalysisResult analyze(String userInput, boolean useLlama) {
        if (containsSensitiveActionRequest(userInput)) {
            return new AnalysisResult(
                    "Refused: sensitive request",
                    "Safety block: this input asks for passwords, one-time codes, or financial transfer instructions.",
                    "ScamGuard cannot help with sensitive data handling. Contact official support channels directly."
            );
        }

        AnalysisResult localResult = runLocalHeuristicAnalysis(userInput);
        if (!useLlama) {
            return new AnalysisResult(
                    localResult.riskLevel,
                    localResult.reasoning,
                    "Llama2 disabled. Showing on-device analysis only."
            );
        }

        try {
            String llmResponse = requestLlamaAnalysis(userInput, localResult.riskLevel);
            return new AnalysisResult(localResult.riskLevel, localResult.reasoning, llmResponse);
        } catch (Exception exception) {
            return new AnalysisResult(
                    localResult.riskLevel,
                    localResult.reasoning,
                    "Could not reach Ollama Llama2 server. Start Ollama and run `ollama run llama2`, then retry.\n\n"
                            + "Error: " + exception.getMessage()
            );
        }
    }

    private boolean containsSensitiveActionRequest(String text) {
        String lowerText = text.toLowerCase(Locale.ROOT);
        return lowerText.contains("password")
                || lowerText.contains("otp")
                || lowerText.contains("one-time code")
                || lowerText.contains("bank transfer")
                || lowerText.contains("send me your card");
    }

    private AnalysisResult runLocalHeuristicAnalysis(String text) {
        String lowerText = text.toLowerCase(Locale.ROOT);
        int score = 0;
        List<String> reasons = new ArrayList<>();

        // Urgency / Threat Language
        if (containsAny(lowerText, "urgent", "immediately", "final warning", "within 24 hours",
                "act now", "expire soon", "last chance", "respond now", "time sensitive",
                "limited time", "do not ignore", "action required", "overdue", "past due")) {
            score += 2;
            reasons.add("Urgency language detected.");
        }

        // Account Impersonation
        if (containsAny(lowerText, "verify account", "account suspended", "click here", "reset your login",
                "confirm your identity", "unusual activity", "unauthorized access", "security alert",
                "update your information", "validate your account", "login attempt", "locked account",
                "reactivate your account", "your account has been", "sign in to confirm")) {
            score += 2;
            reasons.add("Account impersonation pattern detected.");
        }

        // High-Risk Payment
        if (containsAny(lowerText, "gift card", "crypto", "bitcoin", "wire transfer", "bank details",
                "itunes card", "google play card", "steam card", "send money", "money transfer",
                "western union", "moneygram", "zelle", "venmo request", "ethereum", "usdt",
                "pay in crypto", "prepaid card", "routing number", "account number")) {
            score += 3;
            reasons.add("High-risk payment request detected.");
        }

        // Suspicious Links
        if (containsAny(lowerText, "http://", "https://", "bit.ly", "tinyurl",
                "shorturl", "t.co", "rebrand.ly", "cutt.ly", "ow.ly", "is.gd",
                "click the link", "click below", "follow this link", "tap here")) {
            score += 2;
            reasons.add("External link detected.");
        }

        // Generic / Lottery Targeting
        if (containsAny(lowerText, "dear customer", "dear user", "winner", "congratulations",
                "you have been selected", "you are our lucky winner", "claim your prize",
                "you won", "prize money", "lottery winner", "selected for reward",
                "free gift", "you qualify", "exclusive offer for you")) {
            score += 1;
            reasons.add("Generic targeting language detected.");
        }

        // Personal Info Harvesting
        if (containsAny(lowerText, "social security", "ssn", "date of birth", "mother's maiden name",
                "passport number", "driver's license", "credit card number", "cvv", "pin number",
                "full name and address", "provide your details")) {
            score += 3;
            reasons.add("Personal information harvesting detected.");
        }

        // Impersonation of Authority
        if (containsAny(lowerText, "irs", "fbi", "police", "government", "federal agent",
                "tax authority", "ato", "hmrc", "microsoft support", "apple support",
                "your isp", "internet provider", "tech support", "legal action", "warrant")) {
            score += 2;
            reasons.add("Authority impersonation detected.");
        }

        // Too-Good-To-Be-True Offers
        if (containsAny(lowerText, "guaranteed", "risk free", "no obligation", "100% free",
                "earn from home", "work from home opportunity", "make money fast",
                "double your investment", "passive income", "financial freedom",
                "million dollar", "get rich", "zero risk")) {
            score += 2;
            reasons.add("Unrealistic offer language detected.");
        }

        // Secrecy / Pressure Tactics
        if (containsAny(lowerText, "do not tell", "keep this confidential", "tell no one",
                "do not share", "between us", "secret offer", "only you", "private deal",
                "don't contact your bank", "bypass", "avoid authorities")) {
            score += 3;
            reasons.add("Secrecy or pressure tactic detected.");
        }

        // Emotional Manipulation
        if (containsAny(lowerText, "dying", "critical condition", "stranded", "in trouble",
                "please help me", "i need your help urgently", "tragedy", "desperate",
                "my life depends", "emergency situation", "stuck abroad")) {
            score += 2;
            reasons.add("Emotional manipulation language detected.");
        }
        String riskLevel;
        if (score >= 4) {
            riskLevel = "This is a Scam";
        } else if (score >= 3) {
            riskLevel = "Likely a Scam";
        } else if (score >= 2) {
            riskLevel = "Suspicious";
        } else if (score >= 1) {
            riskLevel = "Should keep on Monitor";
        } else {
            riskLevel = "Safe";
        }

        if (reasons.isEmpty()) {
            reasons.add("No strong scam pattern was detected from local heuristics.");
        }

        return new AnalysisResult(riskLevel, String.join(" ", reasons), "");
    }

    private boolean containsAny(String text, String... patterns) {
        for (String pattern : patterns) {
            if (text.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private String requestLlamaAnalysis(String userInput, String localRiskLevel) throws IOException, JSONException {
        String prompt = "You are ScamGuard, a scam-detection assistant.\n"
                + "Classify this message as one of these labels only:\n"
                + "- This is a Scam\n"
                + "- Likely a Scam\n"
                + "- Suspicious\n"
                + "- Should keep on Monitor\n"
                + "- Safe\n"
                + "Include short reasons and one safety recommendation.\n"
                + "Local heuristic result: " + localRiskLevel + "\n"
                + "Message:\n" + userInput;

        JSONObject payload = new JSONObject();
        payload.put("model", OLLAMA_MODEL);
        payload.put("prompt", prompt);
        payload.put("stream", false);

        RequestBody requestBody = RequestBody.create(payload.toString(), JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(OLLAMA_ENDPOINT)
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code());
            }
            if (response.body() == null) {
                throw new IOException("Empty response body.");
            }

            JSONObject responseJson = new JSONObject(response.body().string());
            return responseJson.optString("response", "No model output received.");
        }
    }

    public static class AnalysisResult {
        public final String riskLevel;
        public final String reasoning;
        public final String llmResponse;

        public AnalysisResult(String riskLevel, String reasoning, String llmResponse) {
            this.riskLevel = riskLevel;
            this.reasoning = reasoning;
            this.llmResponse = llmResponse;
        }
    }
}
