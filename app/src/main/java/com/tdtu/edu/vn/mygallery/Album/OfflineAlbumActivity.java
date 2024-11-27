package com.tdtu.edu.vn.mygallery.Album;

import android.annotation.SuppressLint;
import android.content.Intent;
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

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tdtu.edu.vn.mygallery.Fragment.OfflineAlbumFragment;
import com.tdtu.edu.vn.mygallery.Utilities.AppDatabase;
import com.tdtu.edu.vn.mygallery.Favorite.FavoriteActivity;
import com.tdtu.edu.vn.mygallery.LoginActivity;
import com.tdtu.edu.vn.mygallery.MainActivity;
import com.tdtu.edu.vn.mygallery.R;
import com.tdtu.edu.vn.mygallery.Utilities.SearchActivity;

import java.util.List;
import java.util.concurrent.Executors;

public class OfflineAlbumActivity extends AppCompatActivity {

    private AppDatabase db;
    private ListView albumListView;
    private EditText albumNameInput;
    private OfflineAlbumListAdapter adapter;
    private GestureDetector gestureDetector;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity_offline_album);

        initUI();
//        setupBottomNavigationView();


        new Handler().postDelayed(() -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                db = AppDatabase.getInstance(OfflineAlbumActivity.this);
                loadAlbums();  // Load albums once the DB is ready
            });
        }, 100);
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
            Intent intent = new Intent(OfflineAlbumActivity.this, OfflineAlbumInspect.class);
            intent.putExtra("albumId", selectedAlbum.id);
            startActivity(intent);
        });
    }

    public void initUIComponents(OfflineAlbumFragment fragment, View view) {
        LinearLayout parentLayout = view.findViewById(R.id.linearLayout);
        EditText albumNameInput = view.findViewById(R.id.albumName);
        ListView albumListView = view.findViewById(R.id.albumListView);
        Button createAlbumButton = view.findViewById(R.id.createAlbumButton);

        parentLayout.requestFocus();
        parentLayout.setFocusableInTouchMode(true);

        createAlbumButton.setOnClickListener(v -> {
            String albumName = albumNameInput.getText().toString();
            if (!albumName.isEmpty()) {
                fragment.createAlbum(albumName); // Delegate album creation to the fragment
            } else {
                Toast.makeText(this, "Please enter an album name", Toast.LENGTH_SHORT).show();
            }
        });

        albumListView.setOnItemClickListener((parent, clickedView, position, id) -> {
            OfflineAlbum selectedAlbum = fragment.getAdapter().getAlbumAtPosition(position);
            if (selectedAlbum != null && selectedAlbum.id > 0) {
                Intent intent = new Intent(this, OfflineAlbumInspect.class);
                intent.putExtra("albumId", selectedAlbum.id);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Invalid album data", Toast.LENGTH_SHORT).show();
            }
        });
    }


//    @SuppressLint("NonConstantResourceId")
//    private void setupBottomNavigationView() {
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
//
//        // Ensure `bottomNavigationView` is not null
//        if (bottomNavigationView == null) {
//            Toast.makeText(this, "BottomNavigationView not found. Check your layout file.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
//            switch (item.getItemId()) {
//                case R.id.navigation_offline_album:
//                    // Already in OfflineAlbumActivity
//                    return true;
//                case R.id.navigation_main:
//                    startActivity(new Intent(OfflineAlbumActivity.this, MainActivity.class));
//                    return true;
//                case R.id.navigation_favorite:
//                    startActivity(new Intent(OfflineAlbumActivity.this, FavoriteActivity.class));
//                    return true;
//                case R.id.navigation_login:
//                    startActivity(new Intent(OfflineAlbumActivity.this, LoginActivity.class));
//                    return true;
//                case R.id.navigation_search:
//                    startActivity(new Intent(OfflineAlbumActivity.this, SearchActivity.class));
//                    return true;
//                default:
//                    return false;
//            }
//        });
//
//        // Set the selected item to "Offline Album"
//        bottomNavigationView.setSelectedItemId(R.id.navigation_offline_album);
//    }

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

        // Check if the fragment is currently active and handle touch events there
        OfflineAlbumFragment fragment = (OfflineAlbumFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            fragment.handleTouchEvent(event); // Custom method in the fragment to process touch events
        }

        return super.dispatchTouchEvent(event); // Pass to super for default behavior
    }

}
