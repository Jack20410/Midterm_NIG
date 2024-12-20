package com.tdtu.edu.vn.mygallery.Favorite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.tdtu.edu.vn.mygallery.MainActivity;
import com.tdtu.edu.vn.mygallery.R;
import com.tdtu.edu.vn.mygallery.Utilities.RecycleBinActivity;
import com.tdtu.edu.vn.mygallery.Utilities.SearchActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {
    private final List<String> favoriteImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity_favorite);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewFavorites);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        loadFavoriteImages(); // Load images from the Favorite folder

        FavoriteImagesAdapter adapter = new FavoriteImagesAdapter(favoriteImages, this);
        recyclerView.setAdapter(adapter);

        setupRecycleBinButton(); // Set up the Recycle Bin button
    }

    private void setupRecycleBinButton() {
        ImageButton buttonRecycleBin = findViewById(R.id.buttonRecycleBin);
        buttonRecycleBin.setOnClickListener(v -> {
            Intent intent = new Intent(FavoriteActivity.this, RecycleBinActivity.class);
            startActivity(intent);
        });
    }

    private void loadFavoriteImages() {
        File favoritesFolder = new File(getFilesDir(), "Favorites");

        // Check if the folder exists and is a directory
        if (favoritesFolder.exists() && favoritesFolder.isDirectory()) {
            File[] files = favoritesFolder.listFiles();

            if (files != null) {
                favoriteImages.clear();

                for (File file : files) {
                    if (file.isFile()) { // Ensure it's a file
                        favoriteImages.add(file.getAbsolutePath());
                        Log.d("FavoriteImages", "Loaded favorite image: " + file.getAbsolutePath());
                    }
                }
            }
        } else {
            Log.d("FavoriteImages", "Favorites folder is empty or does not exist.");
        }

        Log.d("FavoriteImages", "Total favorite images loaded: " + favoriteImages.size());
    }
}
