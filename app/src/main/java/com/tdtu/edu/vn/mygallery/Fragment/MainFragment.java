package com.tdtu.edu.vn.mygallery.Fragment;

import static com.tdtu.edu.vn.mygallery.R.*;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tdtu.edu.vn.mygallery.Image.ImageAdapter;
import com.tdtu.edu.vn.mygallery.Image.ImageData;
import com.tdtu.edu.vn.mygallery.MainActivity;
import com.tdtu.edu.vn.mygallery.R;
import com.tdtu.edu.vn.mygallery.Utilities.FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private RecyclerView recyclerView;
    private List<ImageData> allImages;
    private GridLayoutManager gridLayoutManager;
    private ScaleGestureDetector scaleGestureDetector;

    private int currentLayer = 2; // Start at Layer 2

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_main, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        scaleGestureDetector = new ScaleGestureDetector(requireContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                if (scaleFactor > 1.0f) {
                    handleZoomIn();
                } else if (scaleFactor < 1.0f) {
                    handleZoomOut();
                }
                return true;
            }
        });

        gridLayoutManager = new GridLayoutManager(requireContext(), currentLayer);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Set a touch listener on the root view to detect touch gestures
        view.setOnTouchListener((v, event) -> scaleGestureDetector.onTouchEvent(event));

        checkPermissionsAndLoadImages();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshImageGrid();
    }

    private void initializeUI() {
        gridLayoutManager = new GridLayoutManager(requireContext(), 2); // Default to Layer 2
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    private void refreshImageGrid() {
        allImages = loadImagesFromDevice();
        displayImagesInGrid(allImages);
    }

    private void checkPermissionsAndLoadImages() {
        allImages = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
            } else {
                allImages = loadImagesFromDevice();
                displayImagesInGrid(allImages);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
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
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                null, null, null)) {

            if (cursor != null) {
                Set<String> recycleBinPaths = getRecycleBinImageNames();

                while (cursor.moveToNext()) {
                    String imagePath = cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    File imageFile = new File(imagePath);

                    if (!imageFile.exists()) {
                        continue;
                    }

                    if (recycleBinPaths.contains(imageFile.getName())) {
                        continue;
                    }

                    imageList.add(new ImageData(imagePath, null, null));
                }
            }
        } catch (Exception e) {
            Log.e("LoadImages", "Error loading images: " + e.getMessage(), e);
        }

        imageList.sort((image1, image2) -> new File(image1.getImagePath()).getName()
                .compareToIgnoreCase(new File(image2.getImagePath()).getName()));

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
            String imagePath = imageData.getImagePath();
            imagePaths.add(imagePath);
        }

        ImageAdapter adapter = new ImageAdapter(imagePaths, requireContext());
        recyclerView.setAdapter(adapter);
    }

    private void handleZoomIn() {
        if (currentLayer == 3) {
            currentLayer = 2;
            gridLayoutManager.setSpanCount(2);
            recyclerView.setLayoutManager(gridLayoutManager);

        } else if (currentLayer == 2) {
            currentLayer = 1;
            gridLayoutManager.setSpanCount(1);
            recyclerView.setLayoutManager(gridLayoutManager);

        }
    }

    private void handleZoomOut() {
        if (currentLayer == 1) {
            currentLayer = 2;
            gridLayoutManager.setSpanCount(2);
            recyclerView.setLayoutManager(gridLayoutManager);

        } else if (currentLayer == 2) {
            currentLayer = 3;
            gridLayoutManager.setSpanCount(3);
            recyclerView.setLayoutManager(gridLayoutManager);

        }
    }

}
