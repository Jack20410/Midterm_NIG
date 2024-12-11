package com.tdtu.edu.vn.mygallery.Utilities;

import android.content.Context;
import android.media.MediaScannerConnection;
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
import com.tdtu.edu.vn.mygallery.R;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class RecycleBinImageAdapter extends RecyclerView.Adapter<RecycleBinImageAdapter.ImageViewHolder> {

    private final List<String> imagePaths;
    private final Context context;

    public RecycleBinImageAdapter(List<String> imagePaths, Context context) {
        this.imagePaths = imagePaths;
        this.context = context;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycle_bin_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);
        Glide.with(context)
                .load(new File(imagePath))
                .placeholder(R.drawable.album_placeholder)
                .error(R.drawable.three_button)
                .into(holder.imageView);

        // Restore Button Action
        holder.restoreButton.setOnClickListener(v -> restoreImage(imagePath, position));

        // Delete Button Action
        holder.deleteButton.setOnClickListener(v -> new AlertDialog.Builder(context)
                .setTitle("Permanently Delete Picture")
                .setMessage("Are you sure you want to permanently delete this picture?")
                .setPositiveButton("Yes", (dialog, which) -> deleteImage(imagePath, position))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show());
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    private void restoreImage(String imagePath, int position) {
        File sourceFile = new File(imagePath);
        File destinationDir = new File(context.getExternalFilesDir(null), "Restored");

        if (!destinationDir.exists() && !destinationDir.mkdirs()) {
            Toast.makeText(context, "Failed to create destination directory", Toast.LENGTH_SHORT).show();
            return;
        }

        File destinationFile = new File(destinationDir, sourceFile.getName());
        try {
            // Move the file to the Restored directory
            Files.move(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Log.d("RecycleBinAdapter", "File restored to: " + destinationFile.getAbsolutePath());

            // Notify the media scanner about the restored file
            MediaScannerConnection.scanFile(
                    context,
                    new String[]{destinationFile.getAbsolutePath()},
                    null,
                    (path, uri) -> Log.d("RecycleBinAdapter", "Image restored and scanned: " + path)
            );

            // Remove the file from the adapter's list
            imagePaths.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, imagePaths.size());

            // Notify the RecycleBinActivity to refresh the UI
            if (context instanceof RecycleBinActivity) {
                ((RecycleBinActivity) context).reloadDeletedImages();
            }

            Toast.makeText(context, "Image restored successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("RecycleBinAdapter", "Failed to restore image", e);
            Toast.makeText(context, "Failed to restore image", Toast.LENGTH_SHORT).show();
        }
    }


    private void deleteImage(String imagePath, int position) {
        File file = new File(imagePath);
        if (file.exists() && file.delete()) {
            imagePaths.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, imagePaths.size());
            Toast.makeText(context, "Image permanently deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to delete image", Toast.LENGTH_SHORT).show();
        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton deleteButton, restoreButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            restoreButton = itemView.findViewById(R.id.restoreButton);
        }
    }
}
