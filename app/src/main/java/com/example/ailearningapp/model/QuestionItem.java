package com.example.ailearningapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QuestionItem implements Serializable {
    public String id;
    public String question;
    public List<String> options = new ArrayList<>();
    public int correctIndex;
}
