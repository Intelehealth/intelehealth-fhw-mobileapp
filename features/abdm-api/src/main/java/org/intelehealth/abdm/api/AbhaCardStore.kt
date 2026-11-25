package org.intelehealth.abdm.api

import android.content.Context
import android.os.Environment
import java.io.File

/** Where downloaded ABHA cards live. Readable even when the plugin is not bundled. */
object AbhaCardStore {
    private const val DIR_NAME = "Intelehealth_AbhaCard"

    @JvmStatic
    fun directory(context: Context): File =
        File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), DIR_NAME)

    @JvmStatic
    fun cardFile(context: Context, abhaNumber: String?): File =
        File(directory(context), "$abhaNumber.png")
}
