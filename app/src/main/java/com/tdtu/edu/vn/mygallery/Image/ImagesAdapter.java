package com.tdtu.edu.vn.mygallery.Image;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.album_placeholder)
                .error(R.drawable.three_button)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.imageView);

        holder.bind(imageUrl);
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
            imageView = itemView.findViewById(R.id.imageView);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    showDeleteConfirmationDialog(position);
                }
            });
        }

        public void bind(String imageUrl) {
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ImageInspectActivity.class);
                intent.putExtra("IMAGE_PATH", imageUrl);
                context.startActivity(intent);
            });
        }

        private void showDeleteConfirmationDialog(int position) {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Picture")
                    .setMessage("Do you want to delete this picture?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (context instanceof ImagesDisplayActivity) {
                            ImagesDisplayActivity.deleteImageFromFirebase(imageUrls.get(position));
                        }

                        imageUrls.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, imageUrls.size());

                        Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        }
    }
}
