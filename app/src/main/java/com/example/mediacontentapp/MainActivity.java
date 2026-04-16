package com.example.mediacontentapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mediacontentapp.sports.model.NewsItem;
import com.example.mediacontentapp.sports.ui.BookmarksFragment;
import com.example.mediacontentapp.sports.ui.HomeFragment;
import com.example.mediacontentapp.sports.ui.NewsDetailFragment;

public class MainActivity extends AppCompatActivity implements
        HomeFragment.HomeNavigationListener,
        NewsDetailFragment.DetailNavigationListener,
        BookmarksFragment.BookmarkNavigationListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            showFragment(new HomeFragment(), false);
        }
    }

    @Override
    public void onNewsSelected(@NonNull NewsItem newsItem) {
        showFragment(NewsDetailFragment.newInstance(newsItem.getId()), true);
    }

    @Override
    public void onOpenBookmarks() {
        showFragment(new BookmarksFragment(), true);
    }

    @Override
    public void onNavigateBack() {
        getOnBackPressedDispatcher().onBackPressed();
    }

    private void showFragment(@NonNull Fragment fragment, boolean addToBackStack) {
        var transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }
}
