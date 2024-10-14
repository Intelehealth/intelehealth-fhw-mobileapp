package org.intelehealth.coreroomdb.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


/**
 * Created by - Prajwal W. on 14/10/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
data class Person(

    @SerializedName("uuid")
    @Expose
    var uuid: String? = null,

    @SerializedName("display")
    @Expose
    var display: String? = null,

    @SerializedName("links")
    @Expose
    var links: List<Link>? = null
)

data class Link(
    @SerializedName("rel")
    @Expose
    var rel: String,

    @SerializedName("uri")
    @Expose
    var uri: String
)