package com.tdtu.edu.vn.mygallery;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tdtu.edu.vn.mygallery.Fragment.FavoriteFragment;
import com.tdtu.edu.vn.mygallery.Fragment.LoginFragment;
import com.tdtu.edu.vn.mygallery.Fragment.MainFragment;
import com.tdtu.edu.vn.mygallery.Fragment.OfflineAlbumFragment;
import com.tdtu.edu.vn.mygallery.Fragment.SearchFragment;
import com.tdtu.edu.vn.mygallery.Notification.NotificationScheduler;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNavigationView();

        // Schedule notifications for reminders
        NotificationScheduler.scheduleRepeatingNotification(this);

        // Load the default fragment (MainFragment)
        if (savedInstanceState == null) {
            boolean selectLogin = getIntent().getBooleanExtra("select_login", false);
            if (selectLogin) {
                loadFragment(new LoginFragment());
            } else {
                loadFragment(new MainFragment());
            }
        }
    }

    private void setupBottomNavigationView() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.navigation_main:
                    selectedFragment = new MainFragment();
                    break;
                case R.id.navigation_offline_album:
                    selectedFragment = new OfflineAlbumFragment();
                    break;
                case R.id.navigation_favorite:
                    selectedFragment = new FavoriteFragment();
                    break;
                case R.id.navigation_login:
                    selectedFragment = new LoginFragment();
                    break;
                case R.id.navigation_search:
                    selectedFragment = new SearchFragment();
                    break;
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });

        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.navigation_main);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
