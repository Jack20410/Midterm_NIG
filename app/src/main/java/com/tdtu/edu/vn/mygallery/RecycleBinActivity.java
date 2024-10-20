package com.tdtu.edu.vn.mygallery;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;


public class RecycleBinActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<String> deletedImagePaths = new ArrayList<>();
    private ImageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_bin);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewRecycleBin);

        // Set LayoutManager and Adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadDeletedImages(); // Load the deleted images and set the adapter
    }

    private void loadDeletedImages() {
        // Retrieve the deleted images (replace with actual logic)
        deletedImagePaths = getDeletedImages();

        // Set the adapter with the deleted images
        adapter = new ImageAdapter(deletedImagePaths, this);
        recyclerView.setAdapter(adapter); // Set the adapter

        Log.d("RecycleBinActivity", "Loaded " + deletedImagePaths.size() + " deleted images.");
    }

    private List<String> getDeletedImages() {
        SharedPreferences sharedPreferences = getSharedPreferences("RecycleBin", MODE_PRIVATE);
        String paths = sharedPreferences.getString("deletedImages", "");

        List<String> imagePaths = new ArrayList<>();
        if (!paths.isEmpty()) {
            String[] pathArray = paths.split(";");
            for (String path : pathArray) {
                imagePaths.add(path);
            }
        }
        return imagePaths;
    }

}
