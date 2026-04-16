package com.example.istream;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.istream.data.AppDatabase;
import com.example.istream.data.PlaylistItem;
import com.example.istream.data.SessionManager;
import com.example.istream.databinding.FragmentHomeBinding;
import com.example.istream.util.YoutubeUtils;

public class HomeFragment extends Fragment {
    public interface HomeNavigationListener {
        void onOpenPlaylist();

        void onLogout();
    }

    private static final String ARG_SELECTED_URL = "selected_url";
    private FragmentHomeBinding binding;
    private AppDatabase appDatabase;
    private SessionManager sessionManager;
    private String lastValidUrl;

    public static HomeFragment newInstance(@NonNull String selectedUrl) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_URL, selectedUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appDatabase = AppDatabase.getInstance(requireContext());
        sessionManager = new SessionManager(requireContext());

        if (!sessionManager.isLoggedIn()) {
            ((HomeNavigationListener) requireActivity()).onLogout();
            return;
        }

        configureWebView();

        Bundle args = getArguments();
        if (args != null) {
            String selectedUrl = args.getString(ARG_SELECTED_URL);
            if (!TextUtils.isEmpty(selectedUrl)) {
                binding.etVideoUrl.setText(selectedUrl);
                playVideo(selectedUrl);
            }
        }

        binding.btnPlay.setOnClickListener(v -> playVideo(binding.etVideoUrl.getText().toString().trim()));
        binding.btnAddToPlaylist.setOnClickListener(v -> addCurrentUrlToPlaylist());
        binding.btnMyPlaylist.setOnClickListener(v ->
                ((HomeNavigationListener) requireActivity()).onOpenPlaylist()
        );
        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            ((HomeNavigationListener) requireActivity()).onLogout();
        });
    }

    private void configureWebView() {
        WebSettings settings = binding.webViewPlayer.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
    }

    private void playVideo(String url) {
        String videoId = YoutubeUtils.extractVideoId(url);
        if (videoId == null) {
            Toast.makeText(requireContext(), R.string.invalid_youtube_url, Toast.LENGTH_SHORT).show();
            return;
        }
        lastValidUrl = url;
        String html = YoutubeUtils.buildEmbedHtml(videoId);
        binding.webViewPlayer.loadDataWithBaseURL(
                "https://com.example.istream/",
                html,
                "text/html",
                "utf-8",
                null
        );
    }

    private void addCurrentUrlToPlaylist() {
        if (TextUtils.isEmpty(lastValidUrl)) {
            String typedUrl = binding.etVideoUrl.getText().toString().trim();
            if (YoutubeUtils.extractVideoId(typedUrl) == null) {
                Toast.makeText(requireContext(), R.string.play_valid_url_first, Toast.LENGTH_SHORT).show();
                return;
            }
            lastValidUrl = typedUrl;
        }

        appDatabase.playlistDao().insert(new PlaylistItem(sessionManager.getUserId(), lastValidUrl));
        Toast.makeText(requireContext(), R.string.added_to_playlist, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
