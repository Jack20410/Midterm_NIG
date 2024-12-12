package com.tdtu.edu.vn.mygallery.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.tdtu.edu.vn.mygallery.Image.ImageInspectActivity;
import com.tdtu.edu.vn.mygallery.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainFragment extends Fragment {

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

        scaleGestureDetector = new ScaleGestureDetector(requireContext(),
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(@NonNull ScaleGestureDetector detector) {
                        float scaleFactor = detector.getScaleFactor();
                        if (scaleFactor > 1.2f) {
                            handleZoomIn();
                        } else if (scaleFactor < 1.2f) {
                            handleZoomOut();
                        }
                        return true;
                    }
                });

        recyclerView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            return false;
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
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_IMAGES)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 100);
            } else {
                allImages = loadImagesFromDevice();
                displayImagesInGrid(allImages);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
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
            Log.e("MainFragment", "Error loading images: " + e.getMessage(), e);
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

        ImageAdapter adapter = new ImageAdapter(imagePaths, requireContext(),
                imagePath -> {
                    Intent intent = new Intent(requireContext(), ImageInspectActivity.class);
                    intent.putExtra("IMAGE_PATH", imagePath);
                    startActivity(intent);
                });

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
        gridLayoutManager.setSpanCount(currentLayer);
        recyclerView.setLayoutManager(gridLayoutManager);
        if (recyclerView.getAdapter() != null) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }
}
