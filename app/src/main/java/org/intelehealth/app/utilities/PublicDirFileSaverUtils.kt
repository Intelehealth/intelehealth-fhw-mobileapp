package org.intelehealth.app.utilities

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import org.intelehealth.app.app.IntelehealthApplication
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


/**
 * Created By Tanvir Hasan on 7/25/24 10:24 AM
 * Email: tanvirhasan553@gmail.com
 */
class PublicDirFileSaverUtils {
    companion object {
        private val TAG: String = "PublicDirFileSaverUtils"

        @JvmStatic
        fun savePdf(
            fileName: String,
            filePath: String,
            environment: String
        ) {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                savePdfForLowerVersion(fileName,filePath,environment)
                return
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, environment)
            }
            val inputSteam = File(filePath).inputStream()
            val resolver: ContentResolver = IntelehealthApplication.getAppContext().contentResolver
            var stream: OutputStream? = null
            var uri: Uri? = null

            try {
                val contentUri = MediaStore.Files.getContentUri("external")
                uri = resolver.insert(contentUri, contentValues)
                val pfd: ParcelFileDescriptor
                try {
                    checkNotNull(uri)
                    pfd = IntelehealthApplication.getAppContext().contentResolver.openFileDescriptor(uri, "w")!!
                    val out = FileOutputStream(pfd.fileDescriptor)

                    val buf = ByteArray(4 * 1024)
                    var len: Int
                    while ((inputSteam.read(buf).also { len = it }) > 0) {
                        out.write(buf, 0, len)
                    }
                    out.close()
                    inputSteam.close()
                    pfd.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                     CustomLog.e(TAG,e.message);
                }

                contentValues.clear()
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                if (uri != null) {
                    IntelehealthApplication.getAppContext().contentResolver.update(uri, contentValues, null, null)
                }
                stream = resolver.openOutputStream(uri!!)
                if (stream == null) {
                    throw IOException("Failed to get output stream.")
                }
            } catch (e: IOException) {
                resolver.delete(uri!!, null, null)
                CustomLog.e(TAG,e.message);
                throw e
            } finally {
                stream?.close()
            }
        }

        private fun savePdfForLowerVersion(
                fileName: String,
                filePath: String,
                environment: String
        ) {
            try {
                if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                    val documentsDir = Environment.getExternalStoragePublicDirectory(environment)


                    val logFile = File(documentsDir, fileName)
                    try {
                        File(filePath).copyTo(logFile)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        CustomLog.e(TAG,e.message);
                    }
                }
            }catch (e: java.lang.Exception){
                e.printStackTrace()
                CustomLog.e(TAG,e.message);
            }
        }
    }
}