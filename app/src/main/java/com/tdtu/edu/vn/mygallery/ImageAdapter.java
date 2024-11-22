package com.tdtu.edu.vn.mygallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
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
            Intent intent = new Intent(context, ImageInspectActivity.class);
            intent.putExtra("IMAGE_PATH", imagePath); // Pass the current image path
            intent.putStringArrayListExtra("ALL_IMAGES", new ArrayList<>(imagePaths)); // Pass all images
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
                        confirmAddToFavorites(imagePath);
                        return true;
                    case R.id.move_to_recycle_bin:
                        confirmMoveToRecycleBin(imagePath);
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



    private void confirmMoveToRecycleBin(String imagePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Move to Recycle Bin")
                .setMessage("Are you sure you want to move this image to the Recycle Bin?")
                .setPositiveButton("Yes", (dialog, which) -> moveToRecycleBin(imagePath))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
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
    /**
     * Move image to Recycle Bin and update the list
     */
    public void addImageToFavorites(String imagePath) {
        File sourceFile = new File(imagePath);
        File favoritesFolder = FileManager.getFavoritesFolder(context);

        if (sourceFile.exists()) {
            boolean copied = FileManager.copyFile(sourceFile, favoritesFolder);
            if (copied) {
                Log.d("Favorites", "File added to Favorites: " + imagePath);
                Toast.makeText(context, "Image added to Favorites", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to add image to Favorites", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void moveToRecycleBin(String imagePath) {
        File sourceFile = new File(imagePath);
        File recycleBinFolder = FileManager.getRecycleBinFolder(context);

        if (sourceFile.exists()) {
            boolean moved = FileManager.moveFile(sourceFile, recycleBinFolder);
            if (moved) {
                Log.d("RecycleBin", "File moved to Recycle Bin: " + imagePath);
                Toast.makeText(context, "Image moved to Recycle Bin", Toast.LENGTH_SHORT).show();
                imagePaths.remove(imagePath);
                notifyDataSetChanged(); // Refresh the RecyclerView
            } else {
                Toast.makeText(context, "Failed to move image to Recycle Bin", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show();
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
