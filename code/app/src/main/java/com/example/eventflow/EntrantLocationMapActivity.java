/**
 * Activity for displaying event and entrant locations on a Google Map.
 * Shows the event's location and, for organizers, the locations from which entrants joined the waiting list.
 * Facilitates spatial visualization of event participation.
 */
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
 * US 02.02.02 — Shows event location and where entrants joined from.
 */
public class EntrantLocationMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FusedLocationProviderClient fusedLocationClient;

    private String eventId;
    private String eventName;
    private double eventLat;
    private double eventLng;
    private String userRole;

    private final List<EntrantLocation> entrantLocations = new ArrayList<>();
    private boolean locationsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_location_map);

        eventId   = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");
        eventLat  = getIntent().getDoubleExtra("eventLat", 53.5461); // default Edmonton
        eventLng  = getIntent().getDoubleExtra("eventLng", -113.4938);
        userRole  = getIntent().getStringExtra("userRole");

        if (eventId == null) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Entrant Locations");
            if (eventName != null) getSupportActionBar().setSubtitle(eventName);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    entrantLocations.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Double lat      = doc.getDouble("latitude");
                        Double lng      = doc.getDouble("longitude");
                        String userName = doc.getString("userName");
                        String userId   = doc.getId();

                        if (lat != null && lng != null && lat != 0 && lng != 0) {
                            String name = userName != null ? userName : "Unknown";
                            entrantLocations.add(new EntrantLocation(name, userId, lat, lng));
                        }
                    }

                    locationsLoaded = true;
                    if (mMap != null) addMarkersToMap();
                })
                .addOnFailureListener(e -> {
                    locationsLoaded = true;
                    if (mMap != null) addMarkersToMap();
                });
    }

    private void addMarkersToMap() {
        if (mMap == null) return;

        mMap.clear();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Always show event location as a GREEN marker
        LatLng eventLocation = new LatLng(eventLat, eventLng);
        mMap.addMarker(new MarkerOptions()
                .position(eventLocation)
                .title(eventName != null ? eventName : "Event Location")
                .snippet("Event is here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Move camera to event location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 13f));

        // Show entrant join locations as BLUE markers (organizer only)
        if ("organizer".equalsIgnoreCase(userRole) || userRole == null) {
            for (EntrantLocation location : entrantLocations) {
                LatLng position = new LatLng(location.latitude, location.longitude);
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(location.userName)
                        .snippet("Joined waiting list from here")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                if (marker != null) marker.setTag(location.userId);
            }

            if (entrantLocations.isEmpty()) {
                Toast.makeText(this, "No entrant location data yet", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        entrantLocations.size() + " entrant(s) on map",
                        Toast.LENGTH_SHORT).show();
            }
        }

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
        if (mMap == null) return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        if (locationsLoaded) addMarkersToMap();
    }

    private static class EntrantLocation {
        String userName, userId;
        double latitude, longitude;

        EntrantLocation(String userName, String userId, double latitude, double longitude) {
            this.userName  = userName;
            this.userId    = userId;
            this.latitude  = latitude;
            this.longitude = longitude;
        }
    }
}