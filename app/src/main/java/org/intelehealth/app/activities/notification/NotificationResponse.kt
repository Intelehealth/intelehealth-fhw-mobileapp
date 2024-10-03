package org.intelehealth.app.activities.notification

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    @SerializedName("total") var total: Int? = null,
    @SerializedName("rows") var rows: ArrayList<NotificationList> = arrayListOf(),
    @SerializedName("totalPages") var totalPages: Int? = null,
    @SerializedName("currentPage") var currentPage: Int? = null
)

data class Data(

    @SerializedName("visitUuid") var visitUuid: String? = null,
    @SerializedName("patientUuid") var patientUuid: String? = null,
    @SerializedName("patientLastName") var patientLastName: String? = null,
    @SerializedName("followupDatetime") var followupDatetime: String? = null,
    @SerializedName("patientFirstName") var patientFirstName: String? = null,
    @SerializedName("patientOpenMrsId") var patientOpenMrsId: String? = null,
    @SerializedName("patientMiddleName") var patientMiddleName: String? = null

)


data class Payload(

    @SerializedName("body") var body: String? = null,
    @SerializedName("data") var data: Data? = Data(),
    @SerializedName("title") var title: String? = null,
    @SerializedName("regTokens") var regTokens: ArrayList<String> = arrayListOf()

)


data class NotificationList(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("description") var description: String? = null,
    @SerializedName("user_uuid") var userUuid: String? = null,
    @SerializedName("payload") var payload: Payload? = Payload(),
    @SerializedName("type") var type: String? = null,
    @SerializedName("isRead") var isRead: String? = null,
    @SerializedName("createdAt") var createdAt: String? = null,
    @SerializedName("updatedAt") var updatedAt: String? = null

)