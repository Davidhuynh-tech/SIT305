package com.example.scamguard;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.scamguard.backend.LocalBackendServer;
import com.example.scamguard.databinding.ActivityMainBinding;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private LocalBackendServer localBackendServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        startLocalBackend();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (localBackendServer != null) {
            localBackendServer.stop();
        }
    }

    private void startLocalBackend() {
        localBackendServer = new LocalBackendServer();
        try {
            localBackendServer.start(LocalBackendServer.SOCKET_READ_TIMEOUT, false);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to start local backend server.", exception);
        }
    }
}