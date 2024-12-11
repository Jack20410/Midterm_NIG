package com.tdtu.edu.vn.mygallery.Image;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.tdtu.edu.vn.mygallery.PhotoLocationActivity;
import com.tdtu.edu.vn.mygallery.Utilities.FileManager;
import com.tdtu.edu.vn.mygallery.R;

import androidx.viewpager2.widget.ViewPager2;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageInspectActivity extends AppCompatActivity {

    private PhotoView photoView;
    private TextView noImagesMessage;
    private ImageButton addToFavoritesButton;
    private ImageButton moveToRecycleBinButton;
    private String imagePath;
    private Context context;
    private List<String> imagePaths; // List of all images in the folder
    private int currentIndex; // Current image index in the folder
    private GestureDetector gestureDetector; // GestureDetector for handling swipe gestures
    private ViewPager2 viewPager;
    private Map<String, String> imageTags = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_inspect);

        // Initialize the context
        context = this;

        // Initialize the views
        viewPager = findViewById(R.id.viewPager);
        noImagesMessage = findViewById(R.id.noImagesMessage);
        addToFavoritesButton = findViewById(R.id.addToFavoritesButton);
        moveToRecycleBinButton = findViewById(R.id.moveToRecycleBinButton);
        ImageButton addTagButton = findViewById(R.id.addTagButton);
        ImageButton mapButton = findViewById(R.id.mapButton);

        // Set up map button click listener
        mapButton.setOnClickListener(v -> handleMapButtonClick());

        // Set up add tag button click listener
        addTagButton.setOnClickListener(v -> {
            if (!imagePaths.isEmpty() && currentIndex >= 0) {
                String currentImagePath = imagePaths.get(currentIndex);
                showTagDialog(currentImagePath);
            } else {
                Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show();
            }
        });

        // Get the image path from the intent
        Intent intent = getIntent();
        imagePath = intent.getStringExtra("IMAGE_PATH");

        if (imagePath == null || imagePath.isEmpty()) {
            Log.e("ImageInspectActivity", "Invalid image path received in intent.");
            showNoImagesMessage();
            return; // Exit early to avoid unnecessary setup
        }

        // Load images from the folder
        loadImagesFromFolder(imagePath);

        if (imagePaths.isEmpty()) {
            showNoImagesMessage();
            addToFavoritesButton.setEnabled(false);
            moveToRecycleBinButton.setEnabled(false);
            return; // Exit early as no images are available
        }

        // Validate and synchronize imagePath with the list
        if (!imagePaths.contains(imagePath)) {
            Log.e("ImageInspectActivity", "The provided image path is not in the loaded image list.");
            imagePath = imagePaths.get(0); // Default to the first image
            currentIndex = 0;
        } else {
            currentIndex = imagePaths.indexOf(imagePath);
        }

        // Call setupViewPager with the valid imagePath
        setupViewPager(imagePath);

        // Set up add to favorites button click listener
        addToFavoritesButton.setOnClickListener(v -> {
            if (!imagePaths.isEmpty() && currentIndex >= 0) {
                confirmAddToFavorites(imagePaths.get(currentIndex));
            } else {
                Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up move to recycle bin button click listener
        moveToRecycleBinButton.setOnClickListener(v -> {
            if (!imagePaths.isEmpty() && currentIndex >= 0) {
                confirmMoveToRecycleBin(imagePaths.get(currentIndex));
            } else {
                Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void handleMapButtonClick() {
        float[] latLong = getLatLongFromImage(imagePath);
        if (latLong != null) {
            Intent mapIntent = new Intent(context, PhotoLocationActivity.class);
            mapIntent.putExtra("photoPath", imagePath);
            mapIntent.putExtra("latitude", (double) latLong[0]);
            mapIntent.putExtra("longitude", (double) latLong[1]);
            startActivity(mapIntent);
        } else {
            Toast.makeText(context, "No location data available for this image.", Toast.LENGTH_SHORT).show();
        }
    }
    private float[] getLatLongFromImage(String imagePath) {
        try {
            android.media.ExifInterface exif = new android.media.ExifInterface(imagePath);
            float[] latLong = new float[2];
            if (exif.getLatLong(latLong)) {
                return latLong;
            }
        } catch (Exception e) {
            Log.e("ImageInspectActivity", "Error extracting location data: " + e.getMessage());
        }
        return null;
    }




    private void showTagDialog(String imagePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add/Modify Tag");

        // Create an input field
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        // Load tag asynchronously
        ImageTagDatabase db = ImageTagDatabase.getInstance(this);
        new Thread(() -> {
            ImageTag existingTag = db.imageTagDao().getTag(imagePath);

            runOnUiThread(() -> {
                // Pre-fill the current tag if it exists
                if (existingTag != null) {
                    input.setText(existingTag.tag);
                }

                builder.setView(input);

                // Add dialog buttons
                builder.setPositiveButton("Save", (dialog, which) -> {
                    String tag = input.getText().toString().trim();

                    if (tag.isEmpty()) {
                        Toast.makeText(this, "Tag cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Check for duplicate tags asynchronously
                    new Thread(() -> {
                        List<ImageTag> allTags = db.imageTagDao().getAllTags();
                        boolean isDuplicate = allTags.stream()
                                .anyMatch(t -> t.tag.equalsIgnoreCase(tag) && !t.imagePath.equals(imagePath));

                        if (isDuplicate) {
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Tag already defined, please use another one.", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            // Save or update tag if no duplicate exists
                            ImageTag imageTag = new ImageTag();
                            imageTag.imagePath = imagePath;
                            imageTag.tag = tag;

                            if (existingTag == null) {
                                db.imageTagDao().insertTag(imageTag);
                            } else {
                                db.imageTagDao().updateTag(imageTag);
                            }

                            runOnUiThread(() -> {
                                Toast.makeText(this, "Tag saved: " + tag, Toast.LENGTH_SHORT).show();
                                Log.d("ImageInspectActivity", "Tag saved for " + imagePath + ": " + tag);
                            });
                        }
                    }).start();
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                builder.show();
            });
        }).start();
    }

    private void loadImagesFromFolder(String currentImagePath) {
        File currentFile = new File(currentImagePath);
        File folder = currentFile.getParentFile();

        if (folder != null && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> {
                String lowerCaseName = name.toLowerCase();
                return lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".jpeg") || lowerCaseName.endsWith(".png");
            });

            imagePaths = new ArrayList<>();
            if (files != null) {
                for (File file : files) {
                    imagePaths.add(file.getAbsolutePath());
                }
                imagePaths.sort(String::compareTo); // Sort files alphabetically
            }

            // Update the current index based on the provided path
            currentIndex = imagePaths.indexOf(currentImagePath);
            Log.d("ImageInspectActivity", "Loaded images: " + imagePaths);
            Log.d("ImageInspectActivity", "Current index: " + currentIndex);
        } else {
            imagePaths = new ArrayList<>(); // Reset the list if folder is invalid
            currentIndex = -1; // No valid index
        }
    }

    private void setupViewPager(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            Log.e("ImageInspectActivity", "Invalid image path provided to setupViewPager.");
            showNoImagesMessage();
            return;
        }

        // Ensure currentIndex is updated based on the provided imagePath
        currentIndex = imagePaths.indexOf(imagePath);

        if (currentIndex == -1) {
            Log.e("ImageInspectActivity", "Provided image path not found in the imagePaths list. Defaulting to the first image.");
            currentIndex = 0; // Fallback to the first image if the path is not in the list
        }

        ImagePagerAdapter adapter = new ImagePagerAdapter(this, imagePaths);
        viewPager.setAdapter(adapter);

        // Ensure the startIndex is within valid bounds
        int startIndex = currentIndex >= imagePaths.size() ? 0 : currentIndex;
        viewPager.setCurrentItem(startIndex, false);

        noImagesMessage.setVisibility(View.GONE);
        viewPager.setVisibility(View.VISIBLE);

        // Add a PageChangeCallback to update currentIndex and imagePath
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentIndex = position; // Update currentIndex
                ImageInspectActivity.this.imagePath = imagePaths.get(position); // Update imagePath field
                Log.d("ImageInspectActivity", "Page selected. Updated index: " + currentIndex + ", Path: " + ImageInspectActivity.this.imagePath);
            }
        });

        Log.d("ImageInspectActivity", "setupViewPager called with imagePath: " + imagePath + ", currentIndex: " + currentIndex);
    }

    private void confirmAddToFavorites(String imagePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add to Favorites")
                .setMessage("Do you want to add this image to Favorites?")
                .setPositiveButton("Yes", (dialog, which) -> addImageToFavorites(imagePath))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void confirmMoveToRecycleBin(String imagePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Move to Recycle Bin")
                .setMessage("Are you sure you want to move this image to the Recycle Bin?")
                .setPositiveButton("Yes", (dialog, which) -> moveToRecycleBin(imagePath))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void addImageToFavorites(String imagePath) {
        File sourceFile = new File(imagePath);
        File favoritesFolder = FileManager.getFavoritesFolder(context);

        if (sourceFile.exists()) {
            boolean copied = FileManager.copyFile(sourceFile, favoritesFolder);
            if (copied) {
                Log.d("Favorites", "File added to Favorites: " + imagePath);
            } else {
                Toast.makeText(context, "Failed to add image to Favorites", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void moveToRecycleBin(String imagePath) {
        File sourceFile = new File(imagePath);
        File recycleBinFolder = FileManager.getRecycleBinFolder(context);

        if (sourceFile.exists()) {
            Log.d("ImageInspectActivity", "Image list before removal: " + imagePaths);
            Log.d("ImageInspectActivity", "Size before removal: " + imagePaths.size());
            Log.d("ImageInspectActivity", "Attempting to delete image at index: " + currentIndex + ", Path: " + imagePath);

            boolean moved = FileManager.moveFile(sourceFile, recycleBinFolder);
            if (moved) {
                Log.d("RecycleBin", "File moved to Recycle Bin: " + imagePath);

                removeFromFavorites(imagePath);
                // Remove the current image from the list
                imagePaths.remove(currentIndex);
                Log.d("ImageInspectActivity", "Deleted image index: " + currentIndex);
                Log.d("ImageInspectActivity", "Image list after removal: " + imagePaths);
                Log.d("ImageInspectActivity", "Size after removal: " + imagePaths.size());

                // Reload folder contents and update the list
                if (!imagePaths.isEmpty()) {
                    // Adjust currentIndex to avoid index out of bounds
                    if (currentIndex >= imagePaths.size()) {
                        currentIndex = 0; // Reset to the first image if out of bounds
                    }

                    // Pass the updated image path to setupViewPager
                    Log.d("ImageInspectActivity", "Updating ViewPager with new image at index: " + currentIndex + ", Path: " + imagePaths.get(currentIndex));
                    setupViewPager(imagePaths.toString());
                } else {
                    Log.d("ImageInspectActivity", "No images left after moving to Recycle Bin.");
                    showNoImagesMessage();
                }
            } else {
                Toast.makeText(context, "Failed to move image to Recycle Bin", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Removes the image from the Favorites folder if it exists there.
     *
     * @param imagePath The absolute path of the image.
     */
    private void removeFromFavorites(String imagePath) {
        File favoritesFolder = new File(getFilesDir(), "Favorites");
        File imageFile = new File(imagePath);

        if (favoritesFolder.exists() && favoritesFolder.isDirectory()) {
            File[] files = favoritesFolder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.getName().equals(imageFile.getName())) { // Check by file name
                        boolean deleted = file.delete();
                        if (deleted) {
                            Log.d("Favorites", "Image removed from Favorites: " + file.getAbsolutePath());
                        } else {
                            Log.d("Favorites", "Failed to remove image from Favorites: " + file.getAbsolutePath());
                        }
                        break; // Exit loop after finding the file
                    }
                }
            }
        }
    }

    private boolean showPreviousImage() {
        if (imagePaths != null && !imagePaths.isEmpty()) {
            currentIndex--;
            if (currentIndex < 0) {
                currentIndex = imagePaths.size() - 1; // Loop back to the last image if at the start
            }

            String previousImagePath = imagePaths.get(currentIndex);
            if (previousImagePath != null && !previousImagePath.isEmpty()) {
                displayImage(previousImagePath);
                return true; // Successfully displayed the previous image
            } else {
                Log.e("ImageInspectActivity", "Previous image path is null or invalid in showPreviousImage.");
                return false; // Failed to display the previous image
            }
        } else {
            Log.e("ImageInspectActivity", "No images available in showPreviousImage.");
            return false;
        }
    }

    private void displayImage(String path) {
        if (path == null || path.isEmpty()) {
            Log.e("ImageInspectActivity", "Invalid image path at current index. Attempting to show previous image.");
            if (!showPreviousImage()) {
                Log.e("ImageInspectActivity", "No valid previous image. Showing no images message.");
                showNoImagesMessage();
            }
            return;
        }

        File file = new File(path);
        if (!file.exists()) {
            Log.e("ImageInspectActivity", "File does not exist at path: " + path);
            if (!showPreviousImage()) {
                Log.e("ImageInspectActivity", "No valid previous image. Showing no images message.");
                showNoImagesMessage();
            }
            return;
        }

        Glide.with(this)
                .load(file)
                .placeholder(R.drawable.album_placeholder)
                .error(R.drawable.three_button)
                .into(photoView);

        if (photoView != null) {
            photoView.setVisibility(View.VISIBLE);
        }
        if (noImagesMessage != null) {
            noImagesMessage.setVisibility(View.GONE);
        }

        imagePath = path; // Update the current image path

        // Display the tag for the current image
        String tag = imageTags.getOrDefault(imagePath, "No tag");
        Log.d("ImageInspectActivity", "Displaying image: " + path + " | Tag: " + tag);
    }

    private void showNoImagesMessage() {
        if (viewPager != null) {
            viewPager.setVisibility(View.GONE); // Hide the ViewPager2 when there are no images
        }

        if (noImagesMessage != null) {
            noImagesMessage.setVisibility(View.VISIBLE); // Show the "No images" message
        }

        if (addToFavoritesButton != null) {
            addToFavoritesButton.setEnabled(false);
        }

        if (moveToRecycleBinButton != null) {
            moveToRecycleBinButton.setEnabled(false);
        }
    }

}