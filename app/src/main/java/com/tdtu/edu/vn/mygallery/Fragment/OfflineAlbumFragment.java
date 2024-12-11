package com.tdtu.edu.vn.mygallery.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tdtu.edu.vn.mygallery.Album.OfflineAlbum;
import com.tdtu.edu.vn.mygallery.Album.OfflineAlbumInspect;
import com.tdtu.edu.vn.mygallery.Album.OfflineAlbumRecyclerAdapter;
import com.tdtu.edu.vn.mygallery.R;
import com.tdtu.edu.vn.mygallery.Utilities.AppDatabase;

import java.util.List;
import java.util.concurrent.Executors;

public class OfflineAlbumFragment extends Fragment {

    private AppDatabase db;
    private RecyclerView albumRecyclerView;
    private EditText albumNameInput;
    private OfflineAlbumRecyclerAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_offline_album, container, false);

        initUI(view);
        Executors.newSingleThreadExecutor().execute(() -> {
            db = AppDatabase.getInstance(requireContext());
            requireActivity().runOnUiThread(() -> {
                if (db == null) {
                    Toast.makeText(requireContext(), "Database initialization failed", Toast.LENGTH_SHORT).show();
                } else {
                    loadAlbums();
                }
            });
        });

        return view;
    }

    private void initUI(View view) {
        LinearLayout parentLayout = view.findViewById(R.id.linearLayout);
        albumNameInput = view.findViewById(R.id.albumName);
        albumRecyclerView = view.findViewById(R.id.albumRecyclerView);
        Button createAlbumButton = view.findViewById(R.id.createAlbumButton);

        parentLayout.requestFocus();
        parentLayout.setFocusableInTouchMode(true);

        createAlbumButton.setOnClickListener(v -> {
            String albumName = albumNameInput.getText().toString();
            if (!albumName.isEmpty()) {
                createAlbum(albumName);
            } else {
                Toast.makeText(requireContext(), "Please enter an album name", Toast.LENGTH_SHORT).show();
            }
        });

        albumRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2)); // 2 columns
    }

    private void createAlbum(String albumName) {
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
                if (adapter == null) {
                    // Provide the database instance as a parameter
                    adapter = new OfflineAlbumRecyclerAdapter(albums, requireContext(), db, album -> {
                        if (album != null && album.id > 0) {
                            Intent intent = new Intent(requireContext(), OfflineAlbumInspect.class);
                            intent.putExtra("albumId", album.id);
                            startActivity(intent);
                        } else {
                            Toast.makeText(requireContext(), "Invalid album data", Toast.LENGTH_SHORT).show();
                        }
                    });
                    albumRecyclerView.setAdapter(adapter);
                } else {
                    adapter.updateAlbumList(albums);
                }
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        loadAlbums();
    }
}
