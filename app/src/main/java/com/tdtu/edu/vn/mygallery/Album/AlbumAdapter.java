package com.tdtu.edu.vn.mygallery.Album;

import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;
import android.widget.ImageButton;
import android.content.Intent;

import com.bumptech.glide.Glide;
import com.tdtu.edu.vn.mygallery.Image.ImagesDisplayActivity;
import com.tdtu.edu.vn.mygallery.OnlineActivity;
import com.tdtu.edu.vn.mygallery.R;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
    private List<Album> albumList;
    private OnlineActivity onlineActivity;  // Reference to MainActivity
    // Constructor to pass the MainActivity reference
    public AlbumAdapter(List<Album> albumList, OnlineActivity onlineActivity) {
        this.albumList = albumList;
        this.onlineActivity = onlineActivity;  // Assign the MainActivity reference

    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albumList.get(position);

        // Set album name
        holder.albumName.setText(album.getAlbumName());

        // Load the image from Firebase URL
        if (album.getImageUrls() != null && !album.getImageUrls().isEmpty()) {
            // Use Glide to load the image URI from Firebase
            Glide.with(onlineActivity)
                    .load(album.getImageUrls())  // Use the Firebase URL directly
                    .placeholder(R.drawable.album_placeholder)
                    .into(holder.albumImage);
        } else {
            // Display a placeholder if no image URI is available
            holder.albumImage.setImageResource(R.drawable.album_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(onlineActivity, ImagesDisplayActivity.class);
            intent.putExtra("albumId", album.getId()); // Pass the album ID to the next activity
            onlineActivity.startActivity(intent);
        });

        // Set rename button click listener
        holder.renameButton.setOnClickListener(v -> {
            // Show dialog to rename the album
            onlineActivity.showRenameDialog(album);

        });

        // Set delete button click listener
        holder.deleteButton.setOnClickListener(v -> {
            // Confirm deletion with a dialog
            new AlertDialog.Builder(onlineActivity)
                    .setTitle("Delete Album")
                    .setMessage("Are you sure you want to delete " + album.getAlbumName() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Delete album
                        onlineActivity.deleteAlbumFromFirebase(album.getId());
                    })
                    .setNegativeButton("No", null)
                    .show();

        });
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }
    // ViewHolder class

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView albumImage;
        TextView albumName;
        ImageButton renameButton, deleteButton;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumImage = itemView.findViewById(R.id.albumImage);
            albumName = itemView.findViewById(R.id.albumName);
            renameButton = itemView.findViewById(R.id.renameButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
