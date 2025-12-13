package org.intelehealth.app.activities.patientDetailActivity

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Base64
import com.google.gson.Gson
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import org.intelehealth.app.abdm.model.AbhaCardResponseBody
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.UrlModifiers
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class AbhaCardDownloadUtil(
    private val patientDto: PatientDTO,
    private val context: Context
) {

    companion object {
        private const val TAG = "AbhaCardDownloadUtil"
        private const val DIR_NAME = "Intelehealth_AbhaCard"
    }

    fun isAbhaCardPresent(): Boolean {
        val file = getAbhaCardFile()
        return file.exists()
    }

    fun downloadAbhaCard(
        scope: String?,
        token: String?,
        accessToken: String,
        activityContext: Activity
    ) {
        val responseBody: Single<AbhaCardResponseBody> =
            getResponseBodySingle(scope, token, accessToken)

        responseBody
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableSingleObserver<AbhaCardResponseBody>() {

                override fun onSuccess(t: AbhaCardResponseBody) {
                    storeAbhaCard(t.image, patientDto.abhaNumber)

                    SessionManager(activityContext).apply {
                        setIsCommunicationNumberUsed(false)
                        setIsPreferredAddressSet(false)
                    }
                }

                override fun onError(e: Throwable) {
                    Timber.tag(TAG).e(e, "ABHA Card download failed")
                }
            })
    }

    private fun getResponseBodySingle(
        scope: String?,
        token: String?,
        accessToken: String
    ): Single<AbhaCardResponseBody> {
        val url = UrlModifiers.getABHACardUrl()
        val apiInterface = AppConstants.apiInterface

        return apiInterface.GET_ABHA_CARD(
            url,
            accessToken,
            scope,
            token
        )
    }

    private fun storeAbhaCard(
        image: String,
        fileName: String
    ) {
        val decodedBytes = Base64.decode(image, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(
            decodedBytes,
            0,
            decodedBytes.size
        ) ?: return

        val dir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            DIR_NAME
        )

        if (!dir.exists()) {
            dir.mkdirs()
        }

        val file = File(dir, "$fileName.png")

        // HARD overwrite
        FileOutputStream(file, false).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
        }
    }

    private fun getAbhaCardFile(): File {
        val dir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            DIR_NAME
        )
        return File(dir, "${patientDto.abhaNumber}.png")
    }
}
