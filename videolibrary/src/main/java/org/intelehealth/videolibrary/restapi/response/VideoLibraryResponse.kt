package org.intelehealth.videolibrary.restapi.response

import com.google.gson.annotations.SerializedName

data class VideoLibraryResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val projectLibraryData: ProjectLibraryData
)