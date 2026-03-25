package com.example.eventflow.org_event.manage_entrant;

import android.os.Bundle;
import android.text.Editable; // ADD THIS
import android.text.TextWatcher; // ADD THIS
import android.widget.EditText; // ADD THIS
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventflow.R;
import java.util.ArrayList;
import java.util.List;

public class CancelledEntrantsActivity extends AppCompatActivity {

    private RecyclerView rvCancelled;
    private CancelledEntrantsAdapter adapter;
    private List<Entrant> cancelledList; // This stays as your "Master List"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancelled_entrants);

        // 1. Setup Back Button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 2. Initialize RecyclerView
        rvCancelled = findViewById(R.id.rvCancelled);
        rvCancelled.setLayoutManager(new LinearLayoutManager(this));

        // 3. Prepare Mock Data
        cancelledList = new ArrayList<>();
        cancelledList.add(new Entrant("Elena Torres", "elena@example.com", "(555) 567-8901", "Mar 17, 2025"));
        cancelledList.add(new Entrant("David Lee", "david@example.com", "(555) 456-7890", "Mar 15, 2025"));
        cancelledList.add(new Entrant("Carla Smith", "carla@example.com", "(555) 345-6789", "Mar 14, 2025"));
        cancelledList.add(new Entrant("Bob Martinez", "bob@example.com", "(555) 234-5678", "Mar 12, 2025"));
        cancelledList.add(new Entrant("Alice Johnson", "alice@example.com", "(555) 123-4567", "Mar 10, 2025"));

        // 4. Set the Adapter
        adapter = new CancelledEntrantsAdapter(cancelledList);
        rvCancelled.setAdapter(adapter);

        // --- NEW: 5. Setup Search Logic ---
        EditText etSearch = findViewById(R.id.etSearch); // Ensure this ID matches your XML
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    // --- NEW: 6. Filter Method ---
    private void filter(String text) {
        List<Entrant> filteredList = new ArrayList<>();

        for (Entrant item : cancelledList) {
            // Check if name or email matches the search text (case insensitive)
            if (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                    item.getEmail().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }

        // Use the updateList method we added to your Adapter earlier
        adapter.updateList(filteredList);
    }
}
