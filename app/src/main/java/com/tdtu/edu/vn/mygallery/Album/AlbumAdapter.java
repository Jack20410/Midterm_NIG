package com.tdtu.edu.vn.mygallery.Album;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tdtu.edu.vn.mygallery.R;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
    private List<Album> albumList;
    private OnAlbumActionListener albumActionListener;

    public interface OnAlbumActionListener {
        void onRenameAlbum(Album album);
        void onDeleteAlbum(Album album);
        void onAlbumClick(Album album);
    }

    public AlbumAdapter(List<Album> albumList, OnAlbumActionListener albumActionListener) {
        this.albumList = albumList;
        this.albumActionListener = albumActionListener;
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
        holder.albumName.setText(album.getAlbumName());

        // Handle clicking the entire album item
        holder.itemView.setOnClickListener(v -> {
            if (albumActionListener != null) {
                albumActionListener.onAlbumClick(album);
            }
        });

        // Existing rename and delete listeners
        holder.renameButton.setOnClickListener(v -> {
            if (albumActionListener != null) {
                albumActionListener.onRenameAlbum(album);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (albumActionListener != null) {
                albumActionListener.onDeleteAlbum(album);
            }
        });
    }


    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        TextView albumName;
        ImageButton renameButton, deleteButton;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumName = itemView.findViewById(R.id.albumName);
            renameButton = itemView.findViewById(R.id.renameButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
