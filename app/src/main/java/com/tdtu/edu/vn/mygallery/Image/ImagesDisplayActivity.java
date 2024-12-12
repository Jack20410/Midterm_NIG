package com.tdtu.edu.vn.mygallery.Image;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tdtu.edu.vn.mygallery.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImagesDisplayActivity extends AppCompatActivity {
    private static final int STORAGE_REQUEST_CODE = 101;
    private static final int STORAGE_PICK_CODE = 103;
    private String albumId;
    private Uri imageUri;
    private RecyclerView imagesRecyclerView;
    private ImageButton storageButton;
    private List<String> imageUrls = new ArrayList<>();
    private ImagesAdapter imagesAdapter;
    private Button toggleViewButton;
    private boolean isGridView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images_display);

        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
        toggleViewButton = findViewById(R.id.toggleViewButton);
        storageButton = findViewById(R.id.storageButton);

        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        imagesAdapter = new ImagesAdapter(imageUrls, this);
        imagesRecyclerView.setAdapter(imagesAdapter);

        albumId = getIntent().getStringExtra("albumId");

        toggleViewButton.setOnClickListener(v -> {
            isGridView = !isGridView;
            switchLayoutManager();
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

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, STORAGE_PICK_CODE);
    }

    private void switchLayoutManager() {
        if (isGridView) {
            imagesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            toggleViewButton.setText("Switch to List View");
        } else {
            imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            toggleViewButton.setText("Switch to Grid View");
        }
    }

    public static void deleteImageFromFirebase(String imageUrl) {
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://midtermnig-default-rtdb.firebaseio.com/")
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("albums");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot albumSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot imageSnapshot : albumSnapshot.child("images").getChildren()) {
                        String firebaseUrl = imageSnapshot.child("url").getValue(String.class);
                        if (firebaseUrl != null && firebaseUrl.equals(imageUrl)) {
                            imageSnapshot.getRef().removeValue();
                            Log.d("Firebase", "Image deleted from database.");
                            return;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Failed to delete image: " + databaseError.getMessage());
            }
        });
    }

    private void loadImagesFromFirebase() {
        DatabaseReference imagesRef = FirebaseDatabase.getInstance("https://midtermnig-default-rtdb.firebaseio.com/")
                .getReference("users")
                .child(FirebaseAuth.getInstance().getUid())
                .child("albums")
                .child(albumId)
                .child("images");

        imagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imageUrls.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HashMap<String, Object> imageData = (HashMap<String, Object>) snapshot.getValue();
                    if (imageData != null) {
                        String imageUrl = (String) imageData.get("url");
                        if (imageUrl != null) {
                            imageUrls.add(imageUrl);
                        }
                    }
                }
                imagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ImagesDisplayActivity.this, "Failed to load images", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageToFirebase(Uri imageUri, String tag) {
        if (imageUri != null) {
            StorageReference fileRef = FirebaseStorage.getInstance()
                    .getReference("images/" + System.currentTimeMillis() + ".jpg");

            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();

                DatabaseReference imagesRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(FirebaseAuth.getInstance().getUid())
                        .child("albums")
                        .child(albumId)
                        .child("images");

                HashMap<String, Object> imageData = new HashMap<>();
                imageData.put("url", imageUrl);
                imageData.put("tag", tag);

                imagesRef.push().setValue(imageData).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
            })).addOnFailureListener(e -> Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show());
        }
    }

    private void promptForTagAndUpload(Uri imageUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a tag for this image");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Upload", (dialog, which) -> {
            String tag = input.getText().toString().trim();
            if (!tag.isEmpty()) {
                uploadImageToFirebase(imageUri, tag);
            } else {
                Toast.makeText(this, "Tag cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == STORAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            promptForTagAndUpload(imageUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
