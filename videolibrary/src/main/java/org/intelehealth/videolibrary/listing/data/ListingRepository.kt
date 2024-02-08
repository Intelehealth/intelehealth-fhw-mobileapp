package org.intelehealth.videolibrary.listing.data

import org.intelehealth.videolibrary.model.Video

class ListingRepository(private val source: ListingDataSource) {

    suspend fun fetchVideos(packageName: String, auth: String) =
        source.fetchVideos(packageName, auth)

    suspend fun insertVideos(videos: List<Video>) = source.insertVideosToDb(videos)

    fun fetchVideosFromDb() = source.fetchVideosFromDb()

}