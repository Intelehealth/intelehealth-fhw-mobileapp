package org.intelehealth.videolibrary.listing.data

import org.intelehealth.videolibrary.model.Video

class ListingRepository(private val source: ListingDataSource) {

    suspend fun fetchVideosFromServer(packageName: String, auth: String) =
        source.fetchVideosFromServer(packageName, auth)

    suspend fun insertVideos(videos: List<Video>) = source.insertVideosToDb(videos)

    fun fetchVideosFromDb() = source.fetchVideosFromDb()

}