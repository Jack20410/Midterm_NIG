package com.tdtu.edu.vn.mygallery.Album;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tdtu.edu.vn.mygallery.R;
import com.tdtu.edu.vn.mygallery.Utilities.AppDatabase;

import java.util.List;
import java.util.concurrent.Executors;

public class OfflineAlbumActivity extends AppCompatActivity {

    private AppDatabase db;
    private ListView albumListView;
    private EditText albumNameInput;
    private OfflineAlbumListAdapter adapter;

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

        return storagePermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
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
        albumNameInput = findViewById(R.id.albumName);
        albumListView = findViewById(R.id.albumListView);

        adapter = new OfflineAlbumListAdapter(this, List.of(), db);
        albumListView.setAdapter(adapter);
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
}
