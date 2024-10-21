package com.tdtu.edu.vn.mygallery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.List;
import java.util.concurrent.Executors;
import android.widget.LinearLayout;
import android.os.Handler;

public class OfflineAlbumActivity extends AppCompatActivity {

    private AppDatabase db;
    private ListView albumListView;
    private EditText albumNameInput;
    private OfflineAlbumListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_album);
        initUI();
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
                // Ensure you pass the listener (this) to the adapter
                adapter = new OfflineAlbumListAdapter(this,albums, db);
                albumListView.setAdapter(adapter);
            });
        });
    }
}

