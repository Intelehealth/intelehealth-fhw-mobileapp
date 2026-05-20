package org.intelehealth.app.activities.filterPatientActivity

import android.view.LayoutInflater
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.github.ajalt.timberkt.v
import org.intelehealth.app.R
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.models.PatientSearchResult
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.klivekit.utils.DateTimeUtils

class FilterResultAdapter(
    private var patientList: MutableList<PatientSearchResult>,
    private val listener: AdapterClickListener): RecyclerView.Adapter<FilterResultAdapter.FilterResultViewHolder>() {
    private var lastSelectedPosition = -1
    protected var patient: PatientDTO = PatientDTO()
    private val patientsDAO = PatientsDAO()
    interface AdapterClickListener {
        fun onItemClick(selectedItem: Any)
    }
    inner class FilterResultViewHolder(val itemView: View): RecyclerView.ViewHolder(itemView){
        private val patientNameTv = itemView.findViewById<TextView>(R.id.tvName)
        private val infoTv = itemView.findViewById<TextView>(R.id.tvInfo)
        private val ihNetworkTv = itemView.findViewById<TextView>(R.id.ihNetworkTv)
       // private val soreTv = itemView.findViewById<TextView>(R.id.soreTv)

        private val nrNetworkTv = itemView.findViewById<TextView>(R.id.nrNetworkTv)
        private val noNRNetworkTv = itemView.findViewById<TextView>(R.id.noNRNetworkTv)
        private val ihScoreTv = itemView.findViewById<TextView>(R.id.tvIHScoreTv)
        private val nrScoreTv = itemView.findViewById<TextView>(R.id.tvNrScoreTv)
        val linkToContinue = itemView.findViewById<Button>(R.id.btnAction)
       // private val nrScoreTv = itemView.findViewById<TextView>(R.id.ihNetworkTv)

        fun onBindView(p: PatientSearchResult){
            patientNameTv.text = "${p.patient?.firstname} ${p.patient?.lastname}"
            //soreTv.text = "${p.source}"
            val age = calculateAge(p.patient?.dateofbirth ?: "")
            val genders = "${p.patient?.gender?.firstOrNull()?.uppercase()}"
            val dob = formatDob(p.patient?.dateofbirth?:"")
            infoTv.text = "$genders . $age yrs . DOB $dob"
            if (p.isNRNetwork){
                nrScoreTv.text = String.format(
                    "NR: %.2f%% match",
                    (p.nrscore * 100)
                )
                noNRNetworkTv.visibility= View.GONE

            }
            if (p.isIHNetwork){
                ihScoreTv.text = String.format(
                    "IH: %.2f%% match",
                    (p.ihscore * 100)
                )
            }

            if (!p.isIHNetwork){
             ihScoreTv.visibility= View.GONE
             ihNetworkTv.visibility= View.GONE
            }
            if (!p.isNRNetwork){
                nrNetworkTv.visibility = View.GONE
                noNRNetworkTv.visibility= View.VISIBLE
                nrScoreTv.visibility = View.GONE
            }
            linkToContinue.setOnClickListener{
                val genders = "${p.patient?.gender?.firstOrNull()?.uppercase()}"
                patient.apply {
                    uuid=p.patient?.uuid
                    sourceId=p.patient?.sourceId
                    firstname=p.patient?.firstname
                    lastname=p.patient?.lastname
                    dateofbirth=p.patient?.dateofbirth
                    gender =genders
                    openmrsId=p.patient?.openmrsId
                    mpiId=p.patient?.mpiId
                    middlename=p.patient?.middlename
                    phonenumber=p.patient?.phonenumber
                    address1=p.patient?.address1
                    address2=p.patient?.address2
                    address3=p.patient?.address3
                    address6=p.patient?.address6
                    cityvillage=p.patient?.cityvillage
                    country=p.patient?.country
                    postalcode=p.patient?.postalcode
                    syncd=false
                }
                patientsDAO.insertPatients(listOf(patient))
                //Toast.makeText(itemView.context, "Clicked"+patient.firstname, Toast.LENGTH_SHORT).show()
            }

        }
    }
    fun formatDob(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            ""
        }
    }
    fun calculateAge(dob: String): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val birthDate = sdf.parse(dob) ?: return 0

        val dobCal = Calendar.getInstance()
        dobCal.time = birthDate

        val today = Calendar.getInstance()

        var age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        return age
    }
    override fun onBindViewHolder(holder: FilterResultViewHolder, position: Int) {
        holder.onBindView(patientList[position])
    }
    override fun getItemCount() = patientList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterResultViewHolder {
        return LayoutInflater.from(parent.context).inflate(R.layout.filter_result_item_layout, parent, false).run {
            FilterResultViewHolder(this)
        }
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