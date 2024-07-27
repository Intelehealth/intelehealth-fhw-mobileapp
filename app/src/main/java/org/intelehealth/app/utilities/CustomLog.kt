package org.intelehealth.app.utilities

import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.github.ajalt.timberkt.Timber.tag
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.app.IntelehealthApplication


/**
 * Created By Tanvir Hasan on 7/24/24 6:49 PM
 * Email: tanvirhasan553@gmail.com
 */
class CustomLog {
    companion object {
        @JvmStatic
        fun v(key: String, msg: String) {
            if (BuildConfig.DEBUG) {
                tag(key).v(msg)
                saveLogData(key, msg, "verbose")
            }
        }

        @JvmStatic
        fun v(key: String, msg: String, vararg args: String) {
            if (BuildConfig.DEBUG) {
                tag(key).v(msg)
                saveLogData(key, String.format(msg, args), "verbose")
            }
        }

        @JvmStatic
        fun d(key: String, msg: String) {
            if (BuildConfig.DEBUG) {
                tag(key).d(msg)
                saveLogData(key, msg, "debug")
            }
        }

        @JvmStatic
        fun d(key: String, msg: String, vararg args: Any) {
            if (BuildConfig.DEBUG) {
                tag(key).d(msg)
                saveLogData(key, String.format(msg, args), "debug")
            }
        }

        @JvmStatic
        fun i(key: String, msg: String) {
            if (BuildConfig.DEBUG) {
                tag(key).i(msg)
                saveLogData(key, msg, "info")
            }
        }

        @JvmStatic
        fun i(key: String, msg: String, vararg args: Any) {
            if (BuildConfig.DEBUG) {
                tag(key).i(msg)
                saveLogData(key, String.format(msg, args), "info")
            }
        }

        @JvmStatic
        fun e(key: String, msg: String) {
            if (BuildConfig.DEBUG) {
                tag(key).e(msg)
                saveLogData(key, msg, "error")
            }
        }

        @JvmStatic
        fun e(key: String, msg: String, vararg args: Any) {
            if (BuildConfig.DEBUG) {
                tag(key).e(msg)
                saveLogData(key, String.format(msg, args), "error")
            }
        }

        /**
         * saving log in public document directory here
         */
        private fun saveLogData(key: String, msg: String, type: String) {
            val sessionManager = SessionManager(IntelehealthApplication.getAppContext())
            val version = sessionManager.customLogVersion
            var fileName = "Intelehealth_${BuildConfig.FLAVOR_client}_${BuildConfig.FLAVOR_server}_${BuildConfig.VERSION_NAME}_version_$version.txt"
            val log = StringBuilder()
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

            var uri: Uri? = findExistingFileUri(fileName)

            if (uri == null) {
                val updatedVersion = version+1
                sessionManager.customLogVersion = updatedVersion
                fileName = "Intelehealth_${BuildConfig.FLAVOR_client}_${BuildConfig.FLAVOR_server}_${BuildConfig.VERSION_NAME}_version_${updatedVersion}_log.txt"

                val values = ContentValues()
                    .apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }

                uri =
                    IntelehealthApplication.getAppContext().contentResolver.insert(
                        MediaStore.Files.getContentUri(
                            "external"
                        ), values
                    )
            }

            if (uri != null) {
                try {
                    IntelehealthApplication.getAppContext().contentResolver.openOutputStream(
                        uri,
                        "wa"
                    ).use { outputStream ->
                        outputStream?.write(log.toString().toByteArray())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * finding existing uri here
         * if exist then we will update otherwise will create new one
         */
        private fun findExistingFileUri(fileName: String): Uri? {
            val collection = MediaStore.Files.getContentUri("external")
            val projection = arrayOf(MediaStore.MediaColumns._ID)
            val selection =
                MediaStore.MediaColumns.RELATIVE_PATH + "=? AND " + MediaStore.MediaColumns.DISPLAY_NAME + "=?"
            val selectionArgs = arrayOf(Environment.DIRECTORY_DOCUMENTS+"/", fileName)

            try {
                IntelehealthApplication.getAppContext().contentResolver.query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )
                    .use { cursor ->
                        if (cursor != null && cursor.moveToFirst()) {
                            val id: Long =
                                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                            return Uri.withAppendedPath(collection, id.toString())
                        }
                    }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            return null
        }

    }
}
