package com.example.lostandfound;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lostandfound.databinding.FragmentAddItemBinding;
import com.example.lostandfound.storage.LostFoundDatabase;
import com.example.lostandfound.storage.LostFoundItem;
import com.example.lostandfound.storage.LostFoundItemDao;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddItemFragment extends Fragment {

    private FragmentAddItemBinding binding;
    private LostFoundItemDao itemDao;
    private Uri selectedImageUri;
    private long selectedDateTimeMillis = -1L;
    private final ActivityResultLauncher<String[]> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) {
                    return;
                }
                selectedImageUri = uri;
                try {
                    requireContext().getContentResolver().takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (SecurityException ignored) {
                }
                binding.imagePreview.setImageURI(uri);
                binding.imagePreview.setVisibility(View.VISIBLE);
                binding.textImageStatus.setText(R.string.image_selected);
            });

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAddItemBinding.inflate(inflater, container, false);
        itemDao = LostFoundDatabase.getInstance(requireContext()).lostFoundItemDao();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonBackHome.setOnClickListener(v ->
                NavHostFragment.findNavController(AddItemFragment.this).navigateUp());

        binding.editDate.setFocusable(false);
        binding.editDate.setClickable(true);
        binding.editDate.setOnClickListener(v -> showDateAndTimePicker());

        binding.buttonPickImage.setOnClickListener(v ->
                imagePickerLauncher.launch(new String[]{"image/*"}));

        binding.buttonSave.setOnClickListener(v -> saveItem());
    }

    private void saveItem() {
        String postType = binding.radioLost.isChecked()
                ? getString(R.string.lost)
                : getString(R.string.found);
        String name = binding.editName.getText().toString().trim();
        String phone = binding.editPhone.getText().toString().trim();
        String description = binding.editDescription.getText().toString().trim();
        String category = binding.spinnerCategory.getSelectedItem().toString();
        String location = binding.editLocation.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), R.string.name_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.isEmpty()) {
            Toast.makeText(requireContext(), R.string.phone_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidPhoneNumber(phone)) {
            Toast.makeText(requireContext(), R.string.phone_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDateTimeMillis <= 0L) {
            Toast.makeText(requireContext(), R.string.date_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDateTimeMillis <= System.currentTimeMillis()) {
            Toast.makeText(requireContext(), R.string.past_date_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (location.isEmpty()) {
            Toast.makeText(requireContext(), R.string.location_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), R.string.image_required, Toast.LENGTH_SHORT).show();
            return;
        }
        String date = DateFormat.getDateTimeInstance().format(new Date(selectedDateTimeMillis));

        LostFoundItem item = new LostFoundItem(
                postType,
                name,
                phone,
                description,
                date,
                category,
                selectedImageUri.toString(),
                location,
                System.currentTimeMillis()
        );
        long rowId = itemDao.insertItem(item);
        if (rowId == -1) {
            Toast.makeText(requireContext(), R.string.save_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(requireContext(), R.string.item_saved, Toast.LENGTH_SHORT).show();
        NavHostFragment.findNavController(AddItemFragment.this)
                .navigate(R.id.action_addItemFragment_to_homeFragment);
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
                                long pickedMillis = picked.getTimeInMillis();
                                if (pickedMillis <= System.currentTimeMillis()) {
                                    Toast.makeText(
                                            requireContext(),
                                            R.string.past_date_error,
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return;
                                }
                                selectedDateTimeMillis = pickedMillis;
                                String formatted = DateFormat.getDateTimeInstance()
                                        .format(new Date(selectedDateTimeMillis));
                                binding.editDate.setText(formatted);
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
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        datePickerDialog.getDatePicker().setMinDate(todayStart.getTimeInMillis());
        datePickerDialog.show();
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("\\d{10}");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
