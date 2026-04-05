package com.example.eventflow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng selectedLocation;
    private Marker selectedMarker;
    private Circle radiusCircle;
    private Button btnConfirm;
    private EditText etSearch;
    private ImageButton btnSearch;
    private int radiusMeters = 500;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        btnConfirm = findViewById(R.id.btnConfirmLocation);
        etSearch = findViewById(R.id.et_map_search);
        btnSearch = findViewById(R.id.btn_map_search);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnConfirm.setOnClickListener(v -> {
            if (selectedLocation != null) {
                String addressName = getAddressFromLatLng(selectedLocation);
                
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", selectedLocation.latitude);
                resultIntent.putExtra("longitude", selectedLocation.longitude);
                resultIntent.putExtra("address", addressName);
                resultIntent.putExtra("radius", radiusMeters);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
            }
        });

        btnSearch.setOnClickListener(v -> searchLocation());

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocation();
                return true;
            }
            return false;
        });
    }

    private void searchLocation() {
        String locationName = etSearch.getText().toString().trim();
        if (locationName.isEmpty()) return;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                updateMarkerAtLocation(latLng);
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("LocationPicker", "Search error", e);
            Toast.makeText(this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMarkerAtLocation(LatLng latLng) {
        selectedLocation = latLng;
        if (selectedMarker != null) {
            selectedMarker.remove();
        }

        String title = getAddressFromLatLng(latLng);
        selectedMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title)
                .draggable(true));
        selectedMarker.showInfoWindow();
        updateRadiusCircle();
    }

    private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                
                String featureName = address.getFeatureName();
                String addressLine = address.getAddressLine(0);
                
                if (featureName != null && !featureName.isEmpty()) {
                    if (Character.isDigit(featureName.charAt(0)) || featureName.contains("-")) {
                        return addressLine;
                    }
                    return featureName;
                }
                return addressLine;
            }
        } catch (IOException e) {
            Log.e("LocationPicker", "Geocoder error", e);
        }
        return String.format(Locale.getDefault(), "%.4f, %.4f", latLng.latitude, latLng.longitude);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        checkLocationPermission();

        LatLng defaultLocation = new LatLng(53.5461, -113.4938);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));

        mMap.setOnMapClickListener(latLng -> updateMarkerAtLocation(latLng));

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {}
            @Override
            public void onMarkerDrag(Marker marker) {}
            @Override
            public void onMarkerDragEnd(Marker marker) {
                selectedLocation = marker.getPosition();
                String title = getAddressFromLatLng(selectedLocation);
                marker.setTitle(title);
                marker.showInfoWindow();
                updateRadiusCircle();
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            getDeviceLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getDeviceLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            }
        }
    }

    private void updateRadiusCircle() {
        if (radiusCircle != null) {
            radiusCircle.remove();
        }

        if (selectedLocation != null) {
            radiusCircle = mMap.addCircle(new CircleOptions()
                    .center(selectedLocation)
                    .radius(radiusMeters)
                    .strokeColor(0xFF4CAF50)
                    .strokeWidth(2)
                    .fillColor(0x224CAF50));
        }
    }
}