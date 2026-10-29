package org.intelehealth.app.utilities

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.github.ajalt.timberkt.Timber.tag
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.app.IntelehealthApplication
import java.io.File
import java.io.FileWriter
import java.io.IOException

class CustomLog {
    companion object {

        // Only messages coming from:
        // if (type.equals("text") && parentNode.isMultiChoice()) {
        // CustomLog.v(TAG, "parentNode " + parentNode.isMultiChoice());
        // }
        // will be printed. Every other CustomLog call is silently dropped.
        private fun shouldLog(msg: String?): Boolean {
            return msg != null && msg.startsWith("parentNode ")
        }

        @JvmStatic
        fun v(key: String, msg: String?) {
            if (!shouldLog(msg)) return
            if (BuildConfig.FLAVOR_server != "production") {
                tag(key).v(msg)
                // saveLogData(key, msg ?: "", "verbose")
            }
        }

        @JvmStatic
        fun v(key: String, msg: String?, vararg args: String) {
            if (!shouldLog(msg)) return
            if (BuildConfig.FLAVOR_server != "production") {
                tag(key).v(msg)
                saveLogData(key, String.format(msg ?: "", args), "verbose")
            }
        }

        @JvmStatic
        fun d(key: String, msg: String?) {
            if (!shouldLog(msg)) return
            if (BuildConfig.FLAVOR_server != "production") {
                tag(key).d(msg)
                saveLogData(key, msg ?: "", "debug")
            }
        }

        @JvmStatic
        fun d(key: String, msg: String?, vararg args: Any) {
            if (!shouldLog(msg)) return
            if (BuildConfig.FLAVOR_server != "production") {
                tag(key).d(msg)
                saveLogData(key, String.format(msg ?: "", args), "debug")
            }
        }

        @JvmStatic
        fun i(key: String, msg: String?) {
            if (!shouldLog(msg)) return
            if (BuildConfig.FLAVOR_server != "production") {
                tag(key).i(msg)
                saveLogData(key, msg ?: "", "info")
            }
        }

        @JvmStatic
        fun i(key: String, msg: String?, vararg args: Any) {
            if (!shouldLog(msg)) return
            if (BuildConfig.FLAVOR_server != "production") {
                tag(key).i(msg)
                saveLogData(key, String.format(msg ?: "", args), "info")
            }
        }

        @JvmStatic
        fun e(key: String, msg: String?) {
            if (!shouldLog(msg)) return
            if (BuildConfig.FLAVOR_server != "production") {
                tag(key).e(msg)
                saveLogData(key, msg ?: "", "error")
            }
        }

        @JvmStatic
        fun e(key: String, msg: String, vararg args: Any) {
            if (!shouldLog(msg)) return
            if (BuildConfig.FLAVOR_server != "production") {
                tag(key).e(msg)
                saveLogData(key, String.format(msg, args), "error")
            }
        }

        // ---- everything below is unchanged ----

        private fun saveLogData(key: String, msg: String, type: String) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                saveFileToExternalStorage(key, msg, type)
                return
            }
            try {
                val sessionManager = getSessionManager()
                val version = sessionManager.customLogVersion
                var fileName = getFileName(version)
                val log = getLog(key, msg, type)

                var uri: Uri? = findExistingFileUri(fileName)

                if (uri == null) {
                    val updatedVersion = System.currentTimeMillis()
                    sessionManager.customLogVersion = updatedVersion.toString()
                    fileName = getFileName(updatedVersion.toString())

                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    }

                    val customDir = File(Environment.getExternalStorageDirectory(), "Documents")

                    if (isDocumentDirectoryExist()) {
                        values.apply {
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                        }
                    } else {
                        customDir.mkdir()
                        values.apply {
                            put(MediaStore.MediaColumns.RELATIVE_PATH, customDir.absolutePath)
                        }
                    }

                    uri = IntelehealthApplication.getAppContext().contentResolver.insert(
                        MediaStore.Files.getContentUri("external"), values
                    )
                }

                if (uri != null) {
                    try {
                        IntelehealthApplication.getAppContext().contentResolver.openOutputStream(
                            uri, "wa"
                        ).use { outputStream ->
                            outputStream?.write(log.toString().toByteArray())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var sm: SessionManager? = null
        private fun getSessionManager(): SessionManager {
            if (sm == null) {
                sm = SessionManager(IntelehealthApplication.getAppContext())
            }
            return sm as SessionManager
        }

        private fun getFileName(version: String?): String {
            return "Intelehealth_${BuildConfig.FLAVOR_client}_${BuildConfig.FLAVOR_server}_${BuildConfig.VERSION_NAME}_$version.txt"
        }

        private fun saveFileToExternalStorage(key: String, msg: String, type: String) {
            try {
                if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                    val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    val sessionManager = getSessionManager()
                    var logVersion = sessionManager.customLogVersion

                    if (sessionManager.customLogVersion.isEmpty()) {
                        logVersion = System.currentTimeMillis().toString()
                        getSessionManager().customLogVersion = logVersion
                    }

                    val fileName = getFileName(logVersion)
                    val logFile = File(documentsDir, fileName)
                    val log = getLog(key, msg, type)
                    try {
                        if (!documentsDir.exists()) {
                            documentsDir.mkdirs()
                        }
                        val writer = FileWriter(logFile, true)
                        writer.append(log)
                        writer.flush()
                        writer.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun getLog(key: String, msg: String, type: String): StringBuilder {
            return StringBuilder()
                .append(
                    DateAndTimeUtils.getDateTimeFromTimestamp(
                        System.currentTimeMillis(),
                        "dd-MM-yyyy hh:mm a"
                    )
                )
                .append(" (")
                .append(type)
                .append(") ")
                .append(key)
                .append(" -> ")
                .append(msg)
                .append("\n")
        }

        private fun findExistingFileUri(fileName: String): Uri? {
            val collection = MediaStore.Files.getContentUri("external")
            val projection = arrayOf(MediaStore.MediaColumns._ID)
            val selection =
                MediaStore.MediaColumns.RELATIVE_PATH + "=? AND " + MediaStore.MediaColumns.DISPLAY_NAME + "=?"
            val selectionArgs = arrayOf(Environment.DIRECTORY_DOCUMENTS + "/", fileName)

            try {
                IntelehealthApplication.getAppContext().contentResolver.query(
                    collection, projection, selection, selectionArgs, null
                ).use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val id: Long =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                        return Uri.withAppendedPath(collection, id.toString())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        private fun isDocumentDirectoryExist(): Boolean {
            val documentsDir: File? = IntelehealthApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            return documentsDir != null
        }
    }
}