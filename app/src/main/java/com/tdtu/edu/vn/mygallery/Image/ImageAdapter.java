package com.tdtu.edu.vn.mygallery.Image;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tdtu.edu.vn.mygallery.R;

import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private final List<String> imagePaths;
    private final Context context;
    private final OnImageClickListener imageClickListener;

    // Constructor
    public ImageAdapter(List<String> imagePaths, Context context, OnImageClickListener imageClickListener) {
        this.imagePaths = imagePaths;
        this.context = context;
        this.imageClickListener = imageClickListener;
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
        if (imageFile.exists()) {
            try {
                Glide.with(context)
                        .load(imageFile)
                        .placeholder(R.drawable.album_placeholder)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(holder.imageView);
            } catch (Exception e) {
                Log.e("ImageAdapter", "Error loading image: " + imagePath, e);
                Toast.makeText(context, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("ImageAdapter", "Image file not found at position " + position + ": " + imagePath);
            Toast.makeText(context, "Image file not found", Toast.LENGTH_SHORT).show();
        }

        // Set image click listener
        holder.imageView.setOnClickListener(v -> {
            if (imageClickListener != null) {
                imageClickListener.onImageClicked(imagePath);
            } else {
                Log.w("ImageAdapter", "No ImageClickListener defined for position " + position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        Glide.with(context).clear(holder.imageView);
        super.onViewRecycled(holder);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    // Interface for image click handling
    public interface OnImageClickListener {
        void onImageClicked(String imagePath);
    }
}
