package Task_4P.com.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import Task_4P.com.R;
import Task_4P.com.data.Event;

public class AddEditEventFragment extends Fragment {
    private TextInputLayout titleLayout;
    private TextInputLayout categoryLayout;
    private TextInputLayout locationLayout;
    private TextInputEditText titleInput;
    private TextInputEditText categoryInput;
    private TextInputEditText locationInput;
    private TextView dateValueText;
    private long selectedDateTimeMillis = -1L;
    private long editingEventId = -1L;
    private EventViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_edit_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        titleLayout = view.findViewById(R.id.title_layout);
        categoryLayout = view.findViewById(R.id.category_layout);
        locationLayout = view.findViewById(R.id.location_layout);
        titleInput = view.findViewById(R.id.title_input);
        categoryInput = view.findViewById(R.id.category_input);
        locationInput = view.findViewById(R.id.location_input);
        dateValueText = view.findViewById(R.id.date_value_text);
        MaterialButton dateButton = view.findViewById(R.id.date_button);
        MaterialButton saveButton = view.findViewById(R.id.save_button);

        viewModel = new ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(EventViewModel.class);

        prefillIfEditing();
        configureTopBarForEditMode();
        dateButton.setOnClickListener(v -> showDateAndTimePicker());
        saveButton.setOnClickListener(v -> saveEvent(view));

        if (titleInput != null) {
            titleInput.addTextChangedListener(SimpleTextWatcher.afterChanged(s -> titleLayout.setError(null)));
        }
    }

    private void prefillIfEditing() {
        Bundle args = getArguments();
        if (args == null || !args.containsKey("eventId")) {
            return;
        }
        editingEventId = args.getLong("eventId", -1L);
        titleInput.setText(args.getString("title", ""));
        categoryInput.setText(args.getString("category", ""));
        locationInput.setText(args.getString("location", ""));
        selectedDateTimeMillis = args.getLong("dateTimeMillis", -1L);
        if (selectedDateTimeMillis > 0) {
            dateValueText.setText(DateFormat.getDateTimeInstance().format(new Date(selectedDateTimeMillis)));
        }
    }

    private void showDateAndTimePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (datePicker, year, month, dayOfMonth) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            requireContext(),
                            (timePicker, hourOfDay, minute) -> {
                                Calendar picked = Calendar.getInstance();
                                picked.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                                selectedDateTimeMillis = picked.getTimeInMillis();
                                dateValueText.setText(DateFormat.getDateTimeInstance()
                                        .format(new Date(selectedDateTimeMillis)));
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            false
                    );
                    timePickerDialog.show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveEvent(View rootView) {
        titleLayout.setError(null);

        String title = titleInput.getText() == null ? "" : titleInput.getText().toString().trim();
        String category = categoryInput.getText() == null ? "" : categoryInput.getText().toString().trim();
        String location = locationInput.getText() == null ? "" : locationInput.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            titleLayout.setError(getString(R.string.title_required));
            return;
        }
        if (selectedDateTimeMillis <= 0L) {
            Toast.makeText(requireContext(), R.string.date_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDateTimeMillis < System.currentTimeMillis()) {
            Toast.makeText(requireContext(), R.string.past_date_error, Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = new Event();
        event.title = title;
        event.category = category;
        event.location = location;
        event.dateTimeMillis = selectedDateTimeMillis;

        if (editingEventId > 0) {
            event.id = editingEventId;
            viewModel.update(event);
            Toast.makeText(requireContext(), R.string.event_updated, Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
        } else {
            viewModel.insert(event);
            Toast.makeText(requireContext(), R.string.event_saved, Toast.LENGTH_SHORT).show();
            clearForm();
        }
    }

    private void configureTopBarForEditMode() {
        MaterialToolbar toolbar = requireActivity().findViewById(R.id.top_app_bar);
        if (toolbar == null) {
            return;
        }

        if (editingEventId > 0) {
            toolbar.setTitle(R.string.edit_event);
            toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
            toolbar.setNavigationOnClickListener(v ->
                    NavHostFragment.findNavController(AddEditEventFragment.this).navigateUp()
            );
        } else {
            toolbar.setTitle(R.string.app_name);
            toolbar.setNavigationIcon(null);
            toolbar.setNavigationOnClickListener(null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MaterialToolbar toolbar = getActivity() == null ? null : getActivity().findViewById(R.id.top_app_bar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name);
            toolbar.setNavigationIcon(null);
            toolbar.setNavigationOnClickListener(null);
        }
    }

    private void clearForm() {
        titleLayout.setError(null);
        titleInput.setText("");
        categoryInput.setText("");
        locationInput.setText("");
        selectedDateTimeMillis = -1L;
        dateValueText.setText(getString(R.string.date_time));
    }

    static class SimpleTextWatcher implements android.text.TextWatcher {
        interface AfterChanged {
            void afterChanged(android.text.Editable s);
        }

        private final AfterChanged afterChanged;

        private SimpleTextWatcher(AfterChanged afterChanged) {
            this.afterChanged = afterChanged;
        }

        static SimpleTextWatcher afterChanged(AfterChanged afterChanged) {
            return new SimpleTextWatcher(afterChanged);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {
            afterChanged.afterChanged(s);
        }
    }
}
