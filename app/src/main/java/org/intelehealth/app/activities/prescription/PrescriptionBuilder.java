package org.intelehealth.app.activities.prescription;

import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.models.Patient;
import org.intelehealth.app.utilities.DateAndTimeUtils;

public class PrescriptionBuilder {
    private AppCompatActivity activityContext;

    public PrescriptionBuilder(AppCompatActivity activityContext) {
        this.activityContext = activityContext;
    }

    public String builder(Patient patient) {
        String prescriptionHTML = "";
        String headingDocTypeTag = "<!doctype html>";
        String headingHTMLLangTag = "<html lang=\"en\">";
        String htmlClosingTag = "</html>";

        prescriptionHTML = headingDocTypeTag + headingHTMLLangTag + buildHeadData() + buildBodyData(patient) + htmlClosingTag;
        return prescriptionHTML;
    }

    private String buildHeadData() {
        String finalHeadString = "";

        String headStartingTag = "<head>";
        String headClosingTag = "</head>\n";
        String headData = "<meta charset=\"utf-8\" />" + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />" + "<link rel=\"icon\" type=\"image/x-icon\" href=\"favicon.ico\" />\n" + "<title>Intelehealth</title>\n" + "<link href=\"https://fonts.googleapis.com/icon?family=Material+Icons\" rel=\"stylesheet\" />" + "<link rel=\"stylesheet\" href=\"https://use.fontawesome.com/releases/v5.8.2/css/all.css\" integrity=\"sha384-oS3vJWv+0UjzBfQzYUhtDYW+Pj2yciDJxpsK1OYPAYjqT085Qq/1cq5FLXAZQ7Ay\" crossorigin=\"anonymous\" />" + "<link rel=\"apple-touch-icon\" href=\"/assets/icons/icon-180x180.png\" />\n" + " <link rel=\"manifest\" href=\"manifest.webmanifest\" />\n" + "<link href=\"https://fonts.googleapis.com/css?family=DM Sans\" rel=\"stylesheet\" />\n" + "<meta name=\"theme-color\" content=\"#2e1e91\" />\n" + "<link rel=\"preconnect\" href=\"https://fonts.gstatic.com\">\n" + "<link href=\"https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500&display=swap\" rel=\"stylesheet\">\n" + "<link href=\"https://fonts.googleapis.com/icon?family=Material+Icons\" rel=\"stylesheet\">\n" + "<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css\" integrity=\"sha384-xOolHFLEh07PJGoPkLv1IbcEPTNtaed2xpHsD9ESMhqIYd0nLMwNLD69Npy4HI+N\" crossorigin=\"anonymous\">";
        String headStyleData = "<style>\n" + "        .modal-nav {\n" + "            position: fixed;\n" + "            top: 0;\n" + "            left: 0;\n" + "            width: 100%;\n" + "            display: flex;\n" + "            align-items: center;\n" + "            justify-content: space-between;\n" + "            background: var(--color-darkestBlue)86;\n" + "            padding: 10px;\n" + "        }\n" + "\n" + "        .title-con {\n" + "            padding: 24px 24px 16px;\n" + "            background: #E6FFF3;\n" + "            position: relative;\n" + "        }\n" + "\n" + "        .title-con .close-btn-con {\n" + "            position: absolute;\n" + "            right: 24px;\n" + "            top: 24px;\n" + "        }\n" + "\n" + "        .title-con .close-btn-con .modal-close-btn {\n" + "            border: none;\n" + "            background: transparent;\n" + "            outline: none;\n" + "        }\n" + "\n" + "        .title-con h6 {\n" + "            font-size: 24px;\n" + "            line-height: 150%;\n" + "            color: var(--color-darkestBlue);\n" + "            text-align: center;\n" + "            font-weight: bold;\n" + "            margin-bottom: 0px;\n" + "        }\n" + "\n" + "        .main-content {\n" + "            padding: 24px;\n" + "        }\n" + "\n" + "        .patient-info-wrapper {\n" + "            font-family: DM Sans;\n" + "        }\n" + "\n" + "        .patient-info-wrapper .patient-info-section {\n" + "            border-right: 1px solid rgba(178, 175, 190, 0.2);\n" + "        }\n" + "\n" + "        .patient-info-wrapper .patient-info-section .patient-img-item {\n" + "            display: flex;\n" + "            flex-direction: row;\n" + "            align-items: center;\n" + "        }\n" + "\n" + "        .patient-info-wrapper .patient-info-section .patient-img-item .patient-img {\n" + "            width: 56px;\n" + "            height: 50px;\n" + "            border-radius: 50%;\n" + "            overflow: hidden;\n" + "        }\n" + "\n" + "        .patient-info-wrapper .patient-info-section .patient-img-item h6 {\n" + "            margin-bottom: 0px;\n" + "            font-size: 18px;\n" + "            font-weight: bold;\n" + "            line-height: 150%;\n" + "            color: var(--color-darkestBlue);\n" + "        }\n" + "\n" + "        .patient-info-wrapper .patient-info-section .patient-img-item p {\n" + "            margin-bottom: 0px;\n" + "            color: var(--color-gray);\n" + "            font-size: 16px;\n" + "            line-height: 150%;\n" + "        }\n" + "\n" + "        .patient-info-wrapper .patient-info-section .patient-info-item h6 {\n" + "            margin-bottom: 0px;\n" + "            font-size: 16px;\n" + "            line-height: 150%;\n" + "            color: var(--color-darkestBlue);\n" + "        }\n" + "\n" + "        .patient-info-wrapper .patient-info-section .patient-info-item p {\n" + "            margin-bottom: 0px;\n" + "            color: var(--color-darkestBlue);\n" + "            font-size: 16px;\n" + "            line-height: 150%;\n" + "        }\n" + "\n" + "\n" + "        .patient-info-wrapper .patient-info-section:last-child {\n" + "            border: none;\n" + "        }\n" + "\n" + "        .data-section .data-section-title {\n" + "            display: flex;\n" + "            align-items: center;\n" + "            border-bottom: 1px solid rgba(178, 175, 190, 0.2);\n" + "            padding: 5px 0px;\n" + "        }\n" + "\n" + "        .data-section .data-section-title img {\n" + "            width: 48px;\n" + "            margin-right: 10px;\n" + "        }\n" + "\n" + "        .data-section .data-section-title h6 {\n" + "            font-size: 20px;\n" + "            line-height: 150%;\n" + "            color: var(--color-darkestBlue);\n" + "            font-weight: bold;\n" + "            margin-bottom: 0px;\n" + "        }\n" + "\n" + "        .items-list {\n" + "            font-family: DM Sans;\n" + "            font-size: 16px;\n" + "            padding: 24px 0px 0px 24px;\n" + "            margin-bottom: 0px;\n" + "        }\n" + "\n" + "        .items-list li {\n" + "            margin-bottom: 5px;\n" + "        }\n" + "\n" + "        .items-list li .list-item {\n" + "            display: flex;\n" + "            flex-wrap: nowrap;\n" + "            align-items: center;\n" + "        }\n" + "\n" + "        .items-list li .list-item label {\n" + "            width: 25%;\n" + "            margin-bottom: 0px;\n" + "            padding: 5px 0px;\n" + "        }\n" + "\n" + "        .items-list li .list-item .list-item-content {\n" + "            padding: 5px 0px;\n" + "        }\n" + "\n" + "        .items-list li .list-item-col {\n" + "            display: flex;\n" + "            flex-wrap: nowrap;\n" + "            flex-direction: column;\n" + "        }\n" + "\n" + "        .text-important-red {\n" + "            color: var(--color-red);\n" + "            font-weight: bold;\n" + "        }\n" + "\n" + "        .text-important-green {\n" + "            color: var(--color-green);\n" + "            font-weight: bold;\n" + "        }\n" + "\n" + "        .table th,\n" + "        .table td {\n" + "            vertical-align: middle;\n" + "            white-space: nowrap;\n" + "        }\n" + "\n" + "        @media (max-width: 768px) {\n" + "            .patient-info-section {\n" + "                border-bottom: 1px solid rgba(178, 175, 190, 0.2);\n" + "                border-right: none !important;\n" + "            }\n" + "\n" + "            .items-list {\n" + "                list-style-type: none;\n" + "                padding: 5px 0px 0px;\n" + "            }\n" + "\n" + "            .items-list li .list-item {\n" + "                flex-direction: column;\n" + "                align-items: flex-start;\n" + "            }\n" + "\n" + "            .items-list li .list-item label {\n" + "                width: 100%;\n" + "                border-top: 1px solid rgba(178, 175, 190, 0.2);\n" + "                font-weight: bold;\n" + "                margin-top: 0.50rem;\n" + "            }\n" + "\n" + "            .items-list li .list-item-col label {\n" + "                width: 100%;\n" + "                border-top: 1px solid rgba(178, 175, 190, 0.2);\n" + "                font-weight: bold;\n" + "                margin-top: 0.50rem;\n" + "            }\n" + "\n" + "            .data-section .data-section-title img {\n" + "                width: 38px;\n" + "                margin-right: 10px;\n" + "            }\n" + "\n" + "            .data-section .data-section-title h6 {\n" + "                font-size: 16px;\n" + "            }\n" + "\n" + "            .cheif-complaint-wrapper h6 {\n" + "                font-size: 16px;\n" + "            }\n" + "\n" + "            .main-content {\n" + "                padding: 10px !important;\n" + "            }\n" + "        }\n" + "\n" + "        .signature {\n" + "            height: 50px;\n" + "            width: 150px;\n" + "        }\n" + "    </style>";

        finalHeadString = headStartingTag + headData + headStyleData + headClosingTag;

        return finalHeadString;
    }

