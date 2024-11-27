package com.tdtu.edu.vn.mygallery.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tdtu.edu.vn.mygallery.Album.OfflineAlbum;
import com.tdtu.edu.vn.mygallery.Album.OfflineAlbumInspect;
import com.tdtu.edu.vn.mygallery.Album.OfflineAlbumListAdapter;
import com.tdtu.edu.vn.mygallery.R;
import com.tdtu.edu.vn.mygallery.Utilities.AppDatabase;

import java.util.List;
import java.util.concurrent.Executors;

public class OfflineAlbumFragment extends Fragment {

    private AppDatabase db;
    private ListView albumListView;
    private EditText albumNameInput;
    private OfflineAlbumListAdapter adapter;
    private GestureDetector gestureDetector;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activity_offline_album, container, false);

        // Initialize the UI components
        initUI(view);

        // Initialize database and load albums
        Executors.newSingleThreadExecutor().execute(() -> {
            db = AppDatabase.getInstance(requireContext());
            loadAlbums();
        });

        return view;
    }

    private void initUI(View view) {
        LinearLayout parentLayout = view.findViewById(R.id.linearLayout);
        albumNameInput = view.findViewById(R.id.albumName);
        albumListView = view.findViewById(R.id.albumListView);
        Button createAlbumButton = view.findViewById(R.id.createAlbumButton);

        parentLayout.requestFocus();
        parentLayout.setFocusableInTouchMode(true);

        // Create Album Button
        createAlbumButton.setOnClickListener(v -> {
            String albumName = albumNameInput.getText().toString();
            if (!albumName.isEmpty()) {
                createAlbum(albumName);
            } else {
                Toast.makeText(requireContext(), "Please enter an album name", Toast.LENGTH_SHORT).show();
            }
        });

        // Album item click listener
        albumListView.setOnItemClickListener((parent, clickedView, position, id) -> {
            OfflineAlbum selectedAlbum = adapter.getAlbumAtPosition(position);
            if (selectedAlbum != null && selectedAlbum.id > 0) {
                Intent intent = new Intent(requireContext(), OfflineAlbumInspect.class);
                intent.putExtra("albumId", selectedAlbum.id);
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "Invalid album data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createAlbum(String albumName) {
        Executors.newSingleThreadExecutor().execute(() -> {
            OfflineAlbum album = new OfflineAlbum(albumName);
            long newRowId = db.offlineAlbumDao().insert(album);
            requireActivity().runOnUiThread(() -> {
                if (newRowId != -1) {
                    Toast.makeText(requireContext(), "Album created", Toast.LENGTH_SHORT).show();
                    loadAlbums();
                } else {
                    Toast.makeText(requireContext(), "Error creating album", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadAlbums() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<OfflineAlbum> albums = db.offlineAlbumDao().getAllAlbums();
            requireActivity().runOnUiThread(() -> {
                adapter = new OfflineAlbumListAdapter(requireContext(), albums, db);
                albumListView.setAdapter(adapter);
            });
        });
    }

    public OfflineAlbumListAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }



public void handleTouchEvent(MotionEvent event) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(event); // Process gestures in the fragment
        }
    }

}
