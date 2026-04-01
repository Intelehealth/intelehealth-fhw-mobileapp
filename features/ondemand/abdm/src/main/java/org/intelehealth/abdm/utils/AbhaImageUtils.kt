package org.intelehealth.abdm.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object AbhaImageUtils {

    @JvmStatic
    fun convertEncodedToFile(
        context: Context,
        base64Image: String,
        patientUuid: String
    ): String? {

        if (base64Image.isBlank()) return null

        val bitmap = base64ToBitmap(base64Image) ?: return null

        val byteArray = compressBitmap(bitmap)
        bitmap.recycle()

        val location = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: return null

        return saveBytesToFile(byteArray, location, patientUuid)
    }

    private fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val cleanBase64 = if (base64.contains(",")) {
                base64.substringAfter(",")
            } else base64

            val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun compressBitmap(bitmap: Bitmap, quality: Int = 90): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }

    private fun saveBytesToFile(
        data: ByteArray, directory: File, fileName: String
    ): String? {
        return try {
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, "$fileName.jpg")
            FileOutputStream(file).use { it.write(data) }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}