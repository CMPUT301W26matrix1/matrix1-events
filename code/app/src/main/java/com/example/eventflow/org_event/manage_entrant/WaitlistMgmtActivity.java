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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(android.graphics.Color.BLACK);
        setContentView(R.layout.activity_waitlist_mgmt);

        ImageView btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.rvEntrants);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        mockEntrants = new ArrayList<>();
        mockEntrants.add(new Entrant("Alice Johnson", "alice@example.com", "Selected"));
        mockEntrants.add(new Entrant("Bob Smith", "bob@example.com", "Waiting"));
        mockEntrants.add(new Entrant("Carol Williams", "carol@example.com", "Accepted"));
        mockEntrants.add(new Entrant("David Brown", "david@example.com", "Cancelled"));
        mockEntrants.add(new Entrant("Eva Martinez", "eva@example.com", "Not selected"));
        mockEntrants.add(new Entrant("Frank Lee", "frank@example.com", "Selected"));
        mockEntrants.add(new Entrant("Grace Kim", "grace@example.com", "Waiting"));
        mockEntrants.add(new Entrant("Henry Davis", "henry@example.com", "Declined"));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WaitlistAdapter(mockEntrants, () -> {
            // Update logic if needed
        });

        recyclerView.setAdapter(adapter);
    }
}
