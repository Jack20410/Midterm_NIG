package com.tdtu.edu.vn.mygallery;

public class ImageData {
        private String imagePath;
        private String dateTaken;
        private String latitude;
        private String longitude;

        public ImageData(String imagePath, String dateTaken, String latitude, String longitude) {
            this.imagePath = imagePath;
            this.dateTaken = dateTaken;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getImagePath() { return imagePath; }
        public String getDateTaken() { return dateTaken; }
        public String getLatitude() { return latitude; }
        public String getLongitude() { return longitude; }

}
