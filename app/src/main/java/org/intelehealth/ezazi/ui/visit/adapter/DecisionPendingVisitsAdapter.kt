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

            binding.listItemHeadTextView.text = activePatientModel.fullName
            binding.listItemBodyTextView.text = activePatientModel.openmrsId
            binding.tvAgeGender.text = ageInYear
            binding.tvStageName.text = activePatientModel.stage

            val listener = View.OnClickListener {
                val visitSummary = Intent(context, TimelineVisitSummaryActivity::class.java)
                val patientUuid = activePatientModel.uuid

                val patientSelection = "uuid = ?"
                val patientArgs = arrayOf(patientUuid)
                val patientColumns =
                    arrayOf("first_name", "middle_name", "last_name", "gender", "date_of_birth")
                val db = AppConstants.inteleHealthDatabaseHelper.writeDb
                val idCursor = db.query("tbl_patient", patientColumns, patientSelection, patientArgs, null, null, null
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
                                        visitCursor.getString(visitCursor.getColumnIndexOrThrow("enddate"))
                                } else {
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
                val sessionManager = SessionManager(context)
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

                visitSummary.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(visitSummary)
            }
            binding.root.setOnClickListener(listener)
        }
    }

}