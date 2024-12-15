package com.tdtu.edu.vn.mygallery.Fragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



import com.tdtu.edu.vn.mygallery.Image.ImageInspectActivity;

import com.tdtu.edu.vn.mygallery.Image.ImageAdapter;
import com.tdtu.edu.vn.mygallery.Image.ImageData;
import com.tdtu.edu.vn.mygallery.Image.ImageTag;
import com.tdtu.edu.vn.mygallery.Image.ImageTagDatabase;
import com.tdtu.edu.vn.mygallery.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText searchInput;
    private ImageButton searchButton;
    private ImageButton searchDateButton;
    private List<ImageData> allImages;
    private ImageTagDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_search, container, false);

        db = ImageTagDatabase.getInstance(requireContext());

        initializeUI(view);
        checkPermissionsAndLoadImages();

        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (query.isEmpty()) {
                displayImagesInGrid(allImages);
            } else {
                searchImagesByTag(query);
            }
        });

        searchDateButton.setOnClickListener(v -> showDatePickerDialog());

        return view;
    }

    private void initializeUI(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        searchInput = view.findViewById(R.id.searchInput);
        searchButton = view.findViewById(R.id.searchButton);
        searchDateButton = view.findViewById(R.id.searchDateButton);
    }

    private void checkPermissionsAndLoadImages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_IMAGES)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 100);
            } else {
                loadImagesFromDevice();
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            } else {
                loadImagesFromDevice();
            }
        }
    }

    private void loadImagesFromDevice() {
        allImages = new ArrayList<>();
        String[] projection = {MediaStore.Images.Media.DATA};

        try (Cursor cursor = requireContext().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")) {

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String imagePath = cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    if (new File(imagePath).exists()) {
                        allImages.add(new ImageData(imagePath, null, null));
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SearchFragment", "Error loading images: " + e.getMessage());
        }
    }

    private void displayImagesInGrid(List<ImageData> imageList) {
        List<String> imagePaths = new ArrayList<>();
        for (ImageData imageData : imageList) {
            imagePaths.add(imageData.getImagePath());
        }

        ImageAdapter adapter = new ImageAdapter(
                imagePaths,
                requireContext(),
                this::onImageClicked // Image click listener
                // Location button click listener
        );
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void onImageClicked(String imagePath) {
        Intent intent = new Intent(requireContext(), ImageInspectActivity.class);
        intent.putExtra("IMAGE_PATH", imagePath);
        startActivity(intent);
    }



    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d", dayOfMonth, month + 1, year);

                    List<ImageData> filteredImages = filterImagesByDate(allImages, selectedDate);
                    displayImagesInGrid(filteredImages);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private List<ImageData> filterImagesByDate(List<ImageData> images, String queryDate) {
        List<ImageData> filteredImages = new ArrayList<>();
        for (ImageData image : images) {
            String dateTaken = image.getDateTaken();
            if (dateTaken != null && dateTaken.contains(queryDate)) {
                filteredImages.add(image);
            }
        }
        return filteredImages;
    }

    private void searchImagesByTag(String query) {
        new Thread(() -> {
            List<ImageTag> taggedImages = db.imageTagDao().searchTags(query);
            List<ImageData> filteredImages = new ArrayList<>();

            for (ImageTag tag : taggedImages) {
                File imageFile = new File(tag.imagePath);
                if (imageFile.exists()) {
                    filteredImages.add(new ImageData(tag.imagePath, null, tag.tag));
                }
            }

            requireActivity().runOnUiThread(() -> displayImagesInGrid(filteredImages));
        }).start();
    }
}
