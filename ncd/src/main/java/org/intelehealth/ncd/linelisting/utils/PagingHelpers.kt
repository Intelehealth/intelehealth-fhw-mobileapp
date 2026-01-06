package org.intelehealth.ncd.linelisting.utils

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.LoadState
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import kotlinx.coroutines.delay
import org.intelehealth.ncd.model.PatientVisitDetails

class PatientVisitDetailsDiffCallback :
    DiffUtil.ItemCallback<PatientVisitDetails>() {
    override fun areItemsTheSame(oldItem: PatientVisitDetails, newItem: PatientVisitDetails) =
        oldItem.patientId == newItem.patientId
    override fun areContentsTheSame(oldItem: PatientVisitDetails, newItem: PatientVisitDetails) =
        oldItem == newItem
}

class NoopListCallback : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}
