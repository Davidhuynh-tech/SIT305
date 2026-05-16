package com.example.ailearningapp.network.dto;

import java.util.List;

public class UserUpsertRequest {
    public String email;
    public String username;
    public String phoneNumber;
    public String accountTier;
    public List<String> interests;
}
