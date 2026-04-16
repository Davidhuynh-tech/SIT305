package com.example.mediacontentapp.sports.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mediacontentapp.R;
import com.example.mediacontentapp.sports.model.NewsItem;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    public interface OnNewsClickListener {
        void onNewsClick(@NonNull NewsItem item);
    }

    private final List<NewsItem> items = new ArrayList<>();
    private final OnNewsClickListener clickListener;
    private final int layoutRes;

    public NewsAdapter(int layoutRes, @NonNull OnNewsClickListener clickListener) {
        this.layoutRes = layoutRes;
        this.clickListener = clickListener;
    }

    public void submitList(@NonNull List<NewsItem> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.category.setText(item.getCategory());
        holder.image.setImageResource(item.getImageResId());
        holder.itemView.setOnClickListener(v -> clickListener.onNewsClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView category;
        final ImageView image;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.news_title);
            category = itemView.findViewById(R.id.news_category);
            image = itemView.findViewById(R.id.news_image);
        }
    }
}
