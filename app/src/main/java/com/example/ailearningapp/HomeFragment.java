package com.example.ailearningapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ailearningapp.databinding.FragmentHomeBinding;
import com.example.ailearningapp.state.SessionStore;
import com.example.ailearningapp.storage.AuthSessionStorage;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.homeGreetingText.setText(getString(R.string.home_greeting, SessionStore.studentName));
        binding.accountTierText.setText(getString(R.string.account_tier_format, SessionStore.accountTier));

        binding.openTaskButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_quizFragment));
        binding.profileButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_profileFragment));
        binding.historyButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_historyFragment));
        binding.upgradeButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_upgradeFragment));

        binding.logoutButton.setOnClickListener(v -> {
            SessionStore.clearSession();
            new AuthSessionStorage(requireContext()).clear();
            NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_loginFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
