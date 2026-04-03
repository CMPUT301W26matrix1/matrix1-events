package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.model.entities.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom QR Scanner Activity to match the requested UI design.
 */
public class CustomScannerActivity extends AppCompatActivity implements DecoratedBarcodeView.TorchListener {

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private ImageButton btnFlash;
    private TextView tvFlashStatus;
    private boolean isFlashOn = false;
    private RecyclerView rvRecentScans;
    private RecentScanAdapter recentScanAdapter;
    private List<Event> recentEvents = new ArrayList<>();
    private TextView tvNoRecentScans;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_scanner);

        db = FirebaseFirestore.getInstance();

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView.setTorchListener(this);

        btnFlash = findViewById(R.id.btn_flash);
        tvFlashStatus = findViewById(R.id.tv_flash_status);
        ImageButton btnBack = findViewById(R.id.btn_scanner_back);
        Button btnJoin = findViewById(R.id.btn_manual_join);
        EditText etManualCode = findViewById(R.id.et_manual_code);
        rvRecentScans = findViewById(R.id.rv_recent_scans);
        tvNoRecentScans = findViewById(R.id.tv_no_recent_scans);

        // Setup RecyclerView
        recentScanAdapter = new RecentScanAdapter(recentEvents, event -> {
            navigateToEventDetails(event.getEventId());
        });
        rvRecentScans.setLayoutManager(new LinearLayoutManager(this));
        rvRecentScans.setAdapter(recentScanAdapter);

        // Initialize capture manager
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        
        // Custom callback to capture result and update recent scans
        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    handleScanResult(result.getText());
                }
            }

            @Override
            public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {}
        });

        btnBack.setOnClickListener(v -> finish());

        btnFlash.setOnClickListener(v -> {
            if (isFlashOn) {
                barcodeScannerView.setTorchOff();
            } else {
                barcodeScannerView.setTorchOn();
            }
        });

        btnJoin.setOnClickListener(v -> {
            String code = etManualCode.getText().toString().trim();
            if (!code.isEmpty()) {
                handleScanResult(code);
            }
        });

        loadRecentScans();
    }

    private void handleScanResult(String rawResult) {
        // Pause scanning to avoid multiple triggers
        barcodeScannerView.pause();
        
        String eventId = rawResult;
        // Check if it's a URL format
        if (rawResult.startsWith("eventflow://details?id=")) {
            eventId = rawResult.substring("eventflow://details?id=".length());
        }
        
        final String finalEventId = eventId;
        db.collection("events").document(finalEventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Event event = documentSnapshot.toObject(Event.class);
                if (event != null) {
                    event.setEventId(documentSnapshot.getId());
                    addToRecent(event);
                    navigateToEventDetails(finalEventId);
                }
            } else {
                Toast.makeText(this, "Event not found: " + finalEventId, Toast.LENGTH_SHORT).show();
                barcodeScannerView.resume();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching event", Toast.LENGTH_SHORT).show();
            barcodeScannerView.resume();
        });
    }

    private void addToRecent(Event event) {
        for (int i = 0; i < recentEvents.size(); i++) {
            if (recentEvents.get(i).getEventId().equals(event.getEventId())) {
                recentEvents.remove(i);
                break;
            }
        }
        recentEvents.add(0, event);
        if (recentEvents.size() > 5) {
            recentEvents.remove(recentEvents.size() - 1);
        }
        updateRecentUI();
    }

    private void loadRecentScans() {
        // In a real app, this might come from local storage
        updateRecentUI();
    }

    private void updateRecentUI() {
        if (recentEvents.isEmpty()) {
            rvRecentScans.setVisibility(android.view.View.GONE);
            tvNoRecentScans.setVisibility(android.view.View.VISIBLE);
        } else {
            rvRecentScans.setVisibility(android.view.View.VISIBLE);
            tvNoRecentScans.setVisibility(android.view.View.GONE);
            recentScanAdapter.updateData(new ArrayList<>(recentEvents));
        }
    }

    private void navigateToEventDetails(String eventId) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("eventId", eventId);
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        intent.putExtra("userId", deviceId);
        intent.putExtra("userRole", "entrant");
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    public void onTorchOn() {
        isFlashOn = true;
        tvFlashStatus.setText("Flash On");
    }

    @Override
    public void onTorchOff() {
        isFlashOn = false;
        tvFlashStatus.setText("Flash Off");
    }
}
