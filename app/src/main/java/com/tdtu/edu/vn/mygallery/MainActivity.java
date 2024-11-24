package com.tdtu.edu.vn.mygallery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.view.ScaleGestureDetector;
import androidx.exifinterface.media.ExifInterface;
import android.view.GestureDetector;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private RecyclerView recyclerView;
    private List<ImageData> allImages;
    private BottomNavigationView bottomNavigationView;
    private GridLayoutManager gridLayoutManager;

    private int currentLayer = 2; // Start at Layer 2
    private ScaleGestureDetector scaleGestureDetector;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        initializeUI();
        setupBottomNavigationView();
        checkPermissionsAndLoadImages();
        FileManager.createAppFolders(this);



        GestureRecyclerView recyclerView = findViewById(R.id.recyclerView);

// Set up ScaleGestureDetector
        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                if (scaleFactor > 1.0f) {
                    Log.d("Gesture", "Zooming in detected with scale factor: " + scaleFactor);
                    handleZoomIn();
                } else if (scaleFactor < 1.0f) {
                    Log.d("Gesture", "Pinching (zooming out) detected with scale factor: " + scaleFactor);
                    handleZoomOut();
                }
                return true;
            }
        });

// Assign the gesture detector to the custom RecyclerView
        recyclerView.setScaleGestureDetector(scaleGestureDetector);


        // Combine ScaleGestureDetector and GestureDetector

    }


    @Override
    protected void onResume() {
        super.onResume();
        refreshImageGrid();
    }
    private void refreshImageGrid() {
        allImages = loadImagesFromDevice();
        displayImagesInGrid(allImages);
    }
    public void openImageInspectActivity(String imagePath) {
        Intent intent = new Intent(this, ImageInspectActivity.class);
        intent.putExtra("IMAGE_PATH", imagePath);
        startActivityForResult(intent, 101); // Use 101 as a request code
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK) {
            if (data != null) {
                String removedImagePath = data.getStringExtra("REMOVED_IMAGE_PATH");
                if (removedImagePath != null) {
                    // Refresh the image grid to reflect the changes
                    refreshImageGrid();
                }
            }
        }
    }
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    private void initializeUI() {
        recyclerView = findViewById(R.id.recyclerView);
        ConstraintLayout parentLayout = findViewById(R.id.constraintLayout);

        parentLayout.requestFocus();
        parentLayout.setFocusableInTouchMode(true);
        parentLayout.clearFocus();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        gridLayoutManager = new GridLayoutManager(this, 2); // Default to Layer 2
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    @SuppressLint("NonConstantResourceId")
    private void setupBottomNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            return switch (item.getItemId()) {
                case R.id.navigation_main -> true;
                case R.id.navigation_offline_album -> {
                    startActivity(new Intent(MainActivity.this, OfflineAlbumActivity.class));
                    yield true;
                }
                case R.id.navigation_favorite -> {
                    startActivity(new Intent(MainActivity.this, FavoriteActivity.class));
                    yield true;
                }
                case R.id.navigation_login -> {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    yield true;
                }
                case R.id.navigation_search -> {
                    startActivity(new Intent(MainActivity.this, SearchActivity.class));
                    yield true;
                }
                default -> false;
            };
        });

        bottomNavigationView.setSelectedItemId(R.id.navigation_main);
    }

    private void checkPermissionsAndLoadImages() {
        allImages = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+ (API 33 and above)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            } else {
                allImages = loadImagesFromDevice();
                displayImagesInGrid(allImages);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10-12 (API 29-32)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                allImages = loadImagesFromDevice();
                displayImagesInGrid(allImages);
            }
        } else {
            // For Android 8-9 (API 26-28)
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                allImages = loadImagesFromDevice();
                displayImagesInGrid(allImages);
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private List<ImageData> loadImagesFromDevice() {
        List<ImageData> imageList = new ArrayList<>();
        String[] projection = {MediaStore.Images.Media.DATA};

        try (Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                null, null, null)) { // Remove sorting here to sort manually later

            if (cursor != null) {
                Set<String> recycleBinPaths = getRecycleBinImageNames();

                while (cursor.moveToNext()) {
                    String imagePath = cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    File imageFile = new File(imagePath);

                    // Skip if the file does not exist
                    if (!imageFile.exists()) {
                        Log.w("LoadImages", "File not found, skipping: " + imagePath);
                        continue;
                    }

                    // Skip if the file is in the Recycle Bin
                    if (recycleBinPaths.contains(imageFile.getName())) {
                        Log.d("FilteredImage", "Excluded image in Recycle Bin: " + imagePath);
                        continue;
                    }

                    // Add the image
                    imageList.add(new ImageData(imagePath, null, null));
                }
            }
        } catch (Exception e) {
            Log.e("LoadImages", "Error loading images: " + e.getMessage(), e);
        }

        // Sort the image list by file name
        imageList.sort((image1, image2) -> new File(image1.getImagePath()).getName()
                .compareToIgnoreCase(new File(image2.getImagePath()).getName()));

        Log.d("LoadImages", "Total images loaded: " + imageList.size());
        return imageList;
    }



    private Set<String> getRecycleBinImages() {
        Set<String> recycleBinPaths = new HashSet<>();
        File recycleBinFolder = new File(getFilesDir(), "RecycleBin");

        if (recycleBinFolder.exists() && recycleBinFolder.isDirectory()) {
            File[] files = recycleBinFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    recycleBinPaths.add(file.getAbsolutePath());
                    Log.d("RecycleBinImages", "File in Recycle Bin: " + file.getAbsolutePath());
                }
            }
        }

        Log.d("RecycleBinImages", "Total files in Recycle Bin: " + recycleBinPaths.size());
        return recycleBinPaths;
    }
    private Set<String> getRecycleBinImageNames() {
        Set<String> recycleBinNames = new HashSet<>();
        File recycleBinFolder = new File(getFilesDir(), "RecycleBin");

        if (recycleBinFolder.exists() && recycleBinFolder.isDirectory()) {
            File[] files = recycleBinFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    recycleBinNames.add(file.getName()); // Add only the filename
                    Log.d("RecycleBinImages", "File in Recycle Bin: " + file.getAbsolutePath());
                }
            }
        }

        Log.d("RecycleBinImages", "Total files in Recycle Bin: " + recycleBinNames.size());
        return recycleBinNames;
    }
    @SuppressLint("NotifyDataSetChanged")
    private void displayImagesInGrid(List<ImageData> imageList) {
        // Fetch the Recycle Bin paths
        Set<String> recycleBinPaths = getRecycleBinImages();

        // Create a filtered list of image paths
        List<String> imagePaths = new ArrayList<>();
        for (ImageData imageData : imageList) {
            String imagePath = imageData.getImagePath();

            // Add the image only if it's not in the Recycle Bin
            if (!recycleBinPaths.contains(new File(imagePath).getAbsolutePath())) {
                imagePaths.add(imagePath);
            } else {
                Log.d("FilteredImage", "Excluded image in Recycle Bin: " + imagePath);
            }
        }

        Log.d("FilteredImage", "Total images displayed: " + imagePaths.size());

        // Ensure the list is sorted alphabetically by file name
        imagePaths.sort(String::compareToIgnoreCase);

        // Set the adapter with the filtered list
        ImageAdapter adapter = new ImageAdapter(imagePaths, this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }



    /**
     * Callback method to refresh the grid when changes occur.
     */




    private void handleZoomIn() {
        if (currentLayer == 3) {
            currentLayer = 2;
            gridLayoutManager.setSpanCount(2);
            recyclerView.setLayoutManager(gridLayoutManager);

        } else if (currentLayer == 2) {
            currentLayer = 1;
            gridLayoutManager.setSpanCount(1);
            recyclerView.setLayoutManager(gridLayoutManager);

        }
    }

    private void handleZoomOut() {
        if (currentLayer == 1) {
            currentLayer = 2;
            gridLayoutManager.setSpanCount(2);
            recyclerView.setLayoutManager(gridLayoutManager);

        } else if (currentLayer == 2) {
            currentLayer = 3;
            gridLayoutManager.setSpanCount(3);
            recyclerView.setLayoutManager(gridLayoutManager);

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return scaleGestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

}
