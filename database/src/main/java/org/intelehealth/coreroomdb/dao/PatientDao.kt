package org.intelehealth.coreroomdb.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.coreroomdb.entity.Patient

/**
 * Created by Vaghela Mithun R. on 02-04-2024 - 10:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Dao
interface PatientDao : CoreDao<Patient> {
    @Query("SELECT * FROM tbl_patient")
    override fun getAll(): LiveData<List<Patient>>

    @Query("SELECT * FROM tbl_patient WHERE uuid = :uuid")
    fun getPatientByUuid(uuid: String): LiveData<Patient>

    @Query("SELECT * FROM tbl_patient WHERE openMrsId = :openMrsId")
    fun getPatientByOpenMrsId(openMrsId: String): LiveData<List<Patient>>

    @Query("SELECT * FROM tbl_patient WHERE phoneNumber = :phoneNumber")
    fun getPatientByPhoneNumber(phoneNumber: String): LiveData<List<Patient>>

    @Query("SELECT * FROM tbl_patient WHERE cityVillage = :city")
    fun getCityPatients(city: String): LiveData<List<Patient>>

    @Query("SELECT * FROM tbl_patient WHERE stateProvince = :state")
    fun getStatePatients(state: String): LiveData<List<Patient>>

    @Query("SELECT * FROM tbl_patient WHERE country = :country")
    fun getCountryPatients(country: String): LiveData<List<Patient>>

    @Query("SELECT * FROM tbl_patient WHERE gender = :gender")
    fun getGenderWisePatients(gender: String): LiveData<List<Patient>>

    @Query("SELECT * FROM tbl_patient WHERE postalCode = :postalCode")
    fun getPostalCodeWisePatients(postalCode: String): LiveData<List<Patient>>

    @Query("SELECT * FROM tbl_patient WHERE creatorUuid = :creatorId")
    fun getPatientByCreatorId(creatorId: String): LiveData<List<Patient>>

    @Query("SELECT * FROM tbl_patient WHERE caste = :cast")
    fun getCastPatients(cast: String): LiveData<List<Patient>>

    @Query("SELECT * FROM tbl_patient WHERE dead = :dead")
    fun getDeadPatients(dead: String): LiveData<List<Patient>>

    @Query("UPDATE tbl_patient SET openMrsId = :openMrsId WHERE uuid = :uuid")
    suspend fun updateOpenMrsId(uuid: String, openMrsId: String)

    @Query("UPDATE tbl_patient SET firstName = :firstName WHERE uuid = :uuid")
    suspend fun updateFirstName(uuid: String, firstName: Boolean)

    @Query("UPDATE tbl_patient SET middleName = :middleName WHERE uuid = :uuid")
    suspend fun updateMiddleName(middleName: String, uuid: String)

    @Query("UPDATE tbl_patient SET lastName = :lastName WHERE uuid = :uuid")
    suspend fun updateLastName(lastName: String, uuid: String)

    @Query("UPDATE tbl_patient SET dateOfBirth = :dob WHERE uuid = :uuid")
    suspend fun updateDob(uuid: String, dob: String)

    @Query("UPDATE tbl_patient SET phoneNumber = :phoneNumber WHERE uuid = :uuid")
    suspend fun updatePhoneNumber(uuid: String, phoneNumber: String)

    @Query("UPDATE tbl_patient SET address1 = :address1 WHERE uuid = :uuid")
    suspend fun updateAddress1(uuid: String, address1: Boolean)

    @Query("UPDATE tbl_patient SET address2 = :address2 WHERE uuid = :uuid")
    suspend fun updateAddress2(address2: String, uuid: String)

    @Query("UPDATE tbl_patient SET dead = :dead WHERE uuid = :uuid")
    suspend fun updateDeadStatus(dead: String, uuid: String)

    @Query("UPDATE tbl_patient SET sync = :isSync WHERE uuid = :uuid")
    suspend fun updateSyncStatus(uuid: String, isSync: Boolean)
}