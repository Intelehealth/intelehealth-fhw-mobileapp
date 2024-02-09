package org.intelehealth.videolibrary.listing.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.intelehealth.videolibrary.model.Video
import org.intelehealth.videolibrary.restapi.VideoLibraryApiClient
import org.intelehealth.videolibrary.restapi.response.VideoLibraryResponse
import org.intelehealth.videolibrary.room.dao.LibraryDao
import retrofit2.Response

/**
 * Created by Arpan Sircar. on 08-02-2024.
 * Email : arpan@intelehealth.org
 * Mob   : +919123116015
 **/

class ListingDataSource(
    private val service: VideoLibraryApiClient,
    private val libraryDao: LibraryDao
) {

    suspend fun fetchVideosFromServer(
        packageName: String,
        auth: String
    ): Flow<Response<VideoLibraryResponse?>> = flow {
        emit(service.fetchVideosFromServer(packageName, auth))
    }

    suspend fun insertVideosToDb(videos: List<Video>) {
        libraryDao.deleteAll()
        libraryDao.insertAll(videos)
    }

    fun fetchVideosFromDb(): Flow<List<Video>> = libraryDao.getAll()

    suspend fun deleteAll() {
        libraryDao.deleteAll()
    }
}