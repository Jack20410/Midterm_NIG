package com.tdtu.edu.vn.mygallery.Album;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tdtu.edu.vn.mygallery.R;
import com.tdtu.edu.vn.mygallery.Utilities.AppDatabase;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;

public class OfflineAlbumRecyclerAdapter extends RecyclerView.Adapter<OfflineAlbumRecyclerAdapter.ViewHolder> {

    private List<OfflineAlbum> albumList;
    private Context context;
    private AppDatabase db;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(OfflineAlbum album);
    }

    public OfflineAlbumRecyclerAdapter(List<OfflineAlbum> albumList, Context context, AppDatabase db, OnItemClickListener listener) {
        this.albumList = albumList;
        this.context = context;
        this.db = db;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OfflineAlbum album = albumList.get(position);

        holder.albumName.setText(album.name);

        if (album.imageUris != null && !album.imageUris.isEmpty()) {
            String[] imageUris = album.imageUris.split(",");
            String firstImagePath = imageUris[0].trim();

            File imageFile = new File(firstImagePath);
            if (imageFile.exists()) {
                Glide.with(context)
                        .load(imageFile)
                        .placeholder(R.drawable.album_placeholder)
                        .into(holder.albumImage);
            } else {
                holder.albumImage.setImageResource(R.drawable.album_placeholder);
            }
        } else {
            holder.albumImage.setImageResource(R.drawable.album_placeholder);
        }

        // Handle item click
        holder.itemView.setOnClickListener(v -> listener.onItemClick(album));

        // Rename button
        holder.renameButton.setOnClickListener(v -> showRenameDialog(album));

        // Delete button
        holder.deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(album));
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public void updateAlbumList(List<OfflineAlbum> newAlbumList) {
        this.albumList = newAlbumList;
        notifyDataSetChanged();
    }

    private void showRenameDialog(OfflineAlbum album) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Rename Album");

        final EditText input = new EditText(context);
        input.setText(album.name);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newAlbumName = input.getText().toString();
            if (!newAlbumName.isEmpty()) {
                renameAlbum(album, newAlbumName);
            } else {
                Toast.makeText(context, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDeleteConfirmationDialog(OfflineAlbum album) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Album")
                .setMessage("Are you sure you want to delete the album '" + album.name + "'?")
                .setPositiveButton("Yes", (dialog, which) -> deleteAlbum(album))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAlbum(OfflineAlbum album) {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.offlineAlbumDao().delete(album);
            ((AppCompatActivity) context).runOnUiThread(() -> {
                albumList.remove(album);
                notifyDataSetChanged();
                Toast.makeText(context, "Album deleted", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void renameAlbum(OfflineAlbum album, String newAlbumName) {
        Executors.newSingleThreadExecutor().execute(() -> {
            album.name = newAlbumName;
            db.offlineAlbumDao().update(album);
            ((AppCompatActivity) context).runOnUiThread(() -> {
                notifyDataSetChanged();
                Toast.makeText(context, "Album renamed to: " + newAlbumName, Toast.LENGTH_SHORT).show();
            });
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView albumImage;
        TextView albumName;
        ImageButton renameButton, deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            albumImage = itemView.findViewById(R.id.albumImage);
            albumName = itemView.findViewById(R.id.albumName);
            renameButton = itemView.findViewById(R.id.renameButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
