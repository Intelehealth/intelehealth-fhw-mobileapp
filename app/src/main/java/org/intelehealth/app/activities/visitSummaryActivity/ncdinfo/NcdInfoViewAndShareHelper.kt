package org.intelehealth.app.activities.visitSummaryActivity.ncdinfo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants.NCD_REPORT_BASE_URL
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.database.dao.SyncDAO
import org.intelehealth.app.database.dao.VisitAttributeListDAO
import org.intelehealth.app.database.dao.VisitsDAO
import org.intelehealth.app.databinding.ActivityVisitSummaryNewBinding
import org.intelehealth.app.models.dto.PatientAttributesDTO
import org.intelehealth.app.models.dto.VisitAttributeDTO
import org.intelehealth.app.syncModule.SyncUtils
import org.intelehealth.app.utilities.CustomLog
import org.intelehealth.app.utilities.UuidDictionary
import org.intelehealth.app.utilities.exception.DAOException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import kotlin.uuid.Uuid

class NcdInfoViewAndShareHelper(
    private val context: Context,
    private val mBinding: ActivityVisitSummaryNewBinding,
    private val visitUuid: String,
    private val visitsDAO: VisitsDAO,
    private val visitAttributeListDAO: VisitAttributeListDAO
) {

    fun showShareDialog(
        patientUuid: String?,
        infoModulesFileUrlsList: List<HealthModuleItem>
    ) {
        val patientsDAO = PatientsDAO()
        val patientWhatsappNo: String? = patientsDAO.getPatientAttributeByPatientUuid(
            patientUuid,
            PatientAttributesDTO.Column.SELF_OR_FAMILY_WHATSAPP.value)

        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        val inflater = LayoutInflater.from(context)
        val convertView = inflater.inflate(R.layout.layout_dialog_share_info_module, null)
        val shareBtn = convertView.findViewById<Button>(R.id.btn_share_info)
        val editText = convertView.findViewById<EditText>(R.id.et_mobileno_info)
        val tvTitle = convertView.findViewById<TextView>(R.id.tv_message_info)
        alertDialogBuilder.setView(convertView)


        tvTitle.text = context.getString(R.string.share_ncd_info_dialog_title)
        if (!patientWhatsappNo.isNullOrEmpty() && patientWhatsappNo != "-" && patientWhatsappNo.all { it.isDigit() }) {
            editText.setText(patientWhatsappNo)
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg)
        alertDialog.window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        val width = context.resources.getDimensionPixelSize(R.dimen.internet_dialog_width)
        alertDialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        alertDialog.show()

        shareBtn.setOnClickListener {
            val phoneNumber = editText.text.toString()
            if (phoneNumber.isNotEmpty() && phoneNumber.length==10) {
                val isInserted: Boolean =
                    visitAttributeListDAO.checkInfoShareInsertedOrNot(visitUuid)
                if (!isInserted) {
                    try {
                        visitAttributeListDAO.insertVisitAttributes(
                            visitUuid,
                            /*UuidDictionary.HEALTH_INFO_SHARE_ATTRIBUTE_NAME*/"true",
                            UuidDictionary.HEALTH_INFO_SHARE_ATTRIBUTE
                        )
                        visitsDAO.updateVisitSync(visitUuid, "0")
                        SyncDAO().pushDataApi()
                    } catch (e: DAOException) {
                        throw RuntimeException(e)
                    }
                }
                val whatsappMessage = generateWhatsappMessage(phoneNumber, infoModulesFileUrlsList, patientUuid)
                CustomLog.v("whatsappMessage", whatsappMessage)
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(whatsappMessage)))
                alertDialog.dismiss()
            } else {
                Toast.makeText(context, context.getString(R.string.enter_whatsapp_number), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun viewNcdInfoModuleInfoNew(infoModulesFileUrlsList: List<HealthModuleItem>, binding: ActivityVisitSummaryNewBinding) {
        val linearLayoutManager = LinearLayoutManager(context)
        binding.layoutVisitSummaryItems.layoutHealthInfoModule.rvInfoModules.layoutManager = linearLayoutManager
        val ncdHealthInfoAdapter = NcdHealthInfoAdapter(infoModulesFileUrlsList, context)
        binding.layoutVisitSummaryItems.layoutHealthInfoModule.rvInfoModules.adapter = ncdHealthInfoAdapter

    }
    private fun generateWhatsappMessage(
        phoneNumber: String,
        fileUrls: List<HealthModuleItem>,
        patientUuid: String?
    ): String {
        val ncdMessageTitle = context.getString(R.string.ncd_report)
        //val baseMessage = context.getString(R.string.msg_ekal_thank_you)
        val ncdReportUrl = NCD_REPORT_BASE_URL + patientUuid
        val baseMessage = if (fileUrls.isNotEmpty()) {
            context.getString(R.string.msg_ekal_thank_you)
        } else {
            context.getString(R.string.msg_ekal_thank_you_ncd_report)
        }
        val urlsPart = if (fileUrls.isNotEmpty()) {
            fileUrls.joinToString(separator = "\n\n") { "${it.displayName}: ${it.url}" } +
                    "\n\n$ncdMessageTitle: $ncdReportUrl"
        } else {
            "$ncdMessageTitle: $ncdReportUrl"
        }
        val messageText = "$baseMessage\n\n$urlsPart"
        val encodedMessage = try {
            URLEncoder.encode(messageText, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            messageText
        }

        Log.d("TAG", "generateWhatsappMessage: encodedMessage : $encodedMessage")

        return "https://api.whatsapp.com/send?phone=$phoneNumber&text=$encodedMessage"
    }

}