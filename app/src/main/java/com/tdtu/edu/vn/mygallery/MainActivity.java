package com.tdtu.edu.vn.mygallery;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

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
import android.widget.Toast;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;

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

        // Initialize gesture detector
        gestureDetector = new GestureDetector(this, new SwipeGestureDetector());

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
    private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
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

    private void loadImagesFromDevice() {
        List<String> imagePaths = new ArrayList<>();
        String[] projection = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                imagePaths.add(imagePath);
            }
            cursor.close();
        }
        Log.d("MainActivity", "Number of images found: " + imagePaths.size());

        displayImagesInGrid(imagePaths); // Display the images using RecyclerView
    }

    private void displayImagesInGrid(List<String> imagePaths) {
        ImageAdapter adapter = new ImageAdapter(imagePaths, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // Display images in a grid format
    }
}