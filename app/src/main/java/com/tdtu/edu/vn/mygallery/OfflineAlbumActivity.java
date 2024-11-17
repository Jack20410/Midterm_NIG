package com.tdtu.edu.vn.mygallery;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
        setContentView(R.layout.activity_offline_album);

        initUI();
        setupBottomNavigationView();


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

    private void setupBottomNavigationView() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_main:
                        startActivity(new Intent(OfflineAlbumActivity.this, MainActivity.class));
                        return true;
                    case R.id.navigation_offline_album:
                        // Already in OfflineAlbumActivity
                        return true;
                    case R.id.navigation_favorite:
                        startActivity(new Intent(OfflineAlbumActivity.this, FavoriteActivity.class));
                        return true;
                    case R.id.navigation_login:
                        startActivity(new Intent(OfflineAlbumActivity.this, LoginActivity.class));
                        return true;
                    case R.id.navigation_search:
                        startActivity(new Intent(OfflineAlbumActivity.this, SearchActivity.class));
                        return true;
                    default:
                        return false;
                }
            }
        });

        // Highlight the offline album icon correctly
        new Handler().post(() -> bottomNavigationView.setSelectedItemId(R.id.navigation_offline_album));
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
            gestureDetector.onTouchEvent(event);
        }
        return super.dispatchTouchEvent(event);
    }


}
