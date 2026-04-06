/**
 * Activity for displaying and sharing QR Codes.
 * Provides functionality to download the QR code or share it with others.
 */
package com.example.eventflow.org_QR;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.OutputStream;

/**
 * This activity displays the generated QR code in full screen
 * after the organizer selects "QR Scan" from the share menu.
 */
public class QRDisplayActivity extends AppCompatActivity {

    private Bitmap qrBitmap;
    private String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_qrdisplay);

        // 1. Retrieve data passed from EventDetailsActivity
        eventName = getIntent().getStringExtra("EVENT_NAME");
        String qrData = getIntent().getStringExtra("QR_DATA");
        
        // Use the eventId as the manual code
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
                qrBitmap = QRGenerator.generateQRCode(qrData);
                ivLargeQR.setImageBitmap(qrBitmap);
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
                if (qrBitmap != null) {
                    saveImageToGallery(qrBitmap, "QR_" + (eventName != null ? eventName.replaceAll("\\s+", "_") : "Event"));
                } else {
                    Toast.makeText(this, "QR Code not ready", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View btnShare = findViewById(R.id.btn_share);
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                showShareMenu();
            });
        }
    }

    private void saveImageToGallery(Bitmap bitmap, String filename) {
        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename + ".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/EventFlow");

                Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = getContentResolver().openOutputStream(imageUri);
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                java.io.File image = new java.io.File(imagesDir, filename + ".jpg");
                fos = new java.io.FileOutputStream(image);
            }

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            if (fos != null) {
                fos.close();
            }
            Toast.makeText(this, "QR Code saved to Gallery", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
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
