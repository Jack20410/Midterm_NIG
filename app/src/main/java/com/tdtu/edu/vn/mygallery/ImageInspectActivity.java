package com.tdtu.edu.vn.mygallery;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

public class ImageInspectActivity extends AppCompatActivity {

    private PhotoView photoView;
    private Button addToFavoritesButton;
    private Button moveToRecycleBinButton;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_inspect);

        photoView = findViewById(R.id.photoView);
        addToFavoritesButton = findViewById(R.id.addToFavoritesButton);


        // Get the image path from the intent
        Intent intent = getIntent();
        imagePath = intent.getStringExtra("IMAGE_PATH");

        if (imagePath != null) {
            // Load the image using Glide
            Glide.with(this)
                    .load(imagePath)
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.three_button)
                    .into(photoView);
        } else {
            Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add to Favorites Button
        addToFavoritesButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("Favorites", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String existingFavorites = sharedPreferences.getString("favoriteImages", "");

            if (!existingFavorites.contains(imagePath)) {
                existingFavorites += imagePath + ";";
            }

            editor.putString("favoriteImages", existingFavorites);
            editor.apply();

            Toast.makeText(this, "Image added to Favorites!", Toast.LENGTH_SHORT).show();
        });


    }
}
