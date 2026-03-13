package com.example.eventflow;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.org_QR.QRGenerator;
import com.example.eventflow.org_event.AttendanceLimit;
import com.example.eventflow.org_event.Event;
import com.example.eventflow.org_event.EventDetailsActivity;
import com.example.eventflow.org_event.EventFormManager;

public class MainActivity extends AppCompatActivity {
    /**
     * Declare the UI components for the screen
     */
    // 1. Declare UI Elements
    private EditText etName, etLocation, etDate, etDescription, etLimit;
    private CheckBox cbLimit;
    private ImageView ivEventPoster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 2. UI fragment
        // Inputs
        etName = findViewById(R.id.et_event_name);
        etLocation = findViewById(R.id.et_event_location);
        etDate = findViewById(R.id.et_event_date);
        etDescription = findViewById(R.id.et_event_description);

        // Attendance Limit
        cbLimit = findViewById(R.id.cb_limit_attendees);
        etLimit = findViewById(R.id.et_max_attendees);

        // Image
        ivEventPoster = findViewById(R.id.iv_event_poster); // Make sure this matches your image ID

        // 3. Delegate Attendance Logic to Helper
        AttendanceLimit.setupLimitToggle(cbLimit, etLimit);

        // 4. HEADER CONTROLS (Navigation & Action)
        View btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish()); // Closes screen and goes back

        View btnAddEvent = findViewById(R.id.btn_add_event);
        btnAddEvent.setOnClickListener(v -> handleAddEvent()); // The Boss gives the order!

        // 5. BOTTOM CONTROLS (Navigation)
        Button btnDelete = findViewById(R.id.btn_delete_event);
        btnDelete.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete the event?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Toast.makeText(this, "Event Deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // B. The New Update Button
        Button btnUpdate = findViewById(R.id.btn_update_event);
        btnUpdate.setOnClickListener(v -> handleUpdateEvent());
    }

    /**
     * Add Event button handle (Organizer view)
     */
    private void handleAddEvent() {
        Event newEvent = EventFormManager.validateAndCreateEvent(
                this, etName, etLocation, etDate, etDescription, cbLimit, etLimit
        );

        if (newEvent == null) return;

        //Go to EventDetailsActivity instead
        android.content.Intent intent = new android.content.Intent(this, EventDetailsActivity.class);

        // the description added, next screen can read it
        intent.putExtra("EVENT_NAME", newEvent.getName());
        intent.putExtra("EVENT_LOCATION", etLocation.getText().toString().trim());
        intent.putExtra("EVENT_DESC", etDescription.getText().toString().trim());
        intent.putExtra("QR_DATA", newEvent.getQRDataString()); // the scanner link

        startActivity(intent);

        Toast.makeText(this, "Review your event details!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Update Event button handle (organizer)
     * For now, it re-validates the form and updates the QR code with a different success message.
     */
    private void handleUpdateEvent() {
        Event updatedEvent = EventFormManager.validateAndCreateEvent(
                this, etName, etLocation, etDate, etDescription, cbLimit, etLimit
        );

        if (updatedEvent == null) return;

        Bitmap generatedQR = QRGenerator.generateQRCode(updatedEvent.getQRDataString());

        if (generatedQR != null) {
            ivEventPoster.setImageBitmap(generatedQR);
            Toast.makeText(this, "Event Updated Successfully!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Error updating QR Code.", Toast.LENGTH_SHORT).show();
        }
    }
}