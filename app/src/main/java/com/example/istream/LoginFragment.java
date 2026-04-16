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
import com.example.istream.data.SessionManager;
import com.example.istream.data.User;
import com.example.istream.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {
    public interface LoginNavigationListener {
        void onOpenSignup();

        void onLoginSuccess();
    }

    private FragmentLoginBinding binding;
    private AppDatabase appDatabase;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appDatabase = AppDatabase.getInstance(requireContext());
        sessionManager = new SessionManager(requireContext());

        if (sessionManager.isLoggedIn()) {
            ((LoginNavigationListener) requireActivity()).onLoginSuccess();
            return;
        }

        binding.btnLogin.setOnClickListener(v -> login());
        binding.btnGoToSignup.setOnClickListener(v ->
                ((LoginNavigationListener) requireActivity()).onOpenSignup()
        );
    }

    private void login() {
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(requireContext(), R.string.fill_login_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        User user = appDatabase.userDao().login(username, password);
        if (user == null) {
            Toast.makeText(requireContext(), R.string.invalid_credentials, Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.login(user.id, user.username);
        ((LoginNavigationListener) requireActivity()).onLoginSuccess();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
