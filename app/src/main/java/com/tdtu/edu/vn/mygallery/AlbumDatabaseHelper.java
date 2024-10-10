package com.tdtu.edu.vn.mygallery;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlbumDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "albums.db";
    private static final int DATABASE_VERSION = 1;

    public AlbumDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Update the column name to "_id" as required by CursorAdapter/SimpleCursorAdapter
        db.execSQL("CREATE TABLE albums (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS albums");
        onCreate(db);
    }
}
