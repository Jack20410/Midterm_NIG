package com.tdtu.edu.vn.mygallery.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tdtu.edu.vn.mygallery.R;
import com.tdtu.edu.vn.mygallery.Utilities.RecycleBinActivity;
import com.tdtu.edu.vn.mygallery.Favorite.FavoriteImagesAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment {

    private List<String> favoriteImages;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_favorite, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewFavorites);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3)); // 3 columns grid layout

        favoriteImages = new ArrayList<>();
        loadFavoriteImages(); // Load images from the Favorites folder

        FavoriteImagesAdapter adapter = new FavoriteImagesAdapter(favoriteImages, requireContext());
        recyclerView.setAdapter(adapter);

        setupRecycleBinButton(view); // Set up the Recycle Bin button

        return view;
    }

    private void setupRecycleBinButton(View view) {
        ImageButton buttonRecycleBin = view.findViewById(R.id.buttonRecycleBin);
        buttonRecycleBin.setOnClickListener(v -> {
            // Navigate to the Recycle Bin activity
            startActivity(new android.content.Intent(requireContext(), RecycleBinActivity.class));
        });
    }

    private void loadFavoriteImages() {
        File favoritesFolder = new File(requireContext().getFilesDir(), "Favorites");

        if (favoritesFolder.exists() && favoritesFolder.isDirectory()) {
            File[] files = favoritesFolder.listFiles();

            if (files != null) {
                favoriteImages.clear();
                for (File file : files) {
                    if (file.isFile()) { // Ensure it's a file
                        favoriteImages.add(file.getAbsolutePath());
                        Log.d("FavoriteImages", "Loaded favorite image: " + file.getAbsolutePath());
                    }
                }
            }
        } else {
            Log.d("FavoriteImages", "Favorites folder is empty or does not exist.");
        }

        Log.d("FavoriteImages", "Total favorite images loaded: " + favoriteImages.size());
    }
}
