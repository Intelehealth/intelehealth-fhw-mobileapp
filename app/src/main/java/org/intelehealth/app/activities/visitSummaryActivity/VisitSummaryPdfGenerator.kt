package org.intelehealth.app.activities.visitSummaryActivity

import android.content.Context
import androidx.core.content.ContextCompat
import org.intelehealth.app.R
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.models.DocumentObject
import org.intelehealth.app.models.VisitSummaryPdfData
import org.intelehealth.app.utilities.Base64Utils
import org.intelehealth.app.utilities.VisitSummarySvgUtils
import java.io.File


/**
 * Created By Tanvir Hasan on 5/29/24 11:02 PM
 * Email: tanvirhasan553@gmail.com
 */
class VisitSummaryPdfGenerator {
    companion object {
        @JvmStatic
        fun generateHtmlContent(
            context: Context,
            visitSummaryPdfData: VisitSummaryPdfData,
        ): String {
            val meta =
                "<meta charset=\"utf-8\" />" + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />" + "<link rel=\"icon\" type=\"image/x-icon\" href=\"favicon.ico\" />\n" + "<title>Intelehealth</title>\n" + "<link href=\"https://fonts.googleapis.com/icon?family=Material+Icons\" rel=\"stylesheet\" />" + "<link rel=\"stylesheet\" href=\"https://use.fontawesome.com/releases/v5.8.2/css/all.css\" integrity=\"sha384-oS3vJWv+0UjzBfQzYUhtDYW+Pj2yciDJxpsK1OYPAYjqT085Qq/1cq5FLXAZQ7Ay\" crossorigin=\"anonymous\" />" + "<link rel=\"apple-touch-icon\" href=\"/assets/icons/icon-180x180.png\" />\n" + " <link rel=\"manifest\" href=\"manifest.webmanifest\" />\n" + "<link href=\"https://fonts.googleapis.com/css?family=DM Sans\" rel=\"stylesheet\" />\n" + "<meta name=\"theme-color\" content=\"#2e1e91\" />\n" + "<link rel=\"preconnect\" href=\"https://fonts.gstatic.com\">\n" + "<link href=\"https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500&display=swap\" rel=\"stylesheet\">\n" + "<link href=\"https://fonts.googleapis.com/icon?family=Material+Icons\" rel=\"stylesheet\">\n" + "<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css\" integrity=\"sha384-xOolHFLEh07PJGoPkLv1IbcEPTNtaed2xpHsD9ESMhqIYd0nLMwNLD69Npy4HI+N\" crossorigin=\"anonymous\">";

            return StringBuilder()
                .append("<html lang=\"en\">")
                .append("<head>")
                .append(meta)
                .append(getStyle(context))
                .append("</head>")
                .append("<body>")
                .append("<div>")
                .append(getUserHtml(context, visitSummaryPdfData))
                .append(getChwHtml(context, visitSummaryPdfData))
                .append(getVitalHtml(context, visitSummaryPdfData))
                .append(getVisitReasonHtml(context, visitSummaryPdfData))
                .append(getPhysicalExamHtml(context, visitSummaryPdfData))
                .append(getMedicalHistoryHtml(context, visitSummaryPdfData))
                .append(getAdditionalNoteHtml(context, visitSummaryPdfData))
                .append(getAdditionalDocHtml(context, visitSummaryPdfData))
                .append(getDoctorSpecialityHtml(context, visitSummaryPdfData.doctorSpeciality))
                .append(getPriorityHtml(context, visitSummaryPdfData))
                .append("</div>")
                .append("</body>")
                .append("</html>").toString()
        }

        private fun getUserHtml(context: Context, visitSummaryPdfData: VisitSummaryPdfData): Any? {
            return "    <div class=\"container\">\n" +
                    "        <div class=\"header\">\n" +
                    "            <img src=\"${getPatientImage(visitSummaryPdfData.patientImage)}\" alt=\"Profile Image\" class=\"profile-image\"/>\n" +
                    "            <div>\n" +
                    "                <h1>${visitSummaryPdfData.patientName}</h1>\n" +
                    "                <p style=\"color: #949494;\">${visitSummaryPdfData.patientId}</p>\n" +
                    "            </div>\n" +
                    "             <p style=\"color: #949494; margin-left: 60%;\">${visitSummaryPdfData.genderAge}</p>\n" +
                    "        </div></div>\n"

        }

        private fun getChwHtml(context: Context, visitSummaryPdfData: VisitSummaryPdfData): Any? {
            return "        <div class=\"section\">\n" +
                    "            <div>\n" +
                    "         <ul>\n" +
                    "             <li><label>• ${
                        ContextCompat.getString(
                            context,
                            R.string.chw_worker
                        )
                    }</label><label> ${visitSummaryPdfData.chwName}</label></li>\n" +
                    "             <li><label>• ${
                        ContextCompat.getString(
                            context,
                            R.string.visitID
                        )
                    }</label><label> ${visitSummaryPdfData.visitId}</label></li>\n" +
                    "         </ul>" +
                    "            </div></div>\n"

        }

        private fun getVitalHtml(context: Context, visitSummaryPdfData: VisitSummaryPdfData): Any? {
            return "        <div class=\"section\">\n" +
                    "            <h2>\n" + VisitSummarySvgUtils.getVitalsSvg() +
                    "                ${
                        ContextCompat.getString(
                            context,
                            R.string.visit_summary_vitals
                        )
                    }\n" +
                    "            </h2>\n" +
                    "        </div>\n" +
                    "        <div class=\"section\">\n" +
                    "            <p>\n" +
                    "                ${ContextCompat.getString(context, R.string.details)}\n" +
                    "            </p>\n" +
                    "            <ul>\n" +
                    "             <li><label>• ${
                        ContextCompat.getString(
                            context,
                            R.string.height_cm
                        )
                    }</label><label> ${visitSummaryPdfData.height}</label></li>\n" +
                    "             <li><label>• ${
                        ContextCompat.getString(
                            context,
                            R.string.weight_kg
                        )
                    }</label><label> ${visitSummaryPdfData.weight}</label></li>\n" +
                    "             <li><label>• ${
                        ContextCompat.getString(
                            context,
                            R.string.visit_summary_bmi
                        )
                    }</label><label> ${visitSummaryPdfData.bmi}</label></li>\n" +
                    "             <li><label>• ${
                        ContextCompat.getString(
                            context,
                            R.string.visit_summary_bp
                        )
                    }</label><label> ${visitSummaryPdfData.bp}</label></li>\n" +
                    "             <li><label>• ${
                        ContextCompat.getString(
                            context,
                            R.string.visit_summary_pulse
                        )
                    }</label><label> ${visitSummaryPdfData.pulse}</label></li>\n" +
                    "             <li><label>• ${visitSummaryPdfData.tempHeader}</label><label> ${visitSummaryPdfData.temp}</label></li>\n" +
                    "             <li><label>• ${
                        ContextCompat.getString(
                            context,
                            R.string.table_spo2
                        )
                    }</label><label> ${visitSummaryPdfData.spoTwo}</label></li>\n" +
                    "             <li><label>• ${
                        ContextCompat.getString(
                            context,
                            R.string.respiratory_rate
                        )
                    }</label><label> ${visitSummaryPdfData.respiratory}</label></li>\n" +
                    "             <li><label>• ${
                        ContextCompat.getString(
                            context,
                            R.string.blood_group_txt
                        )
                    }</label><label> ${visitSummaryPdfData.blGroup}</label></li>\n" +
                    "            </ul></div></div>\n"

        }

        private fun getVisitReasonHtml(
            context: Context,
            visitSummaryPdfData: VisitSummaryPdfData,
        ): Any {
            return "        <div class=\"section\">\n" +
                    "            <h2>\n" + VisitSummarySvgUtils.getVisitReasonSvg() +
                    "                ${
                        ContextCompat.getString(
                            context,
                            R.string.complaint_dialog_title
                        )
                    }\n" +
                    "            </h2>\n" +
                    "            <b><h3>${
                        ContextCompat.getString(
                            context,
                            R.string.chief_complaint
                        )
                    }</h3></b>\n" + "" +
                    "            ${
                        getChiefComplainList(
                            context,
                            visitSummaryPdfData
                        )
                    }" + visitSummaryPdfData.chiefComplain +
                    "        </div>\n"

        }

        private fun getChiefComplainList(
            context: Context,
            visitSummaryPdfData: VisitSummaryPdfData,
        ): String {
            val buttonStringBuilder = StringBuilder()
            for (name in visitSummaryPdfData.chiefComplaintList) {
                buttonStringBuilder
                    .append("<button class=\"button\">${name}</button>")
            }
            return StringBuilder()
                .append("<div>")
                .append(buttonStringBuilder.toString())
                .append("</div>")
                .toString()
        }

        private fun getPhysicalExamHtml(
            context: Context,
            visitSummaryPdfData: VisitSummaryPdfData,
        ): Any {
            return "        <div class=\"section\">\n" +
                    "            <h2>\n" + VisitSummarySvgUtils.getPhysicalExamSvg() +
                    "                ${
                        ContextCompat.getString(
                            context,
                            R.string.physical_examination
                        )
                    }\n" +
                    "            </h2>\n" + visitSummaryPdfData.physicalExam + "<br /><br />" + getPhysicalExamImages(
                visitSummaryPdfData.physicalExamImageList
            ) +
                    "        </div>\n" + ""
        }

        private fun getMedicalHistoryHtml(
            context: Context,
            visitSummaryPdfData: VisitSummaryPdfData,
        ): Any {
            if (visitSummaryPdfData.medicalHistory.isEmpty()) return ""
            return "        <div class=\"section\">\n" +
                    "            <h2>\n" + VisitSummarySvgUtils.getMedicalHistory() +
                    "                ${
                        ContextCompat.getString(
                            context,
                            R.string.visit_summary_medical_history
                        )
                    }\n" +
                    "            </h2>\n" + visitSummaryPdfData.medicalHistory +
                    "        </div>\n" + ""
        }

        private fun getAdditionalNoteHtml(
            context: Context,
            visitSummaryPdfData: VisitSummaryPdfData,
        ): Any {
            if (visitSummaryPdfData.additionalNote.isEmpty()) return ""
            return "        <div class=\"section\">\n" +
                    "            <h2>\n" + VisitSummarySvgUtils.getAdditionalNote() +
                    "                ${
                        ContextCompat.getString(
                            context,
                            R.string.additional_notes
                        )
                    }\n" +
                    "            </h2>\n" + visitSummaryPdfData.additionalNote +
                    "        </div>\n" + ""

        }

        private fun getPriorityHtml(
            context: Context,
            visitSummaryPdfData: VisitSummaryPdfData,
        ): Any {
            return "        <div class=\"section\">\n" +
                    "            <h3>\n" +
                    "                 ${
                        ContextCompat.getString(
                            context,
                            R.string.priority_visits
                        )
                    }\n" +
                    "            </h3>\n" +
                    "            <p>${visitSummaryPdfData.priorityVisit}</p>\n" +
                    "        </div>\n"

        }

        private fun getAdditionalDocHtml(
            context: Context,
            visitSummaryPdfData: VisitSummaryPdfData,
        ): Any {
            if (visitSummaryPdfData.additionalDocList.isEmpty()) return ""
            return "        <div class=\"section\">\n" +
                    "            <h3>  ${
                        ContextCompat.getString(
                            context,
                            R.string.add_additional_documents
                        )
                    }(${visitSummaryPdfData.additionalDocList.size})\n" +
                    "            </h3>\n" + getAdditionalDocImages(visitSummaryPdfData.additionalDocList) +
                    "        </div>\n"

        }

        private fun getDoctorSpecialityHtml(context: Context, doctorSpeciality: String): Any {
            if (doctorSpeciality.isEmpty()) return ""
            return "        <div class=\"section\">\n" +
                    "            <h2>\n" + VisitSummarySvgUtils.getDoctorSpeciality() +
                    "                ${
                        ContextCompat.getString(
                            context,
                            R.string.doctor_speciality
                        )
                    }\n" +
                    "            </h2>\n" +
                    "            <p>${doctorSpeciality}</p>\n" +
                    "        </div>\n"

        }

        fun intToHex(color: Int): String {
            // Convert the integer color value to a hex string
            return String.format("#%06X", 0xFFFFFF and color)
        }

        private fun getPhysicalExamImages(fileList: MutableList<File>): String {
            if (fileList.size <= 0) return ""
            val stringBuilder = StringBuilder()
            stringBuilder.append("<div class=\"image-container\">")
            for (file in fileList) {
                stringBuilder.append(" <img src=\'file://" + file.absolutePath + "'/>")
            }
            stringBuilder.append(" </div>")

            return stringBuilder.toString()
        }

        private fun getAdditionalDocImages(fileList: List<DocumentObject>): String {
            if (fileList.isEmpty()) return ""
            val stringBuilder = StringBuilder()
                .append("<div class=\"image-container\">")
            for (doc in fileList) {
                stringBuilder
                    .append(" <img src=\'file://${doc.documentPhoto}'/>")
            }
            stringBuilder.append("</div>")
            return stringBuilder.toString()
        }

        private fun getPatientImage(patientImage: String): String {
            var patientProfilePhoto = ""
            if (patientImage.isNotEmpty()) {
                patientProfilePhoto =
                    Base64Utils().getBase64FromFileWithConversion(patientImage)
                val format = patientImage.substring(patientImage.length - 3)
                patientProfilePhoto = if (format.equals("png", ignoreCase = true)) {
                    "data:image/png;base64,$patientProfilePhoto"
                } else {
                    "data:image/jpg;base64,$patientProfilePhoto"
                }
            } else {
                patientProfilePhoto =
                    "https://dev.intelehealth.org/intelehealth/assets/svgs/user.svg"
            }
            return patientProfilePhoto
        }


        private fun getStyle(context: Context): Any {
            return "<style>\n" +
                    "      body {\n" +
                    "            width: 80%;\n" +
                    "            font-family: DM Sans;\n" +
                    "            background-color: #FFFFFF;\n" +
                    "            margin: 20px auto;\n" +
                    "            padding: 0;\n" +
                    "        }\n" +
                    "        .container {\n" +
                    "            width: 80%;\n" +
                    "            margin: 20px auto;\n" +
                    "            background-color: #FFFFFF;\n" +
                    "            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\n" +
                    "        }\n" +
                    "        .header {\n" +
                    "            display: flex;\n" +
                    "            align-items: center;\n" +
                    "            border-bottom: 1px solid #bdbdbd;\n" +
                    "            padding-bottom: 10px;\n" +
                    "            margin-bottom: 20px;\n" +
                    "        }\n" +
                    "        .profile-image {\n" +
                    "            border-radius: 50%;\n" +
                    "            width: 50px;\n" +
                    "            height: 50px;\n" +
                    "            margin-right: 15px;\n" +
                    "        }\n" +
                    "        .header h1 {\n" +
                    "            margin: 0;\n" +
                    "            font-size: 24px;\n" +
                    "        }\n" +
                    "        .header p {\n" +
                    "            margin: 5px 0;\n" +
                    "        }\n" +
                    "        .icon {\n" +
                    "            width: 24px;\n" +
                    "            height: 24px;\n" +
                    "            vertical-align: middle;\n" +
                    "            margin-right: 5px;\n" +
                    "        }\n" +
                    "        .section {\n" +
                    "            margin: 20px 0;\n" +
                    "        }\n" +
                    "        .section h2 {\n" +
                    "            display: flex;\n" +
                    "            align-items: center;\n" +
                    "            border-bottom: 1px solid #bdbdbd;\n" +
                    "            page-break-inside: avoid;\n" +
                    "        }\n" +
                    "        .section h2 svg {\n" +
                    "            margin-right: 10px;\n" +
                    "            margin-bottom: 10px;\n" +
                    "            page-break-inside: avoid;\n" +
                    "        }\n" +
                    "        ul {\n" +
                    "            list-style-type: none;\n" +
                    "            padding: 0;\n" +
                    "            display: table;" +
                    "        }\n" +
                    "        ul li {\n" +
                    "            padding: 5px 0;\n" +
                    "            display: table-row;" +
                    "            padding-left: 20px;" +
                    "        }\n" +
                    "        .images, .documents {\n" +
                    "            display: flex;\n" +
                    "            flex-wrap: wrap;\n" + "" +
                    "            height: auto;" +
                    "        }\n" +
                    "        .images img, .documents img {\n" +
                    "            width: 100px;\n" +
                    "            height: 100px;\n" +
                    "            margin: 5px;\n" + "" +
                    "        }\n" + "" +
                    "         .image-container {" +
                    "            page-break-inside: avoid;\n" +
                    "            break-inside: avoid-column; \n" +
                    "            width: 100%; \n" +
                    "            display: block; " +
                    "            flex-wrap: wrap;\n" + "" +
                    "        }" +
                    "        .image-container img { " +
                    "           width: 100px;\n" +
                    "           height: 100px;\n" +
                    "           page-break-inside: avoid;\n" +
                    "           margin-bottom: 10px;" +
                    "        }" +
                    "        li  {\n" +
                    "             padding-left: 20px;\n" +
                    "             display: table-row;\n" +
                    "         }  \n" +
                    "        li label {\n" +
                    "             display: table-cell;\n" +
                    "             color: #545454;" +
                    "         }\n" +
                    "        li label:not(:first-child) {\n" +
                    "             padding-left: 40px;\n" +
                    "             color: #000000;" +
                    "          }" + "" +
                    "         .button {\n" +
                    "              background-color: ${
                        intToHex(
                            ContextCompat.getColor(
                                context,
                                R.color.colorPrimary
                            )
                        )
                    };\n" +
                    "              border: none;\n" +
                    "              color: white;\n" +
                    "              padding: 20px;\n" +
                    "              text-align: center;\n" +
                    "              text-decoration: none;\n" +
                    "              display: inline-block;\n" +
                    "              font-size: 16px;\n" +
                    "              margin: 4px 2px;\n" +
                    "              border-radius: 12px;" +
                    "              margin: 10px;" +
                    "          }" +
                    "           .text-with-margin {\n" +
                    "              margin-left: 20px;\n" +
                    "            }" +
                    "    </style>\n"
        }
    }
}