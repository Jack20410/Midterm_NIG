package com.tdtu.edu.vn.mygallery;

public class ImageData {
    private String imagePath;
    private String dateTaken;
    private String tag;

    // Add a constructor and getter/setter for tags
    public ImageData(String imagePath, String dateTaken, String tag) {
        this.imagePath = imagePath;
        this.dateTaken = dateTaken;
        this.tag = tag;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getDateTaken() {
        return dateTaken;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}

