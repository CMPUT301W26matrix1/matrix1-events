/**
 * Activity for organizers to view the final list of confirmed entrants.
 * Supports searching, exporting to CSV (locally or to Cloud/Drive), and bulk emailing.
 * Handles fetching confirmed participants from Firestore.
 */
package com.example.eventflow.org_event.manage_entrant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.R;
import com.example.eventflow.model.entities.Entrant;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for organizers to view the final list of confirmed entrants.
 * Supports searching, exporting to CSV (locally or to Cloud/Drive), and bulk emailing.
 */
public class OrganizerFinalEntrantsActivity extends AppCompatActivity {

    private static final String TAG = "FinalEntrants";
    private RecyclerView rvEnrolledEntrants;
    private EditText etSearch;
    private TextView tvConfirmedCount, tvEmptyState;
    private LinearLayout btnExportCSV, btnEmailAll;
    private EnrolledEntrantsAdapter adapter;
    private final List<Entrant> enrolledList = new ArrayList<>();
    private final List<Entrant> filteredList = new ArrayList<>();
    private FirebaseFirestore db;
    private String eventId;
    private String eventName;

    // Launcher for saving the CSV file to a user-selected local location
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

        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();

        if (eventId != null && !eventId.isEmpty()) {
            loadConfirmedEntrants();
        } else {
            updateUI();
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

        adapter = new EnrolledEntrantsAdapter(filteredList);
        rvEnrolledEntrants.setLayoutManager(new LinearLayoutManager(this));
        rvEnrolledEntrants.setAdapter(adapter);
    }

    private void setupListeners() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterEntrants(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (btnExportCSV != null) {
            btnExportCSV.setOnClickListener(v -> showExportOptions());
        }
        if (btnEmailAll != null) {
            btnEmailAll.setOnClickListener(v -> emailAllEntrants());
        }
    }

    private void loadConfirmedEntrants() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    List<String> selectedIds = (List<String>) eventDoc.get("selectedEntrants");
                    if (selectedIds == null || selectedIds.isEmpty()) {
                        updateUI();
                        return;
                    }

