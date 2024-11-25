package com.tdtu.edu.vn.mygallery.Album;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "offlinealbum")
public class OfflineAlbum {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;

    // Add a field to hold image URIs (as a comma-separated string for simplicity)
    public String imageUris; // e.g., "uri1,uri2,uri3"

    public OfflineAlbum(String name) {
        this.name = name;
        this.imageUris = ""; // Initialize as empty
    }

    // Method to add an image URI
    public void addImageUri(String uri) {
        if (imageUris == null || imageUris.isEmpty()) {
            imageUris = uri; // First image
        } else {
            imageUris += "," + uri; // Append new URI
        }
    }


}



