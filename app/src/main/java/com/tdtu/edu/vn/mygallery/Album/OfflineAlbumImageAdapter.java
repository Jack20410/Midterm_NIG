package com.tdtu.edu.vn.mygallery.Album;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import android.widget.Toast;
import java.io.File;
import java.util.List;
import android.content.Intent;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import java.util.ArrayList;

public class OfflineAlbumImageAdapter extends RecyclerView.Adapter<OfflineAlbumImageAdapter.ViewHolder> {

    private List<String> imagePaths;
    private Context context;
    private OfflineAlbumDao albumDao;
    private OfflineAlbum currentAlbum;

    public OfflineAlbumImageAdapter(List<String> imagePaths, Context context, OfflineAlbumDao albumDao, OfflineAlbum currentAlbum) {
        this.imagePaths = imagePaths;
        this.context = context;
        this.albumDao = albumDao;
        this.currentAlbum = currentAlbum;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false); // Use item_album
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);
        Log.d("OfflineAlbumImageAdapter", "Loading image path: " + imagePath);
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

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Remove Picture from Album")
                    .setMessage("Are you sure you want to remove this picture from the album?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Remove the image from the album's data
                        removeFromAlbum(imagePath);

                        // Remove the image from the RecyclerView list
                        imagePaths.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, imagePaths.size());

                        Toast.makeText(context, "Image removed from album", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        });
        Log.d("ViewHolder", "ImageView: " + holder.imageView + ", DeleteButton: " + holder.deleteButton);
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    private void removeFromAlbum(String imagePath) {
        new Thread(() -> {
            if (currentAlbum != null && currentAlbum.imageUris != null) {
                // Split the URIs into a list
                List<String> uriList = new ArrayList<>(List.of(currentAlbum.imageUris.split(",")));

                // Remove the specified image path
                uriList.removeIf(uri -> uri.trim().equals(imagePath.trim()));

                // Join the remaining URIs back into a string
                String updatedUris = String.join(",", uriList);

                // Update the album object
                currentAlbum.imageUris = updatedUris;

                // Save the changes to the database
                albumDao.update(currentAlbum);

                Log.d("OfflineAlbumInspect", "Updated URIs: " + updatedUris);  // Log updated URIs for verification
            }
        }).start();
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
