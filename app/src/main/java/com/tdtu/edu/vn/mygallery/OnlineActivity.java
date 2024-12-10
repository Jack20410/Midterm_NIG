package com.tdtu.edu.vn.mygallery;

import android.content.Intent;
import android.os.Bundle;
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
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);

        // Initialize Firebase and views
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        welcomeTextView = findViewById(R.id.welcomeTextView);
        SearchView searchView = findViewById(R.id.searchView);

        // Get the username from the Intent
        String username = getIntent().getStringExtra("username");
        if (username != null) {
            String welcomeMessage = "Welcome, " + username + "!";
            welcomeTextView.setText(welcomeMessage);
            Toast.makeText(this, welcomeMessage, Toast.LENGTH_SHORT).show();
        }

        // SearchView query hint behavior
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                searchView.setQueryHint("");
            } else {
                searchView.setQueryHint("Search images by tags");
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
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

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginFragment.class));
            finish();
        });

        // Initialize FirebaseDatabase and RecyclerView
        database = FirebaseDatabase.getInstance("https://midtermnig-default-rtdb.firebaseio.com/");
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        albumAdapter = new AlbumAdapter(albumList, new AlbumAdapter.OnAlbumActionListener() {
            @Override
            public void onRenameAlbum(Album album) {
                showRenameDialog(album);
            }

            @Override
            public void onDeleteAlbum(Album album) {
                deleteAlbumFromFirebase(album.getId());
            }
        });
        recyclerView.setAdapter(albumAdapter);

        Button fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showCreateAlbumDialog());

        loadAlbumsFromFirebase();
    }

    private void updateRecyclerViewWithImages(List<String> imageUrls) {
        ImagesAdapter imagesAdapter = new ImagesAdapter(imageUrls, this);
        recyclerView.setAdapter(imagesAdapter);
        imagesAdapter.notifyDataSetChanged();
    }

    private void searchImagesByTag(String tag) {
        DatabaseReference imagesRef = database.getReference("users")
                .child(mAuth.getCurrentUser().getUid())
                .child("albums");

        imagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> filteredImages = new ArrayList<>();
                for (DataSnapshot albumSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot imageSnapshot : albumSnapshot.child("images").getChildren()) {
                        HashMap<String, Object> imageData = (HashMap<String, Object>) imageSnapshot.getValue();
                        if (imageData != null) {
                            String imageTag = (String) imageData.get("tag");
                            String imageUrl = (String) imageData.get("url");
                            if (imageTag != null && imageTag.contains(tag)) {
                                filteredImages.add(imageUrl);
                            }
                        }
                    }
                }
                updateRecyclerViewWithImages(filteredImages);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(OnlineActivity.this, "Failed to search images", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRenameDialog(Album album) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Album");

        final EditText input = new EditText(this);
        input.setText(album.getAlbumName());
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newAlbumName = input.getText().toString();
            if (!newAlbumName.isEmpty()) {
                renameAlbumInFirebase(album.getId(), newAlbumName);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void deleteAlbumFromFirebase(String albumId) {
        DatabaseReference albumRef = database.getReference("users")
                .child(mAuth.getCurrentUser().getUid())
                .child("albums").child(albumId);

        albumRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Album deleted successfully", Toast.LENGTH_SHORT).show();
                loadAlbumsFromFirebase();
            } else {
                Toast.makeText(this, "Failed to delete album", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renameAlbumInFirebase(String albumId, String newAlbumName) {
        DatabaseReference albumRef = database.getReference("users")
                .child(mAuth.getCurrentUser().getUid())
                .child("albums").child(albumId);

        albumRef.child("albumName").setValue(newAlbumName).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Album renamed successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to rename album", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateAlbumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Album");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String albumName = input.getText().toString();
            if (!albumName.isEmpty()) {
                albumList.add(new Album(albumName));
                albumAdapter.notifyDataSetChanged();
                saveAlbumToFirebase(albumName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadAlbumsFromFirebase() {
        myRef = database.getReference("users")
                .child(mAuth.getCurrentUser().getUid())
                .child("albums");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                albumList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Album album = snapshot.getValue(Album.class);
                    if (album != null) {
                        album.setId(snapshot.getKey());
                        albumList.add(album);
                    }
                }
                albumAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(OnlineActivity.this, "Failed to load albums", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAlbumToFirebase(String albumName) {
        DatabaseReference albumsRef = database.getReference("users")
                .child(mAuth.getCurrentUser().getUid())
                .child("albums");
        String albumId = albumsRef.push().getKey();
        if (albumId != null) {
            Album album = new Album(albumId, albumName);
            albumsRef.child(albumId).setValue(album).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Album created successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to create album", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
