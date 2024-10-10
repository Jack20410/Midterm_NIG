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

import java.util.List;
import java.util.concurrent.Executors;

public class OfflineAlbumActivity extends AppCompatActivity {

    private AppDatabase db;
    private ListView albumListView;
    private EditText albumNameInput;
    private OfflineAlbumListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_album);

        db = AppDatabase.getInstance(this);
        albumNameInput = findViewById(R.id.albumName);
        albumListView = findViewById(R.id.albumListView);

        Button createAlbumButton = findViewById(R.id.createAlbumButton);
        createAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String albumName = albumNameInput.getText().toString();
                if (!albumName.isEmpty()) {
                    createAlbum(albumName);
                } else {
                    Toast.makeText(OfflineAlbumActivity.this, "Please enter an album name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        albumListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Fetch the selected album using adapter.getItem(position)
                OfflineAlbum selectedAlbum = adapter.getAlbumAtPosition(position);// No need to cast
                Intent intent = new Intent(OfflineAlbumActivity.this, OfflineAlbumInspect.class);
                intent.putExtra("albumId", selectedAlbum.id);  // Pass the album ID
                startActivity(intent);
            }
        });

        // Load albums when activity starts
        loadAlbums();
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

