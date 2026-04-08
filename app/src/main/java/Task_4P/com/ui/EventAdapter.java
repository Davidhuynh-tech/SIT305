package Task_4P.com.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Task_4P.com.R;
import Task_4P.com.data.Event;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    public interface EventActionListener {
        void onEdit(Event event);
        void onDelete(Event event);
    }

    private final EventActionListener listener;
    private final List<Event> items = new ArrayList<>();

    public EventAdapter(EventActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Event> events) {
        items.clear();
        if (events != null) {
            items.addAll(events);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = items.get(position);
        holder.titleText.setText(event.title);
        holder.categoryText.setText(event.category == null ? "" : event.category);
        holder.locationText.setText(event.location == null ? "" : event.location);
        holder.dateTimeText.setText(DateFormat.getDateTimeInstance().format(new Date(event.dateTimeMillis)));

        holder.editButton.setOnClickListener(v -> listener.onEdit(event));
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(event));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        final TextView titleText;
        final TextView categoryText;
        final TextView locationText;
        final TextView dateTimeText;
        final MaterialButton editButton;
        final MaterialButton deleteButton;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.title_text);
            categoryText = itemView.findViewById(R.id.category_text);
            locationText = itemView.findViewById(R.id.location_text);
            dateTimeText = itemView.findViewById(R.id.date_time_text);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
