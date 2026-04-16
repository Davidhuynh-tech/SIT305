package com.example.istream;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.istream.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements
        LoginFragment.LoginNavigationListener,
        SignupFragment.SignupNavigationListener,
        HomeFragment.HomeNavigationListener,
        PlaylistFragment.PlaylistNavigationListener {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            showFragment(new LoginFragment(), false);
        }
    }

    @Override
    public void onOpenSignup() {
        showFragment(new SignupFragment(), true);
    }

    @Override
    public void onLoginSuccess() {
        showFragment(new HomeFragment(), false);
    }

    @Override
    public void onSignupComplete() {
        showFragment(new LoginFragment(), false);
    }

    @Override
    public void onBackToLogin() {
        showFragment(new LoginFragment(), false);
    }

    @Override
    public void onOpenPlaylist() {
        showFragment(new PlaylistFragment(), true);
    }

    @Override
    public void onLogout() {
        showFragment(new LoginFragment(), false);
    }

    @Override
    public void onPlaylistVideoSelected(@NonNull String url) {
        showFragment(HomeFragment.newInstance(url), false);
    }

    @Override
    public void onBackToHome() {
        getOnBackPressedDispatcher().onBackPressed();
    }

    private void showFragment(@NonNull Fragment fragment, boolean addToBackStack) {
        var transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        } else {
            getSupportFragmentManager().popBackStack(
                    null,
                    androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
            );
        }
        transaction.commit();
    }
}