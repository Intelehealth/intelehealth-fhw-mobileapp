package org.intelehealth.videolibrary.restapi.response

import com.google.gson.annotations.SerializedName

data class Video(

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("createdBy")
    val createdBy: String,

    @SerializedName("id")
    val id: Int,

    @SerializedName("projectId")
    val projectId: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("updatedAt")
    val updatedAt: String,

    @SerializedName("videoId")
    val videoId: String

)