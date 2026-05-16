package com.example.ailearningapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ailearningapp.databinding.FragmentLoginBinding;
import com.example.ailearningapp.model.UserAccount;
import com.example.ailearningapp.network.BackendSyncHelper;
import com.example.ailearningapp.storage.AuthSessionStorage;
import com.example.ailearningapp.storage.UserStorage;
import com.example.ailearningapp.state.SessionStore;
import com.example.ailearningapp.validation.InputValidator;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private boolean autoLoginHandled = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.usernameEditText.setHint(R.string.email_hint);
        tryAutoLogin();

        binding.loginButton.setOnClickListener(v -> {
            String email = binding.usernameEditText.getText() == null
                    ? ""
                    : binding.usernameEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText() == null
                    ? ""
                    : binding.passwordEditText.getText().toString();

            binding.usernameEditText.setError(null);
            binding.passwordEditText.setError(null);

            if (!InputValidator.isValidEmail(email)) {
                binding.usernameEditText.setError(getString(R.string.error_invalid_email));
                return;
            }

            if (TextUtils.isEmpty(password)) {
                binding.passwordEditText.setError(getString(R.string.error_password_required));
                return;
            }

            UserStorage userStorage = new UserStorage(requireContext());
            UserAccount account = userStorage.login(email, password);
            if (account == null) {
                Toast.makeText(requireContext(), R.string.error_login_failed, Toast.LENGTH_SHORT).show();
                return;
            }

            SessionStore.studentName = TextUtils.isEmpty(account.username)
                    ? getString(R.string.default_student_name)
                    : account.username;
            SessionStore.currentUserEmail = account.email == null ? "" : account.email;
            SessionStore.accountTier = TextUtils.isEmpty(account.accountTier) ? "Free" : account.accountTier;
            SessionStore.interests.clear();
            SessionStore.interests.addAll(account.interests);
            new AuthSessionStorage(requireContext()).setLoggedInEmail(SessionStore.currentUserEmail);
            BackendSyncHelper.syncCurrentUserSilently();
            NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_homeFragment);
        });

        binding.needAccountText.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_registerFragment));
    }

    private void tryAutoLogin() {
        if (autoLoginHandled || !isAdded()) {
            return;
        }
        autoLoginHandled = true;

        AuthSessionStorage authSessionStorage = new AuthSessionStorage(requireContext());
        String savedEmail = authSessionStorage.getLoggedInEmail();
        if (TextUtils.isEmpty(savedEmail)) {
            return;
        }

        UserStorage userStorage = new UserStorage(requireContext());
        UserAccount account = userStorage.getUserByEmail(savedEmail);
        if (account == null) {
            authSessionStorage.clear();
            return;
        }

        SessionStore.studentName = TextUtils.isEmpty(account.username)
                ? getString(R.string.default_student_name)
                : account.username;
        SessionStore.currentUserEmail = account.email == null ? "" : account.email;
        SessionStore.accountTier = TextUtils.isEmpty(account.accountTier) ? "Free" : account.accountTier;
        SessionStore.interests.clear();
        SessionStore.interests.addAll(account.interests);
        BackendSyncHelper.syncCurrentUserSilently();
        NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_homeFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
