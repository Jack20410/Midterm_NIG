package com.tdtu.edu.vn.mygallery;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface OfflineAlbumDao {
    @Insert
    long insert(OfflineAlbum album);

    @Update
    void update(OfflineAlbum album);

    @Delete
    void delete(OfflineAlbum album);

    @Query("SELECT * FROM offlinealbum WHERE id = :albumId ")
    OfflineAlbum getAlbumById(long albumId);

    @Query("SELECT * FROM offlinealbum")
    List<OfflineAlbum> getAllAlbums();



}

