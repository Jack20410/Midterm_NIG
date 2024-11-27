package com.tdtu.edu.vn.mygallery.Album;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tdtu.edu.vn.mygallery.Utilities.AppDatabase;
import com.tdtu.edu.vn.mygallery.R;

import java.util.List;
import java.util.concurrent.Executors;

public class OfflineAlbumListAdapter extends ArrayAdapter<OfflineAlbum> {
    private AppDatabase db;

    public OfflineAlbumListAdapter(Context context, List<OfflineAlbum> albums, AppDatabase db) {
        super(context, 0, albums);
        this.db = db;  // Initialize the database instance
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the album for this position
        OfflineAlbum album = getItem(position);

        // Check if the existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_album, parent, false);
        }

        TextView albumName = convertView.findViewById(R.id.albumName);
        ImageButton renameButton = convertView.findViewById(R.id.renameButton);
        ImageButton deleteButton = convertView.findViewById(R.id.deleteButton);
        albumName.setText(album.name);

        // Handle item click (clicking on album to open)
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), OfflineAlbumInspect.class);
            intent.putExtra("albumId", album.id); // Pass album ID to the next activity
            getContext().startActivity(intent);
        });

        // Handle rename button click
        renameButton.setOnClickListener(v -> {
            showRenameDialog(album);
        });

        // Handle delete button click
        deleteButton.setOnClickListener(v -> {
            showDeleteConfirmationDialog(album);
        });

        return convertView;
    }

    // Helper method to get the album at a specific position
    public OfflineAlbum getAlbumAtPosition(int position) {
        OfflineAlbum album = getItem(position);
        if (album == null) {
            Log.e("OfflineAlbumListAdapter", "Album at position " + position + " is null");
        }
        return album;
    }


    private void showRenameDialog(OfflineAlbum album) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Rename Album");

        final EditText input = new EditText(getContext());
        input.setText(album.name);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newAlbumName = input.getText().toString();
            if (!newAlbumName.isEmpty()) {
                renameAlbum(album, newAlbumName);
            } else {
                Toast.makeText(getContext(), "Album name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDeleteConfirmationDialog(OfflineAlbum album) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Album")
                .setMessage("Are you sure you want to delete the album '" + album.name + "'?")
                .setPositiveButton("Yes", (dialog, which) -> deleteAlbum(album))
                .setNegativeButton("No", null)
                .show();
    }

    // Method to delete the album from the database
    private void deleteAlbum(OfflineAlbum album) {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.offlineAlbumDao().delete(album); // Assuming you have a delete method in the DAO
            // Update the UI after deleting
            runOnUiThread(() -> {
                remove(album); // Remove the album from the adapter
                notifyDataSetChanged(); // Refresh the adapter
                Toast.makeText(getContext(), "Album deleted", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void runOnUiThread(Runnable action) {
        ((AppCompatActivity) getContext()).runOnUiThread(action);
    }

    // Method to rename the album in the database
    private void renameAlbum(OfflineAlbum album, String newAlbumName) {
        Executors.newSingleThreadExecutor().execute(() -> {
            album.name = newAlbumName;
            db.offlineAlbumDao().update(album); // Assuming you have an update method in the DAO
            // Update the UI after renaming
            runOnUiThread(() -> {
                notifyDataSetChanged(); // Refresh the adapter
                Toast.makeText(getContext(), "Album renamed to: " + newAlbumName, Toast.LENGTH_SHORT).show();
            });
        });
    }
}
