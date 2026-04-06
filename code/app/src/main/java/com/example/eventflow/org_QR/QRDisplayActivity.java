package com.example.eventflow.org_QR;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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
        
        // Use the eventId as the manual code
        // We extract it from the QR data if not passed directly
        String eventId = null;
        if (qrData != null && qrData.startsWith("eventflow://event/")) {
            eventId = qrData.substring("eventflow://event/".length());
        }

        // 2. Initialize UI Elements
        ImageView ivLargeQR = findViewById(R.id.iv_large_qr);
        TextView tvEventTitle = findViewById(R.id.tv_qr_event_title);
        TextView tvEventCode = findViewById(R.id.tv_event_code);
        ImageButton btnCopyCode = findViewById(R.id.btn_copy_code);

        // 3. Populate Data
        if (eventName != null) {
            tvEventTitle.setText(eventName);
        }

        if (eventId != null) {
            tvEventCode.setText(eventId);
        }

        // 4. Generate and display the QR Code
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
        View btnBack = findViewById(R.id.btn_qr_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnCopyCode != null && eventId != null) {
            String finalEventId = eventId;
            btnCopyCode.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Event Code", finalEventId);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Code copied to clipboard", Toast.LENGTH_SHORT).show();
            });
        }

        View btnDownload = findViewById(R.id.btn_download);
        if (btnDownload != null) {
            btnDownload.setOnClickListener(v -> {
                Toast.makeText(this, "Downloading QR Code...", Toast.LENGTH_SHORT).show();
            });
        }

        View btnShare = findViewById(R.id.btn_share);
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                showShareMenu();
            });
        }
    }

    /**
     * Creates "Share with friends" menu
     */
    private void showShareMenu() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.fragment_share_sheet, null);
        bottomSheet.setContentView(view);

        View cancelBtn = view.findViewById(R.id.btn_share_cancel);
        if (cancelBtn != null) {
            cancelBtn.setOnClickListener(v -> bottomSheet.dismiss());
        }

        bottomSheet.show();
    }
}
