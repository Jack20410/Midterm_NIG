package com.tdtu.edu.vn.mygallery;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ImageTagDao {
    @Query("SELECT * FROM image_tags WHERE tag LIKE '%' || :query || '%'")
    List<ImageTag> searchTags(String query);

    @Insert
    void insertTag(ImageTag imageTag);

    @Update
    void updateTag(ImageTag imageTag);

    @Query("SELECT * FROM image_tags WHERE imagePath = :imagePath LIMIT 1")
    ImageTag getTag(String imagePath);

    @Query("SELECT * FROM image_tags")
    List<ImageTag> getAllTags();
}

