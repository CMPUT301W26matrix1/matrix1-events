package com.example.eventflow;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
 */
public class EntrantLocationMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FusedLocationProviderClient fusedLocationClient;

    private String eventId;
    private String eventName;
    private List<EntrantLocation> entrantLocations = new ArrayList<>();
    private boolean locationsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_location_map);

        // DEBUG: Check Google Play Services
        int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            Toast.makeText(this, "Google Play Services error: " + result, Toast.LENGTH_LONG).show();
            Log.e("MapsDebug", "Google Play Services error code: " + result);
        } else {
            Toast.makeText(this, "Google Play Services OK", Toast.LENGTH_SHORT).show();
            Log.d("MapsDebug", "Google Play Services is available");
        }

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
            Log.d("MapsDebug", "Map fragment found, getMapAsync called");
        } else {
            Log.e("MapsDebug", "Map fragment is NULL!");
            Toast.makeText(this, "Map fragment not found", Toast.LENGTH_SHORT).show();
        }

        // Load entrant locations
        loadEntrantLocations();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadEntrantLocations() {
        Toast.makeText(this, "Loading entrant locations...", Toast.LENGTH_SHORT).show();
        Log.d("MapsDebug", "Loading entrant locations for event: " + eventId);

        // Get waiting list entrants from Firestore
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    entrantLocations.clear();
                    Log.d("MapsDebug", "Found " + queryDocumentSnapshots.size() + " waiting list entries");

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        String userName = doc.getString("userName");
                        String userId = doc.getId();

                        Log.d("MapsDebug", "Entry: " + userId + ", lat=" + lat + ", lng=" + lng + ", name=" + userName);

                        if (lat != null && lng != null && userName != null) {
                            entrantLocations.add(new EntrantLocation(
                                    userName, userId, lat, lng));
                        }
                    }

                    locationsLoaded = true;

                    if (entrantLocations.isEmpty()) {
                        Toast.makeText(this,
                                "No location data available for entrants",
                                Toast.LENGTH_LONG).show();
                        Log.d("MapsDebug", "No entrant locations found");
                    } else {
                        Toast.makeText(this,
                                "Found " + entrantLocations.size() + " entrant locations",
                                Toast.LENGTH_SHORT).show();
                        Log.d("MapsDebug", "Found " + entrantLocations.size() + " locations");
                    }

                    // If map is ready, add markers now
                    if (mMap != null) {
                        addMarkersToMap();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Failed to load entrant locations: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e("MapsDebug", "Error loading locations", e);
                    locationsLoaded = true;
                });
    }

    private void addMarkersToMap() {
        if (mMap == null) {
            Log.d("MapsDebug", "addMarkersToMap called but mMap is null");
            return;
        }

        Log.d("MapsDebug", "Adding markers to map, locations count: " + entrantLocations.size());

        mMap.clear();

        // Set map type to NORMAL to show streets and buildings
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // ADD A TEST MARKER TO VERIFY MAP IS WORKING
        LatLng edmonton = new LatLng(53.5461, -113.4938);
        mMap.addMarker(new MarkerOptions()
                .position(edmonton)
                .title("Edmonton")
                .snippet("Map is working!")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Move camera to Edmonton with zoom level
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edmonton, 12f));

        if (entrantLocations.isEmpty()) {
            Log.d("MapsDebug", "No entrant locations, but test marker added");
            Toast.makeText(this, "Test marker added. Add location data to see entrant markers.", Toast.LENGTH_LONG).show();
            return;
        }

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
        Log.d("MapsDebug", "onMapReady called, map is ready!");

        // Set map type to show streets and buildings
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // If locations are already loaded, add markers now
        if (locationsLoaded) {
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