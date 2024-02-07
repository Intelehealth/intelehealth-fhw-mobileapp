package org.intelehealth.videolibrary.listing.data

class ListingRepository(private val source: ListingDataSource) {

    suspend fun fetchVideos(packageName: String, auth: String) =
        source.fetchVideos(packageName, auth)

}