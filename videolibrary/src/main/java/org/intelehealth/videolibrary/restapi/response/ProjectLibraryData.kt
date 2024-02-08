package org.intelehealth.videolibrary.restapi.response

import com.google.gson.annotations.SerializedName
import org.intelehealth.videolibrary.model.Video

data class ProjectLibraryData(

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("packageId")
    val packageId: String,

    @SerializedName("updatedAt")
    val updatedAt: String,

    @SerializedName("videos")
    val videos: List<Video>

)