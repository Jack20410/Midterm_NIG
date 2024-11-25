package com.tdtu.edu.vn.mygallery.Image;


import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {ImageTag.class}, version = 2) // Update the version number here
public abstract class ImageTagDatabase extends RoomDatabase {
    public abstract ImageTagDao imageTagDao();

    private static volatile ImageTagDatabase INSTANCE;

    public static ImageTagDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ImageTagDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    ImageTagDatabase.class,
                                    "image_tags_database"
                            )
                            .fallbackToDestructiveMigration() // Add this line for destructive migration
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
