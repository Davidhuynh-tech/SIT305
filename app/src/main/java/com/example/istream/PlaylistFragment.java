package com.example.istream;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.istream.data.AppDatabase;
import com.example.istream.data.PlaylistItem;
import com.example.istream.data.SessionManager;
import com.example.istream.databinding.FragmentPlaylistBinding;

import java.util.List;

public class PlaylistFragment extends Fragment {
    public interface PlaylistNavigationListener {
        void onPlaylistVideoSelected(@NonNull String url);

        void onBackToHome();

        void onLogout();
    }

    private FragmentPlaylistBinding binding;
    private AppDatabase appDatabase;
    private SessionManager sessionManager;
    private PlaylistAdapter playlistAdapter;
    private PlaylistNavigationListener navigationListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PlaylistNavigationListener) {
            navigationListener = (PlaylistNavigationListener) context;
        } else {
            throw new IllegalStateException("Host activity must implement PlaylistNavigationListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appDatabase = AppDatabase.getInstance(requireContext());
        sessionManager = new SessionManager(requireContext());

        if (!sessionManager.isLoggedIn()) {
            navigationListener.onLogout();
            return;
        }

        playlistAdapter = new PlaylistAdapter(this::openInHome);
        binding.recyclerPlaylist.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerPlaylist.setAdapter(playlistAdapter);

        binding.btnBackHome.setOnClickListener(v ->
                navigationListener.onBackToHome()
        );
        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            navigationListener.onLogout();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            List<PlaylistItem> items = appDatabase.playlistDao().getPlaylistForUser(sessionManager.getUserId());
            playlistAdapter.submitList(items);
            binding.tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void openInHome(PlaylistItem item) {
        navigationListener.onPlaylistVideoSelected(item.videoUrl);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
    }
}
