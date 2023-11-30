package org.intelehealth.ezazi.ui.elcg.model

import org.intelehealth.ezazi.partogram.PartogramConstants.Params

/**
 * Created by Vaghela Mithun R. on 28-11-2023 - 12:43.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
enum class ELCGGraph(val section: String, val attributes: LinkedHashMap<String, String>) {
    SupportiveCare("Supportive Care", LinkedHashMap<String, String>().apply {
        put(Params.COMPANION.conceptId,Params.COMPANION.value)
        put(Params.PAIN_RELIEF.conceptId, Params.PAIN_RELIEF.value)
        put(Params.ORAL_FLUID.conceptId, Params.ORAL_FLUID.value)
        put(Params.POSTURE.conceptId, Params.POSTURE.value)
    }),
    Baby("Baby", LinkedHashMap<String, String>().apply {
        put(Params.BASELINE_FHR.conceptId, Params.BASELINE_FHR.value)
        put(Params.FHR_DEC.conceptId, Params.FHR_DEC.value)
        put(Params.AMNIOTIC_FLUID.conceptId, Params.AMNIOTIC_FLUID.value)
        put(Params.FETAL_POSITION.conceptId, Params.FETAL_POSITION.value)
        put(Params.CAPUT.conceptId, Params.CAPUT.value)
        put(Params.MOULDING.conceptId, Params.MOULDING.value)
    }),
    Women("Woman", LinkedHashMap<String, String>().apply {
        put(Params.PULSE.conceptId, Params.PULSE.value)
        put(Params.SYSTOLIC_BP.conceptId, Params.SYSTOLIC_BP.value)
        put(Params.DIASTOLIC_BP.conceptId, Params.DIASTOLIC_BP.value)
        put(Params.TEMPERATURE.conceptId, Params.TEMPERATURE.value)
        put(Params.URINE_PROTEIN.conceptId, Params.URINE_PROTEIN.value)
        put(Params.URINE_ACETONE.conceptId, Params.URINE_ACETONE.value)
    }),
    LabourProgress("Labour Progress", LinkedHashMap<String, String>().apply {
        put(Params.CONTRACTION_PER_10_MIN.conceptId, Params.CONTRACTION_PER_10_MIN.value)
        put(Params.DURATION_OF_CONTRACTION.conceptId, Params.DURATION_OF_CONTRACTION.value)
        put(Params.CERVIX_PLOT.conceptId, Params.CERVIX_PLOT.value)
        put(Params.DESCENT_PLOT.conceptId, Params.DESCENT_PLOT.value)
    }),
    MedicationAdministration("Medication Administration", LinkedHashMap<String, String>().apply {
        put(Params.OXYTOCIN.conceptId, Params.OXYTOCIN.value)
        put(Params.MEDICINE.conceptId, Params.MEDICINE.value)
        put(Params.IV_FLUID.conceptId, Params.IV_FLUID.value)
    }),
    SharedDecisionMaking("Shared Decision Making", LinkedHashMap<String, String>().apply {
        put(Params.ASSESSMENT.conceptId, Params.ASSESSMENT.value)
        put(Params.PLAN.conceptId, Params.PLAN.value)
    })
}