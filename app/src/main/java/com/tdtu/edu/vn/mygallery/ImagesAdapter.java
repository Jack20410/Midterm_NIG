package com.tdtu.edu.vn.mygallery;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
    }
        @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView); // Ensure this matches your XML layout ID
        }
    }

}
