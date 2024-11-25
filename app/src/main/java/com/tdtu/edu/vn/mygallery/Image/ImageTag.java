package com.tdtu.edu.vn.mygallery.Image;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "image_tags")
public class ImageTag {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String imagePath; // Path to the image
    public String tag; // Tag for the image
}