                    enrolledList.clear();
                    for (String userId : selectedIds) {
                        checkIfAccepted(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading entrants: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateUI();
                });
    }

    private void checkIfAccepted(String userId) {
        db.collection("users").document(userId)
                .collection("event_participations").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String status = doc.getString("status");
                        if ("ACCEPTED".equals(status)) {
                            fetchUserProfile(userId);
                        }
                    }
                });
    }

    private void fetchUserProfile(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    String name = userId;
                    String email = "";
                    String phone = "";

                    if (doc.exists()) {
                        String firstName = doc.getString("firstName");
                        String lastName = doc.getString("lastName");
                        if (firstName != null && !firstName.isEmpty()) {
                            name = firstName + (lastName != null && !lastName.isEmpty() ? " " + lastName : "");
                        }
                        email = doc.getString("email");
                        phone = doc.getString("phone");
                        
                        addEntrantToList(new Entrant(name, email != null ? email : "", phone != null ? phone : "", "", "Confirmed"));
                    } else {
                        findNameFromCredentials(userId);
                    }
                })
                .addOnFailureListener(e -> addEntrantToList(new Entrant(userId, "", "", "", "Confirmed")));
    }

    private void findNameFromCredentials(String userId) {
        db.collection("credentials")
                .whereEqualTo("uid", userId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        String username = snapshots.getDocuments().get(0).getString("username");
                        String email = snapshots.getDocuments().get(0).getString("email");
                        String name = (username != null && !username.isEmpty()) ? username : 
                                     (email != null ? email.split("@")[0] : userId);
                        addEntrantToList(new Entrant(name, email != null ? email : "", "", "", "Confirmed"));
                    } else {
                        addEntrantToList(new Entrant(userId, "", "", "", "Confirmed"));
                    }
                })
                .addOnFailureListener(e -> addEntrantToList(new Entrant(userId, "", "", "", "Confirmed")));
    }

    private void addEntrantToList(Entrant entrant) {
        enrolledList.add(entrant);
        filterEntrants(etSearch != null ? etSearch.getText().toString() : "");
    }

    private void filterEntrants(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(enrolledList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Entrant e : enrolledList) {
                if ((e.getName() != null && e.getName().toLowerCase().contains(lowerQuery)) ||
                    (e.getEmail() != null && e.getEmail().toLowerCase().contains(lowerQuery))) {
                    filteredList.add(e);
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        if (tvConfirmedCount != null) {
            tvConfirmedCount.setText(String.valueOf(filteredList.size()));
        }
        boolean isEmpty = filteredList.isEmpty();
        if (tvEmptyState != null) tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (rvEnrolledEntrants != null) rvEnrolledEntrants.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showExportOptions() {
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No entrants to export", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = {"Save to Device (Local)", "Upload to Google Drive / Share"};
        new AlertDialog.Builder(this)
                .setTitle("Export CSV")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        exportLocally();
                    } else {
                        exportToCloud();
                    }
                })
                .show();
    }

    private void exportLocally() {
        String safeEventName = (eventName != null ? eventName.replaceAll("[^a-zA-Z0-9]", "_") : "event");
        String fileName = "confirmed_entrants_" + safeEventName + ".csv";
        createDocumentLauncher.launch(fileName);
    }

    private void exportToCloud() {
        try {
            String safeEventName = (eventName != null ? eventName.replaceAll("[^a-zA-Z0-9]", "_") : "event");
            File tempFile = new File(getCacheDir(), "confirmed_entrants_" + safeEventName + ".csv");
            
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                // Write BOM for Excel
                fos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
                
                StringBuilder sb = new StringBuilder();
                sb.append("Name,Email,Phone,Status\n");
                for (Entrant e : filteredList) {
                    sb.append(escapeCsv(e.getName())).append(",");
                    sb.append(escapeCsv(e.getEmail())).append(",");
                    sb.append(escapeCsv(e.getPhoneNumber())).append(",");
                    sb.append("\"Confirmed\"\n");
                }
                fos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                fos.flush();
            }

            Uri uri = FileProvider.getUriForFile(this, "com.example.eventflow.fileprovider", tempFile);
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "Save to Drive or Share via..."));

        } catch (IOException e) {
            Log.e(TAG, "Error sharing CSV", e);
            Toast.makeText(this, "Failed to prepare file for upload", Toast.LENGTH_SHORT).show();
        }
    }

    private void writeCsvToUri(Uri uri) {
        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream == null) return;

            // Write BOM for Excel UTF-8 compatibility
            outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            StringBuilder sb = new StringBuilder();
            // Header
            sb.append("Name,Email,Phone,Status\n");
            
            // Data
            for (Entrant e : filteredList) {
                sb.append(escapeCsv(e.getName())).append(",");
                sb.append(escapeCsv(e.getEmail())).append(",");
                sb.append(escapeCsv(e.getPhoneNumber())).append(",");
                sb.append("\"Confirmed\"\n");
            }

            outputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            
            Log.d(TAG, "Successfully wrote " + filteredList.size() + " rows to CSV locally");
            runOnUiThread(() -> showDownloadSuccess(uri));

        } catch (IOException e) {
            Log.e(TAG, "Error writing CSV locally", e);
            runOnUiThread(() -> Toast.makeText(this, "Failed to write file", Toast.LENGTH_SHORT).show());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private void showDownloadSuccess(Uri uri) {
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            Snackbar.make(rootView, "File saved locally", Snackbar.LENGTH_LONG)
                    .setAction("Open", v -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "text/csv");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(Intent.createChooser(intent, "Open with..."));
                        } catch (Exception ex) {
                            intent.setDataAndType(uri, "text/plain");
                            try {
                                startActivity(intent);
                            } catch (Exception e2) {
                                Toast.makeText(this, "No app found to open CSV.", Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .show();
        }
    }

    private void emailAllEntrants() {
        List<String> emails = new ArrayList<>();
        for (Entrant e : filteredList) {
            if (e.getEmail() != null && !e.getEmail().isEmpty()) {
                emails.add(e.getEmail());
            }
        }

        if (emails.isEmpty()) {
            Toast.makeText(this, "No emails to send", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emails.toArray(new String[0]));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Event Update: " + (eventName != null ? eventName : "Event"));
        startActivity(Intent.createChooser(emailIntent, "Send email via..."));
    }

    private class EnrolledEntrantsAdapter extends RecyclerView.Adapter<EnrolledEntrantsAdapter.ViewHolder> {
        private final List<Entrant> entrants;

        EnrolledEntrantsAdapter(List<Entrant> entrants) {
            this.entrants = entrants;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_enrolled_entrant, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Entrant e = entrants.get(position);
            holder.tvName.setText(e.getName() != null ? e.getName() : "Unknown");
            holder.tvEmail.setText(e.getEmail() != null ? e.getEmail() : "");
            holder.tvPhone.setText(e.getPhoneNumber() != null && !e.getPhoneNumber().isEmpty() ? e.getPhoneNumber() : "No phone");
            holder.tvStatus.setText("Confirmed");
            holder.tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        @Override
        public int getItemCount() {
            return entrants.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvEmail, tvPhone, tvStatus;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvEnrolledName);
                tvEmail = v.findViewById(R.id.tvEnrolledEmail);
                tvPhone = v.findViewById(R.id.tvEnrolledPhone);
                tvStatus = v.findViewById(R.id.tvEnrolledStatus);
            }
        }
    }
}
