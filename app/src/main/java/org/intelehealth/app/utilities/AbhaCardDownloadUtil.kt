package org.intelehealth.app.utilities

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Base64
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import org.intelehealth.abdm.model.AbhaCardResponseBody
import org.intelehealth.abdm.restapi.RetrofitProvider
import org.intelehealth.abdm.utils.AbdmManager
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.models.dto.PatientDTO
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
                    AbdmManager.apply {
                        isCommunicationNumberUsed = true
                        isPreferredAddressSet = true
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
        val apiInterface = RetrofitProvider.apiService
        return apiInterface.getAbhaCard(accessToken, scope!!, token!!)
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