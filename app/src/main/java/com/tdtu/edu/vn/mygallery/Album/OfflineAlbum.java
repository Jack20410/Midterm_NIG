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

    // New fields for GPS coordinates
    private Double latitude; // GPS Latitude
    private Double longitude; // GPS Longitude

    // Constructor
    public OfflineAlbum(String name) {
        this.name = name;
        this.imageUris = ""; // Initialize as empty
    }

    // Getter for latitude
    public Double getLatitude() {
        return latitude;
    }

    // Setter for latitude
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    // Getter for longitude
    public Double getLongitude() {
        return longitude;
    }

    // Setter for longitude
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    // Method to add an image URI
    public void addImageUri(String uri) {
        if (imageUris == null || imageUris.isEmpty()) {
            imageUris = uri; // First image
        } else {
            imageUris += "," + uri; // Append new URI
        }
    }

    // Check if the album has location data
    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }
}
