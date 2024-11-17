package com.tdtu.edu.vn.mygallery;
import com.tdtu.edu.vn.mygallery.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
        Log.d("ImageAdapter", "Binding image: " + imagePath);
        // Load the image into the ImageView
        Glide.with(context)
                .load(new File(imagePath))
                .placeholder(R.drawable.album_placeholder)
                .into(holder.imageView);

        // Navigate to ImageInspectActivity when an image is clicked
        holder.itemView.setOnClickListener(v -> {
            Log.d("ImageAdapter", "Image path: " + imagePath);

            Intent intent = new Intent(context, ImageInspectActivity.class);
            intent.putExtra("IMAGE_PATH", imagePath);
            context.startActivity(intent);
        });

        // Show a PopupMenu when the user long-presses the image
        holder.itemView.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.itemView);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.image_options_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.add_to_favorites:
                        addImageToFavorites(imagePath);
                        return true;
                    case R.id.move_to_recycle_bin:
                        moveToRecycleBin(imagePath, position);
                        return true;
                    default:
                        return false;
                }
            });

            popupMenu.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    private void moveToRecycleBin(String imagePath, int position) {
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
    }

    private boolean moveFile(File sourceFile, File destinationFile) {
        try (FileInputStream in = new FileInputStream(sourceFile);
             FileOutputStream out = new FileOutputStream(destinationFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            boolean deleted = sourceFile.delete();
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

        String existingFavorites = sharedPreferences.getString("favoriteImages", "");
        List<String> favoritePaths = new ArrayList<>(Arrays.asList(existingFavorites.split(";")));

        if (favoritePaths.contains(imagePath)) {
            Toast.makeText(context, "Picture already in Favorites", Toast.LENGTH_SHORT).show();
        } else {
            favoritePaths.add(imagePath);
            String updatedFavorites = String.join(";", favoritePaths);
            editor.putString("favoriteImages", updatedFavorites);
            editor.apply();

            Toast.makeText(context, "Image added to favorites", Toast.LENGTH_SHORT).show();
        }
    }

    private void addImageToRecycleBin(String imagePath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("RecycleBin", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String existingImages = sharedPreferences.getString("deletedImages", "");
        List<String> deletedPaths = new ArrayList<>(Arrays.asList(existingImages.split(";")));

        if (deletedPaths.contains(imagePath)) {
            Toast.makeText(context, "Image already in Recycle Bin", Toast.LENGTH_SHORT).show();
        } else {
            deletedPaths.add(imagePath);
            String updatedPaths = String.join(";", deletedPaths);
            editor.putString("deletedImages", updatedPaths);
            editor.apply();

            Toast.makeText(context, "Image moved to Recycle Bin", Toast.LENGTH_SHORT).show();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
