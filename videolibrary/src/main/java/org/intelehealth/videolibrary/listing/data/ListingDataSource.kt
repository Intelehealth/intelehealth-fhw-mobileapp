package org.intelehealth.videolibrary.listing.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.intelehealth.videolibrary.restapi.VideoLibraryApiClient
import org.intelehealth.videolibrary.restapi.response.VideoLibraryResponse
import retrofit2.Response

class ListingDataSource(private val service: VideoLibraryApiClient) {

    suspend fun fetchVideos(
        packageName: String,
        auth: String
    ): Flow<Response<VideoLibraryResponse?>> = flow {
        emit(service.fetchVideos(packageName, auth))
    }

}