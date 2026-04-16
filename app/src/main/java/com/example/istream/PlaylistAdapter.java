package com.example.istream;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.istream.data.PlaylistItem;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(PlaylistItem item);
    }

    private final List<PlaylistItem> items = new ArrayList<>();
    private final OnItemClickListener onItemClickListener;

    public PlaylistAdapter(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void submitList(List<PlaylistItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        PlaylistItem item = items.get(position);
        holder.textView.setText(item.videoUrl);
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
