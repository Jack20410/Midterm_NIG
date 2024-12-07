package com.tdtu.edu.vn.mygallery;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tdtu.edu.vn.mygallery.Fragment.FavoriteFragment;
import com.tdtu.edu.vn.mygallery.Fragment.LoginFragment;
import com.tdtu.edu.vn.mygallery.Fragment.MainFragment;
import com.tdtu.edu.vn.mygallery.Fragment.OfflineAlbumFragment;
import com.tdtu.edu.vn.mygallery.Fragment.SearchFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNavigationView();

        // Load the default fragment (MainFragment)
        if (savedInstanceState == null) {
            // Check if the "select_login" extra is passed
            boolean selectLogin = getIntent().getBooleanExtra("select_login", false);
            if (selectLogin) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_login);
            } else {
                // Default to MainFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new MainFragment())
                        .commit();
            }
        }
    }

    private void setupBottomNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.navigation_main:
                    selectedFragment = new MainFragment();
                    break;
                case R.id.navigation_offline_album:
                    selectedFragment = new OfflineAlbumFragment(); // Define this fragment similarly
                    break;
                case R.id.navigation_favorite:
                    selectedFragment = new FavoriteFragment(); // Define this fragment similarly
                    break;
                case R.id.navigation_login:
                    selectedFragment = new LoginFragment(); // Define this fragment similarly
                    break;
                case R.id.navigation_search:
                    selectedFragment = new SearchFragment(); // Define this fragment similarly
                    break;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.navigation_main);
    }
}
