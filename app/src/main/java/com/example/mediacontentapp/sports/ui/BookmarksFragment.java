package com.example.mediacontentapp.sports.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Set;

public class BookmarksFragment extends Fragment {

    public interface BookmarkNavigationListener {
        void onNewsSelected(@NonNull NewsItem newsItem);

        void onNavigateBack();
    }

    private BookmarkNavigationListener navigationListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BookmarkNavigationListener) {
            navigationListener = (BookmarkNavigationListener) context;
        } else {
            throw new IllegalStateException("Host activity must implement BookmarkNavigationListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookmarks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton backButton = view.findViewById(R.id.bookmark_back_button);
        RecyclerView recyclerView = view.findViewById(R.id.bookmark_recycler);
        TextView emptyText = view.findViewById(R.id.empty_bookmark_text);

        backButton.setOnClickListener(v -> navigationListener.onNavigateBack());

        NewsAdapter adapter = new NewsAdapter(
                R.layout.item_latest_news,
                item -> navigationListener.onNewsSelected(item)
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        List<NewsItem> bookmarkedItems = getBookmarkedItems(new BookmarkStore(requireContext()));
        adapter.submitList(bookmarkedItems);
        emptyText.setVisibility(bookmarkedItems.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @NonNull
    private List<NewsItem> getBookmarkedItems(@NonNull BookmarkStore store) {
        Set<String> bookmarkedIds = store.getBookmarks();
        List<NewsItem> list = new ArrayList<>();
        for (NewsItem item : SportsNewsRepository.getAllNews()) {
            if (bookmarkedIds.contains(String.valueOf(item.getId()))) {
                list.add(item);
            }
        }
        return list;
    }
}
