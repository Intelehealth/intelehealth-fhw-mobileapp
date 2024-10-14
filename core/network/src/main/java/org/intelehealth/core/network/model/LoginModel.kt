package org.intelehealth.core.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by - Prajwal W. on 14/10/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
data class LoginModel(
    @SerializedName("sessionId")
    @Expose
    val sessionId: String,

    @SerializedName("authenticated")
    @Expose
    val authenticated: Boolean,

    @SerializedName("user")
    @Expose
    val user: User,

    @SerializedName("locale")
    @Expose
    val locale: String,

    @SerializedName("allowedLocales")
    @Expose
    val allowedLocales: List<String>
)

data class User(
    @SerializedName("uuid")
    @Expose
    val uuid: String,

    @SerializedName("display")
    @Expose
    val display: String,

    @SerializedName("username")
    @Expose
    val username: String,

    @SerializedName("systemId")
    @Expose
    val systemId: String,

    @SerializedName("userProperties")
    @Expose
    val userProperties: UserProperties,

    @SerializedName("person")
    @Expose
    val person: Person,

    @SerializedName("privileges")
    @Expose
    val privileges: List<Person>,

    @SerializedName("roles")
    @Expose
    val roles: List<Person>,

    @SerializedName("retired")
    @Expose
    val retired: Boolean,

    @SerializedName("links")
    @Expose
    val links: List<Link>,

    @SerializedName("resourceVersion")
    @Expose
    val resourceVersion: String,
)

data class UserProperties(
    @SerializedName("loginAttempts")
    @Expose
    val loginAttempts: String
)

data class Person(
    @SerializedName("uuid")
    @Expose
    val uuid: String,

    @SerializedName("display")
    @Expose
    val display: String,

    @SerializedName("links")
    @Expose
    val links: List<Link>
)
