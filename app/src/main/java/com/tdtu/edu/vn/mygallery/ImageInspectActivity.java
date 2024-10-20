package com.tdtu.edu.vn.mygallery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

public class ImageInspectActivity extends AppCompatActivity {

    private PhotoView photoView;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_inspect);
        photoView = findViewById(R.id.photoView);
        // Get the image path from the intent
        Intent intent = getIntent();
        imagePath = intent.getStringExtra("IMAGE_PATH");

        if (imagePath != null) {
            // Load the image using Glide
            Glide.with(this)
                    .load(imagePath)
                    .placeholder(R.drawable.album_placeholder)  // Add your placeholder image here
                    .error(R.drawable.three_button)  // Add an error image to display if Glide fails
                    .into(photoView);

        } else {
            Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
        }
    }
}