package org.intelehealth.abdm.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.intelehealth.abdm.api.AbdmSession
import org.intelehealth.abdm.api.AbhaCardStore
import org.intelehealth.abdm.constants.AbdmConstant
import org.intelehealth.abdm.model.AbhaProfileResponse
import org.intelehealth.abdm.model.OTPVerificationResponse
import org.intelehealth.abdm.restapi.RetrofitProvider
import timber.log.Timber
import java.io.FileOutputStream

/** Fetches the ABHA card image and stores it in [AbhaCardStore]. */
internal object AbhaCardDownloader {
    private const val TAG = "AbhaCardDownloader"

    /** Token for the getCard API: enrolment token (create flow), profile token, or the login X-token. */
    fun cardToken(
        otpResponse: OTPVerificationResponse?,
        abhaResponse: AbhaProfileResponse?,
        xToken: String?
    ): String? {
        val enrolToken = otpResponse?.tokens?.token
        return when {
            enrolToken != null -> if (enrolToken.startsWith("Bearer")) enrolToken else "Bearer $enrolToken"
            abhaResponse?.token != null -> abhaResponse.token
            else -> xToken
        }
    }

    fun cardScope(otpResponse: OTPVerificationResponse?, abhaResponse: AbhaProfileResponse?): String =
        when {
            otpResponse?.tokens?.token != null -> AbdmConstant.SCOPE_AADHAAR
            abhaResponse?.token != null -> AbdmConstant.SCOPE_MOBILE
            else -> AbdmManager.tempScope
        }

    @SuppressLint("CheckResult")
    fun downloadIfNeeded(context: Context, abhaNumber: String?, session: AbdmSession) {
        if (abhaNumber.isNullOrBlank()) return
        val cardFile = AbhaCardStore.cardFile(context, abhaNumber)
        val isStale = AbdmManager.isCommunicationNumberUsed || AbdmManager.isPreferredAddressSet
        if (cardFile.exists() && !isStale) return

        val scope = session.cardScope
        val token = session.cardToken
        if (scope.isNullOrEmpty() || token.isNullOrEmpty()) {
            Timber.tag(TAG).w("Missing token/scope; skipping ABHA card download")
            return
        }

        val appContext = context.applicationContext
        RetrofitProvider.apiService.getAbhaCard(session.accessToken ?: "", scope, token)
            .subscribeOn(Schedulers.io())
            .map { store(appContext, it.image, abhaNumber) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ stored ->
                if (stored) {
                    AbdmManager.isCommunicationNumberUsed = true
                    AbdmManager.isPreferredAddressSet = true
                }
            }, { Timber.tag(TAG).e(it, "ABHA Card download failed") })
    }

    private fun store(context: Context, image: String?, abhaNumber: String): Boolean {
        if (image.isNullOrEmpty()) return false
        val bytes = Base64.decode(image, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return false
        val directory = AbhaCardStore.directory(context)
        if (!directory.exists()) directory.mkdirs()
        FileOutputStream(AbhaCardStore.cardFile(context, abhaNumber), false).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return true
    }
}
