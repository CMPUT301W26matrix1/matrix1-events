package com.example.eventflow.org_event.manage_entrant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.R;
import com.example.eventflow.model.entities.Entrant;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrganizerFinalEntrantsActivity extends AppCompatActivity {

    private RecyclerView rvEnrolledEntrants;
    private EditText etSearch;
    private TextView tvConfirmedCount, tvEmptyState;
    private LinearLayout btnExportCSV, btnEmailAll;
    private EnrolledEntrantsAdapter adapter;
    private List<Entrant> enrolledList = new ArrayList<>();
    private List<Entrant> filteredList = new ArrayList<>();
    private FirebaseFirestore db;
    private String eventId;
    private String eventName;

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
            btnExportCSV.setOnClickListener(v -> exportToCSV());
        }
        if (btnEmailAll != null) {
            btnEmailAll.setOnClickListener(v -> emailAllEntrants());
        }

        if (eventId != null && !eventId.isEmpty()) {
            loadConfirmedEntrants();
        } else {
            updateEmptyState(true);
            if (tvConfirmedCount != null) tvConfirmedCount.setText("0");
        }
    }

    private void loadConfirmedEntrants() {
        if (eventId == null) return;

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    List<String> selectedIds = (List<String>) eventDoc.get("selectedEntrants");

                    Log.d("FINAL_ENTRANTS", "Selected IDs from event: " + selectedIds);
                    Log.d("FINAL_ENTRANTS", "Count: " + (selectedIds != null ? selectedIds.size() : 0));

                    if (selectedIds == null || selectedIds.isEmpty()) {
                        updateEmptyState(true);
                        if (tvConfirmedCount != null) tvConfirmedCount.setText("0");
                        return;
                    }

                    enrolledList.clear();

                    for (String userId : selectedIds) {
                        checkIfAccepted(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateEmptyState(true);
                });
    }

    private void checkIfAccepted(String userId) {
        db.collection("users").document(userId)
                .collection("event_participations").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String status = doc.getString("status");
                        Log.d("FINAL_ENTRANTS", "User " + userId + " status: " + status);
                        if ("ACCEPTED".equals(status)) {
                            fetchUserProfile(userId);
                        }
                    } else {
                        Log.d("FINAL_ENTRANTS", "No participation record for: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FINAL_ENTRANTS", "Error checking status for: " + userId);
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
                            name = firstName;
                            if (lastName != null && !lastName.isEmpty()) {
                                name = firstName + " " + lastName;
                            }
                        } else {
                            // If no name, try to get from credentials
                            findNameFromCredentials(userId);
                            return;
                        }
                        email = doc.getString("email");
                        if (email == null) email = "";
                        phone = doc.getString("phone");
                        if (phone == null) phone = "";

                        enrolledList.add(new Entrant(name, email, phone, "", "Confirmed"));
                        filterEntrants(etSearch != null ? etSearch.getText().toString() : "");
                        updateUI();
                    } else {
                        // User document doesn't exist, try to find from credentials
                        findNameFromCredentials(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    // Add with userId as name
                    enrolledList.add(new Entrant(userId, "", "", "", "Confirmed"));
                    filterEntrants(etSearch != null ? etSearch.getText().toString() : "");
                    updateUI();
                });
    }

    private void findNameFromCredentials(String userId) {
        db.collection("credentials")
                .whereEqualTo("uid", userId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        String username = snapshots.getDocuments().get(0).getString("username");
                        String email = snapshots.getDocuments().get(0).getString("email");
                        if (username != null && !username.isEmpty()) {
                            enrolledList.add(new Entrant(username, email != null ? email : "", "", "", "Confirmed"));
                        } else if (email != null && !email.isEmpty()) {
                            String nameFromEmail = email.split("@")[0];
                            enrolledList.add(new Entrant(nameFromEmail, email, "", "", "Confirmed"));
                        } else {
                            enrolledList.add(new Entrant(userId, "", "", "", "Confirmed"));
                        }
                    } else {
                        enrolledList.add(new Entrant(userId, "", "", "", "Confirmed"));
                    }
                    filterEntrants(etSearch != null ? etSearch.getText().toString() : "");
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    enrolledList.add(new Entrant(userId, "", "", "", "Confirmed"));
                    filterEntrants(etSearch != null ? etSearch.getText().toString() : "");
                    updateUI();
                });
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

        adapter.updateList(filteredList);
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
            if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
            if (rvEnrolledEntrants != null) rvEnrolledEntrants.setVisibility(View.GONE);
        } else {
            if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
            if (rvEnrolledEntrants != null) rvEnrolledEntrants.setVisibility(View.VISIBLE);
        }
    }

    private void exportToCSV() {
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No entrants to export", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "confirmed_entrants_" + (eventName != null ? eventName.replaceAll("\\s+", "_") : "event") + "_" + System.currentTimeMillis() + ".csv";
        createDocumentLauncher.launch(fileName);
    }

    private void writeCsvToUri(Uri uri) {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("Name,Email,Phone,Status\n");
            for (Entrant e : filteredList) {
                csv.append("\"").append(e.getName()).append("\",");
                csv.append("\"").append(e.getEmail()).append("\",");
                csv.append("\"").append(e.getPhoneNumber() != null ? e.getPhoneNumber() : "").append("\",");
                csv.append("\"Confirmed\"\n");
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
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Event Update - " + (eventName != null ? eventName : "Event"));
        startActivity(Intent.createChooser(emailIntent, "Send email"));
    }

    private class EnrolledEntrantsAdapter extends RecyclerView.Adapter<EnrolledEntrantsAdapter.ViewHolder> {
        private List<Entrant> entrants;

        EnrolledEntrantsAdapter(List<Entrant> entrants) {
            this.entrants = entrants != null ? entrants : new ArrayList<>();
        }

        void updateList(List<Entrant> newList) {
            this.entrants = newList != null ? newList : new ArrayList<>();
            notifyDataSetChanged();
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
            if (position < entrants.size()) {
                Entrant e = entrants.get(position);
                if (e != null) {
                    holder.tvName.setText(e.getName() != null ? e.getName() : "Unknown");
                    holder.tvEmail.setText(e.getEmail() != null ? e.getEmail() : "");
                    holder.tvPhone.setText(e.getPhoneNumber() != null && !e.getPhoneNumber().isEmpty() ? e.getPhoneNumber() : "No phone");
                    holder.tvStatus.setText("Confirmed");
                    holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                }
            }
        }

        @Override
        public int getItemCount() {
            return entrants != null ? entrants.size() : 0;
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