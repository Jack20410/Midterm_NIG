package com.tdtu.edu.vn.mygallery;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.media.ExifInterface;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private boolean isGridView = true;
    private RecyclerView recyclerView;
    private GestureDetector gestureDetector;
    private List<ImageData> allImages;  // Declare the list for all images

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        Button searchButton = findViewById(R.id.searchButton);
        EditText searchInput = findViewById(R.id.searchInput);
        ConstraintLayout parentLayout = findViewById(R.id.constraintLayout);

        // Set initial focus to parent layout to avoid auto-focus on search bar
        parentLayout.requestFocus();
        parentLayout.setFocusableInTouchMode(true);
        parentLayout.clearFocus();

        // Initialize gesture detector
        gestureDetector = new GestureDetector(this, new SwipeGestureDetector());

        // Set swipe gesture on RecyclerView
        recyclerView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // Login button click event
        ImageButton recycleBinButton = findViewById(R.id.recycleBinButton);
        recycleBinButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RecycleBinActivity.class)));

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));

        // Toggle Grid/List view button
        Button toggleViewButton = findViewById(R.id.toggleViewButton);
        toggleViewButton.setOnClickListener(v -> {
            isGridView = !isGridView;
            if (isGridView) {
                recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));
            } else {
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            }
        });

        // Check and request storage permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            allImages = loadImagesFromDevice();  // Load images if permission is granted
            displayImagesInGrid(allImages);
        }

        // Search button click listener
        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                List<ImageData> filteredImages = filterImagesByQuery(allImages, query);
                displayImagesInGrid(filteredImages);
            } else {
                displayImagesInGrid(allImages); // Show all images if query is empty
            }
        });

    }

    // Gesture detector class for handling swipe events
    private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;

            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX < 0) {
                    // Swipe left detected
                    startActivity(new Intent(MainActivity.this, OfflineAlbumActivity.class));
                }
                return true;
            }
            return false;
        }
    }

    private List<ImageData> filterImagesByQuery(List<ImageData> images, String query) {
        List<ImageData> filteredImages = new ArrayList<>();

        // Define date formats for parsing EXIF date
        SimpleDateFormat exifDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat dayMonthYearFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MM-yyyy", Locale.getDefault());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

        // Normalize the query by replacing "/" with "-" for easier matching
        query = query.replace("/", "-");

        for (ImageData image : images) {
            String dateTaken = image.getDateTaken();
            if (dateTaken == null) continue; // Skip if no date is available

            try {
                // Parse the EXIF date into a Date object
                Date exifDate = exifDateFormat.parse(dateTaken);

                // Handle different query formats
                if (query.matches("\\d{2}-\\d{2}-\\d{4}")) { // DD-MM-YYYY
                    Date userDate = dayMonthYearFormat.parse(query);
                    if (dayMonthYearFormat.format(exifDate).equals(dayMonthYearFormat.format(userDate))) {
                        filteredImages.add(image);
                    }
                } else if (query.matches("\\d{2}-\\d{4}")) { // MM-YYYY
                    Date userDate = monthYearFormat.parse(query);
                    if (monthYearFormat.format(exifDate).equals(monthYearFormat.format(userDate))) {
                        filteredImages.add(image);
                    }
                } else if (query.matches("\\d{4}")) { // YYYY
                    Date userDate = yearFormat.parse(query);
                    if (yearFormat.format(exifDate).equals(yearFormat.format(userDate))) {
                        filteredImages.add(image);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace(); // Handle parsing exceptions gracefully
            }
        }
        return filteredImages;
    }


    private List<ImageData> loadImagesFromDevice() {
        List<ImageData> imageList = new ArrayList<>();
        String[] projection = {MediaStore.Images.Media.DATA};

        try (Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")) {

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                    if (new File(imagePath).exists()) {
                        ExifInterface exif = new ExifInterface(imagePath);
                        String dateTaken = exif.getAttribute(ExifInterface.TAG_DATETIME);
                        String latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                        String longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

                        imageList.add(new ImageData(imagePath, dateTaken, latitude, longitude));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("MainActivity", "Number of images loaded: " + imageList.size());
        return imageList;
    }

    private void displayImagesInGrid(List<ImageData> imageList) {
        List<String> imagePaths = new ArrayList<>();
        for (ImageData imageData : imageList) {
            imagePaths.add(imageData.getImagePath());
        }

        ImageAdapter adapter = new ImageAdapter(imagePaths, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.getRecycledViewPool().clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            allImages = loadImagesFromDevice();
            displayImagesInGrid(allImages);
        } else {
            Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
        }
    }
}
