package org.intelehealth.core.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by - Prajwal W. on 14/10/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/

data class Results<T> (
    @SerializedName("results")
    @Expose
    private var results: List<T> = ArrayList<T>(),

    @SerializedName("links")
    @Expose
    private var links: List<Link> = ArrayList()
) : Serializable