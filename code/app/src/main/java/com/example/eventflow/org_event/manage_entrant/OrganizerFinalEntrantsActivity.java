package com.example.eventflow.org_event.manage_entrant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventflow.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class OrganizerFinalEntrantsActivity extends AppCompatActivity {

    private RecyclerView rvEnrolledEntrants;
    private EditText etSearch;
    private TextView tvConfirmedCount, tvEmptyState;
    private LinearLayout btnExportCSV, btnEmailAll;
    private EnrolledEntrantsAdapter adapter;
    private List<Entrant> enrolledList;
    private List<Entrant> filteredList;
    private FirebaseFirestore db;
    private String eventId;
    private String eventName;

    // ActivityResultLauncher for creating a document (CSV export)
    private final ActivityResultLauncher<String> createDocumentLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/csv"),
            uri -> {
                if (uri != null) {
                    writeCsvToUri(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_final_entrants);

        // Get event info from intent - ALLOW NULL
        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        setupListeners();

        // FIX: Even if eventId is null, still show empty state instead of crashing
        if (eventId != null && !eventId.isEmpty()) {
            loadEnrolledEntrants();
        } else {
            // Show empty state with 0 confirmed attendees
            updateEmptyState(true);
            if (tvConfirmedCount != null) {
                tvConfirmedCount.setText("0");
            }
            Toast.makeText(this, "No event selected. Please select an event first.", Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        rvEnrolledEntrants = findViewById(R.id.rvEnrolledEntrants);
        etSearch = findViewById(R.id.etSearch);
        tvConfirmedCount = findViewById(R.id.tvConfirmedCount);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnExportCSV = findViewById(R.id.btnExportCSV);
        btnEmailAll = findViewById(R.id.btnEmailAll);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        enrolledList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new EnrolledEntrantsAdapter(filteredList);
        rvEnrolledEntrants.setLayoutManager(new LinearLayoutManager(this));
        rvEnrolledEntrants.setAdapter(adapter);
    }

    private void setupListeners() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (eventId != null) {
                        filterEntrants(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnExportCSV != null) {
            btnExportCSV.setOnClickListener(v -> {
                if (eventId == null) {
                    Toast.makeText(this, "No event selected", Toast.LENGTH_SHORT).show();
                } else {
                    exportToCSV();
                }
            });
        }

        if (btnEmailAll != null) {
            btnEmailAll.setOnClickListener(v -> {
                if (eventId == null) {
                    Toast.makeText(this, "No event selected", Toast.LENGTH_SHORT).show();
                } else {
                    emailAllEntrants();
                }
            });
        }
    }

    private void loadEnrolledEntrants() {
        // FIX: Add null check here too
        if (eventId == null || eventId.isEmpty()) {
            updateEmptyState(true);
            return;
        }

        db.collection("events").document(eventId)
                .collection("participants")
                .whereEqualTo("status", "Selected")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    enrolledList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        String phone = doc.getString("phone");
                        String joinDate = doc.getString("joinDate");
                        String acceptDate = doc.getString("acceptDate");

                        Entrant entrant = new Entrant(name, email, phone, joinDate, acceptDate);
                        enrolledList.add(entrant);
                    }

                    filteredList.clear();
                    filteredList.addAll(enrolledList);
                    if (adapter != null) {
                        adapter.updateList(filteredList);
                    }
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateEmptyState(true);
                });
    }

    private void filterEntrants(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(enrolledList);
        } else {
            for (Entrant entrant : enrolledList) {
                if ((entrant.getName() != null && entrant.getName().toLowerCase().contains(query.toLowerCase())) ||
                        (entrant.getEmail() != null && entrant.getEmail().toLowerCase().contains(query.toLowerCase()))) {
                    filteredList.add(entrant);
                }
            }
        }

        if (adapter != null) {
            adapter.updateList(filteredList);
        }
        updateUI();
    }

    private void updateUI() {
        if (tvConfirmedCount != null) {
            tvConfirmedCount.setText(String.valueOf(filteredList.size()));
        }

        updateEmptyState(filteredList.isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            if (tvEmptyState != null) tvEmptyState.setVisibility(TextView.VISIBLE);
            if (rvEnrolledEntrants != null) rvEnrolledEntrants.setVisibility(RecyclerView.GONE);
        } else {
            if (tvEmptyState != null) tvEmptyState.setVisibility(TextView.GONE);
            if (rvEnrolledEntrants != null) rvEnrolledEntrants.setVisibility(RecyclerView.VISIBLE);
        }
    }

    private void exportToCSV() {
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No entrants to export", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "enrolled_entrants_" + (eventName != null ? eventName.replaceAll("\\s+", "_") : "event") + "_" + System.currentTimeMillis() + ".csv";
        createDocumentLauncher.launch(fileName);
    }

    private void writeCsvToUri(Uri uri) {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("Name,Email,Phone,Join Date,Accept Date\n");

            for (Entrant entrant : filteredList) {
                csv.append("\"").append(entrant.getName()).append("\",");
                csv.append("\"").append(entrant.getEmail()).append("\",");
                csv.append("\"").append(entrant.getPhoneNumber() != null ? entrant.getPhoneNumber() : "").append("\",");
                csv.append("\"").append(entrant.getJoinDate() != null ? entrant.getJoinDate() : "").append("\",");
                csv.append("\"").append(entrant.getAcceptDate() != null ? entrant.getAcceptDate() : "").append("\"\n");
            }

            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                outputStream.write(csv.toString().getBytes());
                outputStream.close();
                Toast.makeText(this, "CSV exported successfully", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void emailAllEntrants() {
        List<String> emails = new ArrayList<>();
        for (Entrant entrant : filteredList) {
            if (entrant.getEmail() != null && !entrant.getEmail().isEmpty()) {
                emails.add(entrant.getEmail());
            }
        }

        if (emails.isEmpty()) {
            Toast.makeText(this, "No emails to send", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        
        // Use an array of emails for better compatibility
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emails.toArray(new String[0]));
        
        // Professional Subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Important Update: " + (eventName != null ? eventName : "Event"));

        // Formal Auto-Email Letter Script
        String emailBody = "Dear Attendee,\n\n" +
                "We are writing to provide you with an important update regarding our upcoming event: " + (eventName != null ? eventName : "the event") + ".\n\n" +
                "We are excited to have you join us! Please keep this email for your records. " +
                "We will be sharing more details and instructions as we get closer to the event date.\n\n" +
                "If you have any immediate questions or require further assistance, please feel free to reply to this message.\n\n" +
                "Best regards,\n" +
                "The Organizing Team\n" +
                "EventFlow Management";

        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email app installed", Toast.LENGTH_SHORT).show();
        }
    }
}
