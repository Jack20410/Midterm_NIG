package com.tdtu.edu.vn.mygallery;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageButton;
import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImageViewHolder> {
    private List<String> imageUrls;
    private Context context;

    public ImagesAdapter(List<String> imageUrls, Context context) {
        this.imageUrls = imageUrls;
        this.context = context;
    }
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override

    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        // Log the URL to ensure it's correct
        Log.d("ImagesAdapter", "Loading image URL: " + imageUrl);

        // Use Glide or Picasso to load images from URLs
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.album_placeholder)
                .error(R.drawable.three_button)
                .into(holder.imageView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log the imagePath to check if it's null or valid
                Log.d("ImageAdapter", "Image path: " + imageUrl);

                // Navigate to ImagesInspectActivity with the clicked image's path
                Intent intent = new Intent(context, ImageInspectActivity.class);
                intent.putExtra("IMAGE_PATH", imageUrl); // Pass the image path to the activity

                // Start the activity
                context.startActivity(intent);
            }
        });
        holder.deleteButton.setOnClickListener(v -> {
            // Create an AlertDialog to confirm deletion
            new AlertDialog.Builder(context)
                    .setTitle("Delete Picture")
                    .setMessage("Do you want to delete this picture?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Log.d("ImageAdapter", "Deleting image at position: " + position);

                        // First, remove the image entry from Firebase Realtime Database
                        ImagesDisplayActivity.deleteImageFromFirebase(imageUrl);

                        // Remove the image from the list and notify the RecyclerView
                        imageUrls.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, imageUrls.size());

                        Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show();
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
        return imageUrls.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton deleteButton;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);  // Ensure this matches your XML layout ID
            deleteButton = itemView.findViewById(R.id.deleteButton);  // Reference the delete button
        }
    }

}
