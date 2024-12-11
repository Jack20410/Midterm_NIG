package com.tdtu.edu.vn.mygallery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class PhotoLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ImageView photoView;
    private TextView photoSpecsTextView, phoneModelTextView, dateTextView;
    private GoogleMap map;
    private String photoPath;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_location);

        // Initialize views
        photoView = findViewById(R.id.photoView);
        photoSpecsTextView = findViewById(R.id.photoSpecsTextView);
        phoneModelTextView = findViewById(R.id.phoneModelTextView);
        dateTextView = findViewById(R.id.dateTextView);

        // Retrieve data from the intent
        Intent intent = getIntent();
        photoPath = intent.getStringExtra("photoPath");
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);

        Log.d("PhotoLocationActivity", "Received Lat: " + latitude + ", Long: " + longitude);

        // Load the photo
        if (photoPath != null && !photoPath.isEmpty()) {
            File imageFile = new File(photoPath);
            Glide.with(this)
                    .load(imageFile)
                    .placeholder(R.drawable.album_placeholder)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(photoView);

            // Extract photo specifications
            extractPhotoDetails(photoPath);
        } else {
            Toast.makeText(this, "Photo path is invalid", Toast.LENGTH_SHORT).show();
        }

        // Set up the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;

        if (latitude != 0 || longitude != 0) {
            LatLng location = new LatLng(latitude, longitude);
            map.addMarker(new MarkerOptions().position(location).title("Photo Location"));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
            Log.d("PhotoLocationActivity", "Marker added at Lat: " + latitude + ", Long: " + longitude);
        } else {
            Toast.makeText(this, "Cannot display location on map. Invalid coordinates.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Extracts photo details (specifications, phone model, date) and updates the UI.
     */
    private void extractPhotoDetails(String photoPath) {
        try {
            android.media.ExifInterface exif = new android.media.ExifInterface(photoPath);

            // Extract photo specifications
            String width = exif.getAttribute(android.media.ExifInterface.TAG_IMAGE_WIDTH);
            String height = exif.getAttribute(android.media.ExifInterface.TAG_IMAGE_LENGTH);
            String specs = (width != null && height != null) ? width + " x " + height + " pixels" : "Unknown";
            photoSpecsTextView.setText("Resolution: " + specs);

            // Extract phone model
            String make = exif.getAttribute(android.media.ExifInterface.TAG_MAKE);
            String model = exif.getAttribute(android.media.ExifInterface.TAG_MODEL);
            String phoneModel = (make != null ? make : "") + " " + (model != null ? model : "Unknown");
            phoneModelTextView.setText("Phone: " + phoneModel.trim());

            // Extract and format photo date
            String dateTaken = exif.getAttribute(android.media.ExifInterface.TAG_DATETIME);
            if (dateTaken != null) {
                // Convert the date to the desired format
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String formattedDate = outputFormat.format(inputFormat.parse(dateTaken));
                dateTextView.setText("Date Taken: " + formattedDate);
            } else {
                dateTextView.setText("Date Taken: Unknown");
            }

        } catch (Exception e) {
            Log.e("PhotoLocationActivity", "Error extracting photo details: " + e.getMessage());
            Toast.makeText(this, "Failed to retrieve photo details.", Toast.LENGTH_SHORT).show();
        }
    }
}
