package com.tdtu.edu.vn.mygallery;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.provider.MediaStore;
import java.util.List;
import java.util.ArrayList;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import android.util.Log;
import androidx.annotation.NonNull;
import android.Manifest;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.net.Uri;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
public class OfflineAlbumInspect extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 1;
    private long albumId;
    private AppDatabase db;
    private OfflineAlbumDao albumDao;
    private OfflineAlbum currentAlbum;
    private List<String> imageList = new ArrayList<>();
    private OfflineAlbumImageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_album_inspect);
        requestStoragePermission();
        db = AppDatabase.getInstance(this);
        if (db == null) {
            Log.e("OfflineAlbumInspect", "Database instance is null.");
            return;
        }
        albumDao = db.offlineAlbumDao();
        if (albumDao == null) {
            Log.e("OfflineAlbumInspect", "OfflineAlbumDao is null.");
            return;
        }
        albumId = getIntent().getLongExtra("albumId", -1);

        loadImages(albumId);

        Button startSlideshowButton = findViewById(R.id.startSlideshowButton);
        startSlideshowButton.setOnClickListener(v -> {
            if (!imageList.isEmpty()) {
                Log.d("OfflineAlbumInspect", "Number of images in imageList: " + imageList.size());
                Intent intent = new Intent(OfflineAlbumInspect.this, SlideshowActivity.class);
                intent.putStringArrayListExtra("imagePaths", (ArrayList<String>) imageList); // Ensure imageList is properly populated
                startActivity(intent);
            } else {
                Log.d("OfflineAlbumInspect", "imageList is empty.");
                Toast.makeText(OfflineAlbumInspect.this, "No images to display in the slideshow", Toast.LENGTH_SHORT).show();
            }
        });


        Button addPictureButton = findViewById(R.id.addPictureButton);

        addPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPictureToAlbum();
            }
        });
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        } else {
            // Permission is already granted
            loadImages(albumId); // Load images if permission is already granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Important: Call the superclass method

        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImages(albumId);  // Load images if permission is granted
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void loadImages(long albumId) {
        new Thread(() -> {
            currentAlbum = albumDao.getAlbumById(albumId);
            runOnUiThread(() -> {
                if (currentAlbum == null) {
                    Toast.makeText(OfflineAlbumInspect.this, "Album not found", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("OfflineAlbumInspect", "Loaded URIs: " + currentAlbum.imageUris);
                    displayImages();
                }
            });
        }).start();
    }


    private void addPictureToAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData(); // Get the image URI from the intent
            if (selectedImageUri != null && currentAlbum != null) {
                // Copy the image to local storage
                String localImagePath = copyImageToLocalStorage(selectedImageUri);

                // Check if the image was successfully copied
                if (localImagePath != null) {
                    // Append new image path to the album's imageUris
                    if (currentAlbum.imageUris == null || currentAlbum.imageUris.isEmpty()) {
                        currentAlbum.imageUris = localImagePath;  // If it's the first image
                    } else {
                        currentAlbum.imageUris += "," + localImagePath;  // Append with a comma delimiter
                    }

                    Log.d("OfflineAlbumInspect", "Updated imageUris: " + currentAlbum.imageUris);  // Log updated URIs

                    updateAlbum(currentAlbum);  // Update album in the database
                    displayImages();  // Refresh displayed images immediately
                } else {
                    Log.e("OfflineAlbumInspect", "Failed to copy the image to local storage.");
                    Toast.makeText(this, "Error copying the image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private String copyImageToLocalStorage(Uri imageUri) {
        String fileName = "image_" + System.currentTimeMillis() + ".jpg"; // Unique filename for each image
        File outputFile = new File(getFilesDir(), fileName); // Save in internal storage
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri);
             OutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputFile.getAbsolutePath(); // Return the local file path
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void updateAlbum(OfflineAlbum album) {
        new Thread(() -> {
            db.offlineAlbumDao().update(album); // Update the album in the database
        }).start();
    }

    private void displayImages() {
        if (currentAlbum != null && currentAlbum.imageUris != null && !currentAlbum.imageUris.isEmpty()) {
            String[] uris = currentAlbum.imageUris.split(",");
            imageList.clear();

            StringBuilder updatedUris = new StringBuilder();
            for (String uri : uris) {
                uri = uri.trim();
                File file = new File(uri);
                if (file.exists()) {
                    imageList.add(uri);
                    if (updatedUris.length() > 0) {
                        updatedUris.append(",");
                    }
                    updatedUris.append(uri);
                }
            }

            String newUris = updatedUris.toString();
            if (!newUris.equals(currentAlbum.imageUris)) {
                currentAlbum.imageUris = newUris;
                updateAlbum(currentAlbum);
            }

            // Initialize the adapter if it's not already initialized
            if (adapter == null) {
                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                adapter = new OfflineAlbumImageAdapter(imageList, this, albumDao, currentAlbum);

                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            } else {
                adapter.notifyDataSetChanged(); // Refresh the adapter with new data
            }
        } else {
            Toast.makeText(this, "No images found in this album", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadImages(albumId); // Reload the album's images to reflect changes
    }

}



