package com.example.ailearningapp.model;

import com.google.gson.annotations.SerializedName;

public class HintResponse {
    @SerializedName(value = "prompt", alternate = {"promptUsed"})
    public String promptUsed;
    @SerializedName(value = "response", alternate = {"hint"})
    public String response;
}
