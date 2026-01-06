package org.intelehealth.ncd.category.adapter

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import org.intelehealth.ncd.R
import org.intelehealth.ncd.callbacks.PatientClickedListener
import org.intelehealth.ncd.databinding.ListItemSearchProtocolwiseBinding
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.utils.DateAndTimeUtils

class CategoryRecyclerViewAdapter(
    private val patientList: List<PatientVisitDetails>,
    private val resources: Resources,
    private val context: Context,
    private val listener: PatientClickedListener
) : RecyclerView.Adapter<CategoryRecyclerViewAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemSearchProtocolwiseBinding.inflate(inflater, parent, false)
        return CategoryViewHolder(binding, listener)
    }

    override fun getItemCount(): Int = patientList.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(patientList[position])
        Log.d("TAG", "onBindViewHolder: patientList : "+Gson().toJson(patientList))
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
