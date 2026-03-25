package com.example.eventflow.org_event.manage_entrant;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.R;
import java.util.ArrayList;
import java.util.List;

public class WaitlistMgmtActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WaitlistAdapter adapter;
    private List<Entrant> mockEntrants;
    private TextView tvWaitlistCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure status bar matches your dark theme
        getWindow().setStatusBarColor(android.graphics.Color.BLACK);
        setContentView(R.layout.activity_waitlist_mgmt);

        // 1. Initialize Views
        tvWaitlistCount = findViewById(R.id.tvWaitlistCount);
        ImageView btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.rvEntrants);

        // 2. Set up the Back Button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 3. Create Mock Data
        mockEntrants = new ArrayList<>();
        mockEntrants.add(new Entrant("Frank Williams", "Mar 18, 2025"));
        mockEntrants.add(new Entrant("Grace Kim", "Mar 18, 2025"));
        mockEntrants.add(new Entrant("Henry Brown", "Mar 19, 2025"));
        mockEntrants.add(new Entrant("Isla Davis", "Mar 19, 2025"));

        updateCountText();

        // 4. Setup RecyclerView & Adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // We pass a listener to the adapter so we can update the UI when an item is removed
        adapter = new WaitlistAdapter(mockEntrants, () -> {
            updateCountText();
        });

        recyclerView.setAdapter(adapter);
    }

    /**
     * Updates the "4 entrants on waitlist" label dynamically.
     */
    private void updateCountText() {
        if (tvWaitlistCount != null) {
            tvWaitlistCount.setText(mockEntrants.size() + " entrants on waitlist");
        }
    }
}
