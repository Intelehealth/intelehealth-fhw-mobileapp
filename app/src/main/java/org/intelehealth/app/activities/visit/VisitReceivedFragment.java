package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.ayu.visit.common.VisitUtils.convertCtoFNew;
import static org.intelehealth.app.database.dao.ObsDAO.fetchDrDetailsFromLocalDb;
import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_VISIT_COMPLETE;
import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_VISIT_NOTE;
import static org.intelehealth.app.utilities.UuidDictionary.IS_NCD_VISIT_ATTRIBUTE;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.LocaleList;
import android.text.Html;
import android.util.DisplayMetrics;

import org.intelehealth.app.activities.prescription.PrescriptionBuilder;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.ayu.visit.notification.LocalPrescriptionInfo;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.ClsDoctorDetails;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.CustomLog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.onboarding.PrivacyPolicyActivity_New;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.VisitCountInterface;
import org.intelehealth.app.utilities.exception.DAOException;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Prajwal Waingankar on 3/11/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class VisitReceivedFragment extends Fragment implements VisitAdapter.OnVisitClickListener {
    private RecyclerView recycler_recent, recycler_older /*, recycler_month*/;
    private CardView visit_received_card_header;
    private static SQLiteDatabase db;
    private TextView received_endvisit_no, allvisits_txt, priority_visits_txt;
    int totalCounts = 0, totalCounts_recent = 0, totalCounts_older = 0, totalCounts_month = 0;
    private ImageButton filter_icon, priority_cancel;
    private CardView filter_menu;
    private RelativeLayout filter_relative, no_patient_found_block, main_block;
    private List<PrescriptionModel> mRecentList, mOlderList, mMonthsList;
    private VisitAdapter recent_adapter, older_adapter;
    TextView recent_nodata, older_nodata, month_nodata;
    private androidx.appcompat.widget.SearchView searchview_received;
    private ImageView closeButton;
    private ProgressBar progress;
    private VisitCountInterface mlistener;
    private int recentLimit = 40, olderLimit = 40;
    private int recentStart = 0, recentEnd = recentStart + recentLimit;
    private boolean isRecentFullyLoaded = false;
    private int olderStart = 0, olderEnd = olderStart + olderLimit;
    private boolean isolderFullyLoaded = false;
    NestedScrollView nestedscrollview;
    List<PrescriptionModel> mRecentPrescriptionModelList = new ArrayList<>();
    List<PrescriptionModel> mOlderPrescriptionModelList = new ArrayList<>();

    ObsDTO complaint = new ObsDTO();
    ObsDTO famHistory = new ObsDTO();
    ObsDTO patHistory = new ObsDTO();
    ObsDTO phyExam = new ObsDTO();
    ObsDTO height = new ObsDTO();
    ObsDTO weight = new ObsDTO();
    ObsDTO pulse = new ObsDTO();
    ObsDTO bpSys = new ObsDTO();
    ObsDTO bpDias = new ObsDTO();
    ObsDTO temperature = new ObsDTO();
    ObsDTO spO2 = new ObsDTO();
    ObsDTO mBloodGroupObsDTO = new ObsDTO();
    ObsDTO haemoglobinDTO = new ObsDTO();
    ObsDTO sugarRandomDTO = new ObsDTO();
    ObsDTO resp = new ObsDTO();

    String mHeight, mWeight, mBMI, mBP, mPulse, mTemp, mSPO2, mresp;

    String diagnosisReturned = "";
    String rxReturned = "";
    String testsReturned = "";
    String adviceReturned = "";
    String doctorName = "";
    String additionalReturned = "";
    String followUpDate = "";
    String referredSpeciality = "";

    String appLanguage, patientUuid, visitUuid, state, patientName, patientGender, intentTag, visitUUID, medicalAdvice_string = "", medicalAdvice_HyperLink = "", isSynedFlag = "";
    ClsDoctorDetails objClsDoctorDetails;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visit_received, container, false);
        initUI(view);
        setLocale(getContext());
        mlistener = (VisitCountInterface) getActivity();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        defaultData();
        visitData();
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        context.createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }

    public void onShareIconClicked(PrescriptionModel model) {
        SessionManager sessionManager = new SessionManager(requireContext());
        String language = sessionManager.getAppLanguage();

        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            requireContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());
        }

        String[] columnsToReturn = {"startdate"};
        String visitIdOrderBy = "startdate";
        String visitIDSelection = "uuid = ?";
        String[] visitIDArgs = {model.getVisitUuid()};
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();

        final Cursor visitIDCursor = db.query("tbl_visit", columnsToReturn, visitIDSelection, visitIDArgs, null, null, visitIdOrderBy);
        visitIDCursor.moveToLast();
        String startDateTime = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("startdate"));

        String[] eColumns = {"visituuid", "encounter_type_uuid"};
        String[] eValues = {model.getVisitUuid(), UuidDictionary.ENCOUNTER_VITALS};
        EncounterDTO mEncounter = queryAndGetRowAsObject("tbl_encounter", eColumns, eValues, EncounterDTO.class);

        preparePrescriptionDiagnosisInfo(model.getVisitUuid());
        preparePrescriptionVitals(mEncounter.getUuid());

        String[] pColumns = {"uuid"};
        String[] pValues = {model.getPatientUuid()};
        Patient mPatient = queryAndGetRowAsObject("tbl_patient", pColumns, pValues, Patient.class);

        String visitStartDate = DateAndTimeUtils.SimpleDatetoLongDate(startDateTime);

        String fileNamePatientName = mPatient.getFirst_name() + "-" + mPatient.getLast_name().charAt(0);
        String prescriptionString = "Prescription";

        String fileName = fileNamePatientName.concat("-").concat(prescriptionString).concat("-").concat(visitStartDate).concat(".pdf");


        buildAndSavePrescription(fileName, mPatient, visitStartDate, model.getVisitUuid());

        //try {
        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getApplicationContext().getPackageName() + ".provider", pdfFile);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setClipData(ClipData.newRawUri("PDF", uri));

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        //intent.setPackage("com.whatsapp");
        String pkg = StringUtils.getWhatsAppPackage(requireContext());
        if (pkg != null) {
            intent.setPackage(pkg);
            requireActivity().grantUriPermission(
                    pkg,
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            );
            startActivity(intent);
            updateLocalPrescriptionInformations(model.getVisitUuid());
        } else {
            Toast.makeText(requireContext(), getString(R.string.please_install_whatsapp), Toast.LENGTH_LONG).show();
        }
    }

    public <T> T queryAndGetRowAsObject(String tableName, String[] conditionColumns, String[] conditionValues, Class<T> targetClass) {
        if (conditionColumns == null || conditionValues == null || conditionColumns.length != conditionValues.length) {
            throw new IllegalArgumentException("Condition columns and values must not be null and must have the same length.");
        }

        // Build the WHERE clause
        StringBuilder selectionBuilder = new StringBuilder();
        String[] selectionArgs = new String[conditionValues.length];

        for (int i = 0; i < conditionColumns.length; i++) {
            if (i > 0) {
                selectionBuilder.append(" AND ");
            }
            selectionBuilder.append(conditionColumns[i]).append(" = ?");
            selectionArgs[i] = conditionValues[i];
        }

        String selection = selectionBuilder.toString();

        // Query the database
        Cursor cursor = db.query(tableName, null, selection, selectionArgs, null, null, null);
        T obj = null;
        if (cursor != null && cursor.moveToFirst()) {
            obj = mapCursorToObject(cursor, targetClass);
            cursor.close();
        }

        return obj;
    }

    public <T> T mapCursorToObject(Cursor cursor, Class<T> targetClass) {
        try {
            T obj = targetClass.newInstance();
            for (Field field : targetClass.getDeclaredFields()) {
                field.setAccessible(true);
                String columnName = field.getName();
                int columnIndex = cursor.getColumnIndex(columnName);
                if (columnIndex != -1 && !cursor.isNull(columnIndex)) {
                    Class<?> fieldType = field.getType();
                    if (fieldType == int.class || fieldType == Integer.class) {
                        field.set(obj, cursor.getInt(columnIndex));
                    } else if (fieldType == long.class || fieldType == Long.class) {
                        field.set(obj, cursor.getLong(columnIndex));
                    } else if (fieldType == float.class || fieldType == Float.class) {
                        field.set(obj, cursor.getFloat(columnIndex));
                    } else if (fieldType == double.class || fieldType == Double.class) {
                        field.set(obj, cursor.getDouble(columnIndex));
                    } else if (fieldType == String.class) {
                        field.set(obj, cursor.getString(columnIndex));
                    } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                        field.set(obj, cursor.getInt(columnIndex) != 0);
                    }
                }
            }

            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void preparePrescriptionDiagnosisInfo(String visitUuid) {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        String visitnote = "";
        EncounterDAO encounterDAO = new EncounterDAO();
        String encounterIDSelection = "visituuid = ? AND voided = ?";
        String[] encounterIDArgs = {visitUuid, "0"};
        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
        if (encounterCursor != null && encounterCursor.moveToFirst()) {
            do {
                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VISIT_NOTE").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    visitnote = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                }
            } while (encounterCursor.moveToNext());

        }
        encounterCursor.close();

        String[] columns = {"value", " conceptuuid"};
        String visitSelection = "encounteruuid = ? and voided = ? and sync = ?";
        String[] visitArgs = {visitnote, "0", "TRUE"};
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
        if (visitCursor.moveToFirst()) {
            do {
                String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                parseData(dbConceptID, dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }

    public void preparePrescriptionVitals(String encounterId) {
        String[] columns = {"value", " conceptuuid"};
        String visitSelection = "encounteruuid = ? AND voided!='1'";
        String[] visitArgs = {encounterId};
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
        if (visitCursor.moveToFirst()) {
            do {
                String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                parseData(dbConceptID, dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }

    private void buildAndSavePrescription(String fileName, Patient patient, String visitStartDate, String visitUuid) {
        fetchAndSaveDoctorDetails(visitUuid);

        PrescriptionBuilder builder = new PrescriptionBuilder((AppCompatActivity) getActivity());
        builder.setPatientData(patient, visitStartDate);
        builder.setVitals(getVitals());
        builder.setComplaintData(formatComplaintData(complaint.getValue()));
        builder.setDiagnosis(diagnosisReturned);
        builder.setMedication(rxReturned);
        builder.setTests(testsReturned);
        builder.setAdvice(medicalAdvice_string);
        builder.setFollowUp(followUpDate);
        builder.setDoctorData(objClsDoctorDetails);
        builder.build(fileName);
    }

    private void fetchAndSaveDoctorDetails(String visitUuid) {
        if (objClsDoctorDetails != null) {
            return;
        }
        String drDetails = fetchDrDetailsFromLocalDb(visitUuid);
        objClsDoctorDetails = new Gson().fromJson(drDetails, ClsDoctorDetails.class);
    }

    private VitalsObject getVitals() {
        VitalsObject vitalsObject = new VitalsObject();
        vitalsObject.setHeight(checkAndReturnVitalsValue(height));
        vitalsObject.setWeight(checkAndReturnVitalsValue(weight));
        if (weight.getValue() != null) {
            String mWeight = weight.getValue().split(" ")[0];
            String mHeight = height.getValue().split(" ")[0];
            if ((mHeight != null && mWeight != null) && !mHeight.isEmpty() && !mWeight.isEmpty()) {
                double numerator = Double.parseDouble(mWeight) * 10000;
                double denominator = Double.parseDouble(mHeight) * Double.parseDouble(mHeight);
                double bmi_value = numerator / denominator;
                mBMI = String.format(Locale.ENGLISH, "%.2f", bmi_value);
            } else {
                mBMI = "";
            }
        }
        vitalsObject.setBmi(mBMI);
        vitalsObject.setBpsys(checkAndReturnVitalsValue(bpSys));
        vitalsObject.setBpdia(checkAndReturnVitalsValue(bpDias));
        vitalsObject.setPulse(checkAndReturnVitalsValue(pulse));
        vitalsObject.setTemperature(checkAndReturnTemperatureValue(temperature));
        vitalsObject.setResp(checkAndReturnVitalsValue(resp));
        vitalsObject.setHaemoglobin(checkAndReturnVitalsValue(haemoglobinDTO));
        vitalsObject.setBloodGroup(checkAndReturnVitalsValue(mBloodGroupObsDTO));
        vitalsObject.setSugarRandom(checkAndReturnVitalsValue(sugarRandomDTO));
        vitalsObject.setSpo2(checkAndReturnVitalsValue(spO2));
        return vitalsObject;
    }

    private void parseData(String concept_id, String value) {
        switch (concept_id) {
            case UuidDictionary.CURRENT_COMPLAINT: {
                complaint.setValue(value.replace("?<b>", Node.bullet_arrow));
                break;
            }
            case UuidDictionary.PHYSICAL_EXAMINATION: {
                phyExam.setValue(value);
                break;
            }
            case UuidDictionary.HEIGHT: {
                height.setValue(value);
                break;
            }
            case UuidDictionary.WEIGHT: {
                weight.setValue(value);
                break;
            }
            case UuidDictionary.PULSE: {
                pulse.setValue(value);
                break;
            }
            case UuidDictionary.SYSTOLIC_BP: {
                bpSys.setValue(value);
                break;
            }
            case UuidDictionary.DIASTOLIC_BP: {
                bpDias.setValue(value);
                break;
            }
            case UuidDictionary.TEMPERATURE: {
                temperature.setValue(value);
                break;
            }
            case UuidDictionary.RESPIRATORY: {
                resp.setValue(value);
                break;
            }
            case UuidDictionary.SPO2: {
                spO2.setValue(value);
                break;
            }
            case UuidDictionary.BLOOD_GROUP: {
                mBloodGroupObsDTO.setValue(value);
                break;
            }
            case UuidDictionary.HAEMOGLOBIN: {
                haemoglobinDTO.setValue(value);
                break;
            }
            case UuidDictionary.SUGAR_LEVEL_RANDOM: {
                sugarRandomDTO.setValue(value);
                break;
            }
            case UuidDictionary.TELEMEDICINE_DIAGNOSIS: {
                if (!diagnosisReturned.isEmpty()) {
                    diagnosisReturned = diagnosisReturned + ",\n" + value;
                } else {
                    diagnosisReturned = value;
                }
              /*  if (diagnosisCard.getVisibility() != View.VISIBLE) {
                    diagnosisCard.setVisibility(View.VISIBLE);
                }
                diagnosisTextView.setText(diagnosisReturned);*/
                //checkForDoctor();
                break;
            }
            case UuidDictionary.JSV_MEDICATIONS: {
                if (!rxReturned.trim().isEmpty()) {
                    rxReturned = rxReturned + "\n" + value;
                } else {
                    rxReturned = value;
                }
               /* if (prescriptionCard.getVisibility() != View.VISIBLE) {
                    prescriptionCard.setVisibility(View.VISIBLE);
                }
                prescriptionTextView.setText(rxReturned);*/
                //checkForDoctor();
                break;
            }
            case UuidDictionary.MEDICAL_ADVICE: {
                if (!adviceReturned.isEmpty()) {
                    adviceReturned = adviceReturned + "\n" + value;
                    CustomLog.d("GAME", "GAME: " + adviceReturned);
                } else {
                    adviceReturned = value;
                    CustomLog.d("GAME", "GAME_2: " + adviceReturned);
                }
              /*  if (medicalAdviceCard.getVisibility() != View.VISIBLE) {
                    medicalAdviceCard.setVisibility(View.VISIBLE);
                }*/
                //medicalAdviceTextView.setText(adviceReturned);
                CustomLog.d("Hyperlink", "hyper_global: " + medicalAdvice_string);

                int j = adviceReturned.indexOf('<');
                int i = adviceReturned.lastIndexOf('>');
                if (i >= 0 && j >= 0) {
                    medicalAdvice_HyperLink = adviceReturned.substring(j, i + 1);
                } else {
                    medicalAdvice_HyperLink = "";
                }

                CustomLog.d("Hyperlink", "Hyperlink: " + medicalAdvice_HyperLink);

                medicalAdvice_string = adviceReturned.replaceAll(medicalAdvice_HyperLink, "");
                CustomLog.d("Hyperlink", "hyper_string: " + medicalAdvice_string);

                /*
                 * variable a contains the hyperlink sent from webside.
                 * variable b contains the string data (medical advice) of patient.
                 * */
               /* medicalAdvice_string = medicalAdvice_string.replace("\n\n", "\n");
                medicalAdviceTextView.setText(Html.fromHtml(medicalAdvice_HyperLink +
                        medicalAdvice_string.replaceAll("\n", "<br><br>")));*/

                adviceReturned = adviceReturned.replaceAll("\n", "<br><br>");
                //  medicalAdviceTextView.setText(Html.fromHtml(adviceReturned));
               /* medicalAdviceTextView.setText(Html.fromHtml(adviceReturned.replace("Doctor_", "Doctor")));
                medicalAdviceTextView.setMovementMethod(LinkMovementMethod.getInstance());
                CustomLog.d("hyper_textview", "hyper_textview: " + medicalAdviceTextView.getText().toString());*/
                //checkForDoctor();
                break;
            }
            case UuidDictionary.REQUESTED_TESTS: {
                if (!testsReturned.isEmpty()) {
                    testsReturned = testsReturned + "\n\n" + Node.bullet + " " + value;
                } else {
                    testsReturned = Node.bullet + " " + value;
                }
              /*  if (requestedTestsCard.getVisibility() != View.VISIBLE) {
                    requestedTestsCard.setVisibility(View.VISIBLE);
                }
                requestedTestsTextView.setText(testsReturned);*/
                //checkForDoctor();
                break;
            }

            case UuidDictionary.REFERRED_SPECIALIST: {
                if (!referredSpeciality.isEmpty() && !referredSpeciality.contains(value)) {
                    referredSpeciality = referredSpeciality + "\n\n" + Node.bullet + " " + value;
                } else {
                    referredSpeciality = Node.bullet + " " + value;
                }
            }

            case UuidDictionary.ADDITIONAL_COMMENTS: {

//                additionalCommentsCard.setVisibility(View.GONE);

                if (!additionalReturned.isEmpty()) {
                    additionalReturned = additionalReturned + "," + value;
                } else {
                    additionalReturned = value;
                }
////                if (additionalCommentsCard.getVisibility() != View.VISIBLE) {
////                    additionalCommentsCard.setVisibility(View.VISIBLE);
////                }
//                additionalCommentsTextView.setText(additionalReturned);
                //checkForDoctor();
                break;
            }
            case UuidDictionary.FOLLOW_UP_VISIT: {
                if (!followUpDate.isEmpty()) {
                    followUpDate = followUpDate + "," + value;
                } else {
                    followUpDate = value;
                }
              /*  if (followUpDateCard.getVisibility() != View.VISIBLE) {
                    followUpDateCard.setVisibility(View.VISIBLE);
                }
                followUpDateTextView.setText(followUpDate);*/
                //checkForDoctor();
                break;
            }

            default:
                break;
        }
    }

    private String formatComplaintData(String mComplaint) {
        String[] mComplaints = org.apache.commons.lang3.StringUtils.split(mComplaint, Node.bullet_arrow);
        StringBuilder formattedData = new StringBuilder();
        String colon = ":";
        String result = "";

        if (mComplaints != null) {
            for (String mc : mComplaints) {
                String[] complaints = {mc};
                if (complaints != null) {
                    for (String value : complaints) {
                        if (value == null || value.trim().isEmpty()) {
                            continue;
                        }

                        if (value.contains("Associated symptoms")) {
                            continue;
                        }

                        try {
                            int colonIndex = value.indexOf(colon);
                            if (colonIndex > 0) {
                                String formattedValue = value.substring(0, colonIndex).trim();
                                formattedData.append(Node.big_bullet).append(" ").append(formattedValue).append("\n");
                            }
                        } catch (Exception e) {
                            Log.e("FormatComplaint", "Error formatting complaint data", e);
                        }
                    }
                }
            }
            if (formattedData.length() > 0) {
                result = formattedData.toString()
                        .replaceAll("<b>", "")
                        .replaceAll("</b>", "");

                if (result.endsWith("\n")) {
                    result = result.substring(0, result.lastIndexOf("\n"));
                }
            }
        }

        return result;
    }

    public String checkAndReturnVitalsValue(ObsDTO dto) {
        if (dto == null) {
            return "NA";
        } else if (dto.getValue() == null || dto.getValue().equalsIgnoreCase("0")) {
            return "NA";
        } else {
            return dto.getValue();
        }
    }

    public String checkAndReturnTemperatureValue(ObsDTO dto) {
        if (dto == null || dto.getValue() == null || dto.getValue().isEmpty()) {
            return "NA";
        } else {
            return convertCtoFNew(dto.getValue());
        }
    }

    private void updateLocalPrescriptionInformations(String visituuid) {
        List<LocalPrescriptionInfo> prescriptionDataList = new ArrayList<>();
        Gson gson = new Gson();
        SharedPreferences sharedPreference = IntelehealthApplication.getAppContext().getSharedPreferences(IntelehealthApplication.getAppContext().getString(R.string.prescription_share_key), Context.MODE_PRIVATE);
        String prescriptionListJson = sharedPreference.getString(AppConstants.PRESCRIPTION_DATA_LIST, "");
        if (!prescriptionListJson.isEmpty()) {
            Type type = new TypeToken<List<LocalPrescriptionInfo>>() {
            }.getType();
            prescriptionDataList = gson.fromJson(prescriptionListJson, type);
            for (LocalPrescriptionInfo lpi : prescriptionDataList) {
                if (lpi.getVisitUUID().equals(visituuid)) {
                    lpi.setShareStatus(true);
                }
            }
        }
        String prescriptionDataListJson = gson.toJson(prescriptionDataList);
        sharedPreference.edit().putString(AppConstants.PRESCRIPTION_DATA_LIST, prescriptionDataListJson).apply();
        sharedPreference.edit().putBoolean(AppConstants.SHARED_ANY_PRESCRIPTION, true).apply();
    }

    private void initUI(View view) {
        progress = view.findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.search_pat_hint_txt)).setText(getString(R.string.empty_message_for_patinet_search_visit_screen));
        LinearLayout addPatientTV = view.findViewById(R.id.add_new_patientTV);
        addPatientTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PrivacyPolicyActivity_New.class);
                intent.putExtra("intentType", "navigateFurther");
                intent.putExtra("add_patient", "add_patient");
                startActivity(intent);
                getActivity().finish();
            }
        });

        no_patient_found_block = view.findViewById(R.id.no_patient_found_block);
        main_block = view.findViewById(R.id.main_block);
        visit_received_card_header = view.findViewById(R.id.visit_received_card_header);
        searchview_received = view.findViewById(R.id.searchview_received);
        closeButton = searchview_received.findViewById(androidx.appcompat.R.id.search_close_btn);
        recent_nodata = view.findViewById(R.id.recent_nodata);
        older_nodata = view.findViewById(R.id.older_nodata);
        month_nodata = view.findViewById(R.id.month_nodata);
        recycler_recent = view.findViewById(R.id.recycler_recent);
        LinearLayoutManager reLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recycler_recent.setLayoutManager(reLayoutManager);
        recycler_older = view.findViewById(R.id.rv_older);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recycler_older.setLayoutManager(layoutManager);
        nestedscrollview = view.findViewById(R.id.rece_nestedscroll);

        nestedscrollview.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (v.getChildAt(v.getChildCount() - 1) != null) {
                // Scroll Down
                if (scrollY > oldScrollY) {
                    // update recent data as it will not go at very bottom of list.
                   /* if (mRecentList != null && mRecentList.size() == 0) {
                        isRecentFullyLoaded = true;
                    }*/
                    if (!isRecentFullyLoaded)
                        setRecentMoreDataIntoRecyclerView();

                    // Last Item Scroll Down.
                    if (scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) {
                        // update older data as it will not go at very bottom of list.
                        if (mOlderList != null && mOlderList.size() == 0) {
                            isolderFullyLoaded = true;
                            return;
                        }
                        if (!isolderFullyLoaded) {
                            if (mRecentPrescriptionModelList != null && mOlderPrescriptionModelList != null) {
                                if (mRecentPrescriptionModelList.size() > 0 || mOlderPrescriptionModelList.size() > 0) {

                                } else {
                                    Toast.makeText(getActivity(), getString(R.string.loading_more), Toast.LENGTH_SHORT).show();
                                    setOlderMoreDataIntoRecyclerView();
                                }
                            }
                        }
                    }
                }
            }
        });
        received_endvisit_no = view.findViewById(R.id.received_endvisit_no);
        filter_icon = view.findViewById(R.id.filter_icon);
        filter_menu = view.findViewById(R.id.filter_menu);
        allvisits_txt = view.findViewById(R.id.allvisits_txt);
        priority_visits_txt = view.findViewById(R.id.priority_visits_txt);
        filter_relative = view.findViewById(R.id.filter_relative);
        priority_cancel = view.findViewById(R.id.priority_cancel);
    }

    private void defaultData() {
        fetchRecentData();
        fetchOlderData();

        int totalCounts = totalCounts_recent + totalCounts_older;
        CustomLog.d("rece", "defaultData: received" + totalCounts);

//        thisMonths_Visits();
        if (mlistener != null)
            mlistener.receivedCount(totalCounts);
        progress.setVisibility(View.GONE);
    }

    private void fetchOlderData() {
        // Older vistis
        // pagination - start
        mOlderList = olderVisits(olderLimit, olderStart);
        older_adapter = new VisitAdapter(getActivity(), mOlderList, this);
        recycler_older.setNestedScrollingEnabled(false);
        recycler_older.setAdapter(older_adapter);

        olderStart = olderEnd;
        olderEnd += olderLimit;
        // pagination - end

        totalCounts_older = mOlderList.size();
        if (totalCounts_older == 0 || totalCounts_older < 0)
            older_nodata.setVisibility(View.VISIBLE);
        else
            older_nodata.setVisibility(View.GONE);
    }

    private void fetchRecentData() {
        mRecentList = recentVisits(recentLimit, recentStart);
        // pagination - start
        recent_adapter = new VisitAdapter(getActivity(), mRecentList, this);
        recycler_recent.setNestedScrollingEnabled(false);
        recycler_recent.setAdapter(recent_adapter);
        recentStart = recentEnd;
        recentEnd += recentLimit;
        // pagination - end

        totalCounts_recent = mRecentList.size();
        if (totalCounts_recent == 0 || totalCounts_recent < 0)
            recent_nodata.setVisibility(View.VISIBLE);
        else
            recent_nodata.setVisibility(View.GONE);

    }

    private void visitData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int total = new VisitsDAO().getVisitCountsByStatus(false);//getPendingPrescCount();
                Activity activity = getActivity();
                if (activity != null && isAdded()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String htmlvalue = "<b>" + total + " " + IntelehealthApplication.getInstance().getResources().getString(R.string.patients) + " " + "</b>" + IntelehealthApplication.getInstance().getResources().getString(R.string.awaiting_their_prescription);
                            received_endvisit_no.setText(Html.fromHtml(htmlvalue));
                        }
                    });
                }
            }
        }).start();


        // Filter - start
        filter_icon.setOnClickListener(v -> {
            if (filter_menu.getVisibility() == View.VISIBLE)
                filter_menu.setVisibility(View.GONE);
            else
                filter_menu.setVisibility(View.VISIBLE);
        });

        priority_visits_txt.setOnClickListener(v -> {
            filter_relative.setVisibility(View.VISIBLE);    // display filter that is set tag.
            filter_menu.setVisibility(View.GONE);   // hide filter menu

            showOnlyPriorityVisits();
        });

        priority_cancel.setOnClickListener(v -> {
            filter_relative.setVisibility(View.GONE);   // on clicking on cancel for Priority remove the filter tag as well as reset the data as default one.
            defaultData();
        });
        // Filter - end

        // Search - start
        searchview_received.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchOperation(query);
                return false;   // setting to false will close the keyboard when clicked on search btn.
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equalsIgnoreCase("")) {
                    searchview_received.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.blue_border_bg));
                } else {
                    searchview_received.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_common_input_bg));
                }
                return false;
            }
        });


        closeButton.setOnClickListener(v -> {
            no_patient_found_block.setVisibility(View.GONE);
            main_block.setVisibility(View.VISIBLE);
//            defaultData();
            resetData();
            searchview_received.setQuery("", false);

        });
        // Search - end
    }

    private void recent_older_visibility(List<PrescriptionModel> recent, List<PrescriptionModel> older) {
        if (recent.size() == 0 || recent.size() < 0)
            recent_nodata.setVisibility(View.VISIBLE);
        else
            recent_nodata.setVisibility(View.GONE);

        if (older.size() == 0 || older.size() < 0)
            older_nodata.setVisibility(View.VISIBLE);
        else
            older_nodata.setVisibility(View.GONE);
    }

    private void initLimits() {
        recentLimit = 15;
        olderLimit = 15;
        recentStart = 0;
        recentEnd = recentStart + recentLimit;
        olderStart = 0;
        olderEnd = olderStart + olderLimit;
    }


    private void resetData() {
        initLimits();
        mRecentPrescriptionModelList.clear();
        mOlderPrescriptionModelList.clear();

        mRecentList = recentVisits(recentLimit, recentStart);
        mOlderList = olderVisits(olderLimit, olderStart);

        recentStart = recentEnd;
        recentEnd += recentLimit;
        olderStart = olderEnd;
        olderEnd += olderLimit;

        //
        recent_older_visibility(mRecentList, mOlderList);
        CustomLog.d("TAG", "resetData: " + mRecentList.size() + ", " + mOlderList.size());

        recent_adapter = new VisitAdapter(getActivity(), mRecentList, this);
        recycler_recent.setNestedScrollingEnabled(false);
        recycler_recent.setAdapter(recent_adapter);

        older_adapter = new VisitAdapter(getActivity(), mOlderList, this);
        recycler_older.setNestedScrollingEnabled(false);
        recycler_older.setAdapter(older_adapter);
    }

    /**
     * This function will perform the search operation.
     *
     * @param query
     */
    private void searchOperation(String query) {
        CustomLog.v("Search", "Search Word: " + query);
        query = query.toLowerCase().trim();
        query = query.replaceAll(" {2}", " ");

//        List<PrescriptionModel> recent = new ArrayList<>();
//        List<PrescriptionModel> older = new ArrayList<>();

        String finalQuery = query;

        new Thread(new Runnable() {
            @Override
            public void run() {
                // To return all data adding a bigger digit LIMIT to avoid creating duplicate function.
                List<PrescriptionModel> allRecentList = recentVisits();
                List<PrescriptionModel> allOlderList = olderVisits();

                if (!finalQuery.isEmpty()) {
                    mRecentPrescriptionModelList.clear();
                    mOlderPrescriptionModelList.clear();

                    // recent - start
                    if (allRecentList.size() > 0) {
                        for (PrescriptionModel model : allRecentList) {
                            if (model.getMiddle_name() != null) {
                                String firstName = model.getFirst_name().toLowerCase();
                                String middleName = model.getMiddle_name().toLowerCase();
                                String lastName = model.getLast_name().toLowerCase();
                                String fullPartName = firstName + " " + lastName;
                                String fullName = firstName + " " + middleName + " " + lastName;

                                if (firstName.contains(finalQuery) || middleName.contains(finalQuery) ||
                                        lastName.contains(finalQuery) || fullPartName.contains(finalQuery) || fullName.contains(finalQuery)) {
                                    mRecentPrescriptionModelList.add(model);
                                } else {
                                    // dont add in list value.
                                }
                            } else {
                                String firstName = model.getFirst_name().toLowerCase();
                                String lastName = model.getLast_name().toLowerCase();
                                String fullName = firstName + " " + lastName;

                                if (firstName.contains(finalQuery) || lastName.contains(finalQuery) || fullName.contains(finalQuery)) {
                                    mRecentPrescriptionModelList.add(model);
                                } else {
                                    // dont add in list value.
                                }
                            }
                        }
                    }
                    // recent - end

                    // older - start
                    if (allOlderList.size() > 0) {
                        for (PrescriptionModel model : allOlderList) {
                            if (model.getMiddle_name() != null) {
                                String firstName = model.getFirst_name().toLowerCase();
                                String middleName = model.getMiddle_name().toLowerCase();
                                String lastName = model.getLast_name().toLowerCase();
                                String fullPartName = firstName + " " + lastName;
                                String fullName = firstName + " " + middleName + " " + lastName;

                                if (firstName.contains(finalQuery) || middleName.contains(finalQuery)
                                        || lastName.contains(finalQuery) || fullPartName.contains(finalQuery) || fullName.contains(finalQuery)) {
                                    mOlderPrescriptionModelList.add(model);
                                } else {
                                    // do nothing
                                }
                            } else {
                                String firstName = model.getFirst_name().toLowerCase();
                                String lastName = model.getLast_name().toLowerCase();
                                String fullName = firstName + " " + lastName;

                                if (firstName.contains(finalQuery) || lastName.contains(finalQuery) || fullName.contains(finalQuery)) {
                                    mOlderPrescriptionModelList.add(model);
                                } else {
                                    // do nothing
                                }
                            }
                        }
                    }
                    // older - end
                    Activity activity = getActivity();
                    if (isAdded() && activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recent_adapter = new VisitAdapter(getActivity(), mRecentPrescriptionModelList, VisitReceivedFragment.this);
                                recycler_recent.setNestedScrollingEnabled(false);
                                recycler_recent.setAdapter(recent_adapter);

                                older_adapter = new VisitAdapter(getActivity(), mOlderPrescriptionModelList, VisitReceivedFragment.this);
                                recycler_older.setNestedScrollingEnabled(false);
                                recycler_older.setAdapter(older_adapter);

                                /**
                                 * Checking here the query that is entered and it is not empty so check the size of all of these
                                 * arraylists; if there size is 0 than show the no patient found view.
                                 */
                                int allCount = mRecentPrescriptionModelList.size() + mOlderPrescriptionModelList.size();
                                allCountVisibility(allCount);
                                recent_older_visibility(mRecentPrescriptionModelList, mOlderPrescriptionModelList);
                            }
                        });
                    }

                }
            }
        }).start();
    }

    private void allCountVisibility(int allCount) {
        if (allCount == 0 || allCount < 0) {
            no_patient_found_block.setVisibility(View.VISIBLE);
            main_block.setVisibility(View.GONE);
        } else {
            no_patient_found_block.setVisibility(View.GONE);
            main_block.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This function will display all the visit of Emergency who have been recived the presc.
     */
    private void showOnlyPriorityVisits() {
        // todays - start
        List<PrescriptionModel> prio_todays = new ArrayList<>();
        for (int i = 0; i < mRecentList.size(); i++) {
            if (mRecentList.get(i).isEmergency())
                prio_todays.add(mRecentList.get(i));
        }
        totalCounts_recent = prio_todays.size();
        if (totalCounts_recent == 0 || totalCounts_recent < 0)
            recent_nodata.setVisibility(View.VISIBLE);
        else
            recent_nodata.setVisibility(View.GONE);
        recent_adapter = new VisitAdapter(getActivity(), prio_todays, this);
        recycler_recent.setNestedScrollingEnabled(false);
        recycler_recent.setAdapter(recent_adapter);
        // todays - end

        // weeks - start
        List<PrescriptionModel> prio_weeks = new ArrayList<>();
        for (int i = 0; i < mOlderList.size(); i++) {
            if (mOlderList.get(i).isEmergency())
                prio_weeks.add(mOlderList.get(i));
        }
        totalCounts_older = prio_weeks.size();
        if (totalCounts_older == 0 || totalCounts_older < 0)
            older_nodata.setVisibility(View.VISIBLE);
        else
            older_nodata.setVisibility(View.GONE);
        older_adapter = new VisitAdapter(getActivity(), prio_weeks, this);
        recycler_older.setNestedScrollingEnabled(false);
        recycler_older.setAdapter(older_adapter);
        // weeks - end

        // months - start
        List<PrescriptionModel> prio_months = new ArrayList<>();
        for (int i = 0; i < mMonthsList.size(); i++) {
            if (mMonthsList.get(i).isEmergency())
                prio_months.add(mMonthsList.get(i));
        }
        totalCounts_month = prio_months.size();
        if (totalCounts_month == 0 || totalCounts_month < 0)
            month_nodata.setVisibility(View.VISIBLE);
        else
            month_nodata.setVisibility(View.GONE);
        //months_adapter = new VisitAdapter(getActivity(), prio_months);
        // recycler_month.setNestedScrollingEnabled(false);
        //recycler_month.setAdapter(months_adapter);
        // months - end
    }

    // This method will be accessed every time the person scrolls the recyclerView further.
    private void setRecentMoreDataIntoRecyclerView() {
        if (mRecentPrescriptionModelList.size() > 0 || mOlderPrescriptionModelList.size() > 0) {    // on scroll, new data loads issue fix.

        } else {
            if (mRecentList != null && mRecentList.size() == 0) {
                isRecentFullyLoaded = true;
                return;
            }

            //  recentList = recentVisits(recentLimit, recentStart);
            List<PrescriptionModel> tempList = recentVisits(recentLimit, recentStart);  // for n iteration limit be fixed == 15 and start - offset will keep skipping each records.
            if (tempList.size() > 0) {
                mRecentList.addAll(tempList);
                CustomLog.d("TAG", "setRecentMoreDataIntoRecyclerView: " + mRecentList.size());
                recent_adapter.list.addAll(tempList);
                recent_adapter.notifyDataSetChanged();
                recentStart = recentEnd;
                recentEnd += recentLimit;
            }
        }
    }

    private void setOlderMoreDataIntoRecyclerView() {
        if (mRecentPrescriptionModelList.size() > 0 || mOlderPrescriptionModelList.size() > 0) {

        } else {
            if (mOlderList != null && mOlderList.size() == 0) {
                isolderFullyLoaded = true;
                return;
            }

            //  olderList = olderVisits(olderLimit, olderStart);
            List<PrescriptionModel> tempList = olderVisits(olderLimit, olderStart); // for n iteration limit be fixed == 15 and start - offset will keep skipping each records.
            if (tempList.size() > 0) {
                mOlderList.addAll(tempList);
                CustomLog.d("TAG", "setOlderMoreDataIntoRecyclerView: " + mOlderList.size());
                older_adapter.list.addAll(tempList);
                older_adapter.notifyDataSetChanged();
                olderStart = olderEnd;
                olderEnd += olderLimit;
            }
        }
    }

    private List<PrescriptionModel> recentVisits(int limit, int offset) {
        List<PrescriptionModel> recentList = new ArrayList<>();
        db.beginTransaction();

        // ie. visit is active and presc is given.
        Cursor cursor = db.rawQuery("select p.patient_photo, p.first_name, p.middle_name, p.last_name, p.openmrs_id, p.date_of_birth, p.phone_number, p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid," +
                        " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync " +
                        "from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o " +
                        "where p.uuid = v.patientuuid " +
                        "and v.uuid = e.visituuid " +
                        "and euid = o.encounteruuid " +
                        //" and v.enddate is null " +
                        "and e.encounter_type_uuid = ? " +
                        "and (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') " +
                        "AND o.voided = 0 " +//and" + " o.conceptuuid = ? and " +
                        //-- Only latest visit of the patient
                        "AND v.uuid = (" +
                        "        SELECT v2.uuid" +
                        "        FROM tbl_visit v2" +
                        "        WHERE v2.patientuuid = v.patientuuid" +
                        "        ORDER BY v2.startdate DESC" +
                        "        LIMIT 1" +
                        "  )" +
                        "and v.startdate > DATETIME('now', '-4 day') " +

//                        "and (select count(*) from tbl_visit_attribute as attr " + //added sub query to fetch doctor visits only
//                        "where  attr.visit_uuid = v.uuid " +
//                        "and attr.visit_attribute_type_uuid = ?) <=0 " +
                        "and  ( " +
                        "    SELECT count(*) FROM tbl_visit_attribute dva " +
                        "    WHERE dva.visit_uuid = v.uuid and dva.visit_attribute_type_uuid = ?" +
                        "    AND dva.value != ? " +
                        ")  >0 " +
                        " group by p.openmrs_id ORDER BY MAX(v.startdate) DESC limit ? offset ?",

                new String[]{ENCOUNTER_VISIT_COMPLETE, /*IS_NCD_VISIT_ATTRIBUTE*/UuidDictionary.SPECIALITY, AppConstants.DOCTOR_NOT_NEEDED, String.valueOf(limit), String.valueOf(offset)});  // 537bb20d-d09d-4f88-930b-cc45c7d662df -> Diagnosis conceptID.

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                PrescriptionModel model = new PrescriptionModel();
                // emergency - start
                String visitID = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                boolean isCompletedExitedSurvey = false;
                boolean isPrescriptionReceived = false;
                try {
                    isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visitID);
                    isPrescriptionReceived = new EncounterDAO().isPrescriptionReceived(visitID);
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                if (!isCompletedExitedSurvey && isPrescriptionReceived) {
                    String emergencyUuid = "";
                    EncounterDAO encounterDAO = new EncounterDAO();
                    try {
                        emergencyUuid = encounterDAO.getEmergencyEncounters(visitID, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        emergencyUuid = "";
                    }

                    if (!emergencyUuid.equalsIgnoreCase("")) // ie. visit is emergency visit.
                        model.setEmergency(true);
                    else
                        model.setEmergency(false);
                    // emergency - end

                    model.setHasPrescription(true);
                    model.setEncounterUuid(cursor.getString(cursor.getColumnIndexOrThrow("euid")));
                    model.setVisitUuid(visitID);
                    model.setSync(cursor.getString(cursor.getColumnIndexOrThrow("osync")));
                    model.setPatientUuid(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")));
                    model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")));
                    model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                    model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                    model.setMiddle_name(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                    model.setPhone_number(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
                    model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                    model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                    model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                    model.setObsservermodifieddate(cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")));
                    recentList.add(model);

                }
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        if (recentList.isEmpty()) {
            isRecentFullyLoaded = true;
        }
        return recentList;
    }

    private List<PrescriptionModel> recentVisits() {
        List<PrescriptionModel> recentList = new ArrayList<>();
        db.beginTransaction();

        // ie. visit is active and presc is given.
        Cursor cursor = db.rawQuery("select p.patient_photo, p.first_name, p.middle_name, p.last_name, p.openmrs_id, p.date_of_birth, p.phone_number, p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid," +
                        " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync " +
                        "from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o " +
                        "where p.uuid = v.patientuuid " +
                        "and v.uuid = e.visituuid " +
                        "and euid = o.encounteruuid " +
                        //" and v.enddate is null " +
                        "and e.encounter_type_uuid = ? " +
                        "and (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') " +
                        "AND o.voided = 0 " +//and" + " o.conceptuuid = ? and "+
                        //-- Only latest visit of the patient
                        "AND v.uuid = (" +
                        "        SELECT v2.uuid" +
                        "        FROM tbl_visit v2" +
                        "        WHERE v2.patientuuid = v.patientuuid" +
                        "        ORDER BY v2.startdate DESC" +
                        "        LIMIT 1" +
                        "  )" +
                        "and v.startdate > DATETIME('now', '-4 day') " +
//                        "and (select count(*) from tbl_visit_attribute as attr " + //added sub query to fetch doctor visits only
//                        "where  attr.visit_uuid = v.uuid " +
//                        "and attr.visit_attribute_type_uuid = ?) <=0 " +
                        "and  ( " +
                        "    SELECT count(*) FROM tbl_visit_attribute dva " +
                        "    WHERE dva.visit_uuid = v.uuid and dva.visit_attribute_type_uuid = ?" +
                        "    AND dva.value != ? " +
                        ")  >0 " +
                        " group by p.openmrs_id ORDER BY v.startdate DESC",

                new String[]{ENCOUNTER_VISIT_COMPLETE, /*IS_NCD_VISIT_ATTRIBUTE*/UuidDictionary.SPECIALITY, AppConstants.DOCTOR_NOT_NEEDED});  // 537bb20d-d09d-4f88-930b-cc45c7d662df -> Diagnosis conceptID.

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                PrescriptionModel model = new PrescriptionModel();
                // emergency - start
                String visitID = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                boolean isCompletedExitedSurvey = false;
                boolean isPrescriptionReceived = false;
                try {
                    isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visitID);
                    isPrescriptionReceived = new EncounterDAO().isPrescriptionReceived(visitID);
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                if (!isCompletedExitedSurvey && isPrescriptionReceived) {
                    String emergencyUuid = "";
                    EncounterDAO encounterDAO = new EncounterDAO();
                    try {
                        emergencyUuid = encounterDAO.getEmergencyEncounters(visitID, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        emergencyUuid = "";
                    }

                    if (!emergencyUuid.equalsIgnoreCase("")) // ie. visit is emergency visit.
                        model.setEmergency(true);
                    else
                        model.setEmergency(false);
                    // emergency - end

                    model.setHasPrescription(true);
                    model.setEncounterUuid(cursor.getString(cursor.getColumnIndexOrThrow("euid")));
                    model.setVisitUuid(visitID);
                    model.setSync(cursor.getString(cursor.getColumnIndexOrThrow("osync")));
                    model.setPatientUuid(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")));
                    model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")));
                    model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                    model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                    model.setMiddle_name(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                    model.setPhone_number(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
                    model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                    model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                    model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                    model.setObsservermodifieddate(cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")));
                    recentList.add(model);

                }
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        return recentList;
    }


    private List<PrescriptionModel> olderVisits(int limit, int offset) {
        List<PrescriptionModel> olderList = new ArrayList<>();
        db.beginTransaction();

        // ie. visit is active and presc is given.
        Cursor cursor = db.rawQuery("select p.patient_photo, p.first_name, p.middle_name, p.last_name, p.openmrs_id, p.date_of_birth, p.phone_number, p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid," +
                        " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync " +
                        "from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o " +
                        "where p.uuid = v.patientuuid " +
                        "and v.uuid = e.visituuid " +
                        "and euid = o.encounteruuid " +
                        //" and v.enddate is null " +
                        "and e.encounter_type_uuid = ? " +
                        "and (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') " +
                        "AND o.voided = 0 " +//and" + " o.conceptuuid = ?  "+
                        //-- Only latest visit of the patient
                        "AND v.uuid = (" +
                        "        SELECT v2.uuid" +
                        "        FROM tbl_visit v2" +
                        "        WHERE v2.patientuuid = v.patientuuid" +
                        "        ORDER BY v2.startdate DESC" +
                        "        LIMIT 1" +
                        "  )" +
                        "and v.startdate <= DATE('now', '-4 day') " +
//                        "and (select count(*) from tbl_visit_attribute as attr " + //added sub query to fetch doctor visits only
//                        "where  attr.visit_uuid = v.uuid " +
//                        "and attr.visit_attribute_type_uuid = ?) <=0 " +
                        "and  ( " +
                        "    SELECT count(*) FROM tbl_visit_attribute dva " +
                        "    WHERE dva.visit_uuid = v.uuid and dva.visit_attribute_type_uuid = ?" +
                        "    AND dva.value != ? " +
                        ")  >0 " +
                        "group by p.openmrs_id ORDER BY MAX(v.startdate) DESC limit ? offset ?",

                new String[]{ENCOUNTER_VISIT_COMPLETE, /*IS_NCD_VISIT_ATTRIBUTE*/UuidDictionary.SPECIALITY, AppConstants.DOCTOR_NOT_NEEDED, String.valueOf(limit), String.valueOf(offset)});  // not needed as diagnosis is not mandatoy. --> 537bb20d-d09d-4f88-930b-cc45c7d662df -> Diagnosis conceptID.

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                PrescriptionModel model = new PrescriptionModel();

                // emergency - start
                String visitID = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                boolean isCompletedExitedSurvey = false;
                boolean isPrescriptionReceived = false;
                try {
                    isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visitID);
                    isPrescriptionReceived = new EncounterDAO().isPrescriptionReceived(visitID);
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                //if(!isPrescriptionReceived) continue;
                if (!isCompletedExitedSurvey && isPrescriptionReceived) {
                    String emergencyUuid = "";
                    EncounterDAO encounterDAO = new EncounterDAO();
                    try {
                        emergencyUuid = encounterDAO.getEmergencyEncounters(visitID, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        emergencyUuid = "";
                    }

                    if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) // ie. visit is emergency visit.
                        model.setEmergency(true);
                    else
                        model.setEmergency(false);
                    // emergency - end

                    model.setHasPrescription(true);
                    model.setEncounterUuid(cursor.getString(cursor.getColumnIndexOrThrow("euid")));
                    model.setVisitUuid(visitID);
                    model.setSync(cursor.getString(cursor.getColumnIndexOrThrow("osync")));
                    model.setPatientUuid(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")));
                    model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")));
                    model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                    model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                    model.setMiddle_name(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                    model.setPhone_number(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
                    model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                    model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                    model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                    model.setObsservermodifieddate(cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")));
                    olderList.add(model);
                }
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        return olderList;
    }

    private List<PrescriptionModel> olderVisits() {
        List<PrescriptionModel> olderList = new ArrayList<>();
        db.beginTransaction();

        // ie. visit is active and presc is given.
        Cursor cursor = db.rawQuery("select p.patient_photo, p.first_name, p.middle_name, p.last_name, p.openmrs_id, p.date_of_birth, p.phone_number, p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid," +
                        " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync " +
                        "from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o " +
                        "where p.uuid = v.patientuuid " +
                        "and v.uuid = e.visituuid " +
                        "and euid = o.encounteruuid " +
                        //" and v.enddate is null " +
                        "and e.encounter_type_uuid = ? " +
                        "and (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') " +
                        "AND o.voided = 0 " +//and" + " o.conceptuuid = ? and "+
                        //-- Only latest visit of the patient
                        "AND v.uuid = (" +
                        "        SELECT v2.uuid" +
                        "        FROM tbl_visit v2" +
                        "        WHERE v2.patientuuid = v.patientuuid" +
                        "        ORDER BY v2.startdate DESC" +
                        "        LIMIT 1" +
                        "  )" +
                        "and v.startdate <= DATE('now', '-4 day') " +
//                        "and (select count(*) from tbl_visit_attribute as attr " + //added sub query to fetch doctor visits only
//                        "where  attr.visit_uuid = v.uuid " +
//                        "and attr.visit_attribute_type_uuid = ?) <=0 " +
                        "and  ( " +
                        "    SELECT count(*) FROM tbl_visit_attribute dva " +
                        "    WHERE dva.visit_uuid = v.uuid and dva.visit_attribute_type_uuid = ?" +
                        "    AND dva.value != ? " +
                        ")  >0 " +
                        "group by p.openmrs_id ORDER BY v.startdate DESC",

                new String[]{ENCOUNTER_VISIT_COMPLETE, /*IS_NCD_VISIT_ATTRIBUTE*/UuidDictionary.SPECIALITY, AppConstants.DOCTOR_NOT_NEEDED});  // not needed as diagnosis is not mandatoy. --> 537bb20d-d09d-4f88-930b-cc45c7d662df -> Diagnosis conceptID.

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                PrescriptionModel model = new PrescriptionModel();

                // emergency - start
                String visitID = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                boolean isCompletedExitedSurvey = false;
                boolean isPrescriptionReceived = false;
                try {
                    isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visitID);
                    isPrescriptionReceived = new EncounterDAO().isPrescriptionReceived(visitID);
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                if (!isCompletedExitedSurvey && isPrescriptionReceived) {
                    String emergencyUuid = "";
                    EncounterDAO encounterDAO = new EncounterDAO();
                    try {
                        emergencyUuid = encounterDAO.getEmergencyEncounters(visitID, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        emergencyUuid = "";
                    }

                    if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) // ie. visit is emergency visit.
                        model.setEmergency(true);
                    else
                        model.setEmergency(false);
                    // emergency - end

                    model.setHasPrescription(true);
                    model.setEncounterUuid(cursor.getString(cursor.getColumnIndexOrThrow("euid")));
                    model.setVisitUuid(visitID);
                    model.setSync(cursor.getString(cursor.getColumnIndexOrThrow("osync")));
                    model.setPatientUuid(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")));
                    model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")));
                    model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                    model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                    model.setMiddle_name(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                    model.setPhone_number(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
                    model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                    model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                    model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                    model.setObsservermodifieddate(cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")));
                    olderList.add(model);
                }
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        return olderList;
    }

    private void thisMonths_Visits() {
        // new
        mMonthsList = new ArrayList<>();
        db.beginTransaction();

        Cursor cursor = db.rawQuery("select p.patient_photo, p.first_name, p.last_name, p.openmrs_id, p.date_of_birth, p.phone_number, p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid," +
                        " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o where" +
                        " p.uuid = v.patientuuid and v.uuid = e.visituuid and euid = o.encounteruuid and" +
                        "  e.encounter_type_uuid = ? and" +
                        " (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') AND o.voided = 0 and" +
                        " o.conceptuuid = ? and" +
                        " STRFTIME('%Y',date(substr(o.obsservermodifieddate, 1, 4)||'-'||substr(o.obsservermodifieddate, 6, 2)||'-'||substr(o.obsservermodifieddate, 9,2))) = STRFTIME('%Y',DATE('now')) AND " +
                        " STRFTIME('%m',date(substr(o.obsservermodifieddate, 1, 4)||'-'||substr(o.obsservermodifieddate, 6, 2)||'-'||substr(o.obsservermodifieddate, 9,2))) = STRFTIME('%m',DATE('now'))" +
                        " group by p.openmrs_id"
                , new String[]{ENCOUNTER_VISIT_NOTE, "537bb20d-d09d-4f88-930b-cc45c7d662df"});  // 537bb20d-d09d-4f88-930b-cc45c7d662df -> Diagnosis conceptID.

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                PrescriptionModel model = new PrescriptionModel();
                // emergency - start
                String visitID = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                String emergencyUuid = "";
                EncounterDAO encounterDAO = new EncounterDAO();
                try {
                    emergencyUuid = encounterDAO.getEmergencyEncounters(visitID, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    emergencyUuid = "";
                }

                if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) // ie. visit is emergency visit.
                    model.setEmergency(true);
                else
                    model.setEmergency(false);
                // emergency - end

                model.setHasPrescription(true);
                model.setEncounterUuid(cursor.getString(cursor.getColumnIndexOrThrow("euid")));
                model.setVisitUuid(visitID);
                model.setSync(cursor.getString(cursor.getColumnIndexOrThrow("osync")));
                model.setPatientUuid(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")));
                model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")));
                model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                model.setPhone_number(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
                model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                model.setObsservermodifieddate(cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")));
                mMonthsList.add(model);

            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        totalCounts_month = mMonthsList.size();
        if (totalCounts_month == 0 || totalCounts_month < 0)
            month_nodata.setVisibility(View.VISIBLE);
        else
            month_nodata.setVisibility(View.GONE);

        //months_adapter = new VisitAdapter(getActivity(), monthsList);
        //recycler_month.setNestedScrollingEnabled(false);
        //recycler_month.setAdapter(months_adapter);
        progress.setVisibility(View.GONE);

        //  thisWeeks_Visits();
        //new


      /*  ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                monthsList = new ArrayList<>();
                //Background work here
                db.beginTransaction();
                Cursor cursor = db.rawQuery("SELECT * FROM tbl_encounter WHERE (sync = 1 OR sync = 'TRUE' OR sync = 'true') AND " +
                        "voided = 0 AND " +
                        "STRFTIME('%Y',date(substr(modified_date, 1, 4)||'-'||substr(modified_date, 6, 2)||'-'||substr(modified_date, 9,2))) = STRFTIME('%Y',DATE('now')) AND " +
                        "STRFTIME('%m',date(substr(modified_date, 1, 4)||'-'||substr(modified_date, 6, 2)||'-'||substr(modified_date, 9,2))) = STRFTIME('%m',DATE('now')) AND " +
                        "encounter_type_uuid = ?", new String[]{ENCOUNTER_VISIT_NOTE});

                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    do {
                        PrescriptionModel model = new PrescriptionModel();

                        // emergency - start
                        // TODO: 8-11-2022 -> In app currently in sync even when the visit is priority still in sync of app the emergency enc
                        //  is not getting added in local db ie. from server end emergency encounter is not coming to us in pull.
                        String visitID = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                        String emergencyUuid = "";
                        EncounterDAO encounterDAO = new EncounterDAO();
                        try {
                            emergencyUuid = encounterDAO.getEmergencyEncounters(visitID, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                            emergencyUuid = "";
                        }

                        if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) // ie. visit is emergency visit.
                            model.setEmergency(true);
                        else
                            model.setEmergency(false);
                        // emergency - end

                        model.setHasPrescription(true);
                        model.setEncounterUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                        model.setVisitUuid(visitID);
                        model.setSync(cursor.getString(cursor.getColumnIndexOrThrow("sync")));

                        // fetching patientuuid from visit table.
                        Cursor c = db.rawQuery("SELECT * FROM tbl_visit WHERE uuid = ?", new String[]{model.getVisitUuid()});
                        if (c.getCount() > 0 && c.moveToFirst()) {
                            do {
                                model.setPatientUuid(c.getString(c.getColumnIndexOrThrow("patientuuid")));
                                model.setVisit_start_date(c.getString(c.getColumnIndexOrThrow("startdate")));

                                // fetching patient values from Patient table.
                                Cursor cursor2 = db.rawQuery("SELECT * FROM tbl_patient WHERE uuid = ?", new String[]{model.getPatientUuid()});
                                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                                    do {
                                        model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                                        model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                                        model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                                        model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                                        model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                                        model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                                        monthsList.add(model);

                                    }
                                    while (cursor.moveToNext());
                                }
                                cursor.close();
                                // end

                            }
                            while (c.moveToNext());
                        }
                        c.close();
                        //end

                    }
                    while (cursor.moveToNext());
                }
                cursor.close();
                db.setTransactionSuccessful();
                db.endTransaction();

                totalCounts_month = monthsList.size();
                // ednd

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //UI Thread work here
                        if(totalCounts_month == 0 || totalCounts_month < 0)
                            month_nodata.setVisibility(View.VISIBLE);
                        else
                            month_nodata.setVisibility(View.GONE);

                        months_adapter = new VisitAdapter(getActivity(), monthsList);
                        recycler_month.setNestedScrollingEnabled(false);
                        recycler_month.setAdapter(months_adapter);
                        progress.setVisibility(View.GONE);
                    }
                });
            }
        });
*/
    }

}
