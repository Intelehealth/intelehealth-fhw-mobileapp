package org.intelehealth.videolibrary.restapi

import org.intelehealth.videolibrary.restapi.response.VideoLibraryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

/**
 * Created by Arpan Sircar. on 08-02-2024.
 * Email : arpan@intelehealth.org
 * Mob   : +919123116015
 **/

interface VideoLibraryApiClient {

    @GET("api/video-library/getVideosByPackageId/{package}")
    suspend fun fetchVideosFromServer(
        @Path("package") packageName: String,
        @Header("Authorization") auth: String
    ): Response<VideoLibraryResponse?>

}