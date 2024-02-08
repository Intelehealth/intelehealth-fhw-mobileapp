package org.intelehealth.videolibrary.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.intelehealth.videolibrary.model.Video

/**
 * Created by Arpan Sircar. on 08-02-2024.
 * Email : arpan@intelehealth.org
 * Mob   : +919123116015
 **/

@Dao
interface LibraryDao {

    @Query("SELECT * FROM tbl_video_library")
    fun getAll(): Flow<List<Video>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<Video>)

    @Query("DELETE FROM tbl_video_library")
    suspend fun deleteAll()

}