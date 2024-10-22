package com.tdtu.edu.vn.mygallery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;



public class FavoriteActivity extends AppCompatActivity {
    private List<String> favoriteImages = new ArrayList<>();
    private RecyclerView recyclerView;
    private FavoriteImagesAdapter adapter;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        recyclerView = findViewById(R.id.recyclerViewFavorites);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        loadFavoriteImages();  // Load favorites from SharedPreferences

        adapter = new FavoriteImagesAdapter(favoriteImages, this);
        recyclerView.setAdapter(adapter);

        setupGestureDetector();
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


    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new SwipeGestureDetector());
    }

    // Override dispatchTouchEvent to capture all touch events
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(event);
        }
        return super.dispatchTouchEvent(event);
    }

    private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;

            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY) &&
                    Math.abs(diffX) > SWIPE_THRESHOLD &&
                    Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                if (diffX < 0) {
                    // Swipe left: Navigate back to MainActivity
                    Intent intent = new Intent(FavoriteActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
                return true;
            }
            return false;
        }
    }
}
