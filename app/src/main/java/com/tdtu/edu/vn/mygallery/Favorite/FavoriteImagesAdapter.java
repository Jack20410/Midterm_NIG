package com.tdtu.edu.vn.mygallery.Favorite;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.tdtu.edu.vn.mygallery.Image.ImageInspectActivity;
import com.tdtu.edu.vn.mygallery.R;

import android.widget.PopupMenu;
import android.widget.Toast;
import java.io.File;
import java.util.List;
import android.content.Intent;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Arrays;

public class FavoriteImagesAdapter extends RecyclerView.Adapter<FavoriteImagesAdapter.ViewHolder> {

    private List<String> imagePaths;
    private Context context;

    public FavoriteImagesAdapter(List<String> imagePaths, Context context) {
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

        holder.imageView.setOnLongClickListener(v -> {
            showContextMenu(v, position, imagePath);
            return true;
        });
        // Set an OnClickListener on the image view
        holder.imageView.setOnClickListener(new View.OnClickListener() {
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

    }

    private void showContextMenu(View view, int position, String imagePath) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.image_options_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.remove_favorites) {
                new AlertDialog.Builder(context)
                        .setTitle("Remove Picture")
                        .setMessage("Do you want to permanently delete this picture from Favorites?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            boolean isDeleted = permanentlyDeleteFromFavorites(imagePath);
                            if (isDeleted) {
                                imagePaths.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, imagePaths.size());
                                Toast.makeText(context, "Image permanently deleted from Favorites", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to delete image from Favorites", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }


    @Override
    public int getItemCount() {
        return imagePaths.size();
    }
    private void removeFromFavorites(String imagePath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Favorites", Context.MODE_PRIVATE);
        String existingFavorites = sharedPreferences.getString("favoriteImages", "");

        // Create a List of paths and remove the selected one
        List<String> favoritePaths = new ArrayList<>(Arrays.asList(existingFavorites.split(";")));
        favoritePaths.remove(imagePath); // Remove the selected image path

        // Join the remaining paths back into a string
        String updatedFavorites = String.join(";", favoritePaths);

        // Save the updated favorites list to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("favoriteImages", updatedFavorites);
        editor.apply();
    }
    private boolean permanentlyDeleteFromFavorites(String imagePath) {
        File favoritesFolder = new File(context.getFilesDir(), "Favorites");
        File imageFile = new File(imagePath);

        if (favoritesFolder.exists() && favoritesFolder.isDirectory()) {
            File[] files = favoritesFolder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.getName().equals(imageFile.getName())) { // Check by file name
                        boolean deleted = file.delete();
                        if (deleted) {
                            Log.d("Favorites", "Image permanently deleted from Favorites: " + file.getAbsolutePath());
                            return true; // Return success
                        } else {
                            Log.d("Favorites", "Failed to delete image from Favorites: " + file.getAbsolutePath());
                            return false; // Return failure
                        }
                    }
                }
            }
        }

        return false; // Image not found in Favorites
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
//        ImageButton deleteButton;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
//            deleteButton = itemView.findViewById(R.id.deleteButton);

        }
    }
}
