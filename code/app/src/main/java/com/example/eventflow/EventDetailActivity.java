package com.example.eventflow;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EventDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        TextView nameText = findViewById(R.id.detailEventName);
        TextView locationText = findViewById(R.id.detailEventLocation);
        TextView descriptionText = findViewById(R.id.detailEventDescription);

        String name = getIntent().getStringExtra("eventName");
        String location = getIntent().getStringExtra("eventLocation");
        String description = getIntent().getStringExtra("eventDescription");

        nameText.setText(name);
        locationText.setText(location);
        descriptionText.setText(description);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }
}
