package org.intelehealth.coreroomdb.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.coreroomdb.entity.DrSpeciality

/**
 * Created by Vaghela Mithun R. on 02-04-2024 - 10:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Dao
interface DrSpecialityDao : CoreDao<DrSpeciality> {
    @Query("SELECT * FROM tbl_dr_speciality")
    override fun getAll(): LiveData<List<DrSpeciality>>

    @Query("SELECT * FROM tbl_dr_speciality WHERE uuid = :uuid")
    fun getSpecialityByUuid(uuid: String): LiveData<DrSpeciality>

    @Query("SELECT * FROM tbl_dr_speciality WHERE providerUuid = :providerId")
    fun getSpecialityByProviderId(providerId: String): LiveData<DrSpeciality>

    @Query("SELECT * FROM tbl_dr_speciality WHERE attributeTypeUuid = :attrTypeId")
    fun getSpecialityByAttrTypeId(attrTypeId: String): LiveData<DrSpeciality>
}