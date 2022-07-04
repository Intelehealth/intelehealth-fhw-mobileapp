package org.intelehealth.app.activities.householdSurvey;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.activities.searchPatientActivity.SearchPatientAdapter;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.models.pushRequestApiCall.Attribute;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DraftSurveyActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SearchPatientAdapter draftSurveyAdapter;
    private Context context = DraftSurveyActivity.this;
    private List<String> patientUUIDList;
    private List<PatientDTO> patientDTOList = new ArrayList<>();
    private PatientsDAO patientsDAO;
    private SessionManager sessionManager = null;
    SQLiteDatabase db;
    private final int i = 5, LIMIT = 10, OFFSET = 0;
    private ProgressDialog mSyncProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft_survey);
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.draft_survey_title));

        patientsDAO = new PatientsDAO();
//        try {
//            patientUUIDList = fetchUniquePatientUuidFromAttributes(); // Eg: 53
//            for (int i = 0; i < patientUUIDList.size(); i++) {
//                fetchValueAttrFromPatAttrTbl(patientUUIDList.get(i)); // Eg. 40 this patientuuids should be less here
//            }
//        } catch (DAOException e) {
//            e.printStackTrace();
//        }
//
        recyclerView = findViewById(R.id.recycler_draftSurvey);
//        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
//        draftSurveyAdapter = new SearchPatientAdapter(patientDTOList, context);
//        recyclerView.setAdapter(draftSurveyAdapter);


        Executors.newSingleThreadExecutor().execute(() -> {
            // start progress dialog on main thread
            runOnUiThread(() -> {
                mSyncProgressDialog = new ProgressDialog(DraftSurveyActivity.this, R.style.AlertDialogStyle);
                mSyncProgressDialog.setTitle(R.string.loading);
                mSyncProgressDialog.setCancelable(false);
                mSyncProgressDialog.setProgress(i);
                mSyncProgressDialog.show();
            });

            // todo: background tasks
            db.beginTransaction();
            try {
                patientUUIDList = fetchUniquePatientUuidFromAttributes(); // Eg: 53
                for (int i = 0; i < patientUUIDList.size(); i++) {
                    if (isFinishing())
                        break;
                    fetchValueAttrFromPatAttrTbl(patientUUIDList.get(i)); // Eg. 40 this patientuuids should be less here
                }
                db.setTransactionSuccessful();
            } catch (DAOException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }

            // hide progress dialog after background tasks are complete on the main thread
            runOnUiThread(() -> {
                // todo: update your ui / view in activity
                draftSurveyAdapter = new SearchPatientAdapter(patientDTOList, context);
                recyclerView.setAdapter(draftSurveyAdapter);
                mSyncProgressDialog.hide();
            });
        });

    }

    private List<PatientDTO> fetchValueAttrFromPatAttrTbl(String patientuuid) throws DAOException {
//         db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        String name = "";
        boolean draft = false;
//        db.beginTransaction();
        try {
//            String query = "SELECT * from tbl_patient_attribute as c WHERE c.patientuuid = '" + patientuuid + "' and c.modified_date = (SELECT max(d.modified_date) from tbl_patient_attribute as d where d.person_attribute_type_uuid = c.person_attribute_type_uuid) group by person_attribute_type_uuid";
            String query = "SELECT * from tbl_patient_attribute as c WHERE c.patientuuid = ? and c.modified_date = (SELECT max(d.modified_date) from tbl_patient_attribute as d where d.person_attribute_type_uuid = c.person_attribute_type_uuid) group by person_attribute_type_uuid";

            String[] args = new String[]{patientuuid};
            Cursor cursor = db.rawQuery(query, args, null);
            Attribute attribute = new Attribute();
            if (cursor.moveToFirst() && !cursor.isClosed()) {
                while (!cursor.isAfterLast() && !cursor.isClosed()) {
                    attribute = new Attribute();
                    attribute.setAttributeType(cursor.getString(cursor.getColumnIndex("person_attribute_type_uuid")));
                    attribute.setValue(cursor.getString(cursor.getColumnIndex("value")));
                    name = patientsDAO.getAttributesName(attribute.getAttributeType());
                    boolean condition = (attribute.getValue().equalsIgnoreCase("-") ||
                            attribute.getValue().equalsIgnoreCase("[]") ||
                            attribute.getValue().equalsIgnoreCase("Select"));

                    if (name.equalsIgnoreCase("NamePrimaryRespondent") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("HouseholdNumber") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("HouseStructure") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("ResultOfVisit") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("householdHeadName") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("householdHeadGender") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("householdHeadReligion") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("householdHeadCaste") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("noOfSmartphones") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("noOfFeaturePhones") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("noOfEarningMembers") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("primarySourceOfIncome") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("householdElectricityStatus") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("noOfLoadSheddingHrsPerDay") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("noOfLoadSheddingHrsPerWeek") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("runningWaterStatus") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("primarySourceOfRunningWater") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("waterSourceDistance") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("waterSupplyAvailabilityHrsPerDay") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("waterSupplyAvailabilityDaysperWeek") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("householdBankAccountStatus") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("householdCultivableLand") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("averageAnnualHouseholdIncome") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("monthlyFoodExpenditure") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("annualHealthExpenditure") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("annualEducationExpenditure") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("annualClothingExpenditure") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("monthlyIntoxicantsExpenditure") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("householdBPLCardStatus") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("householdAntodayaCardStatus") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("householdRSBYCardStatus") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("householdMGNREGACardStatus") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("cookingFuelType") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("mainLightingSource") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("mainDrinkingWaterSource") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("saferWaterProcess") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("householdToiletFacility") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("householdOpenDefecationStatus") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("foodItemsPreparedInTwentyFourHrs") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("subCentreDistance") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("nearestPrimaryHealthCenterDistance") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("nearestCommunityHealthCenterDistance") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("nearestDistrictHospitalDistance") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("nearestPathologicalLabDistance") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("nearestPrivateClinicMBBSDoctor") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("nearestPrivateClinicAlternateMedicine") && condition) {
                        draft = true;
                    } else if (name.equalsIgnoreCase("nearestTertiaryCareFacility") && condition) {
                        draft = true;
                    }

                    if (draft) {
                        PatientDTO patientDTO = new PatientDTO();
                        String patientSelection = "uuid=?";
                        String[] patientArgs = {patientuuid};
                        Cursor idCursor = db.query("tbl_patient", null, patientSelection, patientArgs,
                                null, null, null);
                        if (idCursor.moveToFirst()) {
                            do {
                                patientDTO.setUuid(patientuuid);
                                patientDTO.setFirstname(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                                patientDTO.setMiddlename(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                                patientDTO.setLastname(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));
                                patientDTO.setOpenmrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("openmrs_id")));
                                patientDTO.setDateofbirth(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                            } while (idCursor.moveToNext());
                            idCursor.close();
                        }
                        patientDTOList.add(patientDTO);
                        cursor.close();
                    } else {
                        if (!cursor.isClosed())
                            cursor.moveToNext();
                    }
                }
            }

            if (!cursor.isClosed())
                cursor.close();
//            db.setTransactionSuccessful();
        } catch (SQLException e) {
            throw new DAOException(e.getMessage());
        } finally {
//            db.endTransaction();
        }
        return patientDTOList;
    }

    private List<String> fetchUniquePatientUuidFromAttributes() throws DAOException {
        List<String> patientUUIDs = new ArrayList<>();
        LinkedHashSet<String> patientUUIDs_hashset = new LinkedHashSet<>();

//        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
//        db.beginTransaction();
        try {
            String query = "SELECT DISTINCT(patientuuid) from tbl_patient_attribute";
            Cursor cursor = db.rawQuery(query, null, null);
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    patientUUIDs_hashset.add(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")));
                }
            }
            cursor.close();
//            db.setTransactionSuccessful();
        } catch (SQLException e) {
            throw new DAOException(e.getMessage());
        } finally {
//            db.endTransaction();
        }
        patientUUIDs.addAll(patientUUIDs_hashset);
        return patientUUIDs;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}