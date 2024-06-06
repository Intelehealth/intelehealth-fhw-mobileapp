package org.intelehealth.ncd.category.adapter

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.ncd.R
import org.intelehealth.ncd.callbacks.PatientClickedListener
import org.intelehealth.ncd.databinding.ListItemSearchBinding
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.utils.DateAndTimeUtils

class CategoryRecyclerViewAdapter(
    private val patientList: List<Patient>,
    private val resources: Resources,
    private val context: Context,
    private val listener: PatientClickedListener
) : RecyclerView.Adapter<CategoryRecyclerViewAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding: ListItemSearchBinding = ListItemSearchBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )

        return CategoryViewHolder(binding, listener)
    }

    override fun getItemCount(): Int = patientList.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.setData(patientList[position], resources, context)
    }

    class CategoryViewHolder(
        private val binding: ListItemSearchBinding,
        private val listener: PatientClickedListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setData(patient: Patient, resources: Resources, context: Context) {
            val headText = "${patient.firstName} ${patient.lastname}, ${patient.openmrsId}"
            binding.listItemHead.text = headText

            val bodyText = "${resources.getText(R.string.age)}: ${
                DateAndTimeUtils.getAgeInYearMonth(
                    patient.dateOfBirth,
                    context = context
                )
            }"
            binding.listItemBody.text = bodyText

            binding.root.setOnClickListener {
                listener.onPatientClicked(patient)
            }
        }
    }
}