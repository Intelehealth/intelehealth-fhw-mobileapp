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
import org.intelehealth.ezazi.ui.prescription.fragment.PrescriptionFragment
import org.intelehealth.ezazi.ui.prescription.fragment.PrescriptionFragment.PrescriptionType
import org.intelehealth.klivekit.chat.model.ItemHeader
import java.util.LinkedList

/**
 * Created by Vaghela Mithun R. on 01-02-2024 - 00:32.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PrescriptionRepository(val database: SQLiteDatabase) {
    fun fetchPrescription(
        visitId: String,
        type: PrescriptionFragment.PrescriptionType,
        allowAdminister: Boolean
    ): List<ItemHeader> {
        return when (type) {
            PrescriptionType.FULL -> fetchFullPrescription(visitId)
            PrescriptionType.PLAN -> fetchPlansPrescription(visitId)
            PrescriptionType.ASSESSMENT -> fetchAssessmentPrescription(visitId)
            PrescriptionType.MEDICINE -> fetchMedicinePrescription(visitId)
            PrescriptionType.OXYTOCIN -> fetchOxytocinIvFluidPrescription(
                visitId,
                Params.PRESCRIBED_OXYTOCIN.conceptId
            )

            PrescriptionType.IV_FLUID -> fetchOxytocinIvFluidPrescription(
                visitId,
                Params.PRESCRIBED_IV_FLUID.conceptId
            )
        }
//        PrescriptionQueryBuilder().buildPrescriptionQuery(visitId).apply {
//            Timber.d { "Prescription Query => $this" }
//            val cursor = database.rawQuery(this, null)
//            retrievePrescription(cursor).apply {
//                return obsMappingToPrescription(this)
//            }
//        }
    }

    private fun fetchFullPrescription(visitId: String): List<ItemHeader> {
        PrescriptionQueryBuilder().buildPrescriptionQuery(visitId).apply {
            Timber.d { "Prescription Query => $this" }
            val cursor = database.rawQuery(this, null)
            retrievePrescription(cursor).apply {
                return obsMappingToPrescription(this)
            }
        }
    }

    fun fetchPlansPrescription(visitId: String): List<ItemHeader> {
        PrescriptionQueryBuilder().buildSingleItemPrescriptionQuery(visitId, Params.PLAN.conceptId)
            .apply {
                Timber.d { "Prescription Query => $this" }
                val cursor = database.rawQuery(this, null)
                retrievePrescription(cursor).apply {
                    val prescriptions = LinkedList<ItemHeader>()
                    return obsMappingToPlanAndAssessmentPrescription(this, prescriptions) {}
                }
            }
    }

    fun fetchAssessmentPrescription(visitId: String): List<ItemHeader> {
        PrescriptionQueryBuilder().buildSingleItemPrescriptionQuery(
            visitId,
            Params.ASSESSMENT.conceptId
        ).apply {
            Timber.d { "Assessment presc Query => $this" }
            val cursor = database.rawQuery(this, null)
            retrievePrescription(cursor).apply {
                val prescriptions = LinkedList<ItemHeader>()
                return obsMappingToPlanAndAssessmentPrescription(this, prescriptions) {}
            }
        }
    }

    fun fetchMedicinePrescription(visitId: String): List<ItemHeader> {
        PrescriptionQueryBuilder().buildSingleItemPrescriptionQuery(
            visitId,
            Params.PRESCRIBED_MEDICINE.conceptId
        ).apply {
            Timber.d { "MEDICINE presc Query => $this" }
            val cursor = database.rawQuery(this, null)
            retrievePrescription(cursor).apply {
                val prescriptions = LinkedList<ItemHeader>()
                mappingMedicines(this, prescriptions) { }
                return prescriptions
            }
        }
    }

    fun fetchOxytocinIvFluidPrescription(visitId: String, conceptId: String): List<ItemHeader> {
        PrescriptionQueryBuilder().buildSingleItemPrescriptionQuery(
            visitId,
            conceptId
        ).apply {
            Timber.d { "Oxytocin Iv Fluid presc Query => $this" }
            val cursor = database.rawQuery(this, null)
            retrievePrescription(cursor).apply {
                val prescriptions = LinkedList<ItemHeader>()
                mappingOxytocinIvFluid(this, prescriptions) { }
                return prescriptions
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
                model.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow("created_date")))
                prescriptionObs.add(model)
            } while (cursor.moveToNext())
        }
        return prescriptionObs
    }

    private fun filterObsByConceptId(conceptId: String, obsList: List<ObsDTO>): List<ObsDTO> {
        return obsList.filter { it.conceptuuid.equals(conceptId) }
    }

    private fun obsMappingToPrescription(obsList: List<ObsDTO>): List<ItemHeader> {
        Timber.d { "Prescription => ${Gson().toJson(obsList)}" }
        val prescriptions = LinkedList<ItemHeader>()
        val plans = filterObsByConceptId(Params.PLAN.conceptId, obsList)
        val assessments = filterObsByConceptId(Params.ASSESSMENT.conceptId, obsList)
        val medicines = filterObsByConceptId(Params.PRESCRIBED_MEDICINE.conceptId, obsList)
        val oxytocins = filterObsByConceptId(Params.PRESCRIBED_OXYTOCIN.conceptId, obsList)
        val ivFluids = filterObsByConceptId(Params.PRESCRIBED_IV_FLUID.conceptId, obsList)

        if (plans.isNotEmpty()) {
            obsMappingToPlanAndAssessmentPrescription(plans, prescriptions) {
                prescriptions.add(CategoryHeader(R.string.lbl_plan))
            }
            Timber.d { "Plan ${Gson().toJson(plans)}" }
        }

        if (assessments.isNotEmpty()) {
            obsMappingToPlanAndAssessmentPrescription(assessments, prescriptions) {
                prescriptions.add(CategoryHeader(R.string.lbl_assessment))
            }
            Timber.d { "Assessments ${Gson().toJson(assessments)}" }
        }

        if (medicines.isNotEmpty()) {
            mappingMedicines(medicines, prescriptions) {
                prescriptions.add(CategoryHeader(R.string.lbl_medicine))
            }
//            medicines.map {
//                Medicine().apply {
//                    obsUuid = it.uuid
//                    creatorName = it.name.let { name ->
//                        if (name.contains("Dr").not()) return@let "Dr.$name"
//                        else return@let name
//                    }
//                    createdAt = it.getCreatedDate(false)
//                    dbFormatToMedicineObject(it.value)
//                }
//            }.apply {
//                if (this.isNotEmpty()) {
//                    prescriptions.add(CategoryHeader(R.string.lbl_medicine))
//                    prescriptions.addAll(this)
//                    Timber.d { "Medicines ${Gson().toJson(this)}" }
//                }
//            }
        }

        if (oxytocins.isNotEmpty()) {
            mappingOxytocinIvFluid(oxytocins, prescriptions) {
                prescriptions.add(CategoryHeader(R.string.lbl_oxytocin))
            }
//            oxytocins.filter {
//                it.value.isNotEmpty() && it.value.equals(ParamInfo.RadioOptions.NO.name).not()
//            }.map {
//                Gson().fromJson(it.value, Medication::class.java).apply {
//                    creatorName = it.name.let { name ->
//                        if (name.contains("Dr").not()) return@let "Dr.$name"
//                        else return@let name
//                    }
//                    createdAt = it.getCreatedDate(false)
//                    return@apply
//                }
//            }.apply {
//                if (this.isNotEmpty()) {
//                    prescriptions.add(CategoryHeader(R.string.lbl_oxytocin))
//                    prescriptions.addAll(this)
//                    Timber.d { "oxytocin ${Gson().toJson(obsList)}" }
//                }
//            }
        }
//
        if (ivFluids.isNotEmpty()) {
            mappingOxytocinIvFluid(ivFluids, prescriptions) {
                prescriptions.add(CategoryHeader(R.string.lbl_iv_fluid))
            }
//            ivFluids.filter {
//                it.value.isNotEmpty() && it.value.equals(ParamInfo.RadioOptions.NO.name).not()
//            }.map {
//                Gson().fromJson(it.value, Medication::class.java).apply {
//                    creatorName = it.name.let { name ->
//                        if (name.contains("Dr").not()) return@let "Dr.$name"
//                        else return@let name
//                    }
//                    createdAt = it.getCreatedDate(false)
//                    return@apply
//                }
//            }.apply {
//                if (this.isNotEmpty()) {
//                    prescriptions.add(CategoryHeader(R.string.lbl_iv_fluid))
//                    prescriptions.addAll(this)
//                    Timber.d { "IV Fluid ${Gson().toJson(this)}" }
//                }
//            }
        }

        return prescriptions
    }

    private fun obsMappingToPlanAndAssessmentPrescription(
        plans: List<ObsDTO>,
        prescriptions: LinkedList<ItemHeader>,
        header: () -> Unit
    ): List<ItemHeader> {
        Timber.d { "Prescription plan => ${Gson().toJson(plans)}" }
        if (plans.isNotEmpty()) {
            header.invoke()
            prescriptions.addAll(plans.map {
                it.noOfLine = 100
                if (it.name.contains("Dr").not()) it.name = "Dr.${it.name}"
                return@map it
            })
            Timber.d { "Plan ${Gson().toJson(plans)}" }
        }

        return prescriptions
    }

    private fun mappingMedicines(
        medicines: List<ObsDTO>,
        prescriptions: LinkedList<ItemHeader>,
        header: () -> Unit
    ) {
        if (medicines.isNotEmpty()) {
            medicines.map {
                Medicine().apply {
                    obsUuid = it.uuid
                    creatorName = it.name.let { name ->
                        if (name.contains("Dr").not()) return@let "Dr.$name"
                        else return@let name
                    }
                    createdAt = it.createdDate()
                    dbFormatToMedicineObject(it.value)
                }
            }.apply {
                if (this.isNotEmpty()) {
                    header.invoke()
                    prescriptions.addAll(this)
                    Timber.d { "Medicines ${Gson().toJson(this)}" }
                }
            }
        }
    }

    private fun mappingOxytocinIvFluid(
        obsList: List<ObsDTO>,
        prescriptions: LinkedList<ItemHeader>,
        header: () -> Unit
    ) {
        obsList.filter {
            it.value.isNotEmpty() && it.value.equals(ParamInfo.RadioOptions.NO.name).not()
        }.map {
            Gson().fromJson(it.value, Medication::class.java).apply {
                creatorName = it.name.let { name ->
                    if (name.contains("Dr").not()) return@let "Dr.$name"
                    else return@let name
                }
                createdAt = it.createdDate()
                return@apply
            }
        }.apply {
            if (this.isNotEmpty()) {
                header.invoke()
                prescriptions.addAll(this)
                Timber.d { "oxytocin ${Gson().toJson(obsList)}" }
            }
        }
    }
}