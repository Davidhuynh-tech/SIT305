package com.example.mediacontentapp.sports.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mediacontentapp.R;
import com.example.mediacontentapp.sports.data.BookmarkStore;
import com.example.mediacontentapp.sports.data.SportsNewsRepository;
import com.example.mediacontentapp.sports.model.NewsItem;
import com.example.mediacontentapp.sports.ui.adapter.NewsAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class NewsDetailFragment extends Fragment {
    private static final String ARG_NEWS_ID = "arg_news_id";

    public interface DetailNavigationListener {
        void onNewsSelected(@NonNull NewsItem newsItem);

        void onNavigateBack();
    }

    private DetailNavigationListener navigationListener;
    private BookmarkStore bookmarkStore;
    private NewsItem currentItem;
    private MaterialButton bookmarkButton;

    public static NewsDetailFragment newInstance(int newsId) {
        NewsDetailFragment fragment = new NewsDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_NEWS_ID, newsId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DetailNavigationListener) {
            navigationListener = (DetailNavigationListener) context;
        } else {
            throw new IllegalStateException("Host activity must implement DetailNavigationListener");
        }
        bookmarkStore = new BookmarkStore(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int id = getArguments() != null ? getArguments().getInt(ARG_NEWS_ID, -1) : -1;
        currentItem = findNewsById(id);
        if (currentItem == null) {
            navigationListener.onNavigateBack();
            return;
        }

        MaterialButton backButton = view.findViewById(R.id.back_button);
        bookmarkButton = view.findViewById(R.id.bookmark_button);
        ImageView image = view.findViewById(R.id.detail_image);
        TextView title = view.findViewById(R.id.detail_title);
        TextView description = view.findViewById(R.id.detail_description);
        RecyclerView relatedRecycler = view.findViewById(R.id.related_recycler);

        backButton.setOnClickListener(v -> navigationListener.onNavigateBack());
        bookmarkButton.setOnClickListener(v -> toggleBookmark());

        image.setImageResource(currentItem.getImageResId());
        title.setText(currentItem.getTitle());
        description.setText(currentItem.getDescription());
        updateBookmarkButton();

        NewsAdapter relatedAdapter = new NewsAdapter(
                R.layout.item_latest_news,
                item -> navigationListener.onNewsSelected(item)
        );
        relatedRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        relatedRecycler.setAdapter(relatedAdapter);
        relatedAdapter.submitList(getRelatedStories(currentItem));
    }

    private void toggleBookmark() {
        bookmarkStore.toggleBookmark(currentItem.getId());
        updateBookmarkButton();
    }

    private void updateBookmarkButton() {
        boolean bookmarked = bookmarkStore.isBookmarked(currentItem.getId());
        bookmarkButton.setText(bookmarked
                ? R.string.remove_bookmark
                : R.string.bookmark_story);
    }

    @Nullable
    private NewsItem findNewsById(int id) {
        for (NewsItem item : SportsNewsRepository.getAllNews()) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    @NonNull
    private List<NewsItem> getRelatedStories(@NonNull NewsItem source) {
        List<NewsItem> related = new ArrayList<>();
        for (NewsItem item : SportsNewsRepository.getAllNews()) {
            if (item.getId() == source.getId()) {
                continue;
            }
            if (item.getCategory().equals(source.getCategory())) {
                related.add(item);
            }
        }
        return related;
    }
}
