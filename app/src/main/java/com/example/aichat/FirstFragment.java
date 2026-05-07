package com.example.aichat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.aichat.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String storedUsername = requireContext()
                .getSharedPreferences("user_session_prefs", android.content.Context.MODE_PRIVATE)
                .getString("username", "");

        if (storedUsername != null && !storedUsername.isEmpty()) {
            navigateToChat(storedUsername);
            return;
        }

        binding.loginButton.setOnClickListener(v -> {
            String username = binding.usernameInput.getText() == null
                    ? ""
                    : binding.usernameInput.getText().toString().trim();

            if (username.isEmpty()) {
                binding.usernameInput.setError(getString(R.string.username_error));
                return;
            }

            requireContext()
                    .getSharedPreferences("user_session_prefs", android.content.Context.MODE_PRIVATE)
                    .edit()
                    .putString("username", username)
                    .apply();

            binding.usernameInput.setError(null);
            navigateToChat(username);
        });
    }

    private void navigateToChat(String username) {
        Bundle args = new Bundle();
        args.putString("username", username);
        NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}