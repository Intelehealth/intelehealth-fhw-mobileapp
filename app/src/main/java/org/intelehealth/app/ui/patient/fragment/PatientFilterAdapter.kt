package org.intelehealth.app.ui.patient.fragment

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.app.R
import org.intelehealth.app.models.PatientSearchResult


class PatientFilterAdapter(
    private var patientList: MutableList<PatientSearchResult>,
    private val context: Context
) : RecyclerView.Adapter<PatientFilterAdapter.PatientViewHolder>() {

    inner class PatientViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        /*val patientNameTv: TextView =
            itemView.findViewById(R.id.tvName)

        val patientInfoTv: TextView =
            itemView.findViewById(R.id.tvInfo)*/
         val filterPossibleTv = itemView.findViewById<TextView>(R.id.filter_txt_possible_type)
        val filterInfoTv = itemView.findViewById<TextView>(R.id.filter_txt_Info)
         val cardView = itemView.findViewById<CardView>(R.id.fu_cardview_item)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PatientViewHolder {

        val view = LayoutInflater.from(context).inflate(
            R.layout.filter_patient_item_layout,
            parent,
            false
        )

        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PatientViewHolder,
        position: Int
    ) {
        val patients = patientList[position]
        val name ="${patients.patient?.firstname} ${patients.patient?.lastname}"
        val gender = patients.patient?.gender
        val sc=
            String.format(
                " %.2f%%",
                (patients.score * 100)
            )
        val dob =patients.patient?.dateofbirth
        holder.filterInfoTv.text = "$name . $gender . DOB $dob. Similarity score: $sc"
        holder.filterPossibleTv.text =
            if (patients.score > 0.95)
                "Patient may already exist"
            else
                "Possible Duplicate Found locally"
        holder.filterPossibleTv.setTextColor(
            if (patients.score > 0.95)
                Color.parseColor("#6F2623")
            else
                Color.parseColor("#7E521E")
        )
        holder.filterInfoTv.setTextColor(
            if (patients.score > 0.95)
                Color.parseColor("#6F2623")
            else
                Color.parseColor("#7E521E")
        )
        holder.cardView.setBackgroundResource(
            if (patients.score > 0.95)
                R.drawable.patient_filter_items_bg_c
            else
                R.drawable.patient_filter_items_bg
        )
    }

    override fun getItemCount(): Int {
        return patientList.size
    }

    fun updatePatientList(newPatients: MutableList<PatientSearchResult>) {
        patientList = newPatients
        notifyDataSetChanged()
    }
    fun addMorePatients(newPatients: List<PatientSearchResult>) {
        val currentPatientSize = patientList.size
        val newPatientsSize = newPatients.size

        patientList.addAll(newPatients)
        notifyItemRangeInserted(currentPatientSize, newPatientsSize)
    }
}