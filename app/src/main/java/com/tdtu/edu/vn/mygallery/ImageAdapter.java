package com.tdtu.edu.vn.mygallery;

import android.content.Context;
import android.media.MediaScannerConnection;
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
                    .setMessage("Do you want to delete this picture?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Proceed with the deletion if the user confirms
                        File file = new File(imagePath);
                        if (file.exists()) {
                            if (file.delete()) {
                                // Rescan the media file so the system knows it is deleted
                                MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);

                                // Remove the item from the list
                                imagePaths.remove(position);

                                // Notify the adapter about the removal
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, imagePaths.size());

                                Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                // If file deletion fails, show a toast (optional)
                                Toast.makeText(context, "Failed to delete image", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // If the user presses No, dismiss the dialog and do nothing
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        });

    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
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
