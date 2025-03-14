package org.intelehealth.klivekit.utils

import android.content.Context
import android.text.TextUtils
import android.util.Log
import org.intelehealth.klivekit.RtcApp
import org.intelehealth.klivekit.RtcEngine
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Calendar
import java.util.Date

/**
 * Created by Tanvir Hasan on 21-02-2024 : 02-00.
 * Email: mhasan@intelehealth.org
 *
 * Created this class to identify log from remote device
 */
class LocalLogger {
    companion object{
        fun appendLog(TAG: String, text: String) {
            try {
                val context: Context = RtcEngine.appContext.getApplicationContext()
                val saveText = TAG + Date(System.currentTimeMillis())+ " >>> " + text
                val calender = Calendar.getInstance()
                val year = calender[Calendar.YEAR]
                val month = calender[Calendar.MONTH] + 1
                val day = calender[Calendar.DAY_OF_MONTH]
                val fileNameDayWise =
                    year.toString() + "" + addZeroForDay(month.toString() + "") + "" + addZeroForDay(day.toString() + "")
                val f =
                    File(context.getExternalFilesDir(null).toString() + "/nas_log/" + fileNameDayWise)
                if (!f.exists()) {
                    f.mkdirs()
                }
                val logFile = File(
                    context.getExternalFilesDir(null)
                        .toString() + "/nas_log/" + fileNameDayWise + "/" + "log.file"
                )
                if (!logFile.exists()) {
                    try {
                        logFile.createNewFile()
                    } catch (ee: IOException) {
                        Log.d(TAG, ee.message!!)
                    }
                }
                //BufferedWriter for performance, true to set append to file flag
                val buf = BufferedWriter(FileWriter(logFile, true))
                buf.append(saveText)
                buf.newLine()
                buf.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        private fun addZeroForDay(day: String): String {
            if (TextUtils.isEmpty(day)) return ""
            return if (day.length == 1) "0$day" else day
        }
    }

}