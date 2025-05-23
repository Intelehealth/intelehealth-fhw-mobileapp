package org.intelehealth.ncd.search.adapter

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import org.intelehealth.ncd.R
import org.intelehealth.ncd.callbacks.PatientClickedListener
import org.intelehealth.ncd.databinding.ListItemSearchWithCategoryBinding
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientWithAttribute
import org.intelehealth.ncd.utils.DateAndTimeUtils

class SearchRecyclerViewAdapter(
    private val patientList: MutableList<PatientWithAttribute>,
    private val resources: Resources,
    private val context: Context,
    private val listener: PatientClickedListener
) : RecyclerView.Adapter<SearchRecyclerViewAdapter.SearchViewHolder>() {

    companion object {
        const val LIST_STARTING_INDEX = 0
    }

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
        holder.setData(patientList[position], resources, context, listener)
    }

    fun clearData() {
        val tempSize = patientList.size
        patientList.clear()
        notifyItemRangeRemoved(LIST_STARTING_INDEX, tempSize)
    }

    class SearchViewHolder(
        private val binding: ListItemSearchWithCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setData(
            data: PatientWithAttribute,
            resources: Resources,
            context: Context,
            listener: PatientClickedListener
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
            binding.rvDiseases.apply {
                data.attributeList?.let {
                    val adapter = NcdNameAdapter(it, context)
                    val layoutManager = FlexboxLayoutManager(context).also { flm ->
                        flm.flexDirection = FlexDirection.ROW
                        flm.justifyContent = JustifyContent.FLEX_START
                    }

                    this.adapter = adapter
                    this.layoutManager = layoutManager
                }
            }

            binding.root.setOnClickListener {
                listener.onPatientClicked(
                    Patient(
                        uuid = data.uuid,
                        firstName = data.firstName,
                        middleName = data.middleName,
                        lastname = data.lastname
                    )
                )
            }
        }
    }
}