package org.intelehealth.app.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Writes the base64 profile photo from a verified ABHA profile to disk and returns the absolute
 * path, which is what `PatientDTO.patientPhoto` holds — the camera flow stores a path the same way.
 *
 * Ported from development_master's `AbdmImageUtils`; kept host-side rather than in `:abdm` because
 * where a patient's photo lives is the host's concern, not the module's. Output convention is
 * preserved deliberately (JPEG q90, `<patientUuid>.jpg`, external Pictures dir) so existing readers
 * of `patientPhoto` behave identically.
 *
 * Returns null on any failure — a missing photo must never block registration.
 */
object AbhaPhotoUtils {

    fun saveEncodedPhoto(context: Context, base64Image: String?, patientUuid: String): String? {
        if (base64Image.isNullOrBlank() || patientUuid.isBlank()) return null
        return runCatching {
            val payload = if (base64Image.contains(",")) base64Image.substringAfter(",") else base64Image
            val bytes = Base64.decode(payload, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null

            val jpeg = ByteArrayOutputStream().also {
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it)
            }.toByteArray()
            bitmap.recycle()

            val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null
            if (!directory.exists()) directory.mkdirs()

            val file = File(directory, "$patientUuid.jpg")
            FileOutputStream(file).use { it.write(jpeg) }
            file.absolutePath
        }.getOrNull()
    }

    private const val JPEG_QUALITY = 90
}
