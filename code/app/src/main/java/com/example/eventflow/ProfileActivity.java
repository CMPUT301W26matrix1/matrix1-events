package com.example.eventflow;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.eventflow.view.profile.ProfileContainerFragment;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ProfileContainerFragment())
                .commit();
        }
    }
}
