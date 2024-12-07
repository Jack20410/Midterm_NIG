package com.tdtu.edu.vn.mygallery.Utilities;

import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;

public class ExifUtils {

    private static final String TAG = "ExifUtils";

    /**
     * Extracts latitude and longitude from the given image file path.
     *
     * @param imagePath The file path of the image.
     * @return A float array containing latitude and longitude, or null if not available.
     */
    public static float[] getLatLongFromImage(String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            float[] latLong = new float[2];

            if (exif.getLatLong(latLong)) {
                Log.d(TAG, "Latitude: " + latLong[0] + ", Longitude: " + latLong[1]);
                return latLong;
            } else {
                Log.d(TAG, "No location data found in the image EXIF metadata.");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading EXIF data from image: " + imagePath, e);
        }
        return null;
    }
}
