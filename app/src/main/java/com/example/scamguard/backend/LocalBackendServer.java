package com.example.scamguard.backend;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class LocalBackendServer extends NanoHTTPD {

    public static final int PORT = 8787;
    private final AnalysisEngine analysisEngine;

    public LocalBackendServer() {
        super("127.0.0.1", PORT);
        this.analysisEngine = new AnalysisEngine();
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            String uri = session.getUri();
            Method method = session.getMethod();

            if ("/api/health".equals(uri) && Method.GET.equals(method)) {
                return jsonOk(new JSONObject().put("status", "ok"));
            }

            if ("/api/analyze".equals(uri) && Method.POST.equals(method)) {
                Map<String, String> bodyFiles = new HashMap<>();
                session.parseBody(bodyFiles);
                String body = bodyFiles.get("postData");
                if (body == null || body.trim().isEmpty()) {
                    return jsonBadRequest("Empty request body.");
                }

                JSONObject request = new JSONObject(body);
                String content = request.optString("content", "").trim();
                boolean useLlama = request.optBoolean("useLlama", false);

                if (content.isEmpty()) {
                    return jsonBadRequest("`content` is required.");
                }

                AnalysisEngine.AnalysisResult result = analysisEngine.analyze(content, useLlama);

                JSONObject response = new JSONObject();
                response.put("riskLevel", result.riskLevel);
                response.put("reasoning", result.reasoning);
                response.put("llmResponse", result.llmResponse);

                return jsonOk(response);
            }

            return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json",
                    "{\"error\":\"Not found\"}");
        } catch (Exception exception) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"" + sanitize(exception.getMessage()) + "\"}");
        }
    }

    private Response jsonOk(JSONObject body) {
        return newFixedLengthResponse(Response.Status.OK, "application/json", body.toString());
    }

    private Response jsonBadRequest(String message) {
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                "{\"error\":\"" + sanitize(message) + "\"}");
    }

    private String sanitize(String value) {
        if (value == null) {
            return "Unknown error";
        }
        return value.replace("\"", "'");
    }
}
