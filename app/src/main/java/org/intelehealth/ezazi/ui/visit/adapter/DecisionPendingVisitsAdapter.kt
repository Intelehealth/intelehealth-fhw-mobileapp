package org.intelehealth.ezazi.ui.visit.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Filter
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.activities.visitSummaryActivity.TimelineVisitSummaryActivity
import org.intelehealth.ezazi.app.AppConstants
import org.intelehealth.ezazi.database.dao.EncounterDAO
import org.intelehealth.ezazi.databinding.ListItemActivePatientEzaziBinding
import org.intelehealth.ezazi.models.dto.PatientDTO
import org.intelehealth.ezazi.utilities.DateAndTimeUtils
import org.intelehealth.ezazi.utilities.SessionManager

/**
 * Created by Kaveri Zaware on 23-01-2024
 * email - kaveri@intelehealth.org
 **/
class DecisionPendingVisitsAdapter(
    private val itemList: List<PatientDTO>,
    private val context: Context
) :
    RecyclerView.Adapter<DecisionPendingVisitsAdapter.ViewHolder>() {
    //private lateinit var rootView: View
    private var actionListener: OnActionListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ListItemActivePatientEzaziBinding = ListItemActivePatientEzaziBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activePatientModel = itemList[position]
        Log.d("tag", "onBindViewHolder: itemList :: " + itemList.size)
        holder.bind(activePatientModel, context)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(private val binding: ListItemActivePatientEzaziBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(activePatientModel: PatientDTO, context: Context) {
            binding.tvBedNumber.visibility = View.GONE
            binding.tvAlertCount.visibility = View.GONE

            if (activePatientModel.syncd != null && activePatientModel.syncd) {
                binding.tvNotUploaded.visibility = View.VISIBLE
                binding.tvNotUploaded.text =
                    context.resources.getString(R.string.visit_not_uploaded)
                binding.tvNotUploaded.setBackgroundColor(context.resources.getColor(R.color.lite_red))
            } else {
                binding.tvNotUploaded.visibility = View.GONE
            }

            val age: String? =
                DateAndTimeUtils.getAgeInYears(activePatientModel.dateofbirth, context)
            val ageInYear = context.getString(R.string.identification_screen_prompt_age) + " " + age
          /*  binding.listItemHeadTextView.text =
                String.format("%s %s", activePatientModel.firstname, activePatientModel.lastname)*/
            binding.listItemHeadTextView.text = activePatientModel.fullName
            binding.listItemBodyTextView.text = activePatientModel.openmrsId
            binding.tvAgeGender.text = ageInYear
            binding.tvStageName.text = activePatientModel.stage

            binding.tvBedNumber.text = "Bed No: ${activePatientModel.bedNo}"

            /*temp if (activePatientModel.enddate == null) {
                 binding.listItemIndicatorTextView.setText(R.string.active)
                 binding.listItemIndicatorTextView.setBackgroundColor(Color.GREEN)
             } else {
                 binding.listItemIndicatorTextView.setText(R.string.closed)
                 binding.listItemIndicatorTextView.setBackgroundColor(Color.RED)
             }*/

            /* val count = activePatientModel.alertFlagTotal
             binding.tvAlertCount.text = count.toString()

             if (count > 22) { // red
                 binding.tvAlertCount.background = ContextCompat.getDrawable(context, R.drawable.ic_high_alert)
                 binding.tvAlertCount.setTextColor(ContextCompat.getColor(context, R.color.colorHighAlert))
                 val padding = context.resources.getDimensionPixelSize(R.dimen.high_alert_top_padding)
                 binding.tvAlertCount.setPadding(0, padding, 0, 0)
             } else if (count >= 15) { // yellow
                 binding.tvAlertCount.background = ContextCompat.getDrawable(context, R.drawable.ic_yellow_alert)
                 binding.tvAlertCount.setTextColor(ContextCompat.getColor(context, R.color.colorMediumAlert))
                 binding.tvAlertCount.setPadding(0, 0, 0, 0)
             } else { // green
                 binding.tvAlertCount.background = ContextCompat.getDrawable(context, R.drawable.ic_normal_alert)
                 binding.tvAlertCount.setTextColor(ContextCompat.getColor(context, R.color.colorNormalAlert))
                 binding.tvAlertCount.setPadding(0, 0, 0, 0)
             }*/
// alert -> end

            /*  if (activePatientModel.obsExistsFlag) {
                  val anim: Animation = AlphaAnimation(1.0f, 0.2f)
                  anim.duration = 1500
                  anim.repeatMode = Animation.INFINITE
                  anim.repeatCount = Animation.INFINITE
                  binding.cardViewTodaysVisit.startAnimation(anim)
                  binding.cardViewTodaysVisit.setCardBackgroundColor(ContextCompat.getColor(context, R.color.blinkCardColor))
              }*/

            /* commented in old code if (!activePatientModel.birthOutcomeValue.isNullOrBlank()) {
                  // Uncomment the following lines if needed
                  // holder.btnEndVisit.visibility = View.VISIBLE
                  // holder.btnEndVisit.text = activePatientModel.birthOutcomeValue
              } else {
                  // Uncomment the following line if needed
                  // holder.btnEndVisit.visibility = View.GONE
              }*/


            val listener = View.OnClickListener {
                val visitSummary = Intent(context, TimelineVisitSummaryActivity::class.java)
                val patientUuid = activePatientModel.uuid

                val patientSelection = "uuid = ?"
                val patientArgs = arrayOf(patientUuid)
                val patientColumns = arrayOf("first_name", "middle_name", "last_name", "gender", "date_of_birth")
                val db = AppConstants.inteleHealthDatabaseHelper.writeDb
                val idCursor = db.query(
                    "tbl_patient",
                    patientColumns,
                    patientSelection,
                    patientArgs,
                    null,
                    null,
                    null
                )
                var visit_id = ""

                var end_date = ""
                var dob = ""
                var mGender = ""
                var patientName = ""
                var float_ageYear_Month = 0f

                if (idCursor.moveToFirst()) {
                    do {
                        mGender = idCursor.getString(idCursor.getColumnIndexOrThrow("gender"))
                        patientName =
                            idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")) + " " +
                                    idCursor.getString(idCursor.getColumnIndexOrThrow("last_name"))
                        dob = idCursor.getString((idCursor.getColumnIndexOrThrow("date_of_birth")))
                    } while (idCursor.moveToNext())
                }
                idCursor.close()

                val visitSelection = "patientuuid = ?"
                val visitArgs = arrayOf(patientUuid)
                val visitColumns = arrayOf("uuid, startdate", "enddate")
                val visitOrderBy = "startdate"
                val visitCursor = db.query(
                    "tbl_visit",
                    visitColumns,
                    visitSelection,
                    visitArgs,
                    null,
                    null,
                    visitOrderBy
                )

                if (visitCursor.count >= 1) {
                    if (visitCursor.moveToLast() && visitCursor != null) {
                        do {
                            if (visitCursor.getString(visitCursor.getColumnIndexOrThrow("uuid"))
                                    .equals("" + activePatientModel.visitUuid)
                            ) {
                                val columnIndex = visitCursor.getColumnIndexOrThrow("enddate")
                                if (!visitCursor.isNull(columnIndex)) {
                                    end_date =
                                        visitCursor.getString(visitCursor.getColumnIndexOrThrow("enddate"))                                } else {
                                    // Handle the case where 'enddate' is null or the column does not exist
                                }

                                visit_id =
                                    visitCursor.getString(visitCursor.getColumnIndexOrThrow("uuid"))
                            }
                        } while (visitCursor.moveToPrevious())
                    }
                }
                visitCursor.close()

                var encounterlocalAdultintial = ""
                var encountervitalsLocal: String? = null
                val encounterIDSelection = "visituuid = ?"
                val encounterIDArgs = arrayOf(visit_id)

                val encounterDAO = EncounterDAO()
                val encounterCursor = db.query(
                    "tbl_encounter",
                    null,
                    encounterIDSelection,
                    encounterIDArgs,
                    null,
                    null,
                    null
                )
                if (encounterCursor != null && encounterCursor.moveToFirst()) {
                    do {
                        if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VITALS") == encounterCursor.getString(
                                encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")
                            )
                        ) {
                            encountervitalsLocal =
                                encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"))
                        }
                        if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL") == encounterCursor.getString(
                                encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")
                            )
                        ) {
                            encounterlocalAdultintial =
                                encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"))
                        }
                    } while (encounterCursor.moveToNext())
                }
                encounterCursor.close()

                val past_visit = end_date.isEmpty()

                float_ageYear_Month = DateAndTimeUtils.getFloat_Age_Year_Month(dob)
                val sessionManager =
                    SessionManager(context) // Replace 'context' with your actual context
                val providerId: String = sessionManager.getProviderID()
                visitSummary.putExtra("visitUuid", visit_id)
                visitSummary.putExtra("patientUuid", patientUuid)
                visitSummary.putExtra("encounterUuidVitals", encountervitalsLocal)
                visitSummary.putExtra("encounterUuidAdultIntial", encounterlocalAdultintial)
                visitSummary.putExtra(
                    "EncounterAdultInitial_LatestVisit",
                    encounterlocalAdultintial
                )
                visitSummary.putExtra("patientNameTimeline", patientName)
                visitSummary.putExtra("name", patientName)
                visitSummary.putExtra("gender", mGender)
                visitSummary.putExtra("float_ageYear_Month", float_ageYear_Month)
                visitSummary.putExtra("tag", "")
                visitSummary.putExtra("providerID", providerId)
                visitSummary.putExtra("pastVisit", past_visit)
                visitSummary.putExtra("isDecisionPending", true)

                if (binding.tvAlertCount.tag == "1") {
                    visitSummary.putExtra("hasPrescription", "true")
                } else {
                    visitSummary.putExtra("hasPrescription", "false")
                }
                visitSummary.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(visitSummary)
                /*val patientStatus = "returning"
                val intent = Intent(context, PatientDetailActivity::class.java)
                intent.putExtra("patientUuid", activePatientModel.patientuuid)
                intent.putExtra("status", patientStatus)
                intent.putExtra("tag", "")

                if (holder.ivPriscription.tag == "1") {
                    intent.putExtra("hasPrescription", "true")
                } else {
                    intent.putExtra("hasPrescription", "false")
                }
                context.startActivity(intent)*/
            }


            binding.root.setOnClickListener(listener)
        }
    }

    fun setActionListener(actionListener: OnActionListener) {
        this.actionListener = actionListener
    }

    interface OnActionListener {
        fun onEndVisitClicked(patientDTO: PatientDTO, hasPrescription: Boolean)
    }
    /*
         fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(charSequence: CharSequence): FilterResults {
                    val keyword = charSequence.toString()
                    val listFiltered = mutableListOf<PatientDTO>()

                    if (keyword.isEmpty()) {
                        listFiltered.addAll(activePatientModels)
                    } else {
                        for (row in activePatientModels) {
                            if (row.isContains(keyword)) {
                                listFiltered.add(row)
                            }
                        }
                    }

                    val filterResults = FilterResults()
                    filterResults.values = listFiltered
                    return filterResults
                }

                override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                    filteractivePatient = filterResults.values as List<ActivePatientModel>
                    notifyDataSetChanged()
                }
            }
        }
    */

}