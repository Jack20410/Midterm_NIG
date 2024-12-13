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
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.tdtu.edu.vn.mygallery.Image.ImageInspectActivity;
import com.tdtu.edu.vn.mygallery.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_favorite, parent, false);
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

            // Set an OnClickListener to view the image
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ImageInspectActivity.class);
                intent.putExtra("IMAGE_PATH", imagePath);
                context.startActivity(intent);
            });

            // Set a long click listener to show the context menu
            holder.itemView.setOnLongClickListener(v -> {
                showContextMenu(v, position, imagePath);
                return true;
            });

        } else {
            Log.e("OfflineAlbumImageAdapter", "Image file not found: " + imagePath);
            holder.imageView.setImageResource(R.drawable.album_placeholder);
        }

        // Handle delete button click
//        holder.deleteButton.setOnClickListener(v -> {
//            new AlertDialog.Builder(context)
//                    .setTitle("Remove Picture from Album")
//                    .setMessage("Are you sure you want to remove this picture from the album?")
//                    .setPositiveButton("Yes", (dialog, which) -> {
//                        removeFromAlbum(imagePath);
//                        imagePaths.remove(position);
//                        notifyItemRemoved(position);
//                        notifyItemRangeChanged(position, imagePaths.size());
//                        Toast.makeText(context, "Image removed from album", Toast.LENGTH_SHORT).show();
//                    })
//                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
//                    .create()
//                    .show();
//        });
    }

    private void showContextMenu(View view, int position, String imagePath) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.image_context_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.remove_album) {
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
                return true;
            }
            return false;
        });
        popupMenu.show();
    }


    @Override
    public int getItemCount() {
        return (imagePaths != null) ? imagePaths.size() : 0;
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
//        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
//            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
