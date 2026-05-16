package com.example.ailearningapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ExplainResponse {
    @SerializedName(value = "prompt", alternate = {"promptUsed"})
    public String promptUsed;
    public List<ExplanationItem> explanations = new ArrayList<>();
}
