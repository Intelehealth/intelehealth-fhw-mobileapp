package org.intelehealth.app.activities.prescription;

import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.models.Patient;
import org.intelehealth.app.utilities.DateAndTimeUtils;

public class PrescriptionBuilder {
    private final AppCompatActivity activityContext;

    public PrescriptionBuilder(AppCompatActivity activityContext) {
        this.activityContext = activityContext;
    }

    public String builder(
            Patient patient,
            String diagnosisData,
            String medicationData,
            String adviceData,
            String testData,
            String referredOutData,
            String followUpData
    ) {
        String prescriptionHTML = "";
        String headingDocTypeTag = "<!doctype html>";
        String headingHTMLLangTag = "<html lang=\"en\">";
        String htmlClosingTag = "</html>";

        prescriptionHTML = headingDocTypeTag + headingHTMLLangTag + buildHeadData() + buildBodyData(patient, diagnosisData, medicationData, adviceData, testData, referredOutData, followUpData) + htmlClosingTag;
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

    private String buildBodyData(
            Patient patient,
            String diagnosisData,
            String medicationData,
            String adviceData,
            String testData,
            String referredOutData,
            String followUpData
    ) {
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
                + generateMainRowData(patient, diagnosisData, medicationData, adviceData, testData, referredOutData, followUpData)
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
                + "<p>" + patient.getNationalID() + "</p>\n"
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

    private String generateMainRowData(
            Patient patient,
            String diagnosisData,
            String medicationData,
            String adviceData,
            String testData,
            String referredOutData,
            String followUpData
    ) {
        String finalMainRowData = "";
        String rowOpeningTag = "<div class=\"row\">\n";
        String rowClosingTag = "</div>";
        String lineBreak = "<br>";

        finalMainRowData = rowOpeningTag
                + generateConsultationDetails(patient)
                + generateDiagnosisData(diagnosisData)
                + generateMedicationData(medicationData)
                + lineBreak
                + generateAdviceData(adviceData)
                + lineBreak
                + generateTestData(testData)
                + lineBreak
                + generateReferredOutData(referredOutData)
                + generateFollowUpData(followUpData)
                + rowClosingTag;

        return finalMainRowData;
    }

    private String generateConsultationDetails(Patient patient) {
        return "<div class=\"col-md-12 px-3 mb-3\">\n"
                + "<div class=\"data-section\">\n"
                + "<div class=\"data-section-title\">\n"
                + "<img src=\"https://dev.intelehealth.org/intelehealth/assets/svgs/consultation-details-blue.svg\"\n"
                + "alt=\"\" />\n"
                + "<h6>Consultation details</h6>\n"
                + "</div>\n"
                + "<div class=\"data-section-content consultation-details\">\n"
                + "<ul class=\"items-list\">\n"
                + "<li>\n"
                + "<div class=\"list-item\">\n"
                + "<label>Patient Id</label>\n"
                + "<div class=\"list-item-content\">\n"
                + patient.getOpenmrs_id() + "\n"
                + "</div>\n"
                + "</div>\n"
                + "</li>\n"
                + "<li>\n"
                + "<div class=\"list-item\">\n"
                + "<label>Prescription Issued</label>\n"
                + "<div class=\"list-item-content\">\n"
                + "03 Nov, 2023\n"
                + "</div>\n"
                + "</div>\n"
                + "</li>\n"
                + "</ul>\n"
                + "</div>\n"
                + "</div>\n"
                + "</div>";
    }

    private String generateDiagnosisData(String diagnosisData) {
        String finalDiagnosisString = "";

        String openingDivTag = "<div class=\"col-md-12 px-3 mb-3\">\n";
        String openingDataSectionTag = "<div class=\"data-section\">\n";
        String closingDivTag = "</div>";

        String diagnosisTitleTag = "<div class=\"data-section-title\">\n"
                + "<img src=\"https://dev.intelehealth.org/intelehealth/assets/svgs/diagnosis.svg\" alt=\"\" />\n"
                + "<h6>Diagnosis</h6>\n"
                + "</div>";

        String dataSectionDivTag = "<div class=\"data-section-content\">\n";
        String unorderedBulletedListTag = "<ul class=\"items-list\"> </ul>";
        String responsiveTableTag = "<div class=\"table-responsive\">";
        String tableStartTag = "<table class=\"table\">";
        String tableEndTag = "</table>";
        String tableHeadData = "<thead>\n"
                + "<tr>\n"
                + "<th scope=\"col\">Diagnosis</th>\n"
                + "<th scope=\"col\">Type</th>\n"
                + "<th scope=\"col\">Status</th>\n"
                + "</tr>\n"
                + "</thead>";

        String tableBodyOpeningTag = "<tbody>";
        String tableBodyClosingTag = "</tbody>";
        String tableRowOpeningTag = "<tr>";
        String tableRowClosingTag = "</tr>";
        String tableDataOpeningTag = "<td>";
        String tableDataClosingTag = "</td>";

        String[][] diagnosisDataArray = bifurcateDiagnosisData(diagnosisData);
        StringBuilder tableDataStringBuilder = new StringBuilder();

        for (String[] array : diagnosisDataArray) {
            tableDataStringBuilder.append(tableRowOpeningTag);

            for (String data : array) {
                tableDataStringBuilder.append(tableDataOpeningTag);
                tableDataStringBuilder.append(data);
                tableDataStringBuilder.append(tableDataClosingTag);
            }
            tableDataStringBuilder = tableDataStringBuilder.append(tableRowClosingTag);
        }

        String tableDataFinalString = tableDataStringBuilder.toString();

        String tableBodyData = tableBodyOpeningTag + tableDataFinalString + tableBodyClosingTag;

        String finalTableData = dataSectionDivTag + unorderedBulletedListTag + responsiveTableTag
                + tableStartTag + tableHeadData + tableBodyData + tableEndTag
                + closingDivTag + closingDivTag + closingDivTag + closingDivTag;

        finalDiagnosisString = openingDivTag + openingDataSectionTag + diagnosisTitleTag + finalTableData + closingDivTag + closingDivTag;
        return finalDiagnosisString;
    }

    private String[][] bifurcateDiagnosisData(String diagnosisData) {
        String[] diagnosisList = new String[1];

        // For multiple diagnosis
        if (diagnosisData.contains("\n")) {
            diagnosisList = diagnosisData.split(",\n");
        } else {
            diagnosisList[0] = diagnosisData;
        }

        String[][] finalDiagnosisList = new String[diagnosisList.length][3];

        for (int i = 0; i < diagnosisList.length; i++) {
            String currentDiagnosis = diagnosisList[i].trim();
            if (currentDiagnosis.contains(":") && currentDiagnosis.contains("&")) {
                finalDiagnosisList[i] = diagnosisList[i].split("\\s*[:&]\\s*");
            }
        }

        return finalDiagnosisList;
    }

    private String generateMedicationData(String medicationData) {
        String finalMedicationData = "";
        String closingDivTag = "</div>";
        String openingDivTag = "<div class=\"col-md-12 px-3 mb-3\">";
        String openingDataSectionTag = "<div class=\"data-section\">";
        String dataSectionTitleTag = "<div class=\"data-section-title\">"
                + "<img src=\"https://dev.intelehealth.org/intelehealth/assets/svgs/medication.svg\" alt=\"\" />"
                + "<h6>Medication</h6>\n"
                + "</div>";

        String tableBodyOpeningTag = "<tbody>";
        String tableBodyClosingTag = "</tbody>";
        String dataSectionDivTag = "<div class=\"data-section-content\">";
        String responsiveTableTag = "<div class=\"table-responsive\">";
        String tableStartTag = "<table class=\"table\">";
        String tableEndTag = "</table>";
        String tableHeadTag = "<thead>"
                + "<tr>\n"
                + "<th scope=\"col\">Drug name</th>\n"
                + "<th scope=\"col\">Strength</th>\n"
                + "<th scope=\"col\">No. of days</th>\n"
                + "<th scope=\"col\">Timing</th>\n"
                + "<th scope=\"col\">Remarks</th>\n"
                + "</tr>\n"
                + "</thead>";

        String tableDataFinalString = bifurcateMedicationData(medicationData);
        String tableAdditionalDataFinalString = handleAdditionalData(medicationData);

        finalMedicationData = openingDivTag
                + openingDataSectionTag
                + dataSectionTitleTag
                + dataSectionDivTag
                + responsiveTableTag
                + tableStartTag
                + tableHeadTag
                + tableBodyOpeningTag
                + tableDataFinalString
                + tableBodyClosingTag
                + tableEndTag
                + closingDivTag;

        if (!tableAdditionalDataFinalString.isEmpty()) {
            finalMedicationData = finalMedicationData + tableAdditionalDataFinalString;
        }


        finalMedicationData = finalMedicationData
                + closingDivTag
                + closingDivTag
                + closingDivTag;

        return finalMedicationData;
    }

    private String bifurcateMedicationData(String medicationData) {
        if (medicationData.isEmpty()) {
            return handleEmptyMedicationData();
        }

        String finalMedicationDataString = "";
        String[] medicationDataArray = medicationData.split("\n");

        if (!checkIfArrayContainsMedicationData(medicationDataArray)) {
            finalMedicationDataString = handleEmptyMedicationData();
        } else {
            String[][] splitMedicationDataArray = new String[medicationDataArray.length][5];
            for (int i = 0; i < splitMedicationDataArray.length; i++) {
                if (medicationDataArray[i].contains(":")) {
                    splitMedicationDataArray[i] = medicationDataArray[i].split(":");
                }
            }

            String tableRowOpeningTag = "<tr>";
            String tableRowClosingTag = " </tr>";
            String tableDataOpeningTag = "<td>";
            String tableDataClosingTag = "</td>";

            StringBuilder builder = new StringBuilder();

            for (String[] tempArray : splitMedicationDataArray) {
                builder.append(tableRowOpeningTag);

                for (String s : tempArray) {
                    if (s != null) {
                        builder.append(tableDataOpeningTag);
                        builder.append(s);
                        builder.append(tableDataClosingTag);
                    }
                }
                builder.append(tableRowClosingTag);
            }

            finalMedicationDataString = builder.toString();
        }

        return finalMedicationDataString;
    }

    private String handleEmptyMedicationData() {
        String finalEmptyMedicationString = "";
        String tableRowOpeningTag = "<tr>";
        String tableRowClosingTag = " </tr>";
        String noMedicinesTableDataOpeningTag = "<td colspan=\"5\" class=\"text-center\">";
        String noMedicinesTableDataClosingTag = "</td>";
        String noMedicinesAddedText = "No medicines added";

        finalEmptyMedicationString = tableRowOpeningTag
                + noMedicinesTableDataOpeningTag
                + noMedicinesAddedText
                + noMedicinesTableDataClosingTag
                + tableRowClosingTag;

        return finalEmptyMedicationString;
    }

    private boolean checkIfArrayContainsMedicationData(String[] medicationArray) {
        boolean containsMedicationData = false;
        for (String data : medicationArray) {
            if (data.contains(":")) {
                containsMedicationData = true;
                break;
            }
        }
        return containsMedicationData;
    }

    private String handleAdditionalData(String medicationData) {
        if (medicationData.isEmpty()) {
            return "";
        }

        String finalAdditionalDataString = "";
        String unorderedListOpeningTag = "<ul class=\"items-list\">";
        String unorderedListClosingTag = "</ul>";
        String listOpeningTag = "<li>";
        String listClosingTag = "</li>";
        String divOpeningTag = "<div class=\"d-flex justify-content-between align-items-center\">";
        String divClosingTag = "</div>";
        String spanOpeningTag = "<span>";
        String spanClosingTag = "</span>";

        StringBuilder additionalInstructionsData = new StringBuilder();
        String[] medicationDataArray = medicationData.split("\n");
        for (String s : medicationDataArray) {
            if (!s.contains(":")) {
                additionalInstructionsData.append(listOpeningTag);
                additionalInstructionsData.append(divOpeningTag);
                additionalInstructionsData.append(spanOpeningTag);
                additionalInstructionsData.append(s);
                additionalInstructionsData.append(spanClosingTag);
                additionalInstructionsData.append(divClosingTag);
                additionalInstructionsData.append(listClosingTag);
            }
        }

        finalAdditionalDataString = unorderedListOpeningTag
                + additionalInstructionsData
                + unorderedListClosingTag;

        return finalAdditionalDataString;
    }

    private String generateAdviceData(String adviceData) {
        String finalAdviceString = "";
        String closingDivTag = "</div>";
        String openingDivTag = "<div class=\"col-md-12 px-3 mb-3\">";
        String dataSectionTag = "<div class=\"data-section\">";
        String dataSectionTitleTag = "<div class=\"data-section-title\">"
                + "<img src=\"https://dev.intelehealth.org/intelehealth/assets/svgs/advice.svg\" alt=\"\" />"
                + "<h6>Advice</h6>"
                + "</div>";

        String dataSectionContentOpeningTag = "<div class=\"data-section-content\">";
        String unorderedListOpeningTag = "<ul class=\"items-list\">";
        String unorderedListClosingTag = "</ul>";

        String bifurcatedAdviceData = checkAndBifurcateAdviceData(adviceData);

        finalAdviceString = openingDivTag
                + dataSectionTag
                + dataSectionTitleTag
                + dataSectionContentOpeningTag
                + unorderedListOpeningTag
                + bifurcatedAdviceData
                + unorderedListClosingTag
                + closingDivTag
                + closingDivTag
                + closingDivTag;

        return finalAdviceString;
    }

    private String checkAndBifurcateAdviceData(String adviceData) {
        StringBuilder finalAdviceStringBuilder = new StringBuilder();
        String listOpeningTag = "<li>";
        String listClosingTag = "</li>";
        String divClassOpeningTagCenter = "<div class=\"d-flex justify-content-between align-items-center\">";
        String closingDivTag = "</div>";
        String spanOpeningTag = "<span>";
        String spanClosingTag = "</span>";

        if (!adviceData.contains("<br><br>")) {
            finalAdviceStringBuilder.append(listOpeningTag);
            finalAdviceStringBuilder.append(divClassOpeningTagCenter);
            finalAdviceStringBuilder.append(spanOpeningTag);
            finalAdviceStringBuilder.append(adviceData);
            finalAdviceStringBuilder.append(spanClosingTag);
            finalAdviceStringBuilder.append(closingDivTag);
            finalAdviceStringBuilder.append(listClosingTag);
        } else {
            String[] adviceArray = adviceData.split("<br><br>");
            for (String advice : adviceArray) {
                finalAdviceStringBuilder.append(listOpeningTag);
                finalAdviceStringBuilder.append(divClassOpeningTagCenter);
                finalAdviceStringBuilder.append(spanOpeningTag);
                finalAdviceStringBuilder.append(advice);
                finalAdviceStringBuilder.append(spanClosingTag);
                finalAdviceStringBuilder.append(closingDivTag);
                finalAdviceStringBuilder.append(listClosingTag);
            }
        }

        return finalAdviceStringBuilder.toString();
    }

    private String generateTestData(String testData) {
        String finalTestString = "";
        String divClosingTag = "</div>";
        String divOpeningTag = "<div class=\"col-md-12 px-3 mb-3\">";
        String divDataSectionOpening = "<div class=\"data-section\">";
        String divDataSectionTitleTag = "<div class=\"data-section-title\">"
                + "<img src=\"https://dev.intelehealth.org/intelehealth/assets/svgs/test.svg\" alt=\"\" />"
                + "<h6>Test</h6>"
                + "</div>";

        String dataSectionContentOpeningTag = "<div class=\"data-section-content\">";
        String unorderedListOpeningTag = "<ul class=\"items-list\">";
        String unorderedListClosingTag = "</ul>";

        String bifurcatedTestsData = checkAndBifurcateTestData(testData);

        finalTestString = divOpeningTag
                + divDataSectionOpening
                + divDataSectionTitleTag
                + dataSectionContentOpeningTag
                + unorderedListOpeningTag
                + bifurcatedTestsData
                + unorderedListClosingTag
                + divClosingTag
                + divClosingTag
                + divClosingTag;

        return finalTestString;
    }

    private String checkAndBifurcateTestData(String testsData) {
        testsData = testsData.replace("• ", "");
        StringBuilder finalTestsStringBuilder = new StringBuilder();
        String listOpeningTag = "<li>";
        String listClosingTag = "</li>";
        String divClassOpeningTagCenter = "<div class=\"d-flex justify-content-between align-items-center\">";
        String closingDivTag = "</div>";
        String spanOpeningTag = "<span>";
        String spanClosingTag = "</span>";

        if (!testsData.contains("\n\n")) {
            finalTestsStringBuilder.append(listOpeningTag);
            finalTestsStringBuilder.append(divClassOpeningTagCenter);
            finalTestsStringBuilder.append(spanOpeningTag);
            finalTestsStringBuilder.append(testsData);
            finalTestsStringBuilder.append(spanClosingTag);
            finalTestsStringBuilder.append(closingDivTag);
            finalTestsStringBuilder.append(listClosingTag);
        } else {
            String[] adviceArray = testsData.split("\n\n");
            for (String advice : adviceArray) {
                finalTestsStringBuilder.append(listOpeningTag);
                finalTestsStringBuilder.append(divClassOpeningTagCenter);
                finalTestsStringBuilder.append(spanOpeningTag);
                finalTestsStringBuilder.append(advice);
                finalTestsStringBuilder.append(spanClosingTag);
                finalTestsStringBuilder.append(closingDivTag);
                finalTestsStringBuilder.append(listClosingTag);
            }
        }

        return finalTestsStringBuilder.toString();
    }

    private String generateReferredOutData(String referredOutData) {
        String finalReferredOutString = "";
        String divClosingTag = "</div>";
        String divOpeningTag = "<div class=\"col-md-12 px-3 mb-3\">";
        String divDataSectionOpening = "<div class=\"data-section\">";
        String divDataSectionTitleTag = "<div class=\"data-section-title\">\n"
                + "<img src=\"https://dev.intelehealth.org/intelehealth/assets/svgs/referral.svg\" alt=\"\" />\n"
                + "<h6>Referral-Out</h6>\n"
                + "</div>";

        String divDataSectionContentOpening = "<div class=\"data-section-content\">";
        String divDataSectionTableResponsive = "<div class=\"table-responsive\">";
        String tableOpeningTag = "<table class=\"table\">";
        String tableClosingTag = "</table>";
        String tableHeadOpeningTag = "<thead>\n"
                + "<tr>\n"
                + "<th scope=\"col\">Referral to</th>\n"
                + "<th scope=\"col\">Referral facility</th>\n"
                + "<th scope=\"col\">Priority of Referral</th>\n"
                + "<th scope=\"col\">Referral for (Reason)</th>\n"
                + "</tr>\n"
                + "</thead>";

        String bifurcatedReferralData = checkAndBifurcateReferredData(referredOutData);

        finalReferredOutString = divOpeningTag
                + divDataSectionOpening
                + divDataSectionTitleTag
                + divDataSectionContentOpening
                + divDataSectionTableResponsive
                + tableOpeningTag
                + tableHeadOpeningTag
                + bifurcatedReferralData
                + tableClosingTag
                + divClosingTag
                + divClosingTag
                + divClosingTag
                + divClosingTag;

        return finalReferredOutString;
    }

    private String checkAndBifurcateReferredData(String referredOutData) {
        StringBuilder finalReferredOutData = new StringBuilder();
        String tableBodyOpeningTag = "<tbody>";
        String tableBodyClosingTag = "</tbody>";
        String tableRowOpeningTag = "<tr>";
        String tableRowClosingTag = "</tr>";
        String tableDataOpeningTag = "<td>";
        String tableDataClosingTag = "</td>";

        String noReferralsAddedOpeningTag = "<td colspan=\"4\" class=\"text-center\">"
                + "No referrals added";

        finalReferredOutData.append(tableBodyOpeningTag);

        if (referredOutData.isEmpty()) {
            finalReferredOutData.append(tableRowOpeningTag);
            finalReferredOutData.append(noReferralsAddedOpeningTag);
            finalReferredOutData.append(tableRowClosingTag);
        } else {
            if (referredOutData.contains(":")) {
                String[] referredOutArray = referredOutData.split(":");
                finalReferredOutData.append(tableRowOpeningTag);
                for (String data : referredOutArray) {
                    finalReferredOutData.append(tableDataOpeningTag);
                    finalReferredOutData.append(data);
                    finalReferredOutData.append(tableDataClosingTag);
                }
                finalReferredOutData.append(tableRowClosingTag);
            }
        }

        finalReferredOutData.append(tableBodyClosingTag);
        return finalReferredOutData.toString();
    }

    private String generateFollowUpData(String followUpData) {
        String finalFollowUpString = "";
        String closingDivTag = "</div>";
        String divOpeningTag = "<div class=\"col-md-12 px-3 mb-3\">";
        String divDataSectionOpeningTag = "<div class=\"data-section\">";
        String divSectionTitleTag = "<div class=\"data-section-title\">"
                + "<img src=\"https://dev.intelehealth.org/intelehealth/assets/svgs/follow-up.svg\" alt=\"\" />"
                + "<h6>Follow-up</h6>"
                + "</div>";

        String closingUnorderedListTag = "</ul>";
        String isFollowUpScheduled = "";
        String[] followUpArrayData = followUpData.split(",");

        if (followUpData.equalsIgnoreCase("")) {
            isFollowUpScheduled = "No";
        } else {
            isFollowUpScheduled = "Yes";
        }

        String divSectionContentOpeningTag = "<div class=\"data-section-content\">"
                + "<ul class=\"items-list\">\n"
                + "<li>"
                + "<div class=\"list-item\">"
                + "<label class=\"border-0\">Follow-up suggested</label>"
                + "<div class=\"list-item-content\">"
                + isFollowUpScheduled
                + "</div>"
                + "</div>"
                + "</li>";

        if (!followUpData.equalsIgnoreCase("")) {

            divSectionContentOpeningTag = divSectionContentOpeningTag
                    + "<li>"
                    + "<div class=\"list-item\">"
                    + "<label>Follow-up Date</label>"
                    + "<div class=\"list-item-content\">"
                    + DateAndTimeUtils.formatDateFromOnetoAnother(followUpArrayData[0], "yyyy-MM-dd", "dd-MM-yyyy")
                    + "</div>"
                    + "</div>"
                    + "</li>";

            divSectionContentOpeningTag = divSectionContentOpeningTag
                    + "<li>"
                    + "<div class=\"list-item\">"
                    + "<label>Follow-up Time</label>"
                    + "<div class=\"list-item-content\">"
                    + followUpArrayData[1].split("Time:")[1]
                    + "</div>"
                    + "</div>"
                    + "</li>";

            divSectionContentOpeningTag = divSectionContentOpeningTag
                    + "<li>"
                    + "<div class=\"list-item\">"
                    + "<label>Reason for follow-up</label>"
                    + "<div class=\"list-item-content\">"
                    + followUpArrayData[2].split("Remark:")[1]
                    + "</div>"
                    + "</div>"
                    + "</li>";
        }

        divSectionContentOpeningTag = divSectionContentOpeningTag
                + closingUnorderedListTag
                + closingDivTag;

        finalFollowUpString = divOpeningTag
                + divDataSectionOpeningTag
                + divSectionTitleTag
                + divSectionContentOpeningTag
                + closingDivTag
                + closingDivTag;


        return finalFollowUpString;
    }
}