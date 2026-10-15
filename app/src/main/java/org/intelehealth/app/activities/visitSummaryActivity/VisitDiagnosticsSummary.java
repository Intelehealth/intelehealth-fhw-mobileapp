package org.intelehealth.app.activities.visitSummaryActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.visit.model.CommonVisitData;
import org.intelehealth.app.databinding.ActivityVisitSummaryNewBinding;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.config.presenter.fields.data.DiagnosticsRepository;
import org.intelehealth.config.presenter.fields.data.PatientVitalRepository;
import org.intelehealth.config.presenter.fields.factory.DiagnosticsViewModelFactory;
import org.intelehealth.config.presenter.fields.factory.PatientVitalViewModelFactory;
import org.intelehealth.config.presenter.fields.viewmodel.DiagnosticsViewModel;
import org.intelehealth.config.room.ConfigDatabase;
import org.intelehealth.config.room.entity.Diagnostics;
import org.intelehealth.config.utility.PatientDiagnosticsConfigKeys;

import java.util.ArrayList;
import java.util.List;

public class VisitDiagnosticsSummary {
    ActivityVisitSummaryNewBinding mBinding;
    Context context;
    private static final String TAG = "VisitDiagnosticsSummary";
    List<Diagnostics> diagnosticsList = new ArrayList<>();
    VisitSummaryActivity_New owner;
    String encounterDiagnostics;
    ObsDTO randomSugar = new ObsDTO();
    ObsDTO fastingGlucose = new ObsDTO();
    ObsDTO nonFastingGlucose = new ObsDTO();
    ObsDTO postPrandialGlucose = new ObsDTO();
    ObsDTO hemoglobin = new ObsDTO();
    ObsDTO uricAcid = new ObsDTO();
    ObsDTO totalCholestrol = new ObsDTO();
    CommonVisitData mCommonVisitData;

    public VisitDiagnosticsSummary(ActivityVisitSummaryNewBinding mBinding, Context context,
                                   List<Diagnostics> diagnosticsList,
                                   VisitSummaryActivity_New owner, String encounterDiagnostics,
                                   CommonVisitData commonVisitData) {
        this.mBinding = mBinding;
        this.context = context;
        this.diagnosticsList = diagnosticsList;
        this.owner = owner;
        this.encounterDiagnostics = encounterDiagnostics;
        this.mCommonVisitData = commonVisitData;
    }

    public void initViews() {
        queryData();
        setupDiagnosticsConfig();
    }

    private void setupDiagnosticsConfig() {
        DiagnosticsRepository repository = new DiagnosticsRepository(ConfigDatabase.getInstance(context).patientDiagnosticsDao());
        DiagnosticsViewModelFactory factory = new DiagnosticsViewModelFactory(repository);
        DiagnosticsViewModel diagnosticsViewModel = new ViewModelProvider(owner, factory).get(DiagnosticsViewModel.class);
        diagnosticsViewModel.getAllEnabledLiveFields()
                .observe(owner, it -> {
                            diagnosticsList = it;
                            CustomLog.v(TAG, new Gson().toJson(diagnosticsList));
                            updateUIOfDiagnostics();
                        }
                );
    }

