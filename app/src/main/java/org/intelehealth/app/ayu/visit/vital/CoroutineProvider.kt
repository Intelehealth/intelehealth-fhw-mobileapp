package org.intelehealth.app.ayu.visit.vital

import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.launch
import org.intelehealth.config.presenter.fields.viewmodel.PatientVitalViewModel

class CoroutineProvider {
    companion object{
        @JvmStatic
        fun usePatientVitalScope(
            scope: LifecycleCoroutineScope,
            patientVitalViewModel: PatientVitalViewModel,
            coroutineDataCallback: CoroutineDataCallback

        ) {
            scope.launch {
                val patientVitalList = patientVitalViewModel.getAllEnabledFields()
                coroutineDataCallback.onReceiveData(patientVitalList)
            }
        }
    }
}