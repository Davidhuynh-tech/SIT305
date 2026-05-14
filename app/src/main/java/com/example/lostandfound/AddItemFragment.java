package com.example.lostandfound;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lostandfound.databinding.FragmentAddItemBinding;
import com.example.lostandfound.storage.LostFoundDatabase;
import com.example.lostandfound.storage.LostFoundItem;
import com.example.lostandfound.storage.LostFoundItemDao;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddItemFragment extends Fragment {

    private FragmentAddItemBinding binding;
    private LostFoundItemDao itemDao;
    private FusedLocationProviderClient fusedLocationClient;
    private final ExecutorService geocoderExecutor = Executors.newSingleThreadExecutor();
    private final Map<String, LatLng> locationSuggestions = new HashMap<>();
    private ArrayAdapter<String> locationAdapter;
    private Uri selectedImageUri;
    private double selectedLatitude = Double.NaN;
    private double selectedLongitude = Double.NaN;
    private boolean suppressLocationTextWatcher = false;
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
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    fillWithCurrentLocation();
                } else {
                    Toast.makeText(requireContext(), R.string.location_permission_required, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAddItemBinding.inflate(inflater, container, false);
        itemDao = LostFoundDatabase.getInstance(requireContext()).lostFoundItemDao();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
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
        binding.buttonGetCurrentLocation.setOnClickListener(v -> requestCurrentLocation());

        setupLocationAutocomplete();

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
        if (Double.isNaN(selectedLatitude) || Double.isNaN(selectedLongitude)) {
            LatLng resolved = resolveCoordinates(location);
            if (resolved == null) {
                Toast.makeText(requireContext(), R.string.select_valid_location, Toast.LENGTH_SHORT).show();
                return;
            }
            selectedLatitude = resolved.latitude;
            selectedLongitude = resolved.longitude;
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
                selectedLatitude,
                selectedLongitude,
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

    private void setupLocationAutocomplete() {
        locationAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );
        binding.editLocation.setAdapter(locationAdapter);
        binding.editLocation.setThreshold(2);
        binding.editLocation.setOnItemClickListener((parent, view, position, id) -> {
            String selected = locationAdapter.getItem(position);
            if (selected != null && locationSuggestions.containsKey(selected)) {
                LatLng latLng = locationSuggestions.get(selected);
                selectedLatitude = latLng.latitude;
                selectedLongitude = latLng.longitude;
                suppressLocationTextWatcher = true;
                binding.editLocation.setText(selected);
                binding.editLocation.setSelection(selected.length());
                suppressLocationTextWatcher = false;
            }
        });

        binding.editLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (suppressLocationTextWatcher) {
                    return;
                }
                selectedLatitude = Double.NaN;
                selectedLongitude = Double.NaN;
                String query = s == null ? "" : s.toString().trim();
                if (query.length() < 3) {
                    locationSuggestions.clear();
                    locationAdapter.clear();
                    return;
                }
                searchLocationSuggestions(query);
            }
        });
    }

    private void searchLocationSuggestions(String query) {
        geocoderExecutor.execute(() -> {
            try {
                if (!Geocoder.isPresent() || !isAdded()) {
                    return;
                }
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(query + ", Australia", 5);
                if (addresses == null) {
                    addresses = new ArrayList<>();
                }
                List<String> labels = new ArrayList<>();
                Map<String, LatLng> mapped = new HashMap<>();
                for (Address address : addresses) {
                    String label = buildAddressLabel(address);
                    if (label.isEmpty()) {
                        continue;
                    }
                    labels.add(label);
                    mapped.put(label, new LatLng(address.getLatitude(), address.getLongitude()));
                }
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    locationSuggestions.clear();
                    locationSuggestions.putAll(mapped);
                    locationAdapter.clear();
                    locationAdapter.addAll(labels);
                    locationAdapter.notifyDataSetChanged();
                    if (!labels.isEmpty() && binding != null) {
                        binding.editLocation.showDropDown();
                    }
                });
            } catch (Exception ignored) {
            }
        });
    }

    private String buildAddressLabel(Address address) {
        String line = address.getAddressLine(0);
        if (line != null && !line.trim().isEmpty()) {
            return line;
        }
        String featureName = address.getFeatureName() == null ? "" : address.getFeatureName();
        String locality = address.getLocality() == null ? "" : address.getLocality();
        String country = address.getCountryName() == null ? "" : address.getCountryName();
        String combined = (featureName + ", " + locality + ", " + country).replaceAll("(,\\s*){2,}", ", ");
        return combined.replaceAll("^,\\s*|,\\s*$", "").trim();
    }

    private LatLng resolveCoordinates(String inputLocation) {
        try {
            if (!Geocoder.isPresent()) {
                return null;
            }
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(inputLocation + ", Australia", 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return new LatLng(address.getLatitude(), address.getLongitude());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void requestCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fillWithCurrentLocation();
        } else {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void fillWithCurrentLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location == null || !isAdded()) {
                        Toast.makeText(requireContext(), R.string.unable_get_location, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (isDefaultGoogleHqLocation(location.getLatitude(), location.getLongitude())) {
                        Toast.makeText(requireContext(), R.string.set_emulator_location_hint, Toast.LENGTH_LONG).show();
                        return;
                    }
                    selectedLatitude = location.getLatitude();
                    selectedLongitude = location.getLongitude();
                    geocoderExecutor.execute(() -> {
                        String label = "";
                        try {
                            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(
                                    selectedLatitude,
                                    selectedLongitude,
                                    1
                            );
                            if (addresses != null && !addresses.isEmpty()) {
                                label = buildAddressLabel(addresses.get(0));
                            }
                        } catch (Exception ignored) {
                        }
                        String finalLabel = label.isEmpty()
                                ? getString(R.string.location_lat_lng_fallback, selectedLatitude, selectedLongitude)
                                : label;
                        if (!isAdded()) {
                            return;
                        }
                        requireActivity().runOnUiThread(() -> {
                            suppressLocationTextWatcher = true;
                            binding.editLocation.setText(finalLabel);
                            binding.editLocation.setSelection(finalLabel.length());
                            suppressLocationTextWatcher = false;
                        });
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), R.string.unable_get_location, Toast.LENGTH_SHORT).show());
    }

    private boolean isDefaultGoogleHqLocation(double latitude, double longitude) {
        double hqLat = 37.4219983;
        double hqLng = -122.084;
        return Math.abs(latitude - hqLat) < 0.02 && Math.abs(longitude - hqLng) < 0.02;
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
