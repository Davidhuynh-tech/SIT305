package Task_4P.com.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import Task_4P.com.R;
import Task_4P.com.data.Event;

public class EventListFragment extends Fragment {
    private EventViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.events_recycler);
        TextView emptyText = view.findViewById(R.id.empty_text);

        viewModel = new ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(EventViewModel.class);

        EventAdapter adapter = new EventAdapter(new EventAdapter.EventActionListener() {
            @Override
            public void onEdit(Event event) {
                Bundle args = new Bundle();
                args.putLong("eventId", event.id);
                args.putString("title", event.title);
                args.putString("category", event.category);
                args.putString("location", event.location);
                args.putLong("dateTimeMillis", event.dateTimeMillis);
                NavHostFragment.findNavController(EventListFragment.this)
                        .navigate(R.id.action_eventListFragment_to_addEditEventFragment, args);
            }

            @Override
            public void onDelete(Event event) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.confirm_delete_title)
                        .setMessage(R.string.confirm_delete_message)
                        .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                        .setPositiveButton(R.string.delete, (dialog, which) -> {
                            viewModel.delete(event);
                            Toast.makeText(requireContext(), R.string.event_deleted, Toast.LENGTH_SHORT).show();
                        })
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel.getAllEvents().observe(getViewLifecycleOwner(), events -> {
            adapter.submitList(events);
            emptyText.setVisibility(events == null || events.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}
