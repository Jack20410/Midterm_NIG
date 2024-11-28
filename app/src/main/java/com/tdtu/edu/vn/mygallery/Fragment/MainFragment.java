package com.tdtu.edu.vn.mygallery.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tdtu.edu.vn.mygallery.Image.ImageAdapter;
import com.tdtu.edu.vn.mygallery.Image.ImageData;
import com.tdtu.edu.vn.mygallery.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;
    private ScaleGestureDetector scaleGestureDetector;
    private List<ImageData> allImages;
    private int currentLayer = 2; // Default grid layer

    @Nullable
    @Override
    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_main, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        gridLayoutManager = new GridLayoutManager(requireContext(), currentLayer);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Initialize ScaleGestureDetector for pinch gestures
        scaleGestureDetector = new ScaleGestureDetector(requireContext(),
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(@NonNull ScaleGestureDetector detector) {
                        float scaleFactor = detector.getScaleFactor();
                        Log.d("Gesture", "Scale Factor: " + scaleFactor);
                        if (scaleFactor > 1.2f) {
                            handleZoomIn();
                        } else if (scaleFactor < 1.2f) {
                            handleZoomOut();
                        }
                        return true;
                    }
                });

        // Attach touch listener to RecyclerView
        recyclerView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            return false; // Allow normal RecyclerView interactions
        });

        checkPermissionsAndLoadImages();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshImageGrid();
    }

    private void refreshImageGrid() {
        if (allImages == null) {
            allImages = loadImagesFromDevice();
        }
        displayImagesInGrid(allImages);
    }

    private void checkPermissionsAndLoadImages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
            } else {
                allImages = loadImagesFromDevice();
                displayImagesInGrid(allImages);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                allImages = loadImagesFromDevice();
                displayImagesInGrid(allImages);
            }
        }
    }

    private List<ImageData> loadImagesFromDevice() {
        List<ImageData> imageList = new ArrayList<>();
        String[] projection = {MediaStore.Images.Media.DATA};

        try (Cursor cursor = requireContext().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)) {

            if (cursor != null) {
                Set<String> recycleBinPaths = getRecycleBinImageNames();

                while (cursor.moveToNext()) {
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    File imageFile = new File(imagePath);

                    if (!imageFile.exists() || recycleBinPaths.contains(imageFile.getName())) {
                        continue;
                    }
                    imageList.add(new ImageData(imagePath, null, null));
                }
            }
        } catch (Exception e) {
            Log.e("LoadImages", "Error loading images: " + e.getMessage(), e);
        }
        return imageList;
    }

    private Set<String> getRecycleBinImageNames() {
        Set<String> recycleBinNames = new HashSet<>();
        File recycleBinFolder = new File(requireContext().getFilesDir(), "RecycleBin");

        if (recycleBinFolder.exists() && recycleBinFolder.isDirectory()) {
            File[] files = recycleBinFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    recycleBinNames.add(file.getName());
                }
            }
        }
        return recycleBinNames;
    }

    private void displayImagesInGrid(List<ImageData> imageList) {
        List<String> imagePaths = new ArrayList<>();
        for (ImageData imageData : imageList) {
            imagePaths.add(imageData.getImagePath());
        }
        ImageAdapter adapter = new ImageAdapter(imagePaths, requireContext());
        recyclerView.setAdapter(adapter);
    }

    private void handleZoomIn() {
        if (currentLayer > 1) {
            currentLayer--;
            updateGridLayout();
        }
    }

    private void handleZoomOut() {
        if (currentLayer < 3) {
            currentLayer++;
            updateGridLayout();
        }
    }

    private void updateGridLayout() {
        Log.d("GridUpdate", "Updating to layer: " + currentLayer);
        gridLayoutManager.setSpanCount(currentLayer);
        recyclerView.setLayoutManager(gridLayoutManager);
        if (recyclerView.getAdapter() != null) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }
}
