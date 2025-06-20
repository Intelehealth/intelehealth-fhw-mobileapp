package org.intelehealth.ncd.category.adapter

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.ncd.R
import org.intelehealth.ncd.callbacks.PatientClickedListener
import org.intelehealth.ncd.databinding.ListItemSearchProtocolwiseBinding
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.utils.DateAndTimeUtils

class CategoryRecyclerViewAdapter(
    private val patientList: List<Patient>,
    private val resources: Resources,
    private val context: Context,
    private val listener: PatientClickedListener
) : RecyclerView.Adapter<CategoryRecyclerViewAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding: ListItemSearchProtocolwiseBinding = ListItemSearchProtocolwiseBinding.inflate(
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
        private val binding: ListItemSearchProtocolwiseBinding,
        private val listener: PatientClickedListener
    ) : RecyclerView.ViewHolder(binding.root) {
        //var patient: PatientDetailsDTO? = null


        /* fun setData(patient: PatientDetailsDTO, resources: Resources, context: Context, holder: CategoryViewHolder){
            holder.patient = patient
            //val sessionManager = SessionManager(context)

            patient.apply {
                // 1. Age + Gender
                //setGenderAgeLocal(context, holder.binding.searchGender, patient.dateofbirth, patient.gender, sessionManager)

                // 2. Name
                holder.binding.searchName.text = "${this.firstname} ${this.lastname}"

                // 3. Priority Tag
                holder.binding.flPriority.visibility = if (this.emergency) View.VISIBLE else View.GONE

                // 4. Visit Start Date
                if (!this.visit_startdate.isNullOrEmpty()) {

                    val isDoctorVisit = Visit().isDoctorVisit(this.visitDTO?.uuid)

                    if (isDoctorVisit) {
                        if (this.prescription_exists) {
                            holder.binding.prescReceivedCV.visibility = View.VISIBLE
                            holder.binding.prescPendingCV.visibility = View.GONE
                        } else {
                            holder.binding.prescPendingCV.visibility = View.VISIBLE
                            holder.binding.prescReceivedCV.visibility = View.GONE
                        }

                        this.visitDTO?.let { visit ->
                            if (visit.sync.equals("false")) {
                                holder.binding.prescPendingCV.visibility = View.GONE
                                holder.binding.prescReceivedCV.visibility = View.GONE
                            }
                            if (visit.enddate != null) {
                                // You may choose to update UI if needed
                            }
                        }

                    } else {
                        // Not doctor visit
                        holder.binding.prescReceivedCV.visibility = View.GONE
                        holder.binding.prescPendingCV.visibility = View.GONE
                    }

                    holder.binding.fuItemCalendar.visibility = View.VISIBLE

                    var visitDate = this.visit_startdate
                   *//* if (sessionManager.appLanguage.equals("hi", ignoreCase = true)) {
                        visitDate = StringUtils.en_hi_dob_three(visitDate)
                    }*//*

                    holder.binding.searchDateRelative.visibility = View.VISIBLE
                    holder.binding.searchDateRelative.text = visitDate

                } else {
                    holder.binding.prescPendingCV.visibility = View.GONE
                    holder.binding.prescReceivedCV.visibility = View.GONE
                    holder.binding.fuItemCalendar.visibility = View.GONE
                    holder.binding.searchDateRelative.visibility = View.GONE
                }

                // 6. Patient Profile Pic
               *//* try {
                    profileImage = imagesDAO.getPatientProfileChangeTime(this.uuid)
                } catch (e: Exception) {
                   // FirebaseCrashlytics.getInstance().recordException(e)
                }*//*

             *//*   if (this.patientPhoto.isNullOrEmpty()) {
                    if (NetworkConnection.isOnline(context)) {
                        profilePicDownloaded(it, holder)
                    }
                }*//*

                *//*if (profileImage != profileImage1) {
                    if (NetworkConnection.isOnline(context)) {
                        profilePicDownloaded(it, holder)
                    }
                }*//*

                if (!this.patientPhoto.isNullOrEmpty()) {
                    val requestBuilder: RequestBuilder<Drawable> = Glide.with(holder.itemView.context)
                        .asDrawable()
                        .sizeMultiplier(0.3f)

                    Glide.with(context)
                        .load(this.patientPhoto)
                        .thumbnail(requestBuilder)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(holder.binding.profileImgview)

                } else {
                    holder.binding.profileImgview.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avatar1))
                }
            }

        }


        }*/

        fun setData(patient: Patient, resources: Resources, context: Context) {
            val headText = "${patient.firstName} ${patient.lastname}, ${patient.openmrsId}"
            binding.searchName.text = headText

           /* val bodyText = "${resources.getText(R.string.age)}: ${
                DateAndTimeUtils.getAgeInYearMonth(
                    patient.dateOfBirth,
                    context = context
                )
            }"*/
            Log.d("TAG", "setData: gender: "+patient.gender)
            val bodyText = "${resources.getText(R.string.age)} ${patient.gender} ${DateAndTimeUtils.getAgeFollowUp(patient.dateOfBirth, context)}"

            binding.searchGender.text = bodyText

            binding.root.setOnClickListener {
                listener.onPatientClicked(patient)
            }
        }
    }
}

