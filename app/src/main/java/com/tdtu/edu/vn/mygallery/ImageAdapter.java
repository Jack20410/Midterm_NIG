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

        // Handle the delete button click
        holder.deleteButton.setOnClickListener(v -> {
            File file = new File(imagePath);
            if (file.exists()) {
                if (file.delete()) {
                    // Rescan the media file so the system knows it is deleted
                    MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);

                    // Remove the item from the list
                    imagePaths.remove(position);
                    // Notify the adapter about the removal
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, imagePaths.size());  // Update the remaining items
                    Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show();
                } else {
                    // If file deletion fails, show a toast (optional)
                    Toast.makeText(context, "Failed to delete image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show();
            }
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
