package org.intelehealth.core.shared.ui.listener

import androidx.lifecycle.LifecycleOwner
import okhttp3.internal.http2.ErrorCode
import org.intelehealth.core.shared.ui.viewmodel.BaseViewModel
import org.intelehealth.core.R
import org.intelehealth.core.model.ErrorModel

/**
 * Created by Vaghela Mithun R. on 07-05-2022.
 * vaghela.mithun@gmail.com
 */
interface ServiceStateListener {
    fun onProgress()
    fun onProgressFinish()
    fun onError(error: ErrorModel)

    fun registerServiceObserver(viewModel: BaseViewModel, viewLifecycleOwner: LifecycleOwner) {
        viewModel.loading.observe(viewLifecycleOwner) {
            it ?: return@observe
            if (it) onProgress() else onProgressFinish()
        }

        viewModel.errorDataResult.observe(viewLifecycleOwner) {
            it ?: return@observe
            if (it.equals(ErrorCode.INTERNAL_ERROR)) {
                onProgressFinish()
                onError(ErrorModel(500, R.string.error_title, R.string.internal_error))
            }
        }
    }
}