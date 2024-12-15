package com.tdtu.edu.vn.mygallery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ProgressBar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tdtu.edu.vn.mygallery.Album.Album;
import com.tdtu.edu.vn.mygallery.Album.AlbumAdapter;
import com.tdtu.edu.vn.mygallery.Fragment.LoginFragment;
import com.tdtu.edu.vn.mygallery.Image.ImagesAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OnlineActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AlbumAdapter albumAdapter;
    private List<Album> albumList = new ArrayList<>();
    // Declare the FirebaseDatabase and DatabaseReference variables
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        welcomeTextView = findViewById(R.id.welcomeTextView);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform search when text is submitted
                searchImagesByTag(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadAlbumsFromFirebase();
                    recyclerView.setAdapter(albumAdapter);
                    albumAdapter.notifyDataSetChanged();
                } else {
                    searchImagesByTag(newText);
                }
                return false;
            }
        });
        // SearchView query hint behavior
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                searchView.setQueryHint("");
            } else {
                searchView.setQueryHint("Search images by tags");
            }
        });
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            // Log out the user from Firebase Authentication
            FirebaseAuth.getInstance().signOut();

            // Show a confirmation toast
            Toast.makeText(OnlineActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Redirect to the login screen (MainActivity hosting LoginFragment)
            Intent intent = new Intent(OnlineActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            startActivity(intent);

            // Finish the current activity to prevent back navigation
            finish();
        });


        // Initialize FirebaseDatabase instance
        database = FirebaseDatabase.getInstance("https://midterm-d06db-default-rtdb.asia-southeast1.firebasedatabase.app");
        // Initialize RecyclerView and AlbumAdapter
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        albumAdapter = new AlbumAdapter(albumList, this);
        recyclerView.setAdapter(albumAdapter);
        Button fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showCreateAlbumDialog());
        //Load username for Personalized welcome
        loadUserName();
        // Load albums from Firebase
        loadAlbumsFromFirebase();
        recyclerView.setAdapter(albumAdapter);
        albumAdapter.notifyDataSetChanged();
    }

    private void loadUserName() {
        String userId = mAuth.getCurrentUser().getUid();
        database.getReference("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child("username").getValue(String.class);
                if (username != null && !username.isEmpty()) {
                    welcomeTextView.setText("Welcome " + username + "!");
                    Toast.makeText(OnlineActivity.this, "Welcome " + username + "!", Toast.LENGTH_SHORT).show();
                } else {
                    welcomeTextView.setText("Welcome!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OnlineActivity.this, "Failed to load username.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecyclerViewWithImages(List<String> imageUrls) {
        ImagesAdapter imagesAdapter = new ImagesAdapter(imageUrls, this);
        recyclerView.setAdapter(imagesAdapter);
        imagesAdapter.notifyDataSetChanged();
    }

    private void searchImagesByTag(String tag) {
        DatabaseReference imagesRef = FirebaseDatabase.getInstance("https://midterm-d06db-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("albums");
        imagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> filteredImages = new ArrayList<>();

                // Loop through each album and its images
                for (DataSnapshot albumSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot imageSnapshot : albumSnapshot.child("images").getChildren()) {
                        HashMap<String, Object> imageData = (HashMap<String, Object>) imageSnapshot.getValue();
                        if (imageData != null) {
                            String imageTag = (String) imageData.get("tag");
                            String imageUrl = (String) imageData.get("url");

                            // If the tag matches the search query, add the image to the filtered list
                            if (imageTag != null && imageTag.contains(tag)) {
                                filteredImages.add(imageUrl);
                            }
                        }
                    }
                }
                // Update the RecyclerView with the filtered images
                updateRecyclerViewWithImages(filteredImages);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(OnlineActivity.this, "Failed to search images", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showRenameDialog(Album album) {
        // Create an alert dialog to rename the album
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Album");

        // Create an EditText to enter the new album name
        final EditText input = new EditText(this);
        input.setText(album.getAlbumName()); // Pre-fill with current album name
        builder.setView(input);

        // Set up the dialog buttons
        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newAlbumName = input.getText().toString();
            if (!newAlbumName.isEmpty()) {
                renameAlbumInFirebase(album.getId(), newAlbumName);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    public void renameAlbumInFirebase(String albumId, String newAlbumName) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://midterm-d06db-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference albumRef = database.getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("albums").child(albumId);

        albumRef.child("albumName").setValue(newAlbumName).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(OnlineActivity.this, "Album renamed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(OnlineActivity.this, "Failed to rename album", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void deleteAlbumFromFirebase(String albumId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://midterm-d06db-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference albumRef = database.getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("albums").child(albumId);

        albumRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(OnlineActivity.this, "Album deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(OnlineActivity.this, "Failed to delete album", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void saveAlbumToFirebase(String albumName) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://midterm-d06db-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference myRef = database.getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("albums");
        String albumId = myRef.push().getKey(); // Generate a unique ID for the album
        if (albumId != null) {
            Album newAlbum = new Album(albumId, albumName); // Create an Album object with both ID and name
            // Save the album to Firebase
            myRef.child(albumId).setValue(newAlbum).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(OnlineActivity.this, "Album saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OnlineActivity.this, "Failed to save album", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(OnlineActivity.this, "Failed to generate album ID", Toast.LENGTH_SHORT).show();
        }
    }
    private void showCreateAlbumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Album");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("Create", (dialog, which) -> {
            String albumName = input.getText().toString();
            if (!albumName.isEmpty()) {
                // Add album to list and notify adapter
                albumList.add(new Album(albumName));
                albumAdapter.notifyDataSetChanged();

                // Save album to Firebase
                saveAlbumToFirebase(albumName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    private void loadAlbumsFromFirebase() {
        // Show ProgressBar
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        DatabaseReference myRef = FirebaseDatabase.getInstance("https://midterm-d06db-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("albums");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                albumList.clear(); // Clear the list to avoid duplicates

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Album album = snapshot.getValue(Album.class);
                    if (album != null) {
                        album.setId(snapshot.getKey()); // Set the Firebase-generated ID
                        albumList.add(album);
                    }
                }

                // Notify adapter of dataset changes
                albumAdapter.notifyDataSetChanged();

                // Hide ProgressBar
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Hide ProgressBar in case of an error
                progressBar.setVisibility(View.GONE);

                // Show an error message
                Toast.makeText(OnlineActivity.this, "Failed to load albums", Toast.LENGTH_SHORT).show();
            }
        });
    }

}