package org.intelehealth.ncd.category.adapter.utils

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.utils.DateAndTimeUtils
import java.io.File


@BindingAdapter("genderWithAge")
fun TextView.setGenderWithAge(patient: PatientVisitDetails?) {
    patient?.let {
        val gender = it.gender ?: ""
        val age = DateAndTimeUtils.getAgeFollowUp(it.dateOfBirth, context)
        text = "$gender $age"
    }

}
@BindingAdapter("profileUrl")
fun bindProfileImage(imageView: ImageView?, url: String?) {
    if (imageView != null && !url.isNullOrEmpty()) {
        val requestBuilder = Glide.with(imageView.context).asDrawable().sizeMultiplier(0.25f)
        Glide.with(imageView.context)
            .load(File(url))
            .thumbnail(requestBuilder)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(imageView)
    }
}
@BindingAdapter("visibleIfNotNullOrEmpty")
fun View.setVisibleIfNotNullOrEmpty(value: String?) {
    visibility = if (value.isNullOrEmpty()) View.GONE else View.VISIBLE
}

@BindingAdapter("prescriptionStatusVisibility")
fun setPrescriptionStatusVisibility(view: View, visitDetail: PatientVisitDetails?) {
    val isPrescriptionExist = visitDetail?.isPrescriptionExist ?: false
    val isStartDateNull = visitDetail?.startDate == null

    view.visibility = if (isPrescriptionExist || isStartDateNull) {
        View.GONE
    } else {
        View.VISIBLE
    }
}

@BindingAdapter("patientDisplayText")
fun setPatientDisplayText(textView: TextView, visitDetail: PatientVisitDetails?) {
    visitDetail?.let {
        val fullName = listOfNotNull(it.firstName, it.lastName)
            .joinToString(" ")
            .trim()

        val openmrsId = it.openmrsId?.takeIf { id -> id.isNotBlank() }

        val displayText = listOfNotNull(fullName.takeIf { it.isNotBlank() }, openmrsId)
            .joinToString(", ")

        Log.d("TAG", "setPatientDisplayText: openmrsId  : "+openmrsId)
        Log.d("TAG", "setPatientDisplayText: displayText  : "+displayText)
        Log.d("TAG", "setPatientDisplayText: fullName  : "+fullName)

        textView.text = displayText
    }
}


