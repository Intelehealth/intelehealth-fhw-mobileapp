package org.intelehealth.ezazi.ui.elcg.model

import org.intelehealth.ezazi.partogram.PartogramConstants.Params

/**
 * Created by Vaghela Mithun R. on 28-11-2023 - 12:43.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
enum class ELCGGraph(val section: String, val attributes: LinkedHashMap<String, String>) {
    SupportiveCare("Supportive Care", LinkedHashMap<String, String>().apply {
        put(Params.COMPANION.value, Params.COMPANION.conceptId)
        put(Params.PAIN_RELIEF.value, Params.PAIN_RELIEF.conceptId)
        put(Params.ORAL_FLUID.value, Params.ORAL_FLUID.conceptId)
        put(Params.POSTURE.value, Params.POSTURE.conceptId)
    }),
    Baby("Baby", LinkedHashMap<String, String>().apply {
        put(Params.BASELINE_FHR.value, Params.BASELINE_FHR.conceptId)
        put(Params.FHR_DEC.value, Params.FHR_DEC.conceptId)
        put(Params.AMNIOTIC_FLUID.value, Params.AMNIOTIC_FLUID.conceptId)
        put(Params.FETAL_POSITION.value, Params.FETAL_POSITION.conceptId)
        put(Params.CAPUTE.value, Params.CAPUTE.conceptId)
        put(Params.MOULDING.value, Params.MOULDING.conceptId)
    }),
    Women("Woman", LinkedHashMap<String, String>().apply {
        put(Params.PULSE.value, Params.PULSE.conceptId)
        put(Params.SYSTOLIC_BP.value, Params.SYSTOLIC_BP.conceptId)
        put(Params.DIASTOLIC_BP.value, Params.DIASTOLIC_BP.conceptId)
        put(Params.TEMPERATURE.value, Params.TEMPERATURE.conceptId)
        put(Params.URINE_PROTEIN.value, Params.URINE_PROTEIN.conceptId)
        put(Params.URINE_ACETONE.value, Params.URINE_ACETONE.conceptId)
    }),
    LabourProgress("Labour Progress", LinkedHashMap<String, String>().apply {
        put(Params.CONTRACTION_PER_10_MIN.value, Params.CONTRACTION_PER_10_MIN.conceptId)
        put(Params.DURATION_OF_CONTRACTION.value, Params.DURATION_OF_CONTRACTION.conceptId)
        put(Params.CERVIX_PLOT.value, Params.CERVIX_PLOT.conceptId)
        put(Params.DESCENT_PLOT.value, Params.DESCENT_PLOT.conceptId)
    }),
    MedicationAdministration("Medication Administration", LinkedHashMap<String, String>().apply {
        put(Params.OXYTOCIN.value, Params.OXYTOCIN.conceptId)
        put(Params.MEDICINE.value, Params.MEDICINE.conceptId)
        put(Params.IV_FLUID.value, Params.IV_FLUID.conceptId)
    }),
    SharedDecisionMaking("Shared Decision Making", LinkedHashMap<String, String>().apply {
        put(Params.ASSESSMENT.value, Params.ASSESSMENT.conceptId)
        put(Params.PLAN.value, Params.PLAN.conceptId)
    })
}