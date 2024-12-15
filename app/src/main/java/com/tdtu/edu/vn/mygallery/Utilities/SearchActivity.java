package com.tdtu.edu.vn.mygallery.Utilities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tdtu.edu.vn.mygallery.Image.ImageAdapter;
import com.tdtu.edu.vn.mygallery.Image.ImageInspectActivity;
import com.tdtu.edu.vn.mygallery.R;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText searchInput;
    private ImageButton searchButton;

    private List<String> allImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity_search);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);

        allImages = loadSampleImages(); // Replace with your actual image loading logic

        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString();
            searchImages(query);
        });

        displayImagesInGrid(allImages);
    }

    private List<String> loadSampleImages() {
        // Replace with actual logic to load images
        return new ArrayList<>();
    }

    private void searchImages(String query) {
        // Implement search logic and refresh the grid
    }

    private void displayImagesInGrid(List<String> images) {
        ImageAdapter adapter = new ImageAdapter(images, this, this::onImageClicked);
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }

    private void onImageClicked(String imagePath) {
        Intent intent = new Intent(this, ImageInspectActivity.class);
        intent.putExtra("IMAGE_PATH", imagePath);
        startActivity(intent);
    }


}
