package com.tdtu.edu.vn.mygallery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
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

public class PhotoLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ImageView photoView;
    private GoogleMap map;
    private String photoPath;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_location);

        photoView = findViewById(R.id.photoView);

        Intent intent = getIntent();
        photoPath = intent.getStringExtra("photoPath");
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);

        Log.d("PhotoLocationActivity", "Received Lat: " + latitude + ", Long: " + longitude);

        if (latitude == 0 && longitude == 0) {
            Toast.makeText(this, "No valid location data available", Toast.LENGTH_SHORT).show();
        }

        if (photoPath != null && !photoPath.isEmpty()) {
            File imageFile = new File(photoPath);
            Glide.with(this)
                    .load(imageFile)
                    .placeholder(R.drawable.album_placeholder)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(photoView);
        } else {
            Toast.makeText(this, "Photo path is invalid", Toast.LENGTH_SHORT).show();
        }

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
}
