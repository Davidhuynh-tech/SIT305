package com.example.ailearningapp.model;

import java.util.ArrayList;
import java.util.List;

public class UserAccount {
    public String username;
    public String email;
    public String password;
    public String phoneNumber;
    public String accountTier = "Free";
    public List<String> interests = new ArrayList<>();
}
