package org.intelehealth.app.activities.filterPatientActivity

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.app.R
import org.intelehealth.app.models.PatientSearchResult
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.StringUtils

class FilterPatientAdapter(
    private var patientList: MutableList<PatientSearchResult>,
    private val listener: AdapterClickListener): RecyclerView.Adapter<FilterPatientAdapter.FilterPatientViewHolder>() {
  private var lastSelectedPosition = -1

  interface AdapterClickListener {
    fun onItemClick(selectedItem: PatientDTO?)
  }

  inner class FilterPatientViewHolder(val itemView: View): RecyclerView.ViewHolder(itemView) {
    //private val patientSelectButton = itemView.findViewById<RadioButton>(R.id.filter_patient_selected)
    private val patientPhoneTv = itemView.findViewById<TextView>(R.id.filter_txt_phone)
    private val patientGender = itemView.findViewById<TextView>(R.id.filter_txt_gender)
    //private val patientNameTv = itemView.findViewById<TextView>(R.id.filter_txt_name)
    //private val patientDobTv = itemView.findViewById<TextView>(R.id.filter_txt_dob)
    //private val patientSourceTv = itemView.findViewById<TextView>(R.id.filter_txt_source)
   // private val patientScoreTv = itemView.findViewById<TextView>(R.id.filter_txt_score)
    private val filterPossibleTv = itemView.findViewById<TextView>(R.id.filter_txt_possible_type)
    private val filterInfoTv = itemView.findViewById<TextView>(R.id.filter_txt_Info)
    private val cardView = itemView.findViewById<CardView>(R.id.fu_cardview_item)
    private val sessionManager = SessionManager(itemView.context)

    fun onBindView(patients: PatientSearchResult) {
     /* StringUtils.setGenderAgeLocal(itemView.context, patientGender,
          patients.patient?.dateofbirth, patients.patient?.gender, sessionManager)*/
     // patientNameTv.text = "${patients.patient?.firstname} ${patients.patient?.lastname}"
     // patientDobTv.text = StringUtils.en_hi_dob_three(patients.patient?.dateofbirth)
      //patientPhoneTv.text = patients.patient?.phonenumber
      //patientSelectButton.isChecked = absoluteAdapterPosition == lastSelectedPosition
      //  patientSourceTv.text = patients.source.name ?: ""
       // patientScoreTv.text = String.format("%.2f", patients.score)
        val name ="${patients.patient?.firstname} ${patients.patient?.lastname}"
        val gender = patients.patient?.gender
val sc=
        String.format(
            " %.2f%%",
            (patients.score * 100)
        )
        val dob =patients.patient?.dateofbirth;
        filterInfoTv.text = "$name . $gender . DOB $dob. Similarity score: $sc"
        filterPossibleTv.text =
            if (patients.score > 0.95)
                "Patient may already exist"
            else
                "Possible Duplicate Found locally"
        filterPossibleTv.setTextColor(
            if (patients.score > 0.95)
                Color.parseColor("#6F2623")
            else
                Color.parseColor("#7E521E")
        )
        cardView.setCardBackgroundColor(
            if (patients.score > 0.95)
                Color.parseColor("#F9ECEB")
            else
                Color.parseColor("#F8EFDC")
        )
      itemView.setOnClickListener {
        val selectedPosition = absoluteAdapterPosition

        if(selectedPosition != lastSelectedPosition) {
          val prevSelectedPosition = lastSelectedPosition
          lastSelectedPosition = selectedPosition

          notifyItemChanged(prevSelectedPosition)
          notifyItemChanged(lastSelectedPosition)

          listener.onItemClick(patients.patient)
        }
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterPatientViewHolder {
    return LayoutInflater.from(parent.context).inflate(R.layout.filter_patient_item_layout, parent, false).run {
      FilterPatientViewHolder(this)
    }
  }

  override fun getItemCount() = patientList.size

  override fun onBindViewHolder(holder: FilterPatientViewHolder, position: Int) {
    holder.onBindView(patientList[position])
  }

  fun updatePatientList(newPatients: MutableList<PatientSearchResult>) {
    lastSelectedPosition = -1
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