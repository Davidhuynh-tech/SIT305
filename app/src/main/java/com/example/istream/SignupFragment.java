package com.example.istream;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.istream.data.AppDatabase;
import com.example.istream.data.User;
import com.example.istream.databinding.FragmentSignupBinding;

public class SignupFragment extends Fragment {
    public interface SignupNavigationListener {
        void onSignupComplete();

        void onBackToLogin();
    }

    private static final String PASSWORD_RULE = "^(?=.*[A-Z])(?=.*\\d).+$";
    private FragmentSignupBinding binding;
    private AppDatabase appDatabase;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appDatabase = AppDatabase.getInstance(requireContext());

        binding.btnSignup.setOnClickListener(v -> signup());
        binding.btnGoToLogin.setOnClickListener(v ->
                ((SignupNavigationListener) requireActivity()).onBackToLogin()
        );
    }

    private void signup() {
        String fullName = binding.etFullName.getText().toString().trim();
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(username)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(requireContext(), R.string.fill_signup_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (username.length() < 2) {
            Toast.makeText(requireContext(), R.string.username_min_length, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.matches(PASSWORD_RULE)) {
            Toast.makeText(requireContext(), R.string.password_rule_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(requireContext(), R.string.password_not_match, Toast.LENGTH_SHORT).show();
            return;
        }
        if (appDatabase.userDao().findByUsername(username) != null) {
            Toast.makeText(requireContext(), R.string.username_exists, Toast.LENGTH_SHORT).show();
            return;
        }

        appDatabase.userDao().insert(new User(fullName, username, password));
        Toast.makeText(requireContext(), R.string.signup_success, Toast.LENGTH_SHORT).show();
        ((SignupNavigationListener) requireActivity()).onSignupComplete();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
