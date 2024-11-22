package com.tdtu.edu.vn.mygallery;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecycleBinActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecycleBinImageAdapter adapter;
    private final List<String> deletedImagePaths = new ArrayList<>();

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
    public void reloadDeletedImages() {
        loadDeletedImages(); // Reload images from the Recycle Bin folder
    }
    private void loadDeletedImages() {
        deletedImagePaths.clear(); // Clear list to avoid duplicates

        File recycleBinFolder = new File(getFilesDir(), "RecycleBin");

        if (recycleBinFolder.exists() && recycleBinFolder.isDirectory()) {
            File[] files = recycleBinFolder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) { // Ensure it's a file and not a directory
                        deletedImagePaths.add(file.getAbsolutePath());
                        Log.d("RecycleBinActivity", "Loaded image: " + file.getAbsolutePath());
                    }
                }
            } else {
                Log.d("RecycleBinActivity", "No files found in the Recycle Bin folder.");
            }
        } else {
            Log.d("RecycleBinActivity", "Recycle Bin folder does not exist.");
        }

        Log.d("RecycleBinActivity", "Total deleted images loaded: " + deletedImagePaths.size());

        adapter.notifyDataSetChanged(); // Ensure RecyclerView updates correctly
    }
}
