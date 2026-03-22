package com.example.eventflow;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.eventflow.event.EventListFragment;

public class BrowseEventsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new EventListFragment())
                .commit();
        }
    }
}
