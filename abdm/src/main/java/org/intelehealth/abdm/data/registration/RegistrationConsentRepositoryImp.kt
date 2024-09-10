package org.intelehealth.abdm.data.registration

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.intelehealth.abdm.R


import org.intelehealth.abdm.domain.model.RegistrationConsent
import org.intelehealth.abdm.domain.registration.RegistrationConsentRepository
import javax.inject.Inject

class RegistrationConsentRepositoryImp @Inject constructor(@ApplicationContext private val context: Context) :
    RegistrationConsentRepository {
    override suspend fun getConsentList():  List<RegistrationConsent> {
        // check internet - end
       val modelList = ArrayList<RegistrationConsent>()
        modelList.add(
            RegistrationConsent(
                context.getString(R.string.abha_consent_line1) + "\n",
                false
            )
        )
        modelList.add(
            RegistrationConsent(
                context.getString(R.string.abha_consent_line2) + "\n",
                false
            )
        )
        modelList.add(
            RegistrationConsent(
                context.getString(R.string.abha_consent_line3) + "\n",
                false
            )
        )
        modelList.add(
            RegistrationConsent(
                context.getString(R.string.abha_consent_line4) + "\n",
                false
            )
        )
        modelList.add(
            RegistrationConsent(
                context.getString(R.string.abha_consent_line5) + "\n",
                false
            )
        )
        modelList.add(
            RegistrationConsent(
                kotlin.String.format(
                    context.getString(R.string.abha_consent_line6),
                    "fetchHwFullName()"
                ) + "\n", false
            )
        )
        modelList.add(
            RegistrationConsent(
                context.getString(R.string.abha_consent_line7) + "\n",
                false
            )
        )
        return modelList
    }

}