package com.tdtu.edu.vn.mygallery.Album;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tdtu.edu.vn.mygallery.PhotoLocationActivity;
import com.tdtu.edu.vn.mygallery.R;
import com.tdtu.edu.vn.mygallery.Utilities.AppDatabase;

import java.util.List;
import java.util.concurrent.Executors;

public class OfflineAlbumActivity extends AppCompatActivity {

    private AppDatabase db;
    private ListView albumListView;
    private EditText albumNameInput;
    private OfflineAlbumListAdapter adapter;
    private GestureDetector gestureDetector;
    private BottomNavigationView bottomNavigationView;

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity_offline_album);

        // Check permissions
        if (!checkPermissions()) {
            requestPermissions();
        }

        initUI();

        new Handler().postDelayed(() -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                db = AppDatabase.getInstance(OfflineAlbumActivity.this);
                loadAlbums();  // Load albums once the DB is ready
            });
        }, 100);
    }

    private boolean checkPermissions() {
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        return storagePermission == PackageManager.PERMISSION_GRANTED &&
                locationPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Add this line

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions denied! App may not work correctly.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initUI() {
        LinearLayout parentLayout = findViewById(R.id.linearLayout);
        parentLayout.requestFocus();
        parentLayout.setFocusableInTouchMode(true);
        parentLayout.clearFocus();

        albumNameInput = findViewById(R.id.albumName);
        albumListView = findViewById(R.id.albumListView);

        adapter = new OfflineAlbumListAdapter(this, List.of(), db);
        albumListView.setAdapter(adapter);

        Button createAlbumButton = findViewById(R.id.createAlbumButton);
        createAlbumButton.setOnClickListener(v -> {
            String albumName = albumNameInput.getText().toString();
            if (!albumName.isEmpty()) {
                createAlbum(albumName);
            } else {
                Toast.makeText(this, "Please enter an album name", Toast.LENGTH_SHORT).show();
            }
        });

        albumListView.setOnItemClickListener((parent, view, position, id) -> {
            OfflineAlbum selectedAlbum = adapter.getAlbumAtPosition(position);
            if (selectedAlbum != null) {
                if (selectedAlbum.hasLocation()) {
                    // Navigate to PhotoLocationActivity if the album has location data
                    Intent mapIntent = new Intent(this, PhotoLocationActivity.class);
                    mapIntent.putExtra("latitude", selectedAlbum.getLatitude());
                    mapIntent.putExtra("longitude", selectedAlbum.getLongitude());
                    startActivity(mapIntent);
                } else {
                    // Navigate to album inspection activity
                    Intent intent = new Intent(OfflineAlbumActivity.this, OfflineAlbumInspect.class);
                    intent.putExtra("albumId", selectedAlbum.id);
                    startActivity(intent);
                }
            } else {
                Toast.makeText(this, "Invalid album data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createAlbum(String albumName) {
        Executors.newSingleThreadExecutor().execute(() -> {
            OfflineAlbum album = new OfflineAlbum(albumName);
            long newRowId = db.offlineAlbumDao().insert(album);
            runOnUiThread(() -> {
                if (newRowId != -1) {
                    Toast.makeText(this, "Album created", Toast.LENGTH_SHORT).show();
                    loadAlbums(); // Reload albums
                } else {
                    Toast.makeText(this, "Error creating album", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadAlbums() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<OfflineAlbum> albums = db.offlineAlbumDao().getAllAlbums();
            runOnUiThread(() -> {
                adapter = new OfflineAlbumListAdapter(this, albums, db);
                albumListView.setAdapter(adapter);
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();  // Refresh the adapter
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(event); // Handle gesture detection at the activity level
        }
        return super.dispatchTouchEvent(event); // Pass to super for default behavior
    }
}
