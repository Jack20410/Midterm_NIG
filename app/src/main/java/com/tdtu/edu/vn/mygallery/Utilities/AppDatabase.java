package com.tdtu.edu.vn.mygallery.Utilities;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import android.content.Context;

import com.tdtu.edu.vn.mygallery.Album.OfflineAlbum;
import com.tdtu.edu.vn.mygallery.Album.OfflineAlbumDao;

@Database(entities = {OfflineAlbum.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract OfflineAlbumDao offlineAlbumDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "offline_album_db")
                    .fallbackToDestructiveMigration()  // Ensure it drops and recreates if there are schema changes
                    .build();
        }
        return instance;
    }

}