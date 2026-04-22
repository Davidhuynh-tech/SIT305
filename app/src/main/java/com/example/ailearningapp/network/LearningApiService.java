package com.example.ailearningapp.network;

import com.example.ailearningapp.model.ExplainResponse;
import com.example.ailearningapp.model.GeneratedTaskResponse;
import com.example.ailearningapp.model.HintResponse;
import com.example.ailearningapp.network.dto.ExplainAnswersRequest;
import com.example.ailearningapp.network.dto.GenerateTaskRequest;
import com.example.ailearningapp.network.dto.HintRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LearningApiService {
    @POST("api/learning/generate-task")
    Call<GeneratedTaskResponse> generateTask(@Body GenerateTaskRequest request);

    @POST("api/learning/generate-hint")
    Call<HintResponse> generateHint(@Body HintRequest request);

    @POST("api/learning/explain-answers")
    Call<ExplainResponse> explainAnswers(@Body ExplainAnswersRequest request);
}
