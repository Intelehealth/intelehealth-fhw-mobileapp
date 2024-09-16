package org.intelehealth.abdm.domain.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ABHAProfile(
    @SerializedName("firstName")
    @Expose
    var firstName: String? = null,

    @SerializedName("middleName")
    @Expose
    var middleName: String? = null,

    @SerializedName("lastName")
    @Expose
    var lastName: String? = null,

    @SerializedName("dob")
    @Expose
    var dob: String? = null,

    @SerializedName("gender")
    @Expose
    var gender: String? = null,

    @SerializedName("photo")
    @Expose
    var photo: String? = null,

    @SerializedName("mobile")
    @Expose
    var mobile: String? = null,

    @SerializedName("email")
    @Expose
    var email: Any? = null,

    @SerializedName("phrAddress")
    @Expose
    var phrAddress: List<String>? = null,

    @SerializedName("address")
    @Expose
    var address: String? = null,

    @SerializedName("districtCode")
    @Expose
    var districtCode: String? = null,

    @SerializedName("stateCode")
    @Expose
    var stateCode: String? = null,

    @SerializedName("pinCode")
    @Expose
    var pinCode: String? = null,

    @SerializedName("abhaType")
    @Expose
    var abhaType: Any? = null,

    @SerializedName("stateName")
    @Expose
    var stateName: String? = null,

    @SerializedName("districtName")
    @Expose
    var districtName: String? = null,

    @SerializedName("ABHANumber")
    @Expose
    var aBHANumber: String? = null,

    @SerializedName("abhaStatus")
    @Expose
    var abhaStatus: String? = null
)