    private String buildBodyData(Patient patient) {
        String finalBodyString = "";
        String startingBodyTag = "<body class=\"font-lato mat-typography\">";
        String closingBodyTag = "</body>\n";

        String divMainContentOpeningTag = "<div class=\"main-content\">";
        String divMainContentClosingTag = "</div>\n";

        String divContainerFluidOpeningTag = "<div class=\"container-fluid\">\n";
        String divContainerFluidClosingTag = "</div>\n";

        finalBodyString = startingBodyTag
                + generatePrescriptionHeadingSection()
                + divMainContentOpeningTag
                + divContainerFluidOpeningTag
                + generatePatientDetailsData(patient)
                + divContainerFluidClosingTag
                + divMainContentClosingTag
                + closingBodyTag;

        return finalBodyString;
    }

    private String generatePrescriptionHeadingSection() {
        return "<div class=\"title-con position-relative\">\n"
                + "<h6>\n"
                + "Intelehealth e-Prescription\n"
                + "<img *ngIf=\"isDownloadPrescription\" class=\"logo position-absolute\"\n"
                + "src=\"https://dev.intelehealth.org/intelehealth/assets/images/Intelehealth-logo-white.png\" width=\"100%\" alt=\"\" />\n"
                + "</h6>\n"
                + "</div>";
    }

    private String generatePatientDetailsData(Patient patient) {
        return "<div class=\"row patient-info-wrapper\">\n"
                + "<div class=\"col-md-3 patient-info-section p-3\">\n"
                + "<div class=\"patient-img-item mb-2\">\n"
                + "<div class=\"patient-img\">\n"
                + "<img src=\"https://dev.intelehealth.org/intelehealth/assets/svgs/user.svg\" alt=\"\"\n"
                + "width=\"100%\" height=\"100%\" />\n"
                + "</div>\n"
                + "<div class=\"ml-3\">\n"
                + "<h6>\n"
                + patient.getFirst_name() + " " + ((!TextUtils.isEmpty(patient.getMiddle_name())) ? patient.getMiddle_name() : "") + " " + patient.getLast_name() + "\n"
                + "</h6>\n"
                + "<p>" + patient.getOpenmrs_id() + "</p>\n"
                + "</div>\n"
                + "</div>\n"
                + "</div>\n"
                + "<div class=\"col-md-3 patient-info-section p-3\">\n"
                + "<div class=\"patient-info-item mb-3\">\n"
                + "<h6>Age</h6>\n"
                + "<p>\n " + DateAndTimeUtils.getAgeInYears(patient.getDate_of_birth(), activityContext) + "\n"
                + "</p>\n"
                + "</div>\n"
                + "<div class=\"patient-info-item\">\n"
                + "<h6>Address</h6>\n"
                + "<p>" + patient.getCity_village() + "</p>\n"
                + "</div>\n"
                + "</div>\n"
                + "<div class=\"col-md-3 patient-info-section p-3\">\n"
                + "<div class=\"patient-info-item mb-3\">\n"
                + "<h6>Occupation</h6>\n"
                + "<p>" + patient.getOccupation() + "</p>\n"
                + "</div>\n"
                + "<div class=\"patient-info-item\">\n"
                + "<h6>National ID</h6>\n"
                + "<p>NA</p>\n"
                + "</div>\n"
                + "</div>\n"
                + "<div class=\"col-md-3 patient-info-section p-3\">\n"
                + "<div class=\"patient-info-item\">\n"
                + "<h6>Contact no.</h6>\n"
                + "<p>\n <img src=\"https://dev.intelehealth.org/intelehealth/assets/svgs/phone-black.svg\" alt=\"\" />\n " + patient.getPhone_number() + "\n"
                + "</p>\n"
                + "</div>\n"
                + "</div>\n"
                + "</div>";
    }
}