package org.intelehealth.videolibrary.restapi

import org.intelehealth.videolibrary.restapi.response.VideoLibraryResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface VideoLibraryApiClient {

    @GET("api/video-library/getVideosByPackageId/{package}")
    suspend fun fetchVideosLibrary(
        @Path("package") packageName: String,
        @Header("Authorization") auth: String
    ): VideoLibraryResponse

}