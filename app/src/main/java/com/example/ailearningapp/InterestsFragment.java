package com.example.ailearningapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ailearningapp.databinding.FragmentInterestsBinding;
import com.example.ailearningapp.model.UserAccount;
import com.example.ailearningapp.network.BackendSyncHelper;
import com.example.ailearningapp.storage.UserStorage;
import com.example.ailearningapp.state.SessionStore;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class InterestsFragment extends Fragment {
    private FragmentInterestsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInterestsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        makeAllChipsCheckable();

        binding.interestsNextButton.setOnClickListener(v -> {
            SessionStore.interests.clear();
            SessionStore.interests.addAll(getSelectedInterests());
            if (SessionStore.interests.isEmpty()) {
                SessionStore.interests.add(getString(R.string.interest_algorithms));
                SessionStore.interests.add(getString(R.string.interest_testing));
            }

            if (!SessionStore.currentUserEmail.isEmpty()) {
                UserStorage userStorage = new UserStorage(requireContext());
                UserAccount account = userStorage.getUserByEmail(SessionStore.currentUserEmail);
                if (account != null) {
                    account.interests = new ArrayList<>(SessionStore.interests);
                    userStorage.updateUser(account);
                }
            }
            BackendSyncHelper.syncCurrentUserSilently();
            NavHostFragment.findNavController(this).navigate(R.id.action_interestsFragment_to_homeFragment);
        });
    }

    private void makeAllChipsCheckable() {
        int childCount = binding.interestsChipGroup.getChildCount();
        for (int index = 0; index < childCount; index++) {
            View child = binding.interestsChipGroup.getChildAt(index);
            if (child instanceof Chip) {
                ((Chip) child).setCheckable(true);
            }
        }
    }

    private List<String> getSelectedInterests() {
        List<String> selected = new ArrayList<>();
        int childCount = binding.interestsChipGroup.getChildCount();
        for (int index = 0; index < childCount; index++) {
            View child = binding.interestsChipGroup.getChildAt(index);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.isChecked()) {
                    selected.add(chip.getText().toString());
                }
            }
        }
        return selected;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
