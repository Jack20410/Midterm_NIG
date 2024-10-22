package com.tdtu.edu.vn.mygallery;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private List<String> imagePaths;
    private Context context;

    public ImageAdapter(List<String> imagePaths, Context context) {
        this.imagePaths = imagePaths;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);
        // Load the image into the ImageView
        Glide.with(context)
                .load(new File(imagePath))  // Load from the local file path
                .placeholder(R.drawable.album_placeholder)  // Add a placeholder image
                .into(holder.imageView);
        // Set an OnClickListener on the image view

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log the imagePath to check if it's null or valid
                Log.d("ImageAdapter", "Image path: " + imagePath);

                // Navigate to ImagesInspectActivity with the clicked image's path
                Intent intent = new Intent(context, ImageInspectActivity.class);
                intent.putExtra("IMAGE_PATH", imagePath); // Pass the image path to the activity

                // Start the activity
                context.startActivity(intent);
            }
        });

        holder.heartButton.setOnClickListener(v -> {
            addImageToFavorites(imagePath);

        });

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Picture")
                    .setMessage("Do you want to move this picture to the Recycle Bin?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        File sourceFile = new File(imagePath);

                        if (sourceFile.exists()) {
                            File recycleBinFolder = new File(context.getFilesDir(), "RecycleBin");
                            if (!recycleBinFolder.exists()) {
                                boolean folderCreated = recycleBinFolder.mkdir();
                                Log.d("ImageAdapter", "RecycleBin folder created: " + folderCreated);
                            }

                            File destinationFile = new File(recycleBinFolder, sourceFile.getName());
                            Log.d("ImageAdapter", "Source: " + sourceFile.getAbsolutePath());
                            Log.d("ImageAdapter", "Destination: " + destinationFile.getAbsolutePath());

                            if (moveFile(sourceFile, destinationFile)) {
                                // Add to Recycle Bin preferences
                                addImageToRecycleBin(destinationFile.getAbsolutePath());

                                // Remove from current list and notify adapter
                                imagePaths.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, imagePaths.size());

                                Toast.makeText(context, "Image moved to Recycle Bin", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to move image", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        });

    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }
    private boolean moveFile(File sourceFile, File destinationFile) {
        try (FileInputStream in = new FileInputStream(sourceFile);
             FileOutputStream out = new FileOutputStream(destinationFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            boolean deleted = sourceFile.delete(); // Delete original file
            if (!deleted) {
                Log.e("ImageAdapter", "Failed to delete the original file: " + sourceFile.getAbsolutePath());
            }
            return deleted;

        } catch (IOException e) {
            Log.e("ImageAdapter", "Error moving file: " + e.getMessage(), e);
            return false;
        }
    }


    private void addImageToFavorites(String imagePath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Favorites", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Get the existing favorites
        String existingFavorites = sharedPreferences.getString("favoriteImages", "");

        // Convert the existing favorites into a List for easy checking
        List<String> favoritePaths = new ArrayList<>(Arrays.asList(existingFavorites.split(";")));

        // Check if the image is already in the favorites
        if (favoritePaths.contains(imagePath)) {
            Toast.makeText(context, "Picture already in Favorites", Toast.LENGTH_SHORT).show();
        } else {
            // Add the new favorite
            favoritePaths.add(imagePath);

            // Join the updated list back into a single string
            String updatedFavorites = String.join(";", favoritePaths);

            // Save the updated favorites list
            editor.putString("favoriteImages", updatedFavorites);
            editor.apply();

            Toast.makeText(context, "Image added to favorites", Toast.LENGTH_SHORT).show();
        }
    }




    private void addImageToRecycleBin(String imagePath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("RecycleBin", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Retrieve the existing list of images in the recycle bin
        String existingImages = sharedPreferences.getString("deletedImages", "");

        // Convert the string into a List for easy manipulation
        List<String> deletedPaths = new ArrayList<>(Arrays.asList(existingImages.split(";")));

        // Check if the image is already in the recycle bin
        if (deletedPaths.contains(imagePath)) {
            Toast.makeText(context, "Image already in Recycle Bin", Toast.LENGTH_SHORT).show();
        } else {
            // Add the new image path to the list
            deletedPaths.add(imagePath);

            // Join the updated list back into a single string
            String updatedPaths = String.join(";", deletedPaths);

            // Save the updated paths back to SharedPreferences
            editor.putString("deletedImages", updatedPaths);
            editor.apply();

            Toast.makeText(context, "Image moved to Recycle Bin", Toast.LENGTH_SHORT).show();
        }
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton deleteButton;
        ImageButton heartButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            heartButton = itemView.findViewById(R.id.heartButton);
        }
    }
}
