package org.intelehealth.config.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.intelehealth.config.room.entity.SubSection

/**
 * Created by Lincon Pradhan
 * Email : lincon@intelehealth.org
 **/
class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromMap(value: Map<String, String>?): String = gson.toJson(value)

    @TypeConverter
    fun toMap(value: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun fromSubSectionList(value: List<SubSection>?): String = gson.toJson(value)

    @TypeConverter
    fun toSubSectionList(value: String?): List<SubSection> {
        if (value.isNullOrEmpty()) {
            return emptyList()
        }

        val listType = object : TypeToken<List<SubSection>>() {}.type
        val parsedList: List<SubSection>? = gson.fromJson(value, listType)

        return parsedList ?: emptyList()
    }

}

