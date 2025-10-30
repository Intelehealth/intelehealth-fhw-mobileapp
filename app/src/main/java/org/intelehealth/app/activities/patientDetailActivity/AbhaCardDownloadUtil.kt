package org.intelehealth.app.activities.patientDetailActivity

import android.app.Activity
import android.graphics.BitmapFactory
import android.util.Base64
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import org.intelehealth.app.abdm.model.AbhaCardResponseBody
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.app.AppConstants.PICTURES_DIRECTORY_PATH
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.utilities.CameraUtils
import org.intelehealth.app.utilities.UrlModifiers
import timber.log.Timber
import java.io.File

class AbhaCardDownloadUtil(private val patientDto: PatientDTO) {

    companion object {
        private const val TAG = "AbhaCardDownloadUtil"
    }

    private val abhaCardImagePath: String = "$PICTURES_DIRECTORY_PATH/Intelehealth_AbhaCard"
    val abhaCardFilePath = File(abhaCardImagePath)

    init {
        if (!doesFilePathExist(abhaCardFilePath)) {
            createFilePath(abhaCardFilePath)
        }
    }

    private fun doesFilePathExist(filePath: File): Boolean = filePath.exists();

    private fun createFilePath(filePath: File) = filePath.mkdirs()

    fun isAbhaCardPresent(): Boolean {
        val fileName: String = patientDto.abhaNumber
        val imageFile = File(abhaCardImagePath, "$fileName.png")
        return imageFile.exists()
    }

    fun downloadAbhaCard(
        scope: String?,
        token: String?,
        accessToken: String,
        activityContext: Activity
    ) {
        var responseBody: Single<AbhaCardResponseBody> = getResponseBodySingle(
            scope,
            token,
            accessToken
        )

        responseBody
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableSingleObserver<AbhaCardResponseBody>() {
                override fun onSuccess(t: AbhaCardResponseBody) {
                    storeAbhaCard(t.image, patientDto.abhaNumber, activityContext)
                }

                override fun onError(e: Throwable) {
                    Timber.tag(TAG).d("onError: Abha Card Download Failed")
                }
            })
    }

    private fun getResponseBodySingle(
        scope: String?,
        token: String?,
        accessToken: String,
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

    private fun storeAbhaCard(image: String, fileName: String, activityContext: Activity) {
        val decodedBytes = Base64.decode(image, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        val filePath = File(AppConstants.IMAGE_PATH + fileName)

        if (!doesFilePathExist(filePath)) {
            createFilePath(filePath)
        }

        CameraUtils(activityContext, fileName, filePath.toString()).also {
            it.compressImageAndSaveAbhaCard(bitmap)
        }
    }
}