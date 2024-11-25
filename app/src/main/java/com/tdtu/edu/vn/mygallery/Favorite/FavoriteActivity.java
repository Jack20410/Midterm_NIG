package com.tdtu.edu.vn.mygallery.Favorite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tdtu.edu.vn.mygallery.Album.OfflineAlbumActivity;
import com.tdtu.edu.vn.mygallery.LoginActivity;
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
        setContentView(R.layout.activity_favorite);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewFavorites);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        loadFavoriteImages(); // Load images from the Favorite folder

        FavoriteImagesAdapter adapter = new FavoriteImagesAdapter(favoriteImages, this);
        recyclerView.setAdapter(adapter);

        setupRecycleBinButton(); // Set up the Recycle Bin button
        setupBottomNavigationView(); // Set up bottom navigation
    }

    private void setupRecycleBinButton() {
        Button buttonRecycleBin = findViewById(R.id.buttonRecycleBin);
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

    private void setupBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return switch (item.getItemId()) {
                    case R.id.navigation_main -> {
                        startActivity(new Intent(FavoriteActivity.this, MainActivity.class));
                        yield true;
                    }
                    case R.id.navigation_offline_album -> {
                        startActivity(new Intent(FavoriteActivity.this, OfflineAlbumActivity.class));
                        yield true;
                    }
                    case R.id.navigation_favorite ->
                        // Already in FavoriteActivity
                            true;
                    case R.id.navigation_login -> {
                        startActivity(new Intent(FavoriteActivity.this, LoginActivity.class));
                        yield true;
                    }
                    case R.id.navigation_search -> {
                        startActivity(new Intent(FavoriteActivity.this, SearchActivity.class));
                        yield true;
                    }
                    default -> false;
                };
            }
        });

        // Highlight the favorite icon correctly
        bottomNavigationView.setSelectedItemId(R.id.navigation_favorite);
    }
}
