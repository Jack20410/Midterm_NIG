package com.tdtu.edu.vn.mygallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import android.widget.Toast;
import java.io.File;
import java.util.List;
import android.content.Intent;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import android.content.SharedPreferences;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import android.provider.MediaStore;
import java.io.IOException;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
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


        // Handle the delete button click
        holder.deleteButton.setOnClickListener(v -> {
            // Create an AlertDialog to confirm deletion
            new AlertDialog.Builder(context)
                    .setTitle("Delete Picture")
                    .setMessage("Do you want to move this picture to the Recycle Bin?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        File sourceFile = new File(imagePath);
                        if (sourceFile.exists()) {
                            // Create Recycle Bin folder inside internal storage
                            File recycleBinFolder = new File(context.getFilesDir(), "RecycleBin");
                            if (!recycleBinFolder.exists()) {
                                boolean folderCreated = recycleBinFolder.mkdir();
                                Log.d("ImageAdapter", "RecycleBin folder created: " + folderCreated);
                            }

                            // Create the new file in the Recycle Bin folder
                            File destinationFile = new File(recycleBinFolder, sourceFile.getName());
                            Log.d("ImageAdapter", "Source: " + sourceFile.getAbsolutePath());
                            Log.d("ImageAdapter", "Destination: " + destinationFile.getAbsolutePath());

                            // Attempt to move the file
                            if (moveFile(sourceFile, destinationFile)) {
                                addToRecycleBin(destinationFile.getAbsolutePath());

                                // Remove the item from the list and notify the adapter
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

            // Delete original file and update MediaStore
            boolean deleted = sourceFile.delete();
            if (deleted) {
                context.getContentResolver().delete(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Images.Media.DATA + "=?",
                        new String[]{sourceFile.getAbsolutePath()}
                );
            }
            return deleted;
        } catch (IOException e) {
            Log.e("ImageAdapter", "Error moving file: " + e.getMessage());
            return false;
        }
    }


    private void addToRecycleBin(String imagePath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("RecycleBin", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Retrieve existing paths from the recycle bin
        String existingPaths = sharedPreferences.getString("deletedImages", "");
        String updatedPaths = existingPaths + ";" + imagePath; // Add new path

        editor.putString("deletedImages", updatedPaths);
        editor.apply();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
