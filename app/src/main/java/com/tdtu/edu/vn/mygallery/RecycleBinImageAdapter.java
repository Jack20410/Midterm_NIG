package com.tdtu.edu.vn.mygallery;
import android.app.Activity; // Import the correct Activity class
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
public class RecycleBinImageAdapter extends RecyclerView.Adapter<RecycleBinImageAdapter.ImageViewHolder> {

    private List<String> imagePaths;
    private Context context;

    public RecycleBinImageAdapter(List<String> imagePaths, Context context) {
        this.imagePaths = imagePaths;
        this.context = context;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycle_bin_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);
        Glide.with(context)
                .load(new File(imagePath))
                .placeholder(R.drawable.album_placeholder) // Placeholder while loading
                .error(R.drawable.three_button) // Fallback if loading fails
                .into(holder.imageView);

        holder.restoreButton.setOnClickListener(v -> {
            restoreImage(imagePath);
            imagePaths.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, imagePaths.size());
            Toast.makeText(context, "Image restored", Toast.LENGTH_SHORT).show();
        });

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Permanently Delete Picture")
                    .setMessage("Are you sure you want to permanently delete this picture?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        File file = new File(imagePath);
                        if (file.exists() && file.delete()) {
                            removeFromRecycleBin(imagePath);
                            imagePaths.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, imagePaths.size());
                            Toast.makeText(context, "Image permanently deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to delete image", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    private void restoreImage(String imagePath) {
        File sourceFile = new File(imagePath);
        File destinationDir = new File("/storage/emulated/0/DCIM/Restored");

        if (!destinationDir.exists() && !destinationDir.mkdirs()) {
            Toast.makeText(context, "Failed to create destination directory", Toast.LENGTH_SHORT).show();
            return;
        }

        File destinationFile = new File(destinationDir, sourceFile.getName());

        try {
            // Use Files.move() for a more reliable file operation
            Files.move(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Log.d("RecycleBinAdapter", "File moved to: " + destinationFile.getAbsolutePath());

            // Scan the restored image so it appears in the gallery
            MediaScannerConnection.scanFile(
                    context,
                    new String[]{destinationFile.getAbsolutePath()},
                    null,
                    (path, uri) -> {
                        Log.d("RecycleBinAdapter", "Image restored and scanned: " + path);

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("restoredImagePath", destinationFile.getAbsolutePath());

                        if (context instanceof RecycleBinActivity) {
                            ((RecycleBinActivity) context).setResult(Activity.RESULT_OK, resultIntent);
                            ((RecycleBinActivity) context).finish(); // Close RecycleBinActivity
                        }
                    }
            );

            // Remove from recycle bin SharedPreferences
            removeFromRecycleBin(imagePath);
        } catch (Exception e) {
            Log.e("RecycleBinAdapter", "Failed to move file", e);
            Toast.makeText(context, "Failed to restore image", Toast.LENGTH_SHORT).show();
        }
    }




    private void removeFromRecycleBin(String imagePath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("RecycleBin", Context.MODE_PRIVATE);
        String existingPaths = sharedPreferences.getString("deletedImages", "");

        // Split paths and remove duplicates or empty entries
        List<String> pathList = new ArrayList<>();
        for (String path : existingPaths.split(";")) {
            if (!path.trim().isEmpty() && !path.equals(imagePath)) {
                pathList.add(path);
            }
        }

        // Join the paths back and save them
        String updatedPaths = String.join(";", pathList);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("deletedImages", updatedPaths);
        editor.apply();
    }


    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton deleteButton, restoreButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            restoreButton = itemView.findViewById(R.id.restoreButton);
        }
    }
}

