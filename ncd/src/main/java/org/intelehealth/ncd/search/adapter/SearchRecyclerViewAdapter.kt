package org.intelehealth.ncd.search.adapter

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.ncd.R
import org.intelehealth.ncd.databinding.ListItemSearchWithCategoryBinding
import org.intelehealth.ncd.model.PatientWithAttribute
import org.intelehealth.ncd.utils.DateAndTimeUtils

class SearchRecyclerViewAdapter(
    private val patientList: List<PatientWithAttribute>,
    private val resources: Resources,
    private val context: Context
) : RecyclerView.Adapter<SearchRecyclerViewAdapter.SearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding: ListItemSearchWithCategoryBinding = ListItemSearchWithCategoryBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return SearchViewHolder(binding)
    }

    override fun getItemCount(): Int = patientList.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.setData(patientList[position], resources, context)
    }

    class SearchViewHolder(
        private val binding: ListItemSearchWithCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setData(
            data: PatientWithAttribute,
            resources: Resources,
            context: Context
        ) {
            val headText = "${data.firstName} ${data.lastname}, ${data.openmrsId}"
            val bodyText = "${resources.getString(R.string.age)}: ${
                DateAndTimeUtils.getAgeInYearMonth(
                    data.dateOfBirth,
                    context = context
                )
            }"

            binding.tvNameOpenmrsId.text = headText
            binding.tvAge.text = bodyText
            binding.root.setOnClickListener {

            }
        }
    }
}