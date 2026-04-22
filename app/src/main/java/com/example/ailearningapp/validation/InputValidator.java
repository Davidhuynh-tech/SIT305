package com.example.ailearningapp.validation;

import android.util.Patterns;

public final class InputValidator {
    private InputValidator() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (char value : password.toCharArray()) {
            if (Character.isUpperCase(value)) {
                hasUpper = true;
            } else if (Character.isLowerCase(value)) {
                hasLower = true;
            } else if (Character.isDigit(value)) {
                hasDigit = true;
            }
        }

        return hasUpper && hasLower && hasDigit;
    }
}
