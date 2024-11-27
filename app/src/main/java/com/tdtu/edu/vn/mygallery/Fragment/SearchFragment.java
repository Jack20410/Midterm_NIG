package com.tdtu.edu.vn.mygallery.Fragment;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tdtu.edu.vn.mygallery.Image.ImageAdapter;
import com.tdtu.edu.vn.mygallery.Image.ImageData;
import com.tdtu.edu.vn.mygallery.Image.ImageTag;
import com.tdtu.edu.vn.mygallery.Image.ImageTagDatabase;
import com.tdtu.edu.vn.mygallery.R;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText searchInput;
    private Button searchButton;
    private Button searchDateButton;
    private List<ImageData> allImages;
    private ImageTagDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_search, container, false);

        db = ImageTagDatabase.getInstance(requireContext()); // Initialize Room database

        initializeUI(view);
        checkPermissionsAndLoadImages();

        // Search by tag/keyword
        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (query.isEmpty()) {
                displayImagesInGrid(allImages);
            } else {
                searchImagesByTag(query);
            }
        });

        // Search by date
        searchDateButton.setOnClickListener(v -> showDatePickerDialog());

        return view;
    }

    private void initializeUI(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3)); // 3 columns grid layout

        searchInput = view.findViewById(R.id.searchInput);
        searchButton = view.findViewById(R.id.searchButton);
        searchDateButton = view.findViewById(R.id.searchDateButton);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    // Format the selected date as dd-MM-yyyy
                    String selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d", dayOfMonth, month + 1, year);

                    // Filter images based on the selected date
                    List<ImageData> filteredImages = filterImagesByDate(allImages, selectedDate);

                    // Display the filtered images
                    displayImagesInGrid(filteredImages);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void checkPermissionsAndLoadImages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_IMAGES)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES},
                        100
                );
            } else {
                loadImagesFromDevice();
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        100
                );
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

    private void searchImagesByTag(String query) {
        new Thread(() -> {
            List<ImageTag> taggedImages = db.imageTagDao().searchTags(query);
            List<ImageData> filteredImages = new ArrayList<>();

            for (ImageTag tag : taggedImages) {
                File imageFile = new File(tag.imagePath);
                if (imageFile.exists()) {
                    filteredImages.add(new ImageData(tag.imagePath, null, tag.tag));
                } else {
                    Log.e("SearchFragment", "Image file not found: " + tag.imagePath);
                }
            }

            requireActivity().runOnUiThread(() -> {
                if (!filteredImages.isEmpty()) {
                    Toast.makeText(requireContext(), "Found " + filteredImages.size() + " image(s) with the tag.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "No images found with that tag.", Toast.LENGTH_SHORT).show();
                }

                displayImagesInGrid(filteredImages);
            });
        }).start();
    }

    private List<ImageData> filterImagesByDate(List<ImageData> images, String queryDate) {
        List<ImageData> filteredImages = new ArrayList<>();
        SimpleDateFormat exifDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());

        for (ImageData image : images) {
            String dateTaken = image.getDateTaken();
            if (dateTaken == null) continue;

            try {
                Date exifDate = exifDateFormat.parse(dateTaken);
                String formattedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(exifDate);
                if (queryDate.equals(formattedDate)) {
                    filteredImages.add(image);
                }
            } catch (ParseException e) {
                Log.e("SearchFragment", "Error parsing date: " + e.getMessage());
            }
        }
        return filteredImages;
    }

    private void displayImagesInGrid(List<ImageData> imageList) {
        List<String> imagePaths = new ArrayList<>();
        for (ImageData imageData : imageList) {
            imagePaths.add(imageData.getImagePath());
        }

        ImageAdapter adapter = new ImageAdapter(imagePaths, requireContext());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
