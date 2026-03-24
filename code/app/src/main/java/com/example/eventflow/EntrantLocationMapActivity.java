package com.example.eventflow;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays entrant locations on a map for organizers.
 * Shows where entrants joined the waiting list from.
 *
 */
public class EntrantLocationMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FusedLocationProviderClient fusedLocationClient;

    private String eventId;
    private String eventName;
    private List<EntrantLocation> entrantLocations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_location_map);

        // Get event data from intent
        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

        if (eventId == null) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Entrant Locations");
            if (eventName != null) {
                getSupportActionBar().setSubtitle(eventName);
            }
        }

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        loadEntrantLocations();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadEntrantLocations() {
        Toast.makeText(this, "Loading entrant locations...", Toast.LENGTH_SHORT).show();

        // Get waiting list entrants from Firestore
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    entrantLocations.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        String userName = doc.getString("userName");
                        String userId = doc.getId();

                        if (lat != null && lng != null && userName != null) {
                            entrantLocations.add(new EntrantLocation(
                                    userName, userId, lat, lng));
                        }
                    }

                    if (entrantLocations.isEmpty()) {
                        Toast.makeText(this,
                                "No location data available for entrants",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "Found " + entrantLocations.size() + " entrant locations",
                                Toast.LENGTH_SHORT).show();
                        addMarkersToMap();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Failed to load entrant locations: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void addMarkersToMap() {
        if (mMap == null) return;

        mMap.clear();

        for (EntrantLocation location : entrantLocations) {
            LatLng position = new LatLng(location.latitude, location.longitude);

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(location.userName)
                    .snippet("Joined waiting list")
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE)));

            if (marker != null) {
                marker.setTag(location.userId);
            }
        }

        // Center and zoom to show all markers
        if (!entrantLocations.isEmpty()) {
            LatLng center = new LatLng(
                    entrantLocations.get(0).latitude,
                    entrantLocations.get(0).longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 12));
        }

        // Request location permission to show user's own location
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void enableMyLocation() {
        if (mMap != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

                // Get and center on user's location
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                LatLng userLocation = new LatLng(
                                        location.getLatitude(),
                                        location.getLongitude());
                                mMap.addMarker(new MarkerOptions()
                                        .position(userLocation)
                                        .title("You are here")
                                        .icon(BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_RED)));
                            }
                        });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        if (!entrantLocations.isEmpty()) {
            addMarkersToMap();
        }
    }

    /**
     * Inner class to hold entrant location data
     */
    private static class EntrantLocation {
        String userName;
        String userId;
        double latitude;
        double longitude;

        EntrantLocation(String userName, String userId, double latitude, double longitude) {
            this.userName = userName;
            this.userId = userId;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}