package org.intelehealth.config.room.entity

import com.google.gson.annotations.SerializedName

/**
 * Created by Lincon Pradhan
 * Email : lincon@intelehealth.org
 **/
data class SupportedLanguage(
    @SerializedName("as")
    val asLang: String,
    @SerializedName("bn")
    val bnLang: String,
    @SerializedName("en")
    val enLang: String,
    @SerializedName("gu")
    val guLang: String,
    @SerializedName("hi")
    val hiLang: String,
    @SerializedName("kn")
    val knLang: String,
    @SerializedName("mr")
    val mrLang: String,
    @SerializedName("or")
    val orLang: String,
    @SerializedName("ru")
    val ruLang: String,
) {
    fun getAllLanguages(): List<String> {
        return listOf(asLang, bnLang, enLang, guLang, hiLang, knLang, mrLang, orLang, ruLang)
    }
}
