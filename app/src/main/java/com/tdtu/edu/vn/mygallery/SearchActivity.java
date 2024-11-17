package com.tdtu.edu.vn.mygallery;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.exifinterface.media.ExifInterface;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText searchInput;
    private Button searchButton;
    private List<ImageData> allImages;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initializeUI();
        setupBottomNavigationView();
        loadImagesFromDevice();

        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (query.isEmpty()) {
                displayImagesInGrid(allImages);
            } else {
                List<ImageData> filteredImages = filterImagesByQuery(allImages, query);
                displayImagesInGrid(filteredImages);
            }
        });
    }

    private void initializeUI() {
        recyclerView = findViewById(R.id.recyclerView);
        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void setupBottomNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_main:
                        startActivity(new Intent(SearchActivity.this, MainActivity.class));
                        return true;
                    case R.id.navigation_offline_album:
                        startActivity(new Intent(SearchActivity.this, OfflineAlbumActivity.class));
                        return true;
                    case R.id.navigation_favorite:
                        startActivity(new Intent(SearchActivity.this, FavoriteActivity.class));
                        return true;
                    case R.id.navigation_login:
                        startActivity(new Intent(SearchActivity.this, LoginActivity.class));
                        return true;
                    case R.id.navigation_search:
                        // Already in SearchActivity
                        return true;
                    default:
                        return false;
                }
            }
        });

        // Highlight the search icon
        bottomNavigationView.setSelectedItemId(R.id.navigation_search);
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
                        allImages.add(new ImageData(imagePath, dateTaken, null, null));
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SearchActivity", "Error loading images: " + e.getMessage());
        }
    }

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

        ImageAdapter adapter = new ImageAdapter(imagePaths, this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setHasFixedSize(true);
    }
}
