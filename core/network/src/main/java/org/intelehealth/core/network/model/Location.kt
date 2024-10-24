package org.intelehealth.core.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


/**
 * Created by - Prajwal W. on 14/10/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
data class Location(
    @Expose
    private var id: Long,
    @Expose
    private var name: String,
    @Expose
    private var parentLocationUuid: String,
    @Expose
    private var description: String,
    @Expose
    private var address2: String,
    @Expose
    private var address1: String,
    @Expose
    private var cityVillage: String,
    @Expose
    private var stateProvince: String,
    @Expose
    private var country: String,
    @Expose
    private var postalCode: String

) : Resource() {

    constructor() : this(0, "", "",
        "", "", "", "",
        "", "", "")
}

data class Link(
    @SerializedName("rel")
    @Expose
    private var rel: String? = null,

    @SerializedName("uri")
    @Expose
    private var uri: String? = null,
) : Serializable


open class Resource (
    @SerializedName("uuid")
    @Expose
    var uuid: String? = null,

    @SerializedName("display")
    @Expose
    var display: String? = null,

    @SerializedName("links")
    @Expose
    var links: List<Link> = arrayListOf()

) : Serializable