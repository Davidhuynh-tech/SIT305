package com.example.ailearningapp.model;

import java.io.Serializable;

public class SubmittedAnswer implements Serializable {
    public String questionId;
    public String questionText;
    public String selectedAnswer;
    public String correctAnswer;
}
