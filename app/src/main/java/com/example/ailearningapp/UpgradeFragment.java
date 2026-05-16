package com.example.ailearningapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ailearningapp.databinding.FragmentUpgradeBinding;
import com.example.ailearningapp.model.UserAccount;
import com.example.ailearningapp.network.ApiClient;
import com.example.ailearningapp.network.BackendSyncHelper;
import com.example.ailearningapp.network.dto.PurchaseRequest;
import com.example.ailearningapp.state.SessionStore;
import com.example.ailearningapp.storage.UserStorage;

public class UpgradeFragment extends Fragment {
    private FragmentUpgradeBinding binding;
    private boolean purchaseInProgress = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUpgradeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updatePurchaseButtonLabel();
        binding.purchaseButton.setOnClickListener(v -> togglePlan());
        binding.backFromUpgradeButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
    }

    private void togglePlan() {
        if (purchaseInProgress) {
            return;
        }

        if (SessionStore.currentUserEmail == null || SessionStore.currentUserEmail.trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.purchase_login_required, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isCurrentlyPro = "Pro".equalsIgnoreCase(SessionStore.accountTier);
        String newPlan = isCurrentlyPro ? "Free" : "Pro";
        purchaseInProgress = true;
        binding.purchaseButton.setEnabled(false);

        PurchaseRequest request = new PurchaseRequest();
        request.email = SessionStore.currentUserEmail;
        request.plan = newPlan;
        ApiClient.getLearningApiService().purchaseUpgrade(request).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                purchaseInProgress = false;
                if (!isAdded()) {
                    return;
                }
                binding.purchaseButton.setEnabled(true);
                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), R.string.purchase_sync_failed, Toast.LENGTH_SHORT).show();
                    return;
                }

                SessionStore.accountTier = newPlan;
                UserStorage userStorage = new UserStorage(requireContext());
                UserAccount user = userStorage.getUserByEmail(SessionStore.currentUserEmail);
                if (user != null) {
                    user.accountTier = newPlan;
                    userStorage.updateUser(user);
                }

                BackendSyncHelper.syncCurrentUserSilently();
                updatePurchaseButtonLabel();
                Toast.makeText(
                        requireContext(),
                        isCurrentlyPro ? R.string.downgrade_success : R.string.purchase_success,
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                purchaseInProgress = false;
                if (!isAdded()) {
                    return;
                }
                binding.purchaseButton.setEnabled(true);
                Toast.makeText(requireContext(), R.string.purchase_sync_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePurchaseButtonLabel() {
        boolean isCurrentlyPro = "Pro".equalsIgnoreCase(SessionStore.accountTier);
        binding.purchaseButton.setText(isCurrentlyPro
                ? getString(R.string.switch_to_basic_button)
                : getString(R.string.purchase_button));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
