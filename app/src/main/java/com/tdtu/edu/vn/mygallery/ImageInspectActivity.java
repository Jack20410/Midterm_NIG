package com.tdtu.edu.vn.mygallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import androidx.viewpager2.widget.ViewPager2;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageInspectActivity extends AppCompatActivity {

    private PhotoView photoView;
    private TextView noImagesMessage;
    private Button addToFavoritesButton;
    private Button moveToRecycleBinButton;
    private String imagePath;
    private Context context;
    private List<String> imagePaths; // List of all images in the folder
    private int currentIndex; // Current image index in the folder
    private GestureDetector gestureDetector; // GestureDetector for handling swipe gestures
    private ViewPager2 viewPager;
    @Override


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_inspect);

        context = this;

        viewPager = findViewById(R.id.viewPager);
        noImagesMessage = findViewById(R.id.noImagesMessage);
        addToFavoritesButton = findViewById(R.id.addToFavoritesButton);
        moveToRecycleBinButton = findViewById(R.id.moveToRecycleBinButton);

        // Get the image path from the intent
        Intent intent = getIntent();
        imagePath = intent.getStringExtra("IMAGE_PATH");

        if (imagePath == null || imagePath.isEmpty()) {
            Log.e("ImageInspectActivity", "Invalid image path received in intent.");
            showNoImagesMessage();
            return; // Exit early to avoid unnecessary setup
        }

        loadImagesFromFolder(imagePath);

        if (imagePaths.isEmpty()) {
            showNoImagesMessage();
            addToFavoritesButton.setEnabled(false);
            moveToRecycleBinButton.setEnabled(false);
            return; // Exit early as no images are available
        }

        // Ensure imagePath is valid and synchronized
        if (!imagePaths.contains(imagePath)) {
            Log.e("ImageInspectActivity", "The provided image path is not in the loaded image list.");
            imagePath = imagePaths.get(0); // Default to the first image
            currentIndex = 0;
        } else {
            currentIndex = imagePaths.indexOf(imagePath);
        }

        // Call the new setupViewPager with the valid imagePath
        setupViewPager(imagePath);

        // Add to Favorites Button
        addToFavoritesButton.setOnClickListener(v -> confirmAddToFavorites(imagePaths.get(currentIndex)));

        // Move to Recycle Bin Button
        moveToRecycleBinButton.setOnClickListener(v -> confirmMoveToRecycleBin(imagePaths.get(currentIndex)));
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


    private void notifyMainActivity(String imagePath) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("REMOVED_IMAGE_PATH", imagePath);
        setResult(RESULT_OK, resultIntent);
    }

    private boolean showNextImage() {
        if (imagePaths != null && !imagePaths.isEmpty()) {
            currentIndex++;
            if (currentIndex >= imagePaths.size()) {
                currentIndex = 0; // Loop back to the first image if at the end
            }

            String nextImagePath = imagePaths.get(currentIndex);
            if (nextImagePath != null && !nextImagePath.isEmpty()) {
                displayImage(nextImagePath);
                return true; // Successfully displayed the next image
            } else {
                Log.e("ImageInspectActivity", "Next image path is null or invalid in showNextImage.");
                return false; // Failed to display the next image
            }
        } else {
            Log.e("ImageInspectActivity", "No images available in showNextImage.");
            return false;
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

            // Fallback to the previous image
            if (!showPreviousImage()) {
                Log.e("ImageInspectActivity", "No valid previous image. Showing no images message.");
                showNoImagesMessage();
            }
            return;
        }

        File file = new File(path);
        if (!file.exists()) {
            Log.e("ImageInspectActivity", "File does not exist at path: " + path);

            // Fallback to the previous image
            if (!showPreviousImage()) {
                Log.e("ImageInspectActivity", "No valid previous image. Showing no images message.");
                showNoImagesMessage();
            }
            return;
        }

        // Proceed to load the valid image
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
        Log.d("ImageInspectActivity", "Displaying image: " + path);
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
