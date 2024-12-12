package com.tdtu.edu.vn.mygallery.Utilities;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;

import com.tdtu.edu.vn.mygallery.Album.OfflineAlbum;
import com.tdtu.edu.vn.mygallery.Album.OfflineAlbumDao;

@Database(entities = {OfflineAlbum.class}, version = 6) // Increment version to 2
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract OfflineAlbumDao offlineAlbumDao();

    // Migration from version 1 to version 2
    private static final Migration MIGRATION_2_6 = new Migration(2, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add new columns for latitude and longitude to the offline album table
            database.execSQL("ALTER TABLE offlinealbum ADD COLUMN latitude REAL");
            database.execSQL("ALTER TABLE offlinealbum ADD COLUMN longitude REAL");
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "offline_album_db")
                    .addMigrations(MIGRATION_2_6) // Add migration here
                    .build();
        }
        return instance;
    }
}
