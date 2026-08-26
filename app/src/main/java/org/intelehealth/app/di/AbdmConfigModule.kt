package org.intelehealth.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intelehealth.abdm.config.AbdmConfig
import org.intelehealth.abdm.config.AbdmPatientLocalStore
import org.intelehealth.abdm.config.AbdmSessionProvider
import org.intelehealth.abdm.config.LocalPatientRecord
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.syncModule.SyncUtils
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.bifurcateAbhaAddress
import org.intelehealth.app.utilities.ensureTrailingSlash
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AbdmConfigModule {
    @Provides
    @Singleton
    fun provideAbdmConfig(): AbdmConfig = object : AbdmConfig {
        override val baseUrl: String = BuildConfig.SERVER_URL.ensureTrailingSlash()

        override val abhaAddressSuffix: String = BuildConfig.ABHA_ADDRESS_SUFFIX
    }

    @Provides
    @Singleton
    fun provideAbdmSessionProvider(
        sessionManager: SessionManager,
    ): AbdmSessionProvider = object : AbdmSessionProvider {
        override fun getEncodedCredentials(): String? {
            return sessionManager.encoded
        }

        override fun getLocationUuid(): String {
            return sessionManager.getLocationUuid()
        }
    }

    @Provides
    @Singleton
    fun provideAbdmPatientLocalStore(): AbdmPatientLocalStore = object : AbdmPatientLocalStore {
        override suspend fun isPatientLinkedWithAbhaAddress(
            openMrsId: String,
            abhaAddress: String,
        ): Boolean = withContext(Dispatchers.IO) {
            runCatching {
                PatientsDAO().isPatientExistWithAbhaAddress(openMrsId, abhaAddress)
            }.getOrDefault(false)
        }

        override suspend fun linkAbha(
            patientUuid: String,
            abhaNumber: String?,
            abhaAddress: String,
        ) {
            withContext(Dispatchers.IO) {
                runCatching {
                    PatientsDAO().updatePatientAbha(
                        patientUuid,
                        abhaNumber,
                        abhaAddress
                    )
                }
            }
        }

        /**
         * ABHA number first, then date of birth with the last ten digits of the phone — ten digits
         * because registration stores "+91XXXXXXXXXX" while ABDM sends the number bare.
         *
         * A household commonly shares one phone, so the demographic pass can return several people. One
         * match is taken as-is; several are narrowed by first name, and anything still ambiguous returns
         * null. Failing to match costs a duplicate patient, which is recoverable; matching the wrong one
         * writes another person's ABHA onto their record, which is not — so ties fail closed.
         */
        override suspend fun findPatientForComparison(
            abhaNumber: String,
            phoneNumber: String,
            dateOfBirth: String,
            firstName: String,
        ): LocalPatientRecord? = withContext(Dispatchers.IO) {
            val dao = PatientsDAO()
            abhaNumber.takeIf { it.isNotBlank() }?.let { number ->
                runCatching { dao.getPatientForComparisonByAbhaNumber(number) }.getOrNull()
                    ?.let { return@withContext it.toLocalRecord() }
            }

            val dob = dateOfBirth.takeIf { it.isNotBlank() } ?: return@withContext null
            val last10 = phoneNumber.filter { it.isDigit() }.takeLast(10)
            if (last10.length < 10) return@withContext null
            val matches = runCatching { dao.getPatientsForComparisonByPhoneAndDob(last10, dob) }
                .getOrDefault(emptyList())

            when {
                matches.size == 1 -> matches.first().toLocalRecord()
                matches.size > 1 -> matches.singleOrNull {
                    it.firstname?.trim().equals(firstName.trim(), ignoreCase = true)
                }
                    ?.toLocalRecord()

                else -> null
            }
        }

        override suspend fun isPatientRegisteredLocally(
            abhaNumberLastFour: String,
            firstName: String,
            lastName: String,
        ): Boolean = withContext(Dispatchers.IO) {
            runCatching {
                PatientsDAO().isPatientRegisteredLocally(abhaNumberLastFour, firstName, lastName)
            }.getOrDefault(false)
        }

        /**
         * Only the street line is taken from the compared address. The village, district and state
         * hierarchy stays as recorded locally — see updatePatientAfterAbhaComparison for why.
         *
         * A successful merge pushes straight away. The write marks the row unsynced but nothing else on
         * this route would carry it up: unlike registration, which syncs at the end of createNewPatient
         * and updatePatient, compare ends at Patient Details, so the linked ABHA would otherwise wait
         * for the next scheduled sync.
         */
        override suspend fun savePatientAfterComparison(record: LocalPatientRecord): Boolean =
            withContext(Dispatchers.IO) {
                val saved = runCatching {
                    PatientsDAO().updatePatientAfterAbhaComparison(
                        record.uuid,
                        record.firstName,
                        record.lastName,
                        record.dateOfBirth,
                        record.gender,
                        bifurcateAbhaAddress(record.address).address1,
                        record.pinCode,
                        record.phoneNumber,
                        record.abhaNumber,
                        record.abhaAddress,
                    )
                }.getOrDefault(false)

                if (saved) runCatching { SyncUtils().syncBackground() }
                saved
            }
    }
}

/**
 * Maps the host patient row into the module-facing comparison record (nulls → empty strings).
 * Village and district are read through [PatientDTO.getVillageWithoutDistrict] and
 * [PatientDTO.getDistrict] rather than the raw `cityvillage` field, so legacy rows that still
 * encode "district:village" in one column normalise instead of surfacing the colon on screen.
 */
private fun PatientDTO.toLocalRecord(): LocalPatientRecord {
    val composedAddress = listOf(
        address1,
        address2,
        villageWithoutDistrict,
        district,
        stateprovince,
    ).filter { !it.isNullOrBlank() }.joinToString(", ")

    return LocalPatientRecord(
        uuid = uuid.orEmpty(),
        openMrsId = openmrsId.orEmpty(),
        firstName = firstname.orEmpty(),
        lastName = lastname.orEmpty(),
        dateOfBirth = dateofbirth.orEmpty(),
        gender = gender.orEmpty(),
        address = composedAddress,
        pinCode = postalcode.orEmpty(),
        phoneNumber = phonenumber.orEmpty(),
        abhaNumber = abhaNumber.orEmpty(),
        abhaAddress = abhaAddress.orEmpty(),
    )
}
