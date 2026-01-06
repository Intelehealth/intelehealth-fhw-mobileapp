package org.intelehealth.app.activities.prescription;

import static org.intelehealth.app.app.AppConstants.CONFIG_FILE_NAME;
import static org.intelehealth.app.utilities.StringUtils.convertCtoF;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.chatHelp.ChatHelpActivity_New;
import org.intelehealth.app.activities.visit.model.PrescribedMedicineModel;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.databinding.LayoutPrescriptionBinding;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.ClsDoctorDetails;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.utilities.Base64Utils;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.ParserUtils;
import org.intelehealth.app.utilities.RegexUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.config.room.entity.FeatureActiveStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrescriptionBuilder {
    private final AppCompatActivity activityContext;
    JSONObject obj;
    String disclaimerStr = "";
    private static String mFileName = CONFIG_FILE_NAME;
    private FeatureActiveStatus mFeatureActiveStatus;

    private final LayoutPrescriptionBinding binding;


    public PrescriptionBuilder(AppCompatActivity activityContext) {
        this.activityContext = activityContext;
        binding = LayoutPrescriptionBinding.inflate(LayoutInflater.from(activityContext));
    }

    public String builder(
            Patient patient,
            VitalsObject vitalsData,
            String diagnosisData,
            String medicationData,
            String adviceData,
            String testData,
            String referredOutData,
            String followUpData,
            ClsDoctorDetails details,
            FeatureActiveStatus featureActiveStatus
    ) {
        mFeatureActiveStatus = featureActiveStatus;
        String prescriptionHTML = "";
        String headingDocTypeTag = "<!doctype html>";
        String headingHTMLLangTag = "<html lang=\"en\">";
        String htmlClosingTag = "</html>";

        prescriptionHTML = headingDocTypeTag
                + headingHTMLLangTag
                + buildHeadData()
                + buildBodyData(patient, vitalsData, diagnosisData, medicationData, adviceData, testData, referredOutData, followUpData, details)
                + buildDisclaimerData()
                + htmlClosingTag;

        return prescriptionHTML;
    }

    private String buildDisclaimerData() {
        SessionManager sessionManager = new SessionManager(activityContext);

        try {
            obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(CONFIG_FILE_NAME, activityContext), String.valueOf(FileUtils.encodeJSON(activityContext, CONFIG_FILE_NAME)))); //Load the config file

            disclaimerStr = obj.getString("prescriptionDisclaimer_English");

            //uncomment the below line if any hindi disclaimer is available
            //disclaimerStr = sessionManager.getAppLanguage().equalsIgnoreCase("hi") ? obj.getString("prescriptionDisclaimer_Hindi") : obj.getString("prescriptionDisclaimer_English");
        } catch (JSONException e) {
            return "";
            //commented to avoid run time crash
            //throw new RuntimeException(e);
        }


        if (disclaimerStr.isEmpty()) {
            return "";
        }

        String finalDisclaimerString;
        String closingDivTag = "</div>";
        String openingDivTag = "<div>";

        String divClassDisclaimerTag =
                "<div style=\" margin-top: 30px; position: fixed; bottom: 0; left: 0; width: 100%; text-align: center;\">" + disclaimerStr + closingDivTag;

        finalDisclaimerString = openingDivTag
                + openingDivTag
                + divClassDisclaimerTag
                + closingDivTag
                + closingDivTag;

        return finalDisclaimerString;
    }

    private String buildHeadData() {
        String finalHeadString = "";

        String headStartingTag = "<head>";
        String headClosingTag = "</head>\n";
        String headData = "<meta charset=\"utf-8\" />" + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />" + "<link rel=\"icon\" type=\"image/x-icon\" href=\"favicon.ico\" />\n" + "<title>Intelehealth</title>\n" + "<link href=\"https://fonts.googleapis.com/icon?family=Material+Icons\" rel=\"stylesheet\" />" + "<link rel=\"stylesheet\" href=\"https://use.fontawesome.com/releases/v5.8.2/css/all.css\" integrity=\"sha384-oS3vJWv+0UjzBfQzYUhtDYW+Pj2yciDJxpsK1OYPAYjqT085Qq/1cq5FLXAZQ7Ay\" crossorigin=\"anonymous\" />" + "<link rel=\"apple-touch-icon\" href=\"/assets/icons/icon-180x180.png\" />\n" + " <link rel=\"manifest\" href=\"manifest.webmanifest\" />\n" + "<link href=\"https://fonts.googleapis.com/css?family=DM Sans\" rel=\"stylesheet\" />\n" + "<meta name=\"theme-color\" content=\"#2e1e91\" />\n" + "<link rel=\"preconnect\" href=\"https://fonts.gstatic.com\">\n" + "<link href=\"https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500&display=swap\" rel=\"stylesheet\">\n" + "<link href=\"https://fonts.googleapis.com/icon?family=Material+Icons\" rel=\"stylesheet\">\n" + "<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css\" integrity=\"sha384-xOolHFLEh07PJGoPkLv1IbcEPTNtaed2xpHsD9ESMhqIYd0nLMwNLD69Npy4HI+N\" crossorigin=\"anonymous\">";
        String headStyleData = "<style>\n" + "        .modal-nav {\n" + "            position: fixed;\n" + "            top: 0;\n" + "            left: 0;\n" + "            width: 100%;\n" + "            display: flex;\n" + "            align-items: center;\n" + "            justify-content: space-between;\n" + "            background: var(--color-darkestBlue)86;\n" + "            padding: 10px;\n" + "        }\n" + "\n" + "        .title-con {\n" + "            padding: 24px 24px 16px;\n" + "            background: #E6FFF3;\n" + "            position: relative;\n" + "        }\n" + "\n" + "        .title-con .close-btn-con {\n" + "            position: absolute;\n" + "            right: 24px;\n" + "            top: 24px;\n" + "        }\n" + "\n" + "        .title-con .close-btn-con .modal-close-btn {\n" + "            border: none;\n" + "            background: transparent;\n" + "            outline: none;\n" + "        }\n" + "\n" + "        .title-con h6 {\n" + "            font-size: 24px;\n" + "            line-height: 150%;\n" + "            color: var(--color-darkestBlue);\n" + "            text-align: center;\n" + "            font-weight: bold;\n" + "            margin-bottom: 0px;\n" + "        }\n" + "\n" + "        .main-content {\n" + "            padding: 24px;\n" + "        }\n" + "\n" + "        .patient-info-wrapper {\n" + "            font-family: DM Sans;\n" + "        }\n" + "\n" + "        .patient-info-wrapper .patient-info-section {\n" + "            border-right: 1px solid rgba(178, 175, 190, 0.2);\n" + "        }\n" + "\n" + "        .patient-info-wrapper .patient-info-section .patient-img-item {\n" + "            display: flex;\n" + "            flex-direction: row;\n" + "            align-items: center;\n" + "        }\n" + "\n" + "        .patient-info-wrapper .patient-info-section .patient-img-item .patient-img {\n" + "            width: 56px;\n" + "            height: 50px;\n" + "            border-radius: 50%;\n" + "            overflow: hidden;\n" + "        }\n" + "\n" + "        .patient-info-wrapper .patient-info-section .patient-img-item h6 {\n" + "            margin-bottom: 0px;\n" + "            font-size: 18px;\n" + "            font-weight: bold;\n" + "            line-height: 150%;\n" + "            color: var(--color-darkestBlue);\n" + "        }\n" + "\n" + "        .patient-info-wrapper .patient-info-section .patient-img-item p {\n" + "            margin-bottom: 0px;\n" + "            color: var(--color-gray);\n" + "            font-size: 16px;\n" + "            line-height: 150%;\n" + "      }\n" + "\n" + "        .patient-info-wrapper .patient-info-section .patient-info-item h6 {\n" + "            margin-bottom: 0px;\n" + "            font-size: 16px;\n" + "            line-height: 150%;\n" + "            color: var(--color-darkestBlue);\n" + "        }\n" + "\n" + "        .patient-info-wrapper .patient-info-section .patient-info-item p {\n" + "            margin-bottom: 0px;\n" + "            color: var(--color-darkestBlue);\n" + "            font-size: 16px;\n" + "            line-height: 150%; word-break: break-all;" + "     }\n" + "\n" + "\n" + "        .patient-info-wrapper .patient-info-section:last-child {\n" + "            border: none;\n" + "        }\n" + "\n" + "        .data-section .data-section-title {\n" + "            display: flex;\n" + "            align-items: center;\n" + "            border-bottom: 1px solid rgba(178, 175, 190, 0.2);\n" + "            padding: 5px 0px;\n" + "        }\n" + "\n" + "        .data-section .data-section-title img {\n" + "            width: 48px;\n" + "            margin-right: 10px;\n" + "        }\n" + "\n" + "        .data-section .data-section-title h6 {\n" + "            font-size: 20px;\n" + "            line-height: 150%;\n" + "            color: var(--color-darkestBlue);\n" + "            font-weight: bold;\n" + "            margin-bottom: 0px;\n" + "        }\n" + "\n" + "        .items-list {\n" + "            font-family: DM Sans;\n" + "            font-size: 16px;\n" + "            padding: 24px 0px 0px 24px;\n" + "            margin-bottom: 0px;\n" + "        }\n" + "\n" + "        .items-list li {\n" + "            margin-bottom: 5px;\n" + "        }\n" + "\n" + "        .items-list li .list-item {\n" + "            display: flex;\n" + "            flex-wrap: nowrap;\n" + "            align-items: center;\n" + "        }\n" + "\n" + "        .items-list li .list-item label {\n" + "            width: 25%;\n" + "            margin-bottom: 0px;\n" + "            padding: 5px 0px;\n" + "        }\n" + "\n" + "        .items-list li .list-item .list-item-content {\n" + "            padding: 5px 0px;\n" + "        }\n" + "\n" + "        .items-list li .list-item-col {\n" + "            display: flex;\n" + "            flex-wrap: nowrap;\n" + "            flex-direction: column;\n" + "        }\n" + "\n" + "        .text-important-red {\n" + "            color: var(--color-red);\n" + "            font-weight: bold;\n" + "        }\n" + "\n" + "        .text-important-green {\n" + "            color: var(--color-green);\n" + "            font-weight: bold;\n" + "        }\n" + "\n" + "        .table th,\n" + "        .table td {\n" + "            vertical-align: middle;\n" + "            white-space: nowrap;\n" + "        }\n" + "\n" + "        @media (max-width: 768px) {\n" + "            .patient-info-section {\n" + "                border-bottom: 1px solid rgba(178, 175, 190, 0.2);\n" + "                border-right: none !important;\n" + "            }\n" + "\n" + "            .items-list {\n" + "                list-style-type: none;\n" + "                padding: 5px 0px 0px;\n" + "            }\n" + "\n" + "            .items-list li .list-item {\n" + "                flex-direction: column;\n" + "                align-items: flex-start;\n" + "            }\n" + "\n" + "            .items-list li .list-item label {\n" + "                width: 100%;\n" + "                border-top: 1px solid rgba(178, 175, 190, 0.2);\n" + "                font-weight: bold;\n" + "                margin-top: 0.50rem;\n" + "            }\n" + "\n" + "            .items-list li .list-item-col label {\n" + "                width: 100%;\n" + "                border-top: 1px solid rgba(178, 175, 190, 0.2);\n" + "                font-weight: bold;\n" + "                margin-top: 0.50rem;\n" + "            }\n" + "\n" + "            .data-section .data-section-title img {\n" + "                width: 38px;\n" + "                margin-right: 10px;\n" + "            }\n" + "\n" + "            .data-section .data-section-title h6 {\n" + "                font-size: 16px;\n" + "            }\n" + "\n" + "            .cheif-complaint-wrapper h6 {\n" + "                font-size: 16px;\n" + "            }\n" + "\n" + "            .main-content {\n" + "                padding: 10px !important;\n" + "            }\n" + "        }\n" + "\n" + "        .signature {\n" + "            height: 50px;\n" + "            width: 150px;\n" + "           padding-right: 20px;\n" + "        }\n" + "    </style>";

        finalHeadString = headStartingTag + headData + headStyleData + headClosingTag;

        return finalHeadString;
    }

    private String buildBodyData(
            Patient patient,
            VitalsObject vitalsData,
            String diagnosisData,
            String medicationData,
            String adviceData,
            String testData,
            String referredOutData,
            String followUpData,
            ClsDoctorDetails details
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
                + generateMainRowData(patient, vitalsData, diagnosisData, medicationData, adviceData, testData, referredOutData, followUpData, details)
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
        String patientProfilePhoto = "";
        if (patient.getPatient_photo() != null && !patient.getPatient_photo().isEmpty()) {

            patientProfilePhoto = new Base64Utils().getBase64FromFileWithConversion(patient.getPatient_photo());
            String format = patient.getPatient_photo().substring(patient.getPatient_photo().length() - 3);

            if (format.equalsIgnoreCase("png")) {
                patientProfilePhoto = "data:image/png;base64," + patientProfilePhoto;
            } else {
                patientProfilePhoto = "data:image/jpg;base64," + patientProfilePhoto;
            }

        } else {
            patientProfilePhoto = "https://dev.intelehealth.org/intelehealth/assets/svgs/user.svg";
        }

        String patientPhoneNumber = "";
        if (patient.getPhone_number() != null && !patient.getPhone_number().equalsIgnoreCase("")) {
            patientPhoneNumber = patient.getPhone_number();
        } else {
            patientPhoneNumber = activityContext.getString(R.string.not_provided);
        }

        return "<div class=\"row patient-info-wrapper\">\n"
                + "<div class=\"col-md-3 patient-info-section p-3\">\n"
                + "<div class=\"patient-img-item mb-2\">\n"
                + "<div class=\"patient-img\">\n"
                + "<img src=\"" + patientProfilePhoto + "\" alt=\"\" style='border-radius: 50%; width: 50px; height: 50px; object-fit: cover;'\n"
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
                + "<h6>Gender</h6>\n"
                + "<p>\n " + getFullGenderStr(patient.getGender()) + "\n"
                + "</p>\n"
                + "</div>\n"
                + "<div class=\"patient-info-item\">\n"
                + "<h6>Age</h6>\n"
                + "<p>\n " + DateAndTimeUtils.getAgeInYears(patient.getDate_of_birth(), activityContext) + "\n"
                + "</div>\n"
                + "</div>\n"
                + "<div class=\"col-md-3 patient-info-section p-3\">\n"
                + "<div class=\"patient-info-item mb-3\">\n"
                + "<h6>Address</h6>\n"
                + "<p>" + patient.getCity_village() + "</p>\n"
                + "</div>\n"
                + "<div class=\"patient-info-item\">\n"
                + "<h6>Occupation</h6>\n"
                + "<p>" + patient.getOccupation() + "</p>\n"
                + "</div>\n"
                + "</div>\n"
                + "<div class=\"col-md-3 patient-info-section p-3\">\n"
                + "<div class=\"patient-info-item mb-3\">\n"
                + "<h6>National ID</h6>\n"
                + "<p>" + patient.getNationalID() + "</p>\n"
                + "</p>\n"
                + "</div>\n"
                + "<div class=\"patient-info-item\">\n"
                + "<h6>Contact no.</h6>\n"
                + "<p>\n <img src=\"https://dev.intelehealth.org/intelehealth/assets/svgs/phone-black.svg\" alt=\"\" />\n " + patientPhoneNumber + "\n"
                + "</div>\n"
                + "</div>\n"
                + "</div>";
    }

    /**
     * return full gender from gender char
     *
     * @param gender
     * @return
     */
    private String getFullGenderStr(String gender) {
        return switch (gender.toLowerCase()) {
            case "f" -> "Female";
            case "m" -> "Male";
            default -> "Other";
        };
    }

    private String generateMainRowData(
            Patient patient,
            VitalsObject vitalsData,
            String diagnosisData,
            String medicationData,
            String adviceData,
            String testData,
            String referredOutData,
            String followUpData,
            ClsDoctorDetails details
    ) {
        String finalMainRowData = "";
        String rowOpeningTag = "<div class=\"row\">\n";
        String rowClosingTag = "</div>";
        String lineBreak = "<br>";

        finalMainRowData = rowOpeningTag
                + generateVitalsData(vitalsData)
                + generateConsultationDetails(patient)
                + generateDiagnosisData(diagnosisData)
                + generateMedicationData(medicationData)
                + generateAdviceData(adviceData)
                + generateTestData(testData)
                + generateReferredOutData(referredOutData)
                + generateFollowUpData(followUpData)
                + rowClosingTag
                + generateDoctorSignatureData(details)
                + rowClosingTag;

        return finalMainRowData;
    }

    private String generateVitalsData(VitalsObject vitalsData) {
//        if (!mFeatureActiveStatus.getVitalSection())  return "";
        String finalVitalsData = "";
        String openingDivTag = "<div class=\"col-md-12 px-3 mb-3\">\n";
        String openingDataSectionTag = "<div class=\"data-section\">\n";
        String closingDivTag = "</div>";

        String vitalsTitleTag = "<div class=\"data-section-title\">\n"
                + "<img src=\"https://dev.intelehealth.org/intelehealth/assets/svgs/vitals.svg\"\n"
                + "alt=\"\" />\n"
                + "<h6>Vitals</h6>\n"
                + "</div>";

        String dataSectionContentConsultationTitle = "<div class=\"data-section-content consultation-details\">";
        String unorderedListOpeningTag = "<ul class=\"items-list\">";
        String unorderedListClosingTag = "</ul>";

        String vitalsDataString = "";
        vitalsDataString = vitalsDataString + createVitalsListItem(activityContext.getString(R.string.prescription_ft), vitalsData.getHeight());
        vitalsDataString = vitalsDataString + createVitalsListItem(activityContext.getString(R.string.prescription_wt), vitalsData.getWeight());
        vitalsDataString = vitalsDataString + createVitalsListItem(activityContext.getString(R.string.table_bmi), vitalsData.getBmi());

        String systolicColor = "";
        if (vitalsData.getBpsys() == null || vitalsData.getBpsys().isEmpty()) {
            systolicColor = getSystolicColor(0);
        } else {
            systolicColor = getSystolicColor(Integer.parseInt(vitalsData.getBpsys()));

        }

        vitalsDataString = vitalsDataString + createBPListItem(activityContext.getString(R.string.prescription_systolic_blood_pressure), vitalsData.getBpsys(), systolicColor);

        String diastolicColor = "";
        if (vitalsData.getBpdia() == null || vitalsData.getBpdia().isEmpty()) {
            diastolicColor = getDiastolicColor(0);
        } else {
            diastolicColor = getDiastolicColor(Integer.parseInt(vitalsData.getBpdia()));
        }

        vitalsDataString = vitalsDataString + createBPListItem(activityContext.getString(R.string.prescription_diastolic_blood_pressure), vitalsData.getBpdia(), diastolicColor);

        vitalsDataString = vitalsDataString + createVitalsListItem(activityContext.getString(R.string.prescription_pulse), vitalsData.getPulse());

        try {
            JSONObject obj = null;
            //TODO: Need to link whether its in license version or not
            boolean hasLicense = !new SessionManager(activityContext).getLicenseKey().isEmpty();
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(CONFIG_FILE_NAME, activityContext), String.valueOf(FileUtils.encodeJSON(activityContext, CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(activityContext, mFileName)));
            }//Load the config file

            if (obj.getBoolean("mTemperature")) {
                if (obj.getBoolean("mCelsius")) {

                    vitalsDataString = vitalsDataString + createVitalsListItem(activityContext.getResources().getString(R.string.prescription_temp_c), !TextUtils.isEmpty(vitalsData.getTemperature()) ? vitalsData.getTemperature().toString() : "");
                } else if (obj.getBoolean("mFahrenheit")) {

                    vitalsDataString = vitalsDataString + createVitalsListItem(activityContext.getResources().getString(R.string.prescription_temp_f), !TextUtils.isEmpty(vitalsData.getTemperature()) ? convertCtoF(vitalsData.getTemperature()) : "");
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //vitalsDataString = vitalsDataString + createVitalsListItem(activityContext.getString(R.string.table_temp), vitalsData.getTemperature());
        vitalsDataString = vitalsDataString + createVitalsListItem(activityContext.getString(R.string.table_spo2), vitalsData.getSpo2());
        vitalsDataString = vitalsDataString + createVitalsListItem(activityContext.getString(R.string.respiratory_rate), vitalsData.getResp());
        vitalsDataString = vitalsDataString + createVitalsListItem(activityContext.getString(R.string.haemoglobin), vitalsData.getHaemoglobin());
        vitalsDataString = vitalsDataString + createVitalsListItem(activityContext.getString(R.string.sugar_random), vitalsData.getSugarRandom());
        vitalsDataString = vitalsDataString + createVitalsListItem(activityContext.getString(R.string.blood_group_txt), vitalsData.getBloodGroup());

        if (vitalsDataString.isEmpty()) return "";

        finalVitalsData = openingDivTag
                + openingDataSectionTag
                + vitalsTitleTag
                + dataSectionContentConsultationTitle
                + unorderedListOpeningTag
                + vitalsDataString
                + unorderedListClosingTag
                + closingDivTag
                + closingDivTag
                + closingDivTag;

        return finalVitalsData;
    }

    private String createBPListItem(String label, String value, String color) {
        String listOpeningTag = "<li>";
        String listClosingTag = "</li>";
        String divListItemOpeningTag = "<div class=\"list-item\">";
        String labelOpeningTag = "<label>";
        String labelClosingTag = "</label>";
        String divListItemContentOpeningTag = "<div class=\"list-item-content\">";
        String closingDivTag = "</div>";

        String newValue = value;
        if (newValue == null || newValue.isEmpty() || newValue.equalsIgnoreCase("0")) {
            newValue = activityContext.getString(R.string.not_provided);
            return newValue;
        }

        if (color != null && !color.isEmpty()) {
            newValue = "<span style=\"color: " + color + ";\">" + newValue + "</span>";
        }

        return listOpeningTag
                + divListItemOpeningTag
                + labelOpeningTag
                + label + (label.endsWith(":") ? " " : ": ")
                + labelClosingTag
                + divListItemContentOpeningTag
                + newValue
                + closingDivTag
                + closingDivTag
                + listClosingTag;
    }

    private String getSystolicColor(int systolic) {
        int colorResourceId;
        if (systolic >= 90 && systolic < 120) {
            colorResourceId = R.color.ui2_sys1_ekal;
        } else if (systolic >= 120 && systolic <= 139) {
            colorResourceId = R.color.ui2_sys2_ekal;
        } else {
            colorResourceId = R.color.ui2_bp_default_ekal;
        }
        int colorInt = ContextCompat.getColor(activityContext, colorResourceId);
        return String.format("#%06X", (0xFFFFFF & colorInt));
    }

    private String getDiastolicColor(int diastolic) {
        int colorResourceId;
        if (diastolic < 80) {
            colorResourceId = R.color.ui2_dia1_ekal;
        } else if (diastolic >= 80 && diastolic <= 99) {
            colorResourceId = R.color.ui2_dia2_ekal;
        } else {
            colorResourceId = R.color.ui2_bp_default_ekal;
        }
        int colorInt = ContextCompat.getColor(activityContext, colorResourceId);
        return String.format("#%06X", (0xFFFFFF & colorInt));
    }

    private String createVitalsListItem(String label, String value) {
        String listOpeningTag = "<li>";
        String listClosingTag = "</li>";
        String divListItemOpeningTag = "<div class=\"list-item\">";
        String labelOpeningTag = "<label>";
        String labelClosingTag = "</label>";
        String divListItemContentOpeningTag = "<div class=\"list-item-content\">";
        String closingDivTag = "</div>";

        String newValue = value;
        if (newValue == null || newValue.isEmpty() || newValue.equalsIgnoreCase("0")) {
            newValue = activityContext.getString(R.string.not_provided);
            //we are not gonna show empty vitals on ui
            //we removing whole ui for corresponding vitals
            //return "";
        }

        return listOpeningTag
                + divListItemOpeningTag
                + labelOpeningTag
                + label + (label.endsWith(":") ? " " : ": ")
                + labelClosingTag
                + divListItemContentOpeningTag
                + newValue
                + closingDivTag
                + closingDivTag
                + listClosingTag;
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
        String lineBreak = "<br>";

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

        if ((tableDataFinalString + tableAdditionalDataFinalString).isEmpty()) {
            return "";
        }

        return finalMedicationData + lineBreak;
    }

    private String bifurcateMedicationData(String medicationData) {
        if (medicationData.isEmpty()) {
            //returning empty because currently if no medication found
            // then we will disable the medication ui
            return "";
            //return handleEmptyMedicationData();
        }

        String finalMedicationDataString = "";
        //String[] medicationDataArray = medicationData.split("\n");

        String tableRowOpeningTag = "<tr>";
        String tableRowClosingTag = " </tr>";
        String tableDataOpeningTag = "<td>";
        String tableDataClosingTag = "</td>";

        StringBuilder builder = new StringBuilder();

        String[] medicationDataArray = medicationData.split("\n");

        for (String medicine : medicationDataArray) {
            if (ParserUtils.Companion.parseMedication(medicine) instanceof PrescribedMedicineModel) {
                PrescribedMedicineModel model = ((PrescribedMedicineModel) ParserUtils.Companion.parseMedication(medicine));

                builder.append(tableRowOpeningTag);

                builder.append(tableDataOpeningTag);
                assert model != null;
                builder.append(model.getMedicineName());
                builder.append(tableDataClosingTag);

                builder.append(tableDataOpeningTag);
                builder.append(model.getStrength());
                builder.append(tableDataClosingTag);

                builder.append(tableDataOpeningTag);
                builder.append(model.getNoOfDays());
                builder.append(tableDataClosingTag);

                builder.append(tableDataOpeningTag);
                builder.append(model.getTiming());
                builder.append(tableDataClosingTag);

                builder.append(tableDataOpeningTag);
                builder.append(model.getRemark());
                builder.append(tableDataClosingTag);

                builder.append(tableRowClosingTag);

                finalMedicationDataString = builder.toString();
            }

    /*    if (!checkIfArrayContainsMedicationData(medicationDataArray)) {
            finalMedicationDataString = handleEmptyMedicationData();
        }
        else {
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
        }*/
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
        String divClassLabelTag = "<div>"
                + "<label class=\"border-0 font-weight-bold\">Additional Instructions:</label>\n"
                + "</div>";
        String unorderedListOpeningTag = "<ul class=\"items-list\">";
        String unorderedListClosingTag = "</ul>";
        String listOpeningTag = "<li>";
        String listClosingTag = "</li>";
        String divOpeningTag = "<div class=\"d-flex justify-content-between align-items-center\">";
        String divClosingTag = "</div>";
        String spanOpeningTag = "<span>";
        String spanClosingTag = "</span>";

        StringBuilder additionalInstructionsData = new StringBuilder();
       /* String[] medicationDataArray = medicationData.split("\n");
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
        }*/

        StringBuilder builder = new StringBuilder();

        String[] medicationDataArray = medicationData.split("\n");

        for (String medicine : medicationDataArray) {
            if (ParserUtils.Companion.parseMedication(medicine) instanceof String) {
                if(!medicine.matches(RegexUtils.getAdditionalInstructionRegex()) && medicine.contains("::")){
                    additionalInstructionsData.append(listOpeningTag);
                    additionalInstructionsData.append(divOpeningTag);
                    additionalInstructionsData.append(spanOpeningTag);
                    additionalInstructionsData.append(ParserUtils.Companion.parseMedication(medicine));
                    additionalInstructionsData.append(spanClosingTag);
                    additionalInstructionsData.append(divClosingTag);
                    additionalInstructionsData.append(listClosingTag);
                }
            }

        }
        if (additionalInstructionsData.length() == 0) return "";

        finalAdditionalDataString = divClassLabelTag
                + unorderedListOpeningTag
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
        String lineBreak = "<br>";

        String bifurcatedAdviceData = checkAndBifurcateAdviceData(adviceData);

        if (bifurcatedAdviceData.isEmpty()) return "";

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

        return finalAdviceString + lineBreak;
    }

    private String checkAndBifurcateAdviceData(String adviceData) {
        StringBuilder finalAdviceStringBuilder = new StringBuilder();
        String listOpeningTag = "<li>";
        String listClosingTag = "</li>";
        String divClassOpeningTagCenter = "<div class=\"d-flex justify-content-between align-items-center\">";
        String closingDivTag = "</div>";
        String spanOpeningTag = "<span>";
        String spanClosingTag = "</span>";

        //removing all bullets as we are adding through html
        adviceData = adviceData.replaceAll(Node.bullet, "");

        if (!adviceData.contains("\n\n")) {
            //checking any advice exist or not
            //if not then return empty string
            //because we will disable advice ui if advice is empty
            if (adviceData.isEmpty()) return "";
            finalAdviceStringBuilder.append(listOpeningTag);
            finalAdviceStringBuilder.append(divClassOpeningTagCenter);
            finalAdviceStringBuilder.append(spanOpeningTag);
            finalAdviceStringBuilder.append(adviceData);
            finalAdviceStringBuilder.append(spanClosingTag);
            finalAdviceStringBuilder.append(closingDivTag);
            finalAdviceStringBuilder.append(listClosingTag);
        } else {
            String[] adviceArray = adviceData.split("\n\n");
            //checking any advice exist or not
            //if not then return empty string
            //because we will disable advice ui if advice is empty
            if (adviceArray.length == 0) return "";
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

        /*if (!adviceData.contains("<br><br>")) {
            //checking any advice exist or not
            //if not then return empty string
            //because we will disable advice ui if advice is empty
            if (adviceData.isEmpty()) return "";
            finalAdviceStringBuilder.append(listOpeningTag);
            finalAdviceStringBuilder.append(divClassOpeningTagCenter);
            finalAdviceStringBuilder.append(spanOpeningTag);
            finalAdviceStringBuilder.append(adviceData);
            finalAdviceStringBuilder.append(spanClosingTag);
            finalAdviceStringBuilder.append(closingDivTag);
            finalAdviceStringBuilder.append(listClosingTag);
        }
        else {
            String[] adviceArray = adviceData.split("<br><br>");
            //checking any advice exist or not
            //if not then return empty string
            //because we will disable advice ui if advice is empty
            if (adviceArray.length == 0) return "";
            for (String advice : adviceArray) {
                finalAdviceStringBuilder.append(listOpeningTag);
                finalAdviceStringBuilder.append(divClassOpeningTagCenter);
                finalAdviceStringBuilder.append(spanOpeningTag);
                finalAdviceStringBuilder.append(advice);
                finalAdviceStringBuilder.append(spanClosingTag);
                finalAdviceStringBuilder.append(closingDivTag);
                finalAdviceStringBuilder.append(listClosingTag);
            }
        }*/

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
        String lineBreak = "<br>";

        String bifurcatedTestsData = checkAndBifurcateTestData(testData);

        if (bifurcatedTestsData.isEmpty()) return "";
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

        return finalTestString + lineBreak;
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
            //checking any test exist or not
            //if not then return empty string
            //because we will disable test ui if test is empty
            if (testsData.isEmpty()) return "";
            finalTestsStringBuilder.append(listOpeningTag);
            finalTestsStringBuilder.append(divClassOpeningTagCenter);
            finalTestsStringBuilder.append(spanOpeningTag);
            finalTestsStringBuilder.append(testsData);
            finalTestsStringBuilder.append(spanClosingTag);
            finalTestsStringBuilder.append(closingDivTag);
            finalTestsStringBuilder.append(listClosingTag);
        } else {
            String[] adviceArray = testsData.split("\n\n");
            //checking any test exist or not
            //if not then return empty string
            //because we will disable test ui if test is empty
            if (adviceArray.length == 0) return "";

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

        if (bifurcatedReferralData.isEmpty()) return "";

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
            //checking any referral out exist or not
            //if not then return empty string
            //because we will disable referral out ui if referral out is empty
            return "";
        } else {
            String[] referredOutArray;
            if (referredOutData.contains("\n\n")) {
                referredOutArray = referredOutData.split("\n\n");
            } else {
                referredOutArray = new String[]{referredOutData};
            }
            //checking any referral out exist or not
            //if not then return empty string
            //because we will disable referral out ui if referral out is empty
            if (referredOutArray.length == 0) return "";

            for (String referred : referredOutArray) {
                if (referred.contains(":")) {
                    String[] referredOutNewArray = referred.split(":");
                    finalReferredOutData.append(tableRowOpeningTag);

                    for (String data : referredOutNewArray) {
                        finalReferredOutData.append(tableDataOpeningTag);
                        finalReferredOutData.append(data);
                        finalReferredOutData.append(tableDataClosingTag);
                    }
                }
            }

            finalReferredOutData.append(tableRowClosingTag);
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

        if (followUpData.equalsIgnoreCase("") || followUpData.equalsIgnoreCase("No")) {
            //checking any follow up exist or not
            //if not then return empty string
            //because we will disable follow up ui if follow up is empty
            return "";
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
            //added these logic to handle array indexOutOfBound exception
            String date = "";
            if (followUpArrayData.length > 0) {
                date = followUpArrayData[0];
            }
            divSectionContentOpeningTag = divSectionContentOpeningTag
                    + "<li>"
                    + "<div class=\"list-item\">"
                    + "<label>Follow-up Date</label>"
                    + "<div class=\"list-item-content\">"
                    + DateAndTimeUtils.formatDateFromOnetoAnother(date, "yyyy-MM-dd", "dd-MM-yyyy")
                    + "</div>"
                    + "</div>"
                    + "</li>";

            if (followUpData.contains("Time:")) {

                //added these logic to handle array indexOutOfBound exception
                String time = "";
                if (followUpArrayData.length > 1) {
                    if (followUpArrayData[1].contains("Time:")) {
                        if (followUpArrayData[1].split("Time:").length > 1) {
                            time = followUpArrayData[1].split("Time:")[1];
                        }
                    }

                }
                divSectionContentOpeningTag = divSectionContentOpeningTag
                        + "<li>"
                        + "<div class=\"list-item\">"
                        + "<label>Follow-up Time</label>"
                        + "<div class=\"list-item-content\">"
                        + time
                        + "</div>"
                        + "</div>"
                        + "</li>";
            }

            if (followUpData.contains("Remark:")) {
                //added these logic to handle array indexOutOfBound exception
                String remarks = "";
                if (followUpArrayData.length > 2) {
                    if (followUpArrayData[2].contains("Remark:")) {
                        if (followUpArrayData[2].split("Remark:").length > 1) {
                            remarks = followUpArrayData[2].split("Remark:")[1];
                        }
                    }

                }
                divSectionContentOpeningTag = divSectionContentOpeningTag
                        + "<li>"
                        + "<div class=\"list-item\">"
                        + "<label>Reason for follow-up</label>"
                        + "<div class=\"list-item-content\">"
                        + remarks
                        + "</div>"
                        + "</div>"
                        + "</li>";
            }
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

    private String generateDoctorSignatureData(ClsDoctorDetails details) {
        if (details == null) {
            return "";
        }

        String finalDoctorSignatureString = "";
        String closingDivTag = "</div>";
        String openingSignatureDivTag = "<div class=\"signature w-100\">";
        String floatRightDivOpeningTag = "<div class=\"float-right my-4\">";
        String imageTag = "<img class=\"signature\" alt=\"\" src=\"" + details.getSignature() + "\"/>";
        String divClassTitleNameTag = "<div class=\"title-name\">" + details.getName() + closingDivTag;
        String divClassTitleSpecializationTag = "<div class=\"title\">" + details.getSpecialization() + closingDivTag;
        String divClassRegistrationTag = "<div class=\"sub-title\">" + "Registration No: " + details.getRegistrationNumber() + closingDivTag;

        finalDoctorSignatureString = openingSignatureDivTag
                + floatRightDivOpeningTag
                + imageTag
                + divClassTitleNameTag
                + divClassTitleSpecializationTag
                + divClassRegistrationTag
                + closingDivTag
                + closingDivTag;

        return finalDoctorSignatureString;
    }

    public void setPatientData(Patient patient, String visitDate) {
        String name = patient.getFirst_name().concat(" ").concat(patient.getLast_name());
        String age = activityContext.getString(R.string.prescription_age, String.valueOf(DateAndTimeUtils.getAge(patient.getDate_of_birth(), activityContext)));
        String gender = activityContext.getString(R.string.prescription_gender, patient.getGender());

        String addressLine1 = checkValueAndReturnNA(patient.getAddress1());
        String addressLine2 = checkValueAndReturnNA(patient.getAddress2());
        String postalCode = checkValueAndReturnNA(patient.getPostal_code());

        //String address = activityContext.getString(R.string.prescription_address, addressLine1.concat(",").concat(addressLine2).concat(", ").concat(postalCode));
        String address = activityContext.getString(R.string.prescription_address, patient.getCity_village());
        String openMrsId = activityContext.getString(R.string.prescription_patient_id, patient.getOpenmrs_id());
        String dateOfVisit = activityContext.getString(R.string.prescription_date_of_visit, visitDate);

        binding.tvPatientName.setText(name);
        binding.tvPatientAge.setText(age);
        binding.tvPatientGender.setText(gender);
        binding.tvPatientAddress.setText(address);
        binding.tvPatientId.setText(openMrsId);
        binding.tvPatientDateOfVisit.setText(dateOfVisit);
    }

    public void setVitals(VitalsObject vitalsObject) {
        String height = getOrganizedDataWithBullets(activityContext.getString(R.string.prescription_height, VisitUtils.convertHeightIntoFeets(checkValueAndReturnNA(vitalsObject.getHeight()), activityContext)));
        String weight = getOrganizedDataWithBullets(activityContext.getString(R.string.prescription_weight, checkValueAndReturnNA(vitalsObject.getWeight())));
        String bmi = getOrganizedDataWithBullets(activityContext.getString(R.string.prescription_bmi, checkValueAndReturnNA(vitalsObject.getBmi())));
        String bpSys = checkValueAndReturnNA(vitalsObject.getBpsys());
        String bpDia = checkValueAndReturnNA(vitalsObject.getBpdia());
        String bloodPressure = getOrganizedDataWithBullets(activityContext.getString(R.string.prescription_bp, bpSys.concat(" / ").concat(bpDia)));
        String pulse = getOrganizedDataWithBullets(activityContext.getString(R.string.prescription_pulse_value, checkValueAndReturnNA(vitalsObject.getPulse())));
        String temperature = getOrganizedDataWithBullets(activityContext.getString(R.string.prescription_temperature, checkValueAndReturnNA(vitalsObject.getTemperature())));
        String respiratoryRate = getOrganizedDataWithBullets(activityContext.getString(R.string.prescription_respiratory_rate, checkValueAndReturnNA(vitalsObject.getResp())));
        String spO2 = getOrganizedDataWithBullets(activityContext.getString(R.string.prescription_spo2, checkValueAndReturnNA(vitalsObject.getSpo2())));
        String haemoglobin = getOrganizedDataWithBullets(activityContext.getString(R.string.prescription_haemoglobin, checkValueAndReturnNA(vitalsObject.getHaemoglobin())));
        String bloodGroup = getOrganizedDataWithBullets(activityContext.getString(R.string.prescription_blood_group, checkValueAndReturnNA(vitalsObject.getBloodGroup())));
//        String sugarFasting = getOrganizedDataWithBullets(activityContext.getString(R.string.prescription_sugar_fasting, "NA"));
        String sugarRandom = getOrganizedDataWithBullets(activityContext.getString(R.string.prescription_sugar_random, checkValueAndReturnNA(vitalsObject.getSugarRandom())));

        binding.tvVitalsHeight.setText(height);
        binding.tvVitalsWeight.setText(weight);
        setBmiStatus(binding.tvVitalsBmi, bmi);
        setBpStatus(binding.tvVitalsBp, bpSys, bpDia);
        binding.tvVitalsBp.setText(bloodPressure);
        binding.tvVitalsPulse.setText(pulse);
        binding.tvVitalsTemperature.setText(temperature);
        binding.tvVitalsRespiratoryRate.setText(respiratoryRate);
        binding.tvVitalsSpo2.setText(spO2);
        binding.tvVitalsHaemoglobin.setText(haemoglobin);
        binding.tvVitalsBloodGroup.setText(bloodGroup);
//        binding.tvVitalsSugarFasting.setText(sugarFasting);
        binding.tvVitalsSugarRandom.setText(sugarRandom);
    }

    private void setBmiStatus(TextView bmiTextView, String mbmi) {
        double bmi = 0.0;
        try {
            bmi = Double.parseDouble(extractBmiFromString(mbmi));
            bmiTextView.setText(mbmi + " " + ContextCompat.getString(activityContext, R.string.kg_m));
            if (bmi < 18.50) {
                bmiTextView.setTextColor(ContextCompat.getColor(activityContext, R.color.ui2_bmi1_ekal));
            } else if (bmi >= 18.50 && bmi <= 22.99) {
                bmiTextView.setTextColor(ContextCompat.getColor(activityContext, R.color.ui2_bmi2_ekal));
            } else if (bmi >= 23 && bmi <= 24.99) {
                bmiTextView.setTextColor(ContextCompat.getColor(activityContext, R.color.ui2_bmi3_ekal));
            } else if (bmi >= 25 && bmi <= 29.99) {
                bmiTextView.setTextColor(ContextCompat.getColor(activityContext, R.color.ui2_bmi4_ekal));
            } else if (bmi > 30) {
                bmiTextView.setTextColor(ContextCompat.getColor(activityContext, R.color.ui2_bmi5_ekal));
            }
        } catch (NumberFormatException exception) {
            bmiTextView.setText(mbmi);
        }
    }

    private void setBpStatus(TextView bpTextView, String mSys, String mDia) {
        if (mSys.equalsIgnoreCase("NA") && mDia.equalsIgnoreCase("NA")) {
            return;
        }

        int sys = Integer.parseInt(mSys);
        int dia = Integer.parseInt(mDia);

        int sysColor = ContextCompat.getColor(activityContext, R.color.ui2_bp_default_ekal);
        int diaColor = ContextCompat.getColor(activityContext, R.color.ui2_bp_default_ekal);

        if (sys >= 90 && sys < 120) {
            sysColor = ContextCompat.getColor(activityContext, R.color.ui2_sys1_ekal);
        } else if (sys >= 120 && sys <= 139) {
            sysColor = ContextCompat.getColor(activityContext, R.color.ui2_sys2_ekal);
        }

        if (dia < 80) {
            diaColor = ContextCompat.getColor(activityContext, R.color.ui2_dia1_ekal);
        } else if (dia >= 80 && dia <= 99) {
            diaColor = ContextCompat.getColor(activityContext, R.color.ui2_dia2_ekal);
        }

        bpTextView.setText(mSys + "/" + mDia);
        bpTextView.setTextColor(sysColor);

        SpannableString spannableString = new SpannableString(mSys + "/" + mDia);
        spannableString.setSpan(new ForegroundColorSpan(sysColor), 0, mSys.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(diaColor), mSys.length() + 1, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        bpTextView.setText(spannableString);
    }

    public static String extractBmiFromString(String input) {
        Pattern pattern = Pattern.compile("BMI:\\s([0-9.]+)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "BMI not found";
        }
    }

    public void setComplaintData(String complaints) {
        complaints = removeNodeBulletsAndLineBreaks(complaints);
        complaints = getOrganizedDataWithBullets(complaints);
        checkDataValidOrHideViews(binding.tvPresentingComplaints, binding.tvPresentingComplaintsData, complaints);
    }

    public void setDiagnosis(String diagnosis) {
        diagnosis = removeNodeBulletsAndLineBreaks(diagnosis);
        diagnosis = getOrganizedDiagnosisDataWithBullets(diagnosis);
        checkDataValidOrHideViews(binding.tvDiagnosis, binding.tvDiagnosisData, diagnosis);
    }

    public void setMedication(String medication) {
        medication = removeNodeBulletsAndLineBreaks(medication);
        medication = getOrganizeMedicineDataWithBullets(medication);
        checkDataValidOrHideViews(binding.tvMedication, binding.tvMedicationData, medication);
    }

    public void setTests(String tests) {
        tests = removeNodeBulletAndLineBreakFromTests(tests);
        tests = getOrganizedDataWithBullets(tests);
        checkDataValidOrHideViews(binding.tvTests, binding.tvTestsData, tests);
    }

    public void setAdvice(String advice) {
       /* advice = removeNodeBulletsAndLineBreaks(advice);
        advice = getOrganizedDataWithBullets(advice);*/
        checkDataValidOrHideViews(binding.tvGeneralAdvice, binding.tvGeneralAdviceData, advice);
    }

    public void setFollowUp(String followUp) {
        checkDataValidOrHideViews(binding.tvFollowUp, binding.tvFollowUpData, followUp);
    }

    public void setDoctorData(ClsDoctorDetails clsDoctorDetails) {
        if (clsDoctorDetails == null) return;

        //checking text signature exist or not if exit then setting the text
        //otherwise setting image signature
        if (clsDoctorDetails.getTextOfSign() != null) {
            binding.imDrSignature.setVisibility(View.GONE);
            binding.tvDrSignature.setVisibility(View.VISIBLE);
            binding.tvDrSignature.setText(clsDoctorDetails.getTextOfSign());
            binding.tvDrSignature.setTypeface(getSignatureTypeface(clsDoctorDetails.getFontOfSign()));
        } else if (clsDoctorDetails.getSignature() != null) {

            binding.tvDrSignature.setVisibility(View.GONE);
            binding.imDrSignature.setVisibility(View.VISIBLE);
            setBase64ToImageView(clsDoctorDetails.getSignature(), binding.imDrSignature);
        }
        binding.tvDrName.setText(checkValueAndReturnNA(clsDoctorDetails.getName()));


        String degreeSpecialization = "";
        if (clsDoctorDetails.getQualification() != null) {
            degreeSpecialization = clsDoctorDetails.getQualification();
        }

        if (clsDoctorDetails.getSpecialization() != null) {
            if (degreeSpecialization.isEmpty() || degreeSpecialization.isBlank()) {
                degreeSpecialization = degreeSpecialization.concat(", ").concat(clsDoctorDetails.getSpecialization());
            } else {
                degreeSpecialization = clsDoctorDetails.getSpecialization();
            }
        }

        binding.tvDrDegreeSpecialization.setText(degreeSpecialization);

        binding.tvDrEmail.setText(activityContext.getString(R.string.prescription_dr_email, checkValueAndReturnNA(clsDoctorDetails.getEmailId())));
        binding.tvDrRegistration.setText(activityContext.getString(R.string.prescription_dr_registration, checkValueAndReturnNA(clsDoctorDetails.getRegistrationNumber())));
    }


    public void setBase64ToImageView(String base64String, ImageView imageView) {
        try {
            // Remove data:image/...;base64, if exists
            if (base64String.contains(",")) {
                base64String = base64String.split(",")[1];
            }

            // Decode Base64 string
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

            // Convert to Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            // Set to ImageView
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String checkValueAndReturnNA(String value) {
        if (value == null || value.isEmpty() || value.isBlank()) {
            return "NA";
        } else {
            return value;
        }
    }


    public String getOrganizedDataWithBullets(String data) {
        if (data == null || data.isBlank() || data.isEmpty()) return data;

        data = data.trim();
        data = Node.big_bullet.concat(" ").concat(data);
        String[] splitData = data.split("\n");
        data = "";

        for (String string : splitData) {
            if (string.contains(Node.big_bullet)) {
                data = string;
                continue;
            }

            data = data.concat("\n");
            data = data.concat(Node.big_bullet).concat(" ").concat(string);
            data = data.concat("\n");
        }
        return data;
    }

    public String getOrganizedDiagnosisDataWithBullets(String data) {
        if (data == null || data.isBlank() || data.isEmpty()) return data;

        data = data.trim();
        data = Node.big_bullet.concat(" ").concat(data);
        String[] splitData;
        if (data.contains("\n\n")) {
            splitData = data.split("\n\n");
        } else {
            splitData = data.split("\n");
        }
        data = "";

        for (String string : splitData) {
            if (string.contains(Node.big_bullet)) {
                data = string.concat("\n");
                continue;
            }

            data = data.concat("\n");
            data = data.concat(Node.big_bullet).concat(" ").concat(string);
            data = data.concat("\n");
        }
        return data;
    }

    /**
     * medication data is different from others
     * so using separate function fot this
     * @param data
     * @return
     */
    public String getOrganizeMedicineDataWithBullets(String data) {
        if (data == null || data.isBlank() || data.isEmpty()) return data;

        data = data.trim();
        data = Node.big_bullet.concat(" ").concat(data);
        String[] splitData = data.split("\n");
        data = "";

        String additionalInstruction = "";

        for (String string : splitData) {
            if (string.contains(Node.big_bullet)) {
                data = string;
                continue;
            }

            //checking the data is matching with the parser regex or not
            //if yes its a medicine, otherwise its a additional instructions
            if (ParserUtils.Companion.parseMedication(string) instanceof PrescribedMedicineModel) {
                data = data.concat("\n");
                data = data.concat(Node.big_bullet).concat(" ").concat(string);
                data = data.concat("\n");
            } else {
                if(!string.matches(RegexUtils.getAdditionalInstructionRegex()) && string.contains("::")){
                    additionalInstruction = additionalInstruction.concat("\n");
                    additionalInstruction = additionalInstruction.concat(Node.big_bullet).concat(" ").concat(string);
                    //additionalInstruction = additionalInstruction.concat("\n");
                }
            }
        }
        if(!additionalInstruction.isEmpty()){
            data = data + "\n\n"
                    + activityContext.getString(R.string.prescription_additional_ins) + ":\n"
                    + additionalInstruction;
        }
        return data;
    }

    private String removeNodeBulletsAndLineBreaks(String data) {
        if (data.contains(Node.big_bullet)) {
            data = data.replaceAll(Node.big_bullet, "\n");
        }

        if (data.contains(Node.bullet)) {
            data = data.replaceAll(Node.bullet, "\n");
        }

        if (data.contains("\n\n")) {
            data = data.replaceAll("\n\n", "\n");
        }

        return data;
    }

    private void checkDataValidOrHideViews(TextView title, TextView subtitle, String data) {
        if (data == null || data.isBlank() || data.isEmpty()) {
            title.setVisibility(View.GONE);
            subtitle.setVisibility(View.GONE);
        } else {
            subtitle.setText(data);
        }
    }

    private String removeNodeBulletAndLineBreakFromTests(String data) {
        if (data.contains("\n\n")) {
            data = data.replaceAll("\n\n", "");
        }

        if (data.contains(Node.bullet)) {
            data = data.replaceAll(Node.bullet, "\n");
        }

        return data;
    }

    private Typeface getSignatureTypeface(String font) {
       // 4.1.3 code
        /* String directory = "font/youthness.ttf";
        if (font != null) {
            if (font.equalsIgnoreCase("Youthness")) {
                directory = "font/youthness.ttf";
            } else if (font.equalsIgnoreCase("Asem")) {
                directory = "font/asem.otf";
            } else if (font.equalsIgnoreCase("Arty")) {
                directory = "font/arty.otf";
            } else if (font.equalsIgnoreCase("Almondita")) {
                directory = "font/almondita.ttf";
            }*/
        // NCD side code
        String directory = "font/almondita.ttf";
        if(font!=null) {
            if (font.equalsIgnoreCase("Youthness")) {
                directory = "font/Youthness.ttf";
            } else if (font.equalsIgnoreCase("Asem")) {
                directory = "font/Asem.otf";
            } else if (font.equalsIgnoreCase("Arty")) {
                directory = "font/Arty.otf";
            } else if (font.equalsIgnoreCase("Almondita")) {
                directory = "font/almondita.ttf";
            }
        }

        return Typeface.createFromAsset(activityContext.getAssets(), directory);
    }

    public void build(String fileName) {
        DisplayMetrics metrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Display display = activityContext.getDisplay();
            if (display != null) {
                display.getRealMetrics(metrics);
            }
        } else {
            WindowManager windowManager = activityContext.getWindowManager();
            windowManager.getDefaultDisplay().getMetrics(metrics);
        }

        // Measure the view at the exact width and unspecified height to determine the total height needed
        binding.getRoot().measure(View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        binding.getRoot().layout(0, 0, metrics.widthPixels, binding.getRoot().getMeasuredHeight());

        int viewHeight = binding.getRoot().getMeasuredHeight();
        int viewWidth = metrics.widthPixels;

        // Create a PDF document with a single page that matches the content height
        PdfDocument pdfDocument = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(viewWidth, viewHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        binding.getRoot().draw(canvas);

        pdfDocument.finishPage(page);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            // This code looks if there are  existing prescription and deletes them.
            Uri contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.MediaColumns.DISPLAY_NAME + "=?";
            String[] selectionArgs = new String[]{fileName};

            try (Cursor cursor = activityContext.getContentResolver().query(contentUri, null, selection, selectionArgs, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    // Found the existing file, delete it
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
                    Uri fileUri = ContentUris.withAppendedId(contentUri, cursor.getLong(idColumn));
                    activityContext.getContentResolver().delete(fileUri, null, null);
                }
            }

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = activityContext.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream out = activityContext.getContentResolver().openOutputStream(uri)) {
                    pdfDocument.writeTo(out);
                    pdfDocument.close();
                } catch (IOException e) {
                    throw new RuntimeException("Error saving PDF", e);
                }
            }
        } else {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File filePath = new File(downloadsDir, fileName);

            if (filePath.exists()) {
                // If the file exists, delete it
                boolean isDeleted = filePath.delete();
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                pdfDocument.writeTo(fos);
                pdfDocument.close();
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException("Error saving PDF", e);
            }
        }
    }
}