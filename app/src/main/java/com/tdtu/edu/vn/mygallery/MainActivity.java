package com.tdtu.edu.vn.mygallery;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import android.widget.Button;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.database.Cursor;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import java.io.File;
import androidx.constraintlayout.widget.ConstraintLayout;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private boolean isGridView = true;
    private RecyclerView recyclerView;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        ConstraintLayout parentLayout = findViewById(R.id.constraintLayout);
        parentLayout.requestFocus();
        parentLayout.setFocusableInTouchMode(true); // Ensure it can take focus

        // Clear focus from all views to ensure no element (like search bar) is focused initially
        parentLayout.clearFocus();
        // Initialize gesture detector
        gestureDetector = new GestureDetector(this, new SwipeGestureDetector());

        ImageButton recycleBinButton = findViewById(R.id.recycleBinButton);
        recycleBinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecycleBinActivity.class);
                startActivity(intent);
            }
        });

        // Set a touch listener on the RecyclerView to detect swipe gestures
        recyclerView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start LoginActivity when the button is pressed
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        loadAndDisplayImages();
        Button toggleViewButton = findViewById(R.id.toggleViewButton); // Assuming this button is defined in your XML
        toggleViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGridView = !isGridView;
                if (isGridView) {
                    recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));
                } else {
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                }
            }
        });

        // Check and request storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            loadImagesFromDevice(); // Load images if permission is already granted
        }
    }

    // Gesture detector class for handling swipe events
    // Gesture detector class for handling swipe events
    private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Check if both MotionEvent objects are non-null
            if (e1 == null || e2 == null) {
                return false; // Ignore the gesture if events are null
            }

            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            // Only handle horizontal swipes
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX < 0) {
                        // Swipe left detected
                        Intent intent = new Intent(MainActivity.this, OfflineAlbumActivity.class);
                        startActivity(intent);
                    }
                    return true;
                }
            }
            return false;
        }
    }


    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with file operations
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private List<String> loadImagesFromDevice() {
        List<String> imagePaths = new ArrayList<>();
        String[] projection = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                if (new File(imagePath).exists()) {
                    imagePaths.add(imagePath);
                }
            }
            cursor.close();
        }

        Log.d("MainActivity", "Number of images loaded: " + imagePaths.size());
        return imagePaths;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String restoredImagePath = data.getStringExtra("restoredImagePath");

            if (restoredImagePath != null) {
                Log.d("MainActivity", "Restored image: " + restoredImagePath);

                // Reload images including the restored one
                List<String> imagePaths = loadImagesFromDevice();
                if (!imagePaths.contains(restoredImagePath)) {
                    imagePaths.add(0, restoredImagePath);  // Add at the top of the list
                }

                displayImagesInGrid(imagePaths);
                Toast.makeText(this, "Image restored successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }




    // Load and display images from the device's storage
    private void loadAndDisplayImages() {
        List<String> imagePaths = loadImagesFromDevice();
        displayImagesInGrid(imagePaths);
    }

    private void displayImagesInGrid(List<String> imagePaths) {
        ImageAdapter adapter = new ImageAdapter(imagePaths, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.getRecycledViewPool().clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if a restored image path is provided in the intent extras
        Intent intent = getIntent();
        String restoredImagePath = intent.getStringExtra("restoredImagePath");

        if (restoredImagePath != null) {
            Log.d("MainActivity", "Restored image: " + restoredImagePath);

            // Reload the images and ensure the restored image appears
            List<String> imagePaths = loadImagesFromDevice();
            displayImagesInGrid(imagePaths); // Reload images

            // Clear the intent extra to avoid duplicate reloads
            getIntent().removeExtra("restoredImagePath");

            Toast.makeText(this, "Image restored successfully", Toast.LENGTH_SHORT).show();
        } else {
            // Load images normally if no restored image is provided
            List<String> imagePaths = loadImagesFromDevice();
            displayImagesInGrid(imagePaths);
        }
    }


}