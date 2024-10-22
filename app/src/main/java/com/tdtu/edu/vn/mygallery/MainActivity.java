package com.tdtu.edu.vn.mygallery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    // Constants
    private static final int PERMISSION_REQUEST_CODE = 100;

    // UI Elements and Variables
    private boolean isGridView = true;
    private RecyclerView recyclerView;
    private GestureDetector gestureDetector;
    private List<ImageData> allImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUI();
        setupGestureDetector();
        setupListeners();
        checkPermissionsAndLoadImages();

    }

    // Initialization
    private void initializeUI() {
        recyclerView = findViewById(R.id.recyclerView);
        ConstraintLayout parentLayout = findViewById(R.id.constraintLayout);

        // Clear focus on the parent layout to avoid input issues
        parentLayout.requestFocus();
        parentLayout.setFocusableInTouchMode(true);
        parentLayout.clearFocus();
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new SwipeGestureDetector());
        recyclerView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    // Setting up listeners for buttons
    private void setupListeners() {
        setupRecycleBinButton();
        setupLoginButton();
        setupToggleViewButton();
        setupSearchButton();
    }

    private void setupRecycleBinButton() {
        ImageButton recycleBinButton = findViewById(R.id.recycleBinButton);
        recycleBinButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RecycleBinActivity.class))
        );
    }

    private void setupLoginButton() {
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginActivity.class))
        );
    }

    private void setupToggleViewButton() {
        Button toggleViewButton = findViewById(R.id.toggleViewButton);
        toggleViewButton.setOnClickListener(v -> {
            isGridView = !isGridView;
            recyclerView.setLayoutManager(isGridView ?
                    new GridLayoutManager(MainActivity.this, 3) :
                    new LinearLayoutManager(MainActivity.this)
            );
        });
    }

    private void setupSearchButton() {
        Button searchButton = findViewById(R.id.searchButton);
        EditText searchInput = findViewById(R.id.searchInput);

        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            List<ImageData> filteredImages = query.isEmpty() ?
                    allImages : filterImagesByQuery(allImages, query);
            displayImagesInGrid(filteredImages);
        });
    }

    // Permissions Handling
    private void checkPermissionsAndLoadImages() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            allImages = loadImagesFromDevice();
            displayImagesInGrid(allImages);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            allImages = loadImagesFromDevice();
            displayImagesInGrid(allImages);
        } else {
            Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
        }
    }


    // Gesture Handling
    private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;

            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY) &&
                    Math.abs(diffX) > SWIPE_THRESHOLD &&
                    Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                if (diffX < 0) {
                    // Swipe left: Navigate to OfflineAlbumActivity
                    startActivity(new Intent(MainActivity.this, OfflineAlbumActivity.class));
                } else {
                    // Swipe right: Navigate to FavoriteActivity
                    startActivity(new Intent(MainActivity.this, FavoriteActivity.class));
                }
                return true;
            }
            return false;
        }
    }


    // Image Loading and Display
    private List<ImageData> loadImagesFromDevice() {
        List<ImageData> imageList = new ArrayList<>();
        String[] projection = {MediaStore.Images.Media.DATA};

        try (Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")) {

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String imagePath = cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                    if (new File(imagePath).exists()) {
                        ExifInterface exif = new ExifInterface(imagePath);
                        String dateTaken = exif.getAttribute(ExifInterface.TAG_DATETIME);
                        imageList.add(new ImageData(imagePath, dateTaken, null, null));
                    }
                }
            }
        } catch (IOException e) {
            Log.e("MainActivity", "Error loading images: " + e.getMessage());
        }

        Log.d("MainActivity", "Loaded " + imageList.size() + " images.");
        return imageList;
    }

    private void displayImagesInGrid(List<ImageData> imageList) {
        List<String> imagePaths = new ArrayList<>();
        for (ImageData imageData : imageList) {
            imagePaths.add(imageData.getImagePath());
        }

        ImageAdapter adapter = new ImageAdapter(imagePaths, this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();  // Notify the adapter of changes
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setHasFixedSize(true);
    }
    @Override
    protected void onResume() {
        super.onResume();
        allImages = loadImagesFromDevice();  // Reload images from the device
        displayImagesInGrid(allImages);      // Refresh the RecyclerView
    }

    // Image Filtering by Query
    private List<ImageData> filterImagesByQuery(List<ImageData> images, String query) {
        List<ImageData> filteredImages = new ArrayList<>();
        SimpleDateFormat exifDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());

        query = query.replace("/", "-");

        for (ImageData image : images) {
            String dateTaken = image.getDateTaken();
            if (dateTaken == null) continue;

            try {
                Date exifDate = exifDateFormat.parse(dateTaken);
                if (query.equals(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(exifDate))) {
                    filteredImages.add(image);
                }
            } catch (ParseException e) {
                Log.e("MainActivity", "Error parsing date: " + e.getMessage());
            }
        }
        return filteredImages;
    }
}
