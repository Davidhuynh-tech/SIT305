package com.example.ailearningapp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GeneratedTaskResponse implements Serializable {
    @SerializedName(value = "prompt", alternate = {"promptUsed"})
    public String promptUsed;
    public List<QuestionItem> questions = new ArrayList<>();
}
