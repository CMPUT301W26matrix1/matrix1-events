/**
 * Activity for viewing the list of Enrolled Entrants.
 * Displays participants who have accepted invitations and provides CSV export functionality.
 */
package com.example.eventflow.org_event.manage_entrant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.R;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class EnrolledEntrantsActivity extends AppCompatActivity {

    private RecyclerView rvEnrolled;
    private EnrolledEntrantsAdapter adapter; // Corrected to match your filename
    private List<Entrant> enrolledList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrolled_entrants);

        // 1. Setup Header & Back Button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 2. Setup RecyclerView
        rvEnrolled = findViewById(R.id.rvEnrolled);
        rvEnrolled.setLayoutManager(new LinearLayoutManager(this));

        // 3. Prepare Mock Data
        enrolledList = new ArrayList<>();
        enrolledList.add(new Entrant("Jack Wilson", "jack@example.com", "(555) 012-3456", "Feb 20, 2025", "Feb 21, 2025"));
        enrolledList.add(new Entrant("Karen Taylor", "karen@example.com", "(555) 123-4568", "Feb 22, 2025", "Feb 23, 2025"));
        enrolledList.add(new Entrant("Liam Anderson", "liam@example.com", "(555) 234-5679", "Feb 24, 2025", "Feb 25, 2025"));
        enrolledList.add(new Entrant("Mia Thomas", "mia@example.com", "(555) 345-6780", "Feb 26, 2025", "Feb 27, 2025"));

        // 4. Update the "X confirmed attendees" count UI
        TextView tvAttendeeCount = findViewById(R.id.tvAttendeeCount);
        if (tvAttendeeCount != null) {
            tvAttendeeCount.setText(enrolledList.size() + " confirmed attendees");
        }

        // 5. Initialize and Set Adapter
        adapter = new EnrolledEntrantsAdapter(enrolledList);
        rvEnrolled.setAdapter(adapter);

        // 6. Export Button Logic
        MaterialButton btnExport = findViewById(R.id.btnExport);
        if (btnExport != null) {
            btnExport.setOnClickListener(v -> exportToCSV());
        }
    }

    /**
     * Converts the enrolledList into a CSV format and triggers the system share sheet.
     */
    private void exportToCSV() {
        // 1. Create the CSV Content
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("Name,Email,Phone,Joined Date,Accepted Date\n");

        for (Entrant entrant : enrolledList) {
            csvContent.append(entrant.getName()).append(",")
                    .append(entrant.getEmail()).append(",")
                    .append(entrant.getPhoneNumber()).append(",")
                    .append(entrant.getJoinDate()).append(",")
                    .append(entrant.getAcceptDate()).append("\n");
        }

        try {
            // 2. Save file to internal cache (No "Write External Storage" permission needed)
            File file = new File(getCacheDir(), "Enrolled_Attendees.csv");
            FileOutputStream out = new FileOutputStream(file);
            out.write(csvContent.toString().getBytes());
            out.close();

            // 3. Share the file via Intent
            Uri contentUri = FileProvider.getUriForFile(this,
                    "com.example.eventflow.fileprovider", file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Attendee List Export");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Save or Send CSV"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
