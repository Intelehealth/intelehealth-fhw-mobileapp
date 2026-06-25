package org.intelehealth.ncd.linelisting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.ncd.callbacks.PatientClickedListener
import org.intelehealth.ncd.databinding.ListItemSearchProtocolwiseBinding
import org.intelehealth.ncd.model.PatientVisitDetails

class PatientVisitPagingAdapter (
    private val listener: PatientClickedListener
) : PagingDataAdapter<PatientVisitDetails, PatientVisitPagingAdapter.CategoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemSearchProtocolwiseBinding.inflate(inflater, parent, false)
        return CategoryViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val patient = getItem(position) ?: return
        holder.bind(patient)
    }

    class CategoryViewHolder(
        private val binding: ListItemSearchProtocolwiseBinding,
        private val listener: PatientClickedListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(patient: PatientVisitDetails) {
            binding.visitDetail = patient
            binding.executePendingBindings()
            try {
                Class.forName("org.intelehealth.app.utilities.DownloadFilesUtils")
                    .getMethod(
                        "ensurePatientProfileImage",
                        android.content.Context::class.java,
                        android.widget.ImageView::class.java,
                        String::class.java,
                        String::class.java
                    )
                    .invoke(
                        null,
                        binding.root.context,
                        binding.profileImgview,
                        patient.patientId,
                        patient.patientPhoto
                    )
            } catch (_: Exception) {
            }

            binding.root.setOnClickListener {
                listener.onPatientClicked(patient)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PatientVisitDetails>() {
            override fun areItemsTheSame(oldItem: PatientVisitDetails, newItem: PatientVisitDetails) =
                oldItem.patientId == newItem.patientId

            override fun areContentsTheSame(oldItem: PatientVisitDetails, newItem: PatientVisitDetails) =
                oldItem == newItem
        }
    }
}