package org.intelehealth.app.ui.patient.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.app.R
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2
import org.intelehealth.app.models.PatientSearchResult
import org.intelehealth.app.models.dto.PatientDTO
import java.io.Serializable


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
         val patientListRal = itemView.findViewById<RelativeLayout>(R.id.patientListRal)
         val btnArrow = itemView.findViewById<ImageButton>(R.id.btnArrow)
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
            if (patients.score >= 0.95)
                "Patient may already exist"
            else if(patients.score>=0.80)
                "Probable Duplicate Found locally"
            else
                "Possible Duplicate Found locally"
        holder.filterPossibleTv.setTextColor(
            if (patients.score >= 0.95)
                Color.parseColor("#6F2623")
            else if(patients.score>=0.80)
                Color.parseColor("#FF8A3D")
            else
                Color.parseColor("#7E521E")
        )
        holder.filterInfoTv.setTextColor(
            if (patients.score >= 0.95)
                Color.parseColor("#6F2623")
            else if(patients.score>= 0.80)
                Color.parseColor("#FF8A3D")
            else
                Color.parseColor("#7E521E")
        )
        holder.cardView.setBackgroundResource(
            if (patients.score >= 0.95)
                R.drawable.patient_filter_items_bg_c
           else if (patients.score >= 0.80)
                R.drawable.patient_filter_items_bg_p
            else
                R.drawable.patient_filter_items_bg
        )

        //click listener moved from onbind view holder
        //to prevent multiple initialization
        if (patients.score >= 0.95) {
            holder.btnArrow.visibility = View.VISIBLE

            val clickListener = View.OnClickListener {
                val position = holder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return@OnClickListener

                val patientDTO = patientList[position].patient ?: return@OnClickListener

                context.startActivity(
                    Intent(context, PatientDetailActivity2::class.java).apply {
                        putExtra("patientUuid", patientDTO.uuid)
                        putExtra("patientName", "${patientDTO.firstname} ${patientDTO.lastname}")
                        putExtra("tag", "searchPatient")
                        putExtra("hasPrescription", false)

                        putExtra(
                            "BUNDLE",
                            Bundle().apply {
                                putSerializable("patientDTO", patientDTO)
                            }
                        )
                    }
                )

                if (context is Activity) {
                    context.finish()
                }
            }

            holder.patientListRal.setOnClickListener(clickListener)
            holder.btnArrow.setOnClickListener(clickListener)

        } else {
            holder.btnArrow.visibility = View.GONE
            holder.patientListRal.setOnClickListener(null)
            holder.btnArrow.setOnClickListener(null)
        }
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