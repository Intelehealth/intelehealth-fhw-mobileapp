package org.intelehealth.ncd.pagination

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.ncd.callbacks.PatientClickedListener
import org.intelehealth.ncd.databinding.ListItemSearchProtocolwiseBinding
import org.intelehealth.ncd.model.PatientVisitDetails

class PatientPagingAdapter (
    private val resources: Resources,
    private val context: Context,
    private val listener: PatientClickedListener
) : PagingDataAdapter<PatientVisitDetails, PatientPagingAdapter.CategoryViewHolder>(DiffCallback) {

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<PatientVisitDetails>() {
            override fun areItemsTheSame(
                oldItem: PatientVisitDetails,
                newItem: PatientVisitDetails
            ): Boolean = oldItem.patientId == newItem.patientId

            override fun areContentsTheSame(
                oldItem: PatientVisitDetails,
                newItem: PatientVisitDetails
            ): Boolean = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemSearchProtocolwiseBinding.inflate(inflater, parent, false)
        return CategoryViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val patient = getItem(position)
        Log.d("TAG", "kkkkkkonBindViewHolder: patient : "+patient)
        if (patient != null) {
            holder.bind(patient)
        }
    }

    class CategoryViewHolder(
        private val binding: ListItemSearchProtocolwiseBinding,
        private val listener: PatientClickedListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(patient: PatientVisitDetails) {
            binding.visitDetail = patient
            binding.executePendingBindings()

            binding.root.setOnClickListener {
                listener.onPatientClicked(patient)
            }
        }
    }
}
