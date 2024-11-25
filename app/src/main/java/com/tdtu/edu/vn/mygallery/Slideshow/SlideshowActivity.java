package com.tdtu.edu.vn.mygallery.Slideshow;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.tdtu.edu.vn.mygallery.R;

import java.util.ArrayList;

public class SlideshowActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private SlideshowAdapter slideshowAdapter;
    private ArrayList<String> imagePaths;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private int currentPosition = 0; // Track the current image
    private static final int SLIDE_DELAY = 3000; // 3 seconds per slide
    private Button stopButton; // Stop button to stop the slideshow and music

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slideshow);

        // Get the image paths from the intent
        Intent intent = getIntent();
        imagePaths = intent.getStringArrayListExtra("imagePaths");

        if (imagePaths == null || imagePaths.isEmpty()) {
            Log.d("SlideshowActivity", "No images passed to the slideshow.");
            Toast.makeText(this, "No images to display in the slideshow", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if no images
            return;
        } else {
            Log.d("SlideshowActivity", "Number of images passed: " + imagePaths.size());

            // Set up ViewPager2 for slideshow
            viewPager = findViewById(R.id.viewPager);
            slideshowAdapter = new SlideshowAdapter(imagePaths, this);
            viewPager.setAdapter(slideshowAdapter);

            // Set up Now Playing Text
            TextView nowPlayingText = findViewById(R.id.nowPlayingText);
            nowPlayingText.setText("Now Playing: Background Music");

            // Start the background music
            mediaPlayer = MediaPlayer.create(this, R.raw.theme); // background_music.mp3 should be in res/raw
            mediaPlayer.setLooping(true); // Loop the music
            mediaPlayer.start();

            // Start the slideshow
            startSlideshow();

            // Set up the Stop Button to stop the slideshow and music
            stopButton = findViewById(R.id.stopButton);
            stopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopSlideshow();
                }
            });
        }
    }

    private void startSlideshow() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentPosition < slideshowAdapter.getItemCount()) {
                    viewPager.setCurrentItem(currentPosition, true); // Move to the next item
                    currentPosition++;
                    handler.postDelayed(this, SLIDE_DELAY); // Continue the slideshow
                } else {
                    // If we reach the end of the list, loop back to the start
                    currentPosition = 0;
                    handler.postDelayed(this, SLIDE_DELAY);
                }
            }
        }, SLIDE_DELAY);
    }

    private void stopSlideshow() {
        // Stop the music
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // Stop the slideshow
        handler.removeCallbacksAndMessages(null); // Stop the handler

        // Show a message and finish the activity
        Toast.makeText(SlideshowActivity.this, "Slideshow stopped", Toast.LENGTH_SHORT).show();
        finish(); // Close the activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the music when the activity is destroyed
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        handler.removeCallbacksAndMessages(null); // Stop the handler
    }
}
