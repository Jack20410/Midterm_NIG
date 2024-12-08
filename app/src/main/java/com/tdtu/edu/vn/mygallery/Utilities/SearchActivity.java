package com.tdtu.edu.vn.mygallery.Utilities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.tdtu.edu.vn.mygallery.Album.OfflineAlbumActivity;
import com.tdtu.edu.vn.mygallery.Favorite.FavoriteActivity;
import com.tdtu.edu.vn.mygallery.Image.ImageAdapter;
import com.tdtu.edu.vn.mygallery.Image.ImageData;
import com.tdtu.edu.vn.mygallery.Image.ImageTag;
import com.tdtu.edu.vn.mygallery.Image.ImageTagDatabase;
import com.tdtu.edu.vn.mygallery.MainActivity;
import com.tdtu.edu.vn.mygallery.R;

import androidx.exifinterface.media.ExifInterface;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.os.Build;
import android.Manifest;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText searchInput;
    private Button searchButton;
    private Button searchDateButton;
    private List<ImageData> allImages; // List for images fetched from the database
    private BottomNavigationView bottomNavigationView;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private ImageTagDatabase db; // Room Database instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity_search);

        db = ImageTagDatabase.getInstance(this); // Initialize the database
        initializeUI();

        // Check permissions and load images
        checkPermissionsAndLoadImages();

        // Search by tag/keyword
        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (query.isEmpty()) {
                displayImagesInGrid(allImages);
            } else {
                searchImagesByTag(query);
            }
        });

        // Search by date
        searchDateButton.setOnClickListener(v -> showDatePickerDialog());
    }

    private void initializeUI() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // Grid layout with 3 columns
        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);
        searchDateButton = findViewById(R.id.searchDateButton);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    // Format the selected date as dd-MM-yyyy
                    String selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d", dayOfMonth, month + 1, year);

                    // Filter images based on the selected date
                    List<ImageData> filteredImages = filterImagesByDate(allImages, selectedDate);

                    // Display the filtered images
                    displayImagesInGrid(filteredImages);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

//    private void setupBottomNavigationView() {
//        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
//            switch (item.getItemId()) {
//                case R.id.navigation_main:
//                    startActivity(new Intent(SearchActivity.this, MainActivity.class));
//                    return true;
//                case R.id.navigation_offline_album:
//                    startActivity(new Intent(SearchActivity.this, OfflineAlbumActivity.class));
//                    return true;
//                case R.id.navigation_favorite:
//                    startActivity(new Intent(SearchActivity.this, FavoriteActivity.class));
//                    return true;
//                case R.id.navigation_login:
//                    startActivity(new Intent(SearchActivity.this, LoginActivity.class));
//                    return true;
//                case R.id.navigation_search:
//                    return true; // Already in search activity
//                default:
//                    return false;
//            }
//        });
//        bottomNavigationView.setSelectedItemId(R.id.navigation_search);
//    }

    private void checkPermissionsAndLoadImages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            } else {
                loadImagesFromDevice();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                loadImagesFromDevice();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImagesFromDevice();
            } else {
                Toast.makeText(this, "Permission denied! Cannot load images.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadImagesFromDevice() {
        allImages = new ArrayList<>();
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
                        allImages.add(new ImageData(imagePath, dateTaken, null));
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SearchActivity", "Error loading images: " + e.getMessage());
        }
    }

    private void searchImagesByTag(String query) {
        new Thread(() -> {
            List<ImageTag> taggedImages = db.imageTagDao().searchTags(query);
            List<ImageData> filteredImages = new ArrayList<>();

            for (ImageTag tag : taggedImages) {
                File imageFile = new File(tag.imagePath);
                if (imageFile.exists()) {
                    filteredImages.add(new ImageData(tag.imagePath, null, tag.tag));
                } else {
                    Log.e("SearchActivity", "Image file not found: " + tag.imagePath);
                }
            }

            runOnUiThread(() -> {
                if (!filteredImages.isEmpty()) {
                    // Display Toast when tags are found
                    Toast.makeText(SearchActivity.this, "Found " + filteredImages.size() + " image(s) with the tag.", Toast.LENGTH_SHORT).show();
                } else {
                    // Display Toast when no tags are found
                    Toast.makeText(SearchActivity.this, "No images found with that tag.", Toast.LENGTH_SHORT).show();
                }

                // Display the filtered images in the grid
                Log.d("SearchActivity", "Filtered images: " + filteredImages);
                displayImagesInGrid(filteredImages);
            });
        }).start();
    }

    private List<ImageData> filterImagesByDate(List<ImageData> images, String queryDate) {
        List<ImageData> filteredImages = new ArrayList<>();
        SimpleDateFormat exifDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());

        for (ImageData image : images) {
            String dateTaken = image.getDateTaken();
            if (dateTaken == null) continue;

            try {
                Date exifDate = exifDateFormat.parse(dateTaken);
                String formattedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(exifDate);
                if (queryDate.equals(formattedDate)) {
                    filteredImages.add(image);
                }
            } catch (ParseException e) {
                Log.e("SearchActivity", "Error parsing date: " + e.getMessage());
            }
        }
        return filteredImages;
    }

    private void displayImagesInGrid(List<ImageData> imageList) {
        List<String> imagePaths = new ArrayList<>();
        for (ImageData imageData : imageList) {
            imagePaths.add(imageData.getImagePath());
        }

        if (imagePaths.isEmpty()) {
            Log.e("SearchActivity", "No valid images to display in the grid.");
        } else {
            Log.d("SearchActivity", "Displaying images in grid: " + imagePaths);
        }

        ImageAdapter adapter = new ImageAdapter(imagePaths, this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

}

