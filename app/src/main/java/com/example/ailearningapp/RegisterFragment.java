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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ailearningapp.databinding.FragmentRegisterBinding;
import com.example.ailearningapp.model.UserAccount;
import com.example.ailearningapp.network.BackendSyncHelper;
import com.example.ailearningapp.storage.AuthSessionStorage;
import com.example.ailearningapp.storage.UserStorage;
import com.example.ailearningapp.state.SessionStore;
import com.example.ailearningapp.validation.InputValidator;

import java.util.ArrayList;
import java.util.Arrays;

public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding binding;
    private boolean isNavigating;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.backToLoginButton.setOnClickListener(v ->
                navigateIfCurrent(R.id.registerFragment, R.id.action_registerFragment_to_loginFragment));

        binding.createAccountButton.setOnClickListener(v -> {
            if (isNavigating) {
                return;
            }

            String username = getText(binding.registerUsernameEditText);
            String email = getText(binding.registerEmailEditText);
            String confirmEmail = getText(binding.registerConfirmEmailEditText);
            String password = getText(binding.registerPasswordEditText);
            String confirmPassword = getText(binding.registerConfirmPasswordEditText);
            String phone = getText(binding.registerPhoneEditText);

            if (!validateInputs(username, email, confirmEmail, password, confirmPassword, phone)) {
                return;
            }

            UserAccount userAccount = new UserAccount();
            userAccount.username = username;
            userAccount.email = email;
            userAccount.password = password;
            userAccount.phoneNumber = phone;
            userAccount.accountTier = "Free";
            userAccount.interests = new ArrayList<>(Arrays.asList(
                    getString(R.string.interest_algorithms),
                    getString(R.string.interest_testing)
            ));

            UserStorage userStorage = new UserStorage(requireContext());
            if (!userStorage.saveUser(userAccount)) {
                binding.registerEmailEditText.setError(getString(R.string.error_email_exists));
                Toast.makeText(requireContext(), R.string.error_email_exists, Toast.LENGTH_SHORT).show();
                return;
            }

            SessionStore.studentName = username;
            SessionStore.currentUserEmail = email;
            SessionStore.accountTier = "Free";
            SessionStore.interests.clear();
            SessionStore.interests.addAll(userAccount.interests);
            new AuthSessionStorage(requireContext()).setLoggedInEmail(email);
            BackendSyncHelper.syncCurrentUserSilently();
            Toast.makeText(requireContext(), R.string.success_account_created, Toast.LENGTH_SHORT).show();
            navigateIfCurrent(R.id.registerFragment, R.id.action_registerFragment_to_interestsFragment);
        });
    }

    private void navigateIfCurrent(int expectedDestinationId, int actionId) {
        if (!isAdded()) {
            return;
        }
        NavController navController = NavHostFragment.findNavController(this);
        if (navController.getCurrentDestination() == null || navController.getCurrentDestination().getId() != expectedDestinationId) {
            return;
        }
        isNavigating = true;
        binding.createAccountButton.setEnabled(false);
        binding.backToLoginButton.setEnabled(false);
        navController.navigate(actionId);
    }

    private boolean validateInputs(String username, String email, String confirmEmail, String password, String confirmPassword, String phone) {
        binding.registerUsernameEditText.setError(null);
        binding.registerEmailEditText.setError(null);
        binding.registerConfirmEmailEditText.setError(null);
        binding.registerPasswordEditText.setError(null);
        binding.registerConfirmPasswordEditText.setError(null);
        binding.registerPhoneEditText.setError(null);

        if (TextUtils.isEmpty(username)) {
            binding.registerUsernameEditText.setError(getString(R.string.error_username_required));
            return false;
        }

        if (!InputValidator.isValidEmail(email)) {
            binding.registerEmailEditText.setError(getString(R.string.error_invalid_email));
            return false;
        }

        if (!TextUtils.equals(email.trim(), confirmEmail.trim())) {
            binding.registerConfirmEmailEditText.setError(getString(R.string.error_email_mismatch));
            return false;
        }

        if (!InputValidator.isValidPassword(password)) {
            binding.registerPasswordEditText.setError(getString(R.string.error_invalid_password));
            return false;
        }

        if (!TextUtils.equals(password, confirmPassword)) {
            binding.registerConfirmPasswordEditText.setError(getString(R.string.error_password_mismatch));
            return false;
        }

        if (!TextUtils.isEmpty(phone) && phone.replaceAll("\\D", "").length() < 8) {
            binding.registerPhoneEditText.setError(getString(R.string.error_invalid_phone));
            return false;
        }

        return true;
    }

    private String getText(android.widget.EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isNavigating = false;
        binding = null;
    }
}
