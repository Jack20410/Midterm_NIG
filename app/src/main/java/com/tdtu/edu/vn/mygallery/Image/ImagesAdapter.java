package com.tdtu.edu.vn.mygallery.Image;

import static com.tdtu.edu.vn.mygallery.Image.ImagesDisplayActivity.deleteImageFromFirebase;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageButton;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.tdtu.edu.vn.mygallery.R;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_favorite, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        // Log the URL to ensure it's correct
        Log.d("ImagesAdapter", "Loading image URL: " + imageUrl);

        // Use Glide to load images with smooth transitions and proper error handling
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.album_placeholder)
                .error(R.drawable.three_button)
                .transition(DrawableTransitionOptions.withCrossFade())  // Smooth transition
                .into(holder.imageView);

        // Set up click listener to inspect the image
//        holder.bind(imageUrl);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ImageInspectActivity.class);
            intent.putExtra("IMAGE_PATH", imageUrl);
            context.startActivity(intent);
        });

        // Add a long-click listener to show the contextual menu
        holder.itemView.setOnLongClickListener(v -> {
            showContextMenu(v, position);
            return true; // Indicate the event is consumed
        });
    }

    private void showContextMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.image_context_menu_online, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.remove_img_online) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Picture")
                        .setMessage("Do you want to delete this picture?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Log.d("ImagesAdapter", "Deleting image at position: " + position);

                            // Remove the image from Firebase or the list (update this logic as needed)
                            deleteImageFromFirebase(imageUrls.get(position));
                            imageUrls.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, imageUrls.size());
                            Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show();
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
    public void onViewRecycled(@NonNull ImageViewHolder holder) {
        super.onViewRecycled(holder);

        // Clear the Glide request when the view is recycled to free memory
        Glide.with(context).clear(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);  // Ensure this matches your XML layout ID
        }
        public void bind(String imageUrl) {
            itemView.setOnClickListener(v -> {
                // Log the imagePath to check if it's null or valid
                Log.d("ImageAdapter", "Image path: " + imageUrl);

                // Navigate to ImageInspectActivity with the clicked image's path
                Intent intent = new Intent(context, ImageInspectActivity.class);
                intent.putExtra("IMAGE_PATH", imageUrl);  // Pass the image path to the activity
                context.startActivity(intent);
            });
        }

//        private void showDeleteConfirmationDialog(int position) {
//            // Create an AlertDialog to confirm deletion
//            new AlertDialog.Builder(context)
//                    .setTitle("Delete Picture")
//                    .setMessage("Do you want to delete this picture?")
//                    .setPositiveButton("Yes", (dialog, which) -> {
//                        Log.d("ImageAdapter", "Deleting image at position: " + position);
//
//                        // Remove the image from Firebase
//
//
//                        // Remove the image from the list and notify RecyclerView
//                        imageUrls.remove(position);
//                        notifyItemRemoved(position);
//                        notifyItemRangeChanged(position, imageUrls.size());
//
//                        Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show();
//                    })
//                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
//                    .create()
//                    .show();
//        }
    }
}
