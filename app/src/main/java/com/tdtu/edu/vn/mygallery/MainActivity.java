package com.tdtu.edu.vn.mygallery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Constants
    private static final int PERMISSION_REQUEST_CODE = 100;

    // UI Elements and Variables
    private RecyclerView recyclerView;
    private List<ImageData> allImages;
    private BottomNavigationView bottomNavigationView;
    private float startX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUI();
        setupBottomNavigationView();
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

        // Initialize BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void setupBottomNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_main:
                        // Already in MainActivity
                        return true;
                    case R.id.navigation_offline_album:
                        startActivity(new Intent(MainActivity.this, OfflineAlbumActivity.class));
                        return true;
                    case R.id.navigation_favorite:
                        startActivity(new Intent(MainActivity.this, FavoriteActivity.class));
                        return true;
                    case R.id.navigation_login:
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        return true;
                    case R.id.navigation_search:
                        startActivity(new Intent(MainActivity.this, SearchActivity.class));
                        return true;
                    default:
                        return false;
                }
            }
        });

        // Highlight the current icon
        bottomNavigationView.setSelectedItemId(R.id.navigation_main);
    }

    // Permissions Handling
    private void checkPermissionsAndLoadImages() {
        allImages = new ArrayList<>(); // Initialize the list to prevent null pointer exceptions

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
            Log.d("MainActivity", "All Images: " + allImages.toString());
        } else {
            Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
        }
    }

    // Image Loading and Display
    private List<ImageData> loadImagesFromDevice() {
        List<ImageData> imageList = new ArrayList<>(); // Always initialize the list
        String[] projection = {MediaStore.Images.Media.DATA};

        try (Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")) {

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String imagePath = cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                    if (new File(imagePath).exists()) {
                        imageList.add(new ImageData(imagePath, null, null, null));
                    }
                }
            }
        } catch (Exception e) {
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
        adapter.notifyDataSetChanged();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setHasFixedSize(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        allImages = loadImagesFromDevice();  // Reload images from the device
        displayImagesInGrid(allImages);      // Refresh the RecyclerView
    }


}
