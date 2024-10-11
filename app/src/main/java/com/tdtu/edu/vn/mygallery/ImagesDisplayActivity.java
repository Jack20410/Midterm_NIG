package com.tdtu.edu.vn.mygallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import android.widget.ImageView;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.EditText;
import java.util.HashMap;

public class ImagesDisplayActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;
    private static final int CAMERA_CAPTURE_CODE = 102;
    private static final int STORAGE_PICK_CODE = 103;
    private ImageView imageView;
    private String albumId;
    private Uri imageUri;
    private RecyclerView imagesRecyclerView;
    private Button cameraButton, storageButton;
    private List<String> imageUrls = new ArrayList<>();  // List to store image URLs
    private ImagesAdapter imagesAdapter;  // Adapter to display images

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images_display);
        RecyclerView imagesRecyclerView = findViewById(R.id.imagesRecyclerView); // Ensure the ID matches
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // Setting layout manager
        // Retrieve album ID passed from MainActivity
        albumId = getIntent().getStringExtra("albumId");
        cameraButton = findViewById(R.id.cameraButton);
        storageButton = findViewById(R.id.storageButton);
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        imagesAdapter = new ImagesAdapter(imageUrls, ImagesDisplayActivity.this);
        imagesRecyclerView.setAdapter(imagesAdapter);
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView); // Reference to RecyclerView
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set the layout manager
        imagesRecyclerView.setAdapter(imagesAdapter);

        cameraButton.setOnClickListener(v -> {
            if (!checkCameraPermission()) {
                requestCameraPermission();
            } else {
                openCamera();
            }
        });

        storageButton.setOnClickListener(v -> {
            if (!checkStoragePermission()) {
                requestStoragePermission();
            } else {
                openGallery();
            }
        });

        if (albumId != null) {

            loadImagesFromFirebase();
        } else {
            Toast.makeText(this, "Album ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_CAPTURE_CODE);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, STORAGE_PICK_CODE);
    }

    private void uploadImageToFirebase(Uri imageUri, String tag) {
        if (imageUri != null) {
            // Create a reference to Firebase Storage
            StorageReference fileRef = FirebaseStorage.getInstance().getReference("images/" + System.currentTimeMillis() + ".jpg");

            // Upload the image
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();  // This is the URL of the uploaded image

                            // Create a HashMap to store image data (URL and tags)
                            HashMap<String, Object> imageData = new HashMap<>();
                            imageData.put("url", imageUrl);
                            imageData.put("tag", tag);  // Add the tag or keyword

                            // Save the image data to Firebase Realtime Database
                            DatabaseReference imagesRef = FirebaseDatabase.getInstance("https://midterm-d06db-default-rtdb.asia-southeast1.firebasedatabase.app")
                                    .getReference("users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child("albums")
                                    .child(albumId)
                                    .child("images");

                            imagesRef.push().setValue(imageData).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ImagesDisplayActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ImagesDisplayActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(ImagesDisplayActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
        }
    }
    private void loadImagesFromFirebase() {
        DatabaseReference imagesRef = FirebaseDatabase.getInstance("https://midterm-d06db-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("albums")
                .child(albumId)
                .child("images");

        imagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imageUrls.clear(); // Clear the list to avoid duplicates
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Retrieve each image data as a HashMap
                    HashMap<String, Object> imageData = (HashMap<String, Object>) snapshot.getValue();
                    if (imageData != null) {
                        // Assuming the image URL is stored in the 'url' field in the HashMap
                        String imageUrl = (String) imageData.get("url");
                        if (imageUrl != null) {
                            imageUrls.add(imageUrl); // Add the URL to the list
                            String imageId = snapshot.getKey(); // Get the ID (key) of the image
                            Log.d("ImagesDisplayActivity", "Image ID: " + imageId + ", URL: " + imageUrl); // Log the ID and URL
                        }
                    }
                }
                imagesAdapter.notifyDataSetChanged(); // Update the RecyclerView with the new images
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ImagesDisplayActivity.this, "Failed to load images", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void promptForTagAndUpload(Uri imageUri) {
        // Create an AlertDialog to prompt the user for a tag
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a tag for this image");

        // Set up the input
        final EditText input = new EditText(this);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Upload", (dialog, which) -> {
            String tag = input.getText().toString().trim();
            if (!tag.isEmpty()) {
                // Call uploadImageToFirebase with the imageUri and the user-defined tag
                uploadImageToFirebase(imageUri, tag);
            } else {
                Toast.makeText(ImagesDisplayActivity.this, "Tag cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_CAPTURE_CODE && data != null) {
                // Handle the image captured from the camera
                imageUri = data.getData();
                promptForTagAndUpload(imageUri); // Prompt for tag before uploading
            } else if (requestCode == STORAGE_PICK_CODE && data != null) {
                // Handle the image picked from the gallery
                imageUri = data.getData();
                promptForTagAndUpload(imageUri); // Prompt for tag before uploading
            }
        }
    }


}
