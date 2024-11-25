package com.tdtu.edu.vn.mygallery.Utilities;

import android.content.Context;
import android.util.Log;

import com.tdtu.edu.vn.mygallery.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManager {

    private static final String TAG = "FileManager";

    public static File getRecycleBinFolder(Context context) {
        File recycleBin = new File(context.getFilesDir(), "RecycleBin");
        if (!recycleBin.exists()) {
            boolean created = recycleBin.mkdir();
            Log.d(TAG, "Recycle Bin folder created: " + created);
        }
        return recycleBin;
    }

    public static File getFavoritesFolder(Context context) {
        File favorites = new File(context.getFilesDir(), "Favorites");
        if (!favorites.exists()) {
            boolean created = favorites.mkdir();
            Log.d(TAG, "Favorites folder created: " + created);
        }
        return favorites;
    }

    public static boolean moveFile(File sourceFile, File destinationFolder) {
        try {
            if (!destinationFolder.exists()) {
                boolean folderCreated = destinationFolder.mkdirs();
                Log.d("FileManager", "Destination folder created: " + folderCreated);
            }

            File destinationFile = new File(destinationFolder, sourceFile.getName());
            try (FileInputStream fis = new FileInputStream(sourceFile);
                 FileOutputStream fos = new FileOutputStream(destinationFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }

            // Delete source file after copying
            boolean deleted = sourceFile.delete();
            if (!deleted) {
                Log.e("FileManager", "Failed to delete source file: " + sourceFile.getAbsolutePath());
            }

            return true;
        } catch (IOException e) {
            Log.e("FileManager", "Error moving file: " + e.getMessage(), e);
            return false;
        }
    }


    public static boolean copyFile(File source, File destinationFolder) {
        if (!destinationFolder.exists() && !destinationFolder.mkdirs()) {
            Log.e(TAG, "Failed to create destination folder: " + destinationFolder.getAbsolutePath());
            return false;
        }

        File destinationFile = new File(destinationFolder, source.getName());
        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(destinationFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error copying file: " + e.getMessage(), e);
            return false;
        }
    }

    public static void createAppFolders(MainActivity mainActivity) {
    }
}
