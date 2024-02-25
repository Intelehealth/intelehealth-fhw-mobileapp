package org.intelehealth.ezazi.ui.prescription.data

import org.intelehealth.ezazi.ui.prescription.listener.MedicineChangeListener

/**
 * Created by Vaghela Mithun R. on 25-02-2024 - 01:01.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
object MedicineSingleton {
    var medicineListener: MedicineChangeListener? = null
    var oxytocinListener: MedicineChangeListener? = null
    var ivFluidListener: MedicineChangeListener? = null
    var planListener: MedicineChangeListener? = null
    var assessmentListener: MedicineChangeListener? = null
}