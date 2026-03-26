package com.example.eventflow;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class FullscreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        ImageView ivFullscreenImage = findViewById(R.id.iv_fullscreen_image);

        String imageUrl = getIntent().getStringExtra("imageUrl");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).into(ivFullscreenImage);
        }

        // Close when clicked
        ivFullscreenImage.setOnClickListener(v -> finish());
    }
}