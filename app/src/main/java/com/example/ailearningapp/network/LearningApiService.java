package com.example.ailearningapp.network;

import com.example.ailearningapp.model.ExplainResponse;
import com.example.ailearningapp.model.GeneratedTaskResponse;
import com.example.ailearningapp.model.HistoryResponse;
import com.example.ailearningapp.model.HintResponse;
import com.example.ailearningapp.model.ShareLinkResponse;
import com.example.ailearningapp.network.dto.ExplainAnswersRequest;
import com.example.ailearningapp.network.dto.GenerateTaskRequest;
import com.example.ailearningapp.network.dto.HistoryAppendRequest;
import com.example.ailearningapp.network.dto.HintRequest;
import com.example.ailearningapp.network.dto.PurchaseRequest;
import com.example.ailearningapp.network.dto.ShareLinkRequest;
import com.example.ailearningapp.network.dto.UserUpsertRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LearningApiService {
    @POST("api/learning/generate-task")
    Call<GeneratedTaskResponse> generateTask(@Body GenerateTaskRequest request);

    @POST("api/learning/generate-hint")
    Call<HintResponse> generateHint(@Body HintRequest request);

    @POST("api/learning/explain-answers")
    Call<ExplainResponse> explainAnswers(@Body ExplainAnswersRequest request);

    @POST("api/users/upsert")
    Call<Void> upsertUser(@Body UserUpsertRequest request);

    @POST("api/history/append")
    Call<Void> appendHistory(@Body HistoryAppendRequest request);

    @GET("api/history/{email}")
    Call<HistoryResponse> getHistory(@Path("email") String email);

    @POST("api/share/create-link")
    Call<ShareLinkResponse> createShareLink(@Body ShareLinkRequest request);

    @POST("api/purchase/upgrade")
    Call<Void> purchaseUpgrade(@Body PurchaseRequest request);
}
