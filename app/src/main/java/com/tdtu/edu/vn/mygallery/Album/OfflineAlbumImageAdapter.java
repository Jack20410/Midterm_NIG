package com.tdtu.edu.vn.mygallery.Album;

import android.content.Context;
import android.content.Intent;
import android.media.ExifInterface;
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
import com.google.android.gms.maps.model.LatLng;
import com.tdtu.edu.vn.mygallery.PhotoLocationActivity;
import com.tdtu.edu.vn.mygallery.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OfflineAlbumImageAdapter extends RecyclerView.Adapter<OfflineAlbumImageAdapter.ViewHolder> {

    private List<String> imagePaths;
    private Context context;
    private OfflineAlbumDao     albumDao;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);
        File imageFile = new File(imagePath);

        // Load the image into the ImageView
        if (imageFile.exists()) {
            Glide.with(context)
                    .load(imageFile)
                    .placeholder(R.drawable.album_placeholder) // Fallback image
                    .thumbnail(0.1f) // Load a low-resolution thumbnail first
                    .centerCrop()    // Ensure the image fits properly
                    .into(holder.imageView);

            // Check if the photo has location data
            LatLng location = getPhotoLocation(imagePath);
            if (location != null) {
                holder.locationIcon.setVisibility(View.VISIBLE); // Show location icon
            } else {
                holder.locationIcon.setVisibility(View.GONE); // Hide location icon
            }

            // Set an OnClickListener on the image view
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, PhotoLocationActivity.class);
                intent.putExtra("photoPath", imagePath);

                if (location != null) {
                    intent.putExtra("latitude", location.latitude);
                    intent.putExtra("longitude", location.longitude);
                } else {
                    Toast.makeText(context, "No location data available for this photo", Toast.LENGTH_SHORT).show();
                }

                context.startActivity(intent);
            });
        } else {
            Log.e("OfflineAlbumImageAdapter", "Image file not found: " + imagePath);
            holder.imageView.setImageResource(R.drawable.album_placeholder);
            holder.locationIcon.setVisibility(View.GONE); // Hide location icon for invalid images
        }

        // Handle delete button click
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Remove Picture from Album")
                    .setMessage("Are you sure you want to remove this picture from the album?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        removeFromAlbum(imagePath);
                        imagePaths.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, imagePaths.size());
                        Toast.makeText(context, "Image removed from album", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return (imagePaths != null) ? imagePaths.size() : 0;
    }

    // Extract location data from EXIF metadata
    private LatLng getPhotoLocation(String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            float[] latLong = new float[2];
            if (exif.getLatLong(latLong)) {
                return new LatLng(latLong[0], latLong[1]);
            }
        } catch (Exception e) {
            Log.e("OfflineAlbumImageAdapter", "Error reading EXIF data: " + e.getMessage());
        }
        return null;
    }

    private synchronized void removeFromAlbum(String imagePath) {
        new Thread(() -> {
            if (currentAlbum != null && currentAlbum.imageUris != null) {
                List<String> uriList = new ArrayList<>(List.of(currentAlbum.imageUris.split(",")));
                uriList.removeIf(uri -> uri.trim().equals(imagePath.trim()));
                String updatedUris = String.join(",", uriList);
                currentAlbum.imageUris = updatedUris;
                albumDao.update(currentAlbum);
            }
        }).start();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton deleteButton;
        ImageView locationIcon; // Add an optional icon for location metadata

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            locationIcon = itemView.findViewById(R.id.locationIcon); // Add this to your item_image.xml
        }
    }
}
