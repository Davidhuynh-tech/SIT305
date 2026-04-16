package com.example.mediacontentapp.sports.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mediacontentapp.R;
import com.example.mediacontentapp.sports.data.SportsNewsRepository;
import com.example.mediacontentapp.sports.model.NewsItem;
import com.example.mediacontentapp.sports.ui.adapter.NewsAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    public interface HomeNavigationListener {
        void onNewsSelected(@NonNull NewsItem newsItem);

        void onOpenBookmarks();
    }

    private HomeNavigationListener navigationListener;
    private NewsAdapter featuredAdapter;
    private NewsAdapter latestAdapter;
    private List<NewsItem> allNews;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof HomeNavigationListener) {
            navigationListener = (HomeNavigationListener) context;
        } else {
            throw new IllegalStateException("Host activity must implement HomeNavigationListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        allNews = SportsNewsRepository.getAllNews();

        RecyclerView featuredRecycler = view.findViewById(R.id.featured_recycler);
        RecyclerView latestRecycler = view.findViewById(R.id.latest_recycler);
        EditText filterInput = view.findViewById(R.id.filter_input);
        MaterialButton bookmarkButton = view.findViewById(R.id.open_bookmarks_button);

        featuredAdapter = new NewsAdapter(R.layout.item_featured_news, item -> navigationListener.onNewsSelected(item));
        latestAdapter = new NewsAdapter(R.layout.item_latest_news, item -> navigationListener.onNewsSelected(item));

        featuredRecycler.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        featuredRecycler.setAdapter(featuredAdapter);

        latestRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        latestRecycler.setAdapter(latestAdapter);

        bookmarkButton.setOnClickListener(v -> navigationListener.onOpenBookmarks());

        filterInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNews(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        filterNews("");
    }

    private void filterNews(@NonNull String filterText) {
        String query = filterText.trim().toLowerCase(Locale.US);
        List<NewsItem> featured = new ArrayList<>();
        List<NewsItem> latest = new ArrayList<>();

        for (NewsItem item : allNews) {
            boolean matches = query.isEmpty()
                    || item.getCategory().toLowerCase(Locale.US).contains(query)
                    || item.getTitle().toLowerCase(Locale.US).contains(query);

            if (!matches) {
                continue;
            }

            if (item.isFeatured()) {
                featured.add(item);
            } else {
                latest.add(item);
            }
        }

        featuredAdapter.submitList(featured);
        latestAdapter.submitList(latest);
    }
}
