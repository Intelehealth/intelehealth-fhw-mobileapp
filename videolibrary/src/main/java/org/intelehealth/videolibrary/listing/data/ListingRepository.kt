package org.intelehealth.videolibrary.listing.data

import org.intelehealth.videolibrary.model.Video

/**
 * Created by Arpan Sircar. on 08-02-2024.
 * Email : arpan@intelehealth.org
 * Mob   : +919123116015
 **/

class ListingRepository(private val source: ListingDataSource) {

    suspend fun fetchVideosFromServer(packageName: String, auth: String) =
        source.fetchVideosFromServer(packageName, auth)

    suspend fun insertVideos(videos: List<Video>) = source.insertVideosToDb(videos)

    fun fetchVideosFromDb() = source.fetchVideosFromDb()

    suspend fun deleteAll() {
        source.deleteAll()
    }
}