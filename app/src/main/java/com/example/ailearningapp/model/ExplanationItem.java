package com.example.ailearningapp.model;

import java.io.Serializable;

public class ExplanationItem implements Serializable {
    public String question;
    public String selectedAnswer;
    public String explanation;
    public boolean correct;
}
