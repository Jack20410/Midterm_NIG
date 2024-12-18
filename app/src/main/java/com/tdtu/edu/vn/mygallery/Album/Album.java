package com.tdtu.edu.vn.mygallery.Album;

import java.net.URI;

public class Album {
    private String imageUrls;
    private String id; // Firebase ID of the album
    private String albumName;
    // Default constructor required for calls to DataSnapshot.getValue(Album.class)
    public Album() {
    }

    public Album(String id, String albumName) {
        this.id = id;
        this.albumName = albumName;
    }

    public Album(String imageUrls, String id, String albumName) {
        this.imageUrls = imageUrls;
        this.id = id;
        this.albumName = albumName;
    }

    // Constructor with just the album name (ID will be set later)
    public Album(String albumName) {
        this.albumName = albumName;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getAlbumName() {
        return albumName;
    }
    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }
}
