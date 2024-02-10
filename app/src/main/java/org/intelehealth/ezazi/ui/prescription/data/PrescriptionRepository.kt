package org.intelehealth.ezazi.ui.prescription.data

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.models.dto.ObsDTO
import org.intelehealth.ezazi.partogram.PartogramConstants.Params
import org.intelehealth.ezazi.partogram.model.Medication
import org.intelehealth.ezazi.partogram.model.Medicine
import org.intelehealth.ezazi.partogram.model.ParamInfo
import org.intelehealth.ezazi.ui.elcg.model.CategoryHeader
import org.intelehealth.klivekit.chat.model.ItemHeader
import java.util.LinkedList

/**
 * Created by Vaghela Mithun R. on 01-02-2024 - 00:32.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PrescriptionRepository(val database: SQLiteDatabase) {
    fun fetchPrescription(visitId: String, creatorId: String): List<ItemHeader> {
        PrescriptionQueryBuilder().buildPrescriptionQuery(visitId, creatorId).apply {
            Timber.d { "Prescription Query => $this" }
            val cursor = database.rawQuery(this, null)
            retrievePrescription(cursor).apply {
                return obsMappingToPrescription(this)
            }
        }
    }

    private fun retrievePrescription(cursor: Cursor): List<ObsDTO> {
        val prescriptionObs: MutableList<ObsDTO> = ArrayList()
        if (cursor.moveToFirst()) {
            do {
                val model = ObsDTO()
                model.uuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"))
                model.value = cursor.getString(cursor.getColumnIndexOrThrow("value"))
                model.conceptuuid = cursor.getString(cursor.getColumnIndexOrThrow("conceptuuid"))
                model.creator = cursor.getString(cursor.getColumnIndexOrThrow("creator"))
                model.name = cursor.getString(cursor.getColumnIndexOrThrow("given_name"))
                model.createdDate = cursor.getString(cursor.getColumnIndexOrThrow("created_date"))
                prescriptionObs.add(model)
            } while (cursor.moveToNext())
        }
        return prescriptionObs
    }

    private fun obsMappingToPrescription(obsList: List<ObsDTO>): List<ItemHeader> {
        Timber.d { "Prescription => ${Gson().toJson(obsList)}" }
        val prescriptions = LinkedList<ItemHeader>()
        val plans = obsList.filter { it.conceptuuid.equals(Params.PLAN.conceptId) }
        val medicines =
            obsList.filter { it.conceptuuid.equals(Params.PRESCRIBED_MEDICINE.conceptId) }
        val oxytocins =
            obsList.filter { it.conceptuuid.equals(Params.PRESCRIBED_OXYTOCIN.conceptId) }
        val ivFluids =
            obsList.filter { it.conceptuuid.equals(Params.PRESCRIBED_IV_FLUID.conceptId) }
        if (plans.isNotEmpty()) {

            prescriptions.add(CategoryHeader(R.string.lbl_plan))
            prescriptions.addAll(plans.map {
                it.noOfLine = 100
                if (it.name.contains("Dr").not()) it.name = "Dr.${it.name}"
                return@map it
            })
            Timber.d { "Plan ${Gson().toJson(plans)}" }
        }

        if (medicines.isNotEmpty()) {
            medicines.map {
                Medicine().apply {
                    obsUuid = it.uuid
                    creatorName = it.name.let { name ->
                        if (name.contains("Dr").not()) return@let "Dr.$name"
                        else return@let name
                    }
                    setCreatedAt(it.createdDate)
                    dbFormatToMedicineObject(it.value)
                }
            }.apply {
                if (this.isNotEmpty()) {
                    prescriptions.add(CategoryHeader(R.string.lbl_medicine))
                    prescriptions.addAll(this)
                    Timber.d { "Medicines ${Gson().toJson(this)}" }
                }
            }
        }

        if (oxytocins.isNotEmpty()) {
            oxytocins.filter {
                it.value.isNotEmpty() && it.value.equals(ParamInfo.RadioOptions.NO.name).not()
            }.map {
                Gson().fromJson(it.value, Medication::class.java).apply {
                    creatorName = it.name.let { name ->
                        if (name.contains("Dr").not()) return@let "Dr.$name"
                        else return@let name
                    }
                    setCreatedAt(it.createdDate)
                    return@apply
                }
            }.apply {
                if (this.isNotEmpty()) {
                    prescriptions.add(CategoryHeader(R.string.lbl_oxytocin))
                    prescriptions.addAll(this)
                    Timber.d { "oxytocin ${Gson().toJson(obsList)}" }
                }
            }
        }
//
        if (ivFluids.isNotEmpty()) {
            ivFluids.filter {
                it.value.isNotEmpty() && it.value.equals(ParamInfo.RadioOptions.NO.name).not()
            }.map {
                Gson().fromJson(it.value, Medication::class.java).apply {
                    creatorName = it.name.let { name ->
                        if (name.contains("Dr").not()) return@let "Dr.$name"
                        else return@let name
                    }
                    setCreatedAt(it.createdDate)
                    return@apply
                }
            }.apply {
                if (this.isNotEmpty()) {
                    prescriptions.add(CategoryHeader(R.string.lbl_iv_fluid))
                    prescriptions.addAll(this)
                    Timber.d { "IV Fluid ${Gson().toJson(this)}" }
                }
            }
        }

        return prescriptions
    }
}