package org.intelehealth.app.registry

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData

/**
 * Created by Vaghela Mithun R. on 19-09-2022.
 * vaghela.mithun@gmail.com
 */
class IntentResultRegistry constructor(
    val context: Context,
    registry: ActivityResultRegistry
) {
    private val resultContracts = ActivityResultContracts.StartActivityForResult()

    private var resultData = MutableLiveData<ActivityResult?>()

    companion object {
        private const val RESULT_REGISTRY_KEY = "result_registry_key"
    }

    private val registerResult = registry.register(RESULT_REGISTRY_KEY, resultContracts) {
//        checkResult(it.resultCode == Activity.RESULT_OK, it.data) {
        resultData.postValue(it)
//        }
    }

    private fun checkResult(resultOk: Boolean, data: Intent?, result: () -> Unit) {
        if (resultOk && data != null) result.invoke()
    }

    fun lunchIntentForResult(intent: Intent): MutableLiveData<ActivityResult?> {
        registerResult.launch(intent)
        return resultData
    }
}