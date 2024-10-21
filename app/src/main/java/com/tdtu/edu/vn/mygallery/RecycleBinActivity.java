package com.tdtu.edu.vn.mygallery;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class RecycleBinActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecycleBinImageAdapter adapter;
    private List<String> deletedImagePaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_bin);

        recyclerView = findViewById(R.id.recyclerViewRecycleBin);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter once to avoid multiple instances
        adapter = new RecycleBinImageAdapter(deletedImagePaths, this);
        recyclerView.setAdapter(adapter);

        loadDeletedImages(); // Load images on activity creation
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDeletedImages(); // Reload to ensure updates are reflected
    }

    private void loadDeletedImages() {
        deletedImagePaths.clear(); // Clear list to avoid duplicates

        SharedPreferences sharedPreferences = getSharedPreferences("RecycleBin", MODE_PRIVATE);
        String paths = sharedPreferences.getString("deletedImages", "");

        if (!paths.isEmpty()) {
            for (String path : paths.split(";")) {
                if (!path.trim().isEmpty() && new File(path).exists()) {
                    deletedImagePaths.add(path);
                }
            }
        }

        Log.d("RecycleBinActivity", "Deleted images loaded: " + deletedImagePaths.size());

        adapter.notifyDataSetChanged(); // Ensure RecyclerView updates correctly
    }

}
