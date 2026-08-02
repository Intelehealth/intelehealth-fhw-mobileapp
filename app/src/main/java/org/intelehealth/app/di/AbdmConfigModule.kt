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

        override suspend fun findPatientForComparison(
            abhaNumber: String,
            phoneNumber: String,
        ): LocalPatientRecord? = withContext(Dispatchers.IO) {
            val dao = PatientsDAO()
            val dto = runCatching { dao.getPatientForComparisonByAbhaNumber(abhaNumber) }.getOrNull()
                ?: runCatching { dao.getPatientForComparisonByPhone(phoneNumber) }.getOrNull()
            dto?.toLocalRecord()
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

        override suspend fun savePatientAfterComparison(record: LocalPatientRecord): Boolean =
            withContext(Dispatchers.IO) {
                val addr = bifurcateAbhaAddress(record.address)
                runCatching {
                    PatientsDAO().updatePatientAfterAbhaComparison(
                        record.uuid,
                        record.firstName,
                        record.lastName,
                        record.dateOfBirth,
                        record.gender,
                        addr.address1,
                        addr.cityVillage,
                        addr.countyDistrict,
                        addr.stateProvince,
                        record.pinCode,
                        record.phoneNumber,
                        record.abhaNumber,
                        record.abhaAddress,
                    )
                }.getOrDefault(false)
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
