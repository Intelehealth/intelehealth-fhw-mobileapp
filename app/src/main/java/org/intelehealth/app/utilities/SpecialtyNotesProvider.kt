package org.intelehealth.app.utilities

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.intelehealth.app.models.SpecialtyNote
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Loads specialty-wise notes & precautions bundled at assets/specialty_notes.json
 * and looks them up by the doctor's specialization name.
 */
object SpecialtyNotesProvider {
    private const val ASSET_FILE_NAME = "specialty_notes.json"
    private var cachedNotes: List<SpecialtyNote>? = null

    private fun loadAll(context: Context): List<SpecialtyNote> {
        cachedNotes?.let { return it }
        val json = BufferedReader(InputStreamReader(context.assets.open(ASSET_FILE_NAME))).use { it.readText() }
        val type = object : TypeToken<List<SpecialtyNote>>() {}.type
        val notes: List<SpecialtyNote> = Gson().fromJson(json, type) ?: emptyList()
        cachedNotes = notes
        return notes
    }

    fun getNotesFor(context: Context, specialization: String?): List<String>? {
        if (specialization.isNullOrBlank()) return null
        val match = loadAll(context).firstOrNull { it.isEnabled == 1 && matches(it.specialty, specialization) }
        return match?.notes
    }

    private fun matches(specialty: String, specialization: String): Boolean {
        val a = specialty.trim()
        val b = specialization.trim()
        return a.equals(b, ignoreCase = true) ||
                a.contains(b, ignoreCase = true) ||
                b.contains(a, ignoreCase = true)
    }
}
