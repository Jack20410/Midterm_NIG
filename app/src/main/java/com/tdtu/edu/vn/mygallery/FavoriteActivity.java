package com.tdtu.edu.vn.mygallery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoriteActivity extends AppCompatActivity {
    private List<String> favoriteImages = new ArrayList<>();
    private RecyclerView recyclerView;
    private FavoriteImagesAdapter adapter;
    private GestureDetector gestureDetector;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        recyclerView = findViewById(R.id.recyclerViewFavorites);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        loadFavoriteImages();  // Load favorites from SharedPreferences

        adapter = new FavoriteImagesAdapter(favoriteImages, this);
        recyclerView.setAdapter(adapter);


        setupBottomNavigationView();
    }

    private void loadFavoriteImages() {
        SharedPreferences sharedPreferences = getSharedPreferences("Favorites", MODE_PRIVATE);
        String savedFavorites = sharedPreferences.getString("favoriteImages", "");

        if (!savedFavorites.isEmpty()) {
            String[] paths = savedFavorites.split(";");
            // Use a Set to ensure no duplicate paths are added
            Set<String> uniquePaths = new HashSet<>(Arrays.asList(paths));
            favoriteImages.clear(); // Clear existing data to prevent re-adding
            favoriteImages.addAll(uniquePaths); // Add only unique paths
        }
    }



    private void setupBottomNavigationView() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_main:
                        startActivity(new Intent(FavoriteActivity.this, MainActivity.class));
                        return true;
                    case R.id.navigation_offline_album:
                        startActivity(new Intent(FavoriteActivity.this, OfflineAlbumActivity.class));
                        return true;
                    case R.id.navigation_favorite:
                        // Already in FavoriteActivity
                        return true;
                    case R.id.navigation_login:
                        startActivity(new Intent(FavoriteActivity.this, LoginActivity.class));
                        return true;
                    case R.id.navigation_search:
                        startActivity(new Intent(FavoriteActivity.this, SearchActivity.class));
                        return true;
                    default:
                        return false;
                }
            }
        });

        // Highlight the favorite icon correctly
        bottomNavigationView.setSelectedItemId(R.id.navigation_favorite);
    }

    // Override dispatchTouchEvent to capture all touch events
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(event);
        }
        return super.dispatchTouchEvent(event);
    }


}