    public void queryData() {
        Log.d(TAG, "kkkqueryData: encounterDiagnostics  : " + encounterDiagnostics);
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        //vitals display code
        String visitSelection = "encounteruuid = ? AND voided!='1'";
        String[] visitArgs = {encounterDiagnostics};
        String[] columns = {"value", " conceptuuid"};

        if (encounterDiagnostics != null) {
            try {
                Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
                if (visitCursor != null && visitCursor.moveToFirst()) {
                    do {
                        String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                        String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                        parseData(dbConceptID, dbValue);
                    } while (visitCursor.moveToNext());
                }
                if (visitCursor != null) {
                    visitCursor.close();
                }
            } catch (SQLException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        // Render once after the query, including values with no (non-voided) obs, so a diagnostic
        // cleared in edit mode shows "No information" instead of keeping its old text.
        setDiagnosticDataToViews();
    }

    private void parseData(String concept_id, String value) {
        Log.d(TAG, "kkkparseData: concept_id : " + concept_id);
        Log.d(TAG, "kkkparseData: value : " + value);

        switch (concept_id) {
            case UuidDictionary.BLOOD_GLUCOSE_RANDOM: {
                randomSugar.setValue(value);
                break;
            }
            case UuidDictionary.BLOOD_GLUCOSE_FASTING: {
                fastingGlucose.setValue(value);
                break;
            }
            case UuidDictionary.BLOOD_GLUCOSE: {
                nonFastingGlucose.setValue(value);
                break;
            }
            case UuidDictionary.BLOOD_GLUCOSE_POST_PRANDIAL: {
                postPrandialGlucose.setValue(value);
                break;
            }
            case UuidDictionary.HEMOGLOBIN: {
                hemoglobin.setValue(value);
                break;
            }
            case UuidDictionary.URIC_ACID: {
                uricAcid.setValue(value);
                break;
            }
            case UuidDictionary.TOTAL_CHOLESTEROL: {
                totalCholestrol.setValue(value);
                break;
            }

            default:
                CustomLog.i(TAG, "parseData: " + value);
                break;
        }

    }

    /** Shows the value, or "No information" when it is missing, blank or "0". */
    private void showDiagnosticValue(TextView view, ObsDTO obs) {
        String value = obs.getValue();
        if (value == null || value.trim().isEmpty() || value.trim().equals("0")) {
            view.setText(context.getResources().getString(R.string.no_information));
        } else {
            view.setText(value);
        }
    }

    private void setDiagnosticDataToViews() {
        showDiagnosticValue(mBinding.layoutVisitSummarySections.textViewGlucoseRandomValue, randomSugar);
        CustomLog.d(TAG, "onCreate: " + randomSugar.getValue());
        showDiagnosticValue(mBinding.layoutVisitSummarySections.textViewGlucoseFastingValue, fastingGlucose);
        /*if (nonFastingGlucose.getValue() != null) {
            if (nonFastingGlucose.getValue().trim().isEmpty() || nonFastingGlucose.getValue().trim().equals("0")) {
                mBinding.layoutVisitSummarySections.textViewGlucoseNonFasting.setText(context.getResources().getString(R.string.no_information));
            } else {
                mBinding.layoutVisitSummarySections.textViewGlucoseNonFasting.setText(nonFastingGlucose.getValue());
            }
        }*/
        showDiagnosticValue(mBinding.layoutVisitSummarySections.textViewPostPrandialValue, postPrandialGlucose);
        showDiagnosticValue(mBinding.layoutVisitSummarySections.textViewHemoglobinValue, hemoglobin);
        showDiagnosticValue(mBinding.layoutVisitSummarySections.textViewUricAcidValue, uricAcid);
        showDiagnosticValue(mBinding.layoutVisitSummarySections.textViewTotalCholestrolValue, totalCholestrol);

    }

    private void updateUIOfDiagnostics() {
        mBinding.layoutVisitSummarySections.llGlucoseRandomContainer.setVisibility(View.GONE);
        mBinding.layoutVisitSummarySections.llGlucoseFastingContainer.setVisibility(View.GONE);
        //mBinding.layoutVisitSummarySections.llGlucoseNonFasting.setVisibility(View.GONE);
        mBinding.layoutVisitSummarySections.llPostPrandialContainer.setVisibility(View.GONE);
        mBinding.layoutVisitSummarySections.llHemoglobinContainer.setVisibility(View.GONE);
        mBinding.layoutVisitSummarySections.llUricAcidContainer.setVisibility(View.GONE);
        mBinding.layoutVisitSummarySections.llTotalCholestrolContainer.setVisibility(View.GONE);

        for (Diagnostics diagnostics : diagnosticsList) {
            CustomLog.v(TAG, diagnostics.getName() + "\t" + diagnostics.getDiagnosticsKey());

           /* if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.BLOOD_GLUCOSE)) {
                mBinding.layoutVisitSummarySections.llGlucoseNonFasting.setVisibility(View.VISIBLE);
            } else */
            if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.RANDOM_BLOOD_SUGAR)) {
                mBinding.layoutVisitSummarySections.llGlucoseRandomContainer.setVisibility(View.VISIBLE);
            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.FASTING_BLOOD_SUGAR)) {
                mBinding.layoutVisitSummarySections.llGlucoseFastingContainer.setVisibility(View.VISIBLE);
            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.POST_PRANDIAL_BLOOD_SUGAR)) {
                mBinding.layoutVisitSummarySections.llPostPrandialContainer.setVisibility(View.VISIBLE);
            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.HEAMOGLOBIN)) {
                mBinding.layoutVisitSummarySections.llHemoglobinContainer.setVisibility(View.VISIBLE);
            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.URIC_ACID)) {
                mBinding.layoutVisitSummarySections.llUricAcidContainer.setVisibility(View.VISIBLE);
            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.TOTAL_CHOLESTEROL)) {
                mBinding.layoutVisitSummarySections.llTotalCholestrolContainer.setVisibility(View.VISIBLE);
            }
        }
    }


}
