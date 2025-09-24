package org.intelehealth.app.activities.visitSummaryActivity.ncdinfo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.intelehealth.app.R
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.models.dto.PatientAttributesDTO
import org.intelehealth.app.utilities.CustomLog
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class NcdInfoViewAndShareHelper (private val context: Context) {

    private val baseUrl = "https://afitraining.ekalarogya.org:3004/ncdinfo/"

    fun generateFileUrls(fileNames: List<String>): List<String> {
        return fileNames.map { "$baseUrl$it" }
    }


    fun generateWhatsappMessage(phoneNumber: String, fileUrls: List<String>): String {
        val concatenatedUrls = fileUrls.joinToString(separator = ",\n\n")
        val messageText = context.getString(R.string.ncd_info_whatsapp_msg) + "\n" +concatenatedUrls

        val encodedMessage = try {
            URLEncoder.encode(messageText, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            messageText
        }

        return "https://api.whatsapp.com/send?phone=$phoneNumber&text=$encodedMessage"
    }


    fun showShareDialog(
        patientUuid: String?,
        infoModulesFileUrlsList: List<String>
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
                val whatsappMessage = generateWhatsappMessage(phoneNumber, infoModulesFileUrlsList)
                CustomLog.v("whatsappMessage", whatsappMessage)
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(whatsappMessage)))
                alertDialog.dismiss()
            } else {
                Toast.makeText(context, context.getString(R.string.enter_whatsapp_number), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun viewNcdInfoModules(
        infoModulesFileUrlsList: List<String>
    ) {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        val inflater = LayoutInflater.from(context)
        val convertView = inflater.inflate(R.layout.layout_dialog_share_info_module, null)
        alertDialogBuilder.setView(convertView)

        val editText = convertView.findViewById<EditText>(R.id.et_mobileno_info)
        val tvTitle = convertView.findViewById<TextView>(R.id.tv_message_info)
        val tvMessageBody = convertView.findViewById<TextView>(R.id.tv_message_body_info)
        val shareBtn = convertView.findViewById<Button>(R.id.btn_share_info)
        editText.visibility = View.GONE
        tvTitle.text = context.getString(R.string.suggested_info_modules)
        tvMessageBody.visibility = View.VISIBLE

        val formattedLinks = infoModulesFileUrlsList.mapIndexed { index, url ->
            val name = getModuleNameFromUrl(url)
            "${index + 1}. <a href=\"$url\">$name</a>"
        }.joinToString("<br><br>")
        tvMessageBody.text = HtmlCompat.fromHtml(formattedLinks, HtmlCompat.FROM_HTML_MODE_LEGACY)
        tvMessageBody.movementMethod = LinkMovementMethod.getInstance()

        shareBtn.visibility  =View.GONE

        val alertDialog = alertDialogBuilder.create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg)
        alertDialog.window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        val width = context.resources.getDimensionPixelSize(R.dimen.internet_dialog_width)
        alertDialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        alertDialog.show()
    }

    private fun getModuleNameFromUrl(url: String): String {
        val fileName = url.substringAfterLast("/")
        val withoutExt = fileName.substringBeforeLast(".")

        // remove prefix & language code
        val cleaned = withoutExt
            .removePrefix("hypertension_followup_")
            .substringBeforeLast("_")

        return cleaned
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }
    }

}