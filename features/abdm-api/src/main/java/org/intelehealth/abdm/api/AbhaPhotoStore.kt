package org.intelehealth.abdm.api

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/** Saves the base64 profile photo from an ABHA profile as the patient's photo file. */
object AbhaPhotoStore {
    private const val TAG = "AbhaPhotoStore"

    /** Returns the saved file's absolute path, or null if there is no usable photo. */
    @JvmStatic
    fun save(context: Context, base64Image: String?, patientUuid: String): String? {
        if (base64Image.isNullOrBlank()) return null

        val bitmap = decode(base64Image) ?: return null
        val bytes = ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.toByteArray()
        }
        bitmap.recycle()

        val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null
        return try {
            if (!directory.exists()) directory.mkdirs()
            val file = File(directory, "$patientUuid.jpg")
            FileOutputStream(file).use { it.write(bytes) }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Unable to save ABHA photo", e)
            null
        }
    }

    private fun decode(base64: String): Bitmap? = try {
        val clean = if (base64.contains(",")) base64.substringAfter(",") else base64
        val bytes = Base64.decode(clean, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        Log.e(TAG, "Invalid ABHA photo", e)
        null
    }
}
