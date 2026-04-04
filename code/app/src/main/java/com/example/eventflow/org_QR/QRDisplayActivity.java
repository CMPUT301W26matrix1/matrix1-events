package com.example.eventflow.org_QR;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

/**
 * This activity displays the generated QR code in full screen
 * after the organizer selects "QR Scan" from the share menu.
 */
public class QRDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_qrdisplay);

        // 1. Retrieve data passed from EventDetailsActivity
        String eventName = getIntent().getStringExtra("EVENT_NAME");
        String qrData = getIntent().getStringExtra("QR_DATA");

        // 2. Initialize UI Elements
        ImageView ivLargeQR = findViewById(R.id.iv_large_qr);
        TextView tvEventTitle = findViewById(R.id.tv_qr_event_title);

        // 3. Populate the Title
        if (eventName != null) {
            tvEventTitle.setText(eventName);
        }

        // 4. Generate and display the QR Code using your QRGenerator
        if (qrData != null) {
            try {
                Bitmap bitmap = QRGenerator.generateQRCode(qrData);
                ivLargeQR.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error generating QR Code", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No QR data found!", Toast.LENGTH_SHORT).show();
        }

        // 5. Button Logic
        findViewById(R.id.btn_qr_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_download).setOnClickListener(v -> {
            Toast.makeText(this, "Downloading QR Code...", Toast.LENGTH_SHORT).show();
            // Logic for saving bitmap to gallery would go here
        });

        findViewById(R.id.btn_share).setOnClickListener(v -> {
            showShareMenu();
        });

        // 6. Navigation Mockup
        findViewById(R.id.nav_dashboard).setOnClickListener(v -> finish());
        findViewById(R.id.nav_create).setOnClickListener(v -> finish());
        findViewById(R.id.nav_profile).setOnClickListener(v -> finish());
    }

    /**
     * Creates "Share with friends" menu as per your mockup
     */
    private void showShareMenu() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.fragment_share_sheet, null);
        bottomSheet.setContentView(view);

        view.findViewById(R.id.btn_share_cancel).setOnClickListener(v -> bottomSheet.dismiss());

        bottomSheet.show();
    }
}
