package com.example.lostandfound;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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

import com.example.lostandfound.databinding.FragmentMapItemsBinding;
import com.example.lostandfound.storage.LostFoundDatabase;
import com.example.lostandfound.storage.LostFoundItem;
import com.example.lostandfound.storage.LostFoundItemDao;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Arrays;
import java.util.List;

public class MapItemsFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapItemsBinding binding;
    private LostFoundItemDao itemDao;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap googleMap;
    private LatLng currentLatLng;
    private ArrayAdapter<String> radiusAdapter;
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    updateCurrentLocationAndRender();
                } else {
                    Toast.makeText(requireContext(), R.string.location_permission_required, Toast.LENGTH_SHORT).show();
                    renderMarkers();
                }
            });

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMapItemsBinding.inflate(inflater, container, false);
        itemDao = LostFoundDatabase.getInstance(requireContext()).lostFoundItemDao();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonBackHome.setOnClickListener(v ->
                NavHostFragment.findNavController(MapItemsFragment.this).navigateUp());
        binding.buttonApplyRadius.setOnClickListener(v -> renderMarkers());
        binding.buttonUseCurrentLocation.setOnClickListener(v -> requestCurrentLocation());
        setupRadiusSpinner();
        setupMap();
    }

    private void setupRadiusSpinner() {
        radiusAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Arrays.asList("1", "3", "5", "10", "20", "50")
        );
        radiusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRadius.setAdapter(radiusAdapter);
        binding.spinnerRadius.setSelection(2);
    }

    private void setupMap() {
        SupportMapFragment mapFragment = new SupportMapFragment();
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.mapContainer, mapFragment)
                .commitNow();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        renderMarkers();
    }

    private void requestCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            updateCurrentLocationAndRender();
        } else {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void updateCurrentLocationAndRender() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null && !isDefaultGoogleHqLocation(
                            location.getLatitude(),
                            location.getLongitude()
                    )) {
                        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    } else if (location != null) {
                        currentLatLng = null;
                        Toast.makeText(requireContext(), R.string.set_emulator_location_hint, Toast.LENGTH_LONG).show();
                    } else {
                        currentLatLng = null;
                    }
                    renderMarkers();
                })
                .addOnFailureListener(e -> {
                    currentLatLng = null;
                    renderMarkers();
                });
    }

    private boolean isDefaultGoogleHqLocation(double latitude, double longitude) {
        double hqLat = 37.4219983;
        double hqLng = -122.084;
        return Math.abs(latitude - hqLat) < 0.02 && Math.abs(longitude - hqLng) < 0.02;
    }

    private void renderMarkers() {
        if (googleMap == null || binding == null) {
            return;
        }

        googleMap.clear();
        List<LostFoundItem> items = itemDao.getAllItems();
        if (items.isEmpty()) {
            Toast.makeText(requireContext(), R.string.no_items_found, Toast.LENGTH_SHORT).show();
            return;
        }

        double radiusKm = Double.parseDouble(binding.spinnerRadius.getSelectedItem().toString());
        int shownCount = 0;
        LatLng firstMarker = null;

        for (LostFoundItem item : items) {
            LatLng itemLatLng = new LatLng(item.getLatitude(), item.getLongitude());
            if (currentLatLng != null) {
                float[] distanceResult = new float[1];
                Location.distanceBetween(
                        currentLatLng.latitude,
                        currentLatLng.longitude,
                        itemLatLng.latitude,
                        itemLatLng.longitude,
                        distanceResult
                );
                double distanceKm = distanceResult[0] / 1000.0;
                if (distanceKm > radiusKm) {
                    continue;
                }
            }

            googleMap.addMarker(new MarkerOptions()
                    .position(itemLatLng)
                    .title(item.getPostType() + " - " + item.getCategory())
                    .snippet(item.getLocation()));
            shownCount++;
            if (firstMarker == null) {
                firstMarker = itemLatLng;
            }
        }

        if (currentLatLng != null) {
            googleMap.addMarker(new MarkerOptions()
                    .position(currentLatLng)
                    .title(getString(R.string.your_location))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            googleMap.addCircle(new CircleOptions()
                    .center(currentLatLng)
                    .radius(radiusKm * 1000.0)
                    .strokeWidth(2f)
                    .strokeColor(0xAA2F6FED)
                    .fillColor(0x222F6FED));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f));
        } else if (firstMarker != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstMarker, 12f));
        }

        if (shownCount == 0) {
            Toast.makeText(requireContext(), R.string.no_items_in_radius, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
