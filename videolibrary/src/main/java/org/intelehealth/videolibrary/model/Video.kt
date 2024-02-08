package org.intelehealth.videolibrary.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Arpan Sircar. on 08-02-2024.
 * Email : arpan@intelehealth.org
 * Mob   : +919123116015
 **/

@Entity(tableName = "tbl_video_library")
data class Video(

    @PrimaryKey
    @SerializedName("id")
    val id: Int,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("createdBy")
    val createdBy: String,

    @SerializedName("projectId")
    val projectId: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("updatedAt")
    val updatedAt: String,

    @SerializedName("videoId")
    val videoId: String

)