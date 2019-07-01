package io.intelehealth.client.database.dao;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.models.dto.EncounterDTO;
import io.intelehealth.client.models.dto.ObsDTO;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.UuidDictionary;
import io.intelehealth.client.utilities.exception.DAOException;
import retrofit2.Call;

import static io.intelehealth.client.app.AppConstants.apiInterface;

public class EmergencyEncounterDAO {
    SessionManager sessionManager = null;

    public boolean uploadEncounterEmergency(String visitUuid, Integer voided) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
        Date todayDate = new Date();
        String thisDate = currentDate.format(todayDate);

        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = new EncounterDTO();
        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();

        String uuid = UUID.randomUUID().toString();
        encounterDTO.setEncounterTime(thisDate);
        encounterDTO.setUuid(uuid);
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("EMERGENCY"));
        encounterDTO.setProvideruuid(sessionManager.getProviderID());
        encounterDTO.setVisituuid(visitUuid);
        encounterDTO.setVoided(voided);
        encounterDTO.setSyncd(false);

        obsDTO.setConceptuuid(UuidDictionary.EMERGENCY_OBS);
        obsDTO.setCreator(1);
        obsDTO.setUuid(UUID.randomUUID().toString());
        obsDTO.setEncounteruuid(uuid);
        obsDTO.setValue("emergency");

        try {
            encounterDAO.createEncountersToDB(encounterDTO);
            obsDAO.insertEmergencyObs(obsDTO);
        } catch (DAOException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return true;
    }

    public boolean removeEncounterEmergency(final String visitUuid, SQLiteDatabase db) {

        boolean success = false;
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        SessionManager session = new SessionManager(IntelehealthApplication.getAppContext());
        if (visitUuid != null) {
            String selectQuery = "SELECT uuid FROM tbl_encounter WHERE visituuid='" + visitUuid + "'  and encounter_type_uuid='ca5f5dc3-4f0b-4097-9cae-5cf2eb44a09c'";
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        String name = cursor.getString(cursor.getColumnIndex("uuid"));
                        String url = "http://" + session.getServerUrl() + ":8080/openmrs/ws/rest/v1/encounter/" + name;
                        Call<Void> patientUUIDResponsemodelCall = apiInterface.DELETE_ENCOUNTER(url, "Basic " + session.getEncoded());
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                        try {

                            int responsecode = patientUUIDResponsemodelCall.execute().raw().code();
                            if (responsecode == 204) {
                                String delteQuery = "DELETE FROM tbl_encounter WHERE visituuid='" + visitUuid + "' and encounter_type_uuid='ca5f5dc3-4f0b-4097-9cae-5cf2eb44a09c'";
                                success = true;
                                db.execSQL(delteQuery);
                            } else if (responsecode == 403) {
                                Log.d("403", "unauthorized" + responsecode);

                            }
                        } catch (IOException e) {
                            Crashlytics.getInstance().core.logException(e);
                        }
                        cursor.moveToNext();
                    }
                }

            }
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return success;
    }

    public boolean isEmergency(String visitUuid) throws DAOException {
        boolean isEmergency = false;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        String selectQuery = "SELECT uuid FROM tbl_encounter WHERE visituuid='" + visitUuid + "'  and encounter_type_uuid='ca5f5dc3-4f0b-4097-9cae-5cf2eb44a09c'";
        db.beginTransaction();
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        isEmergency = true;
                        cursor.moveToNext();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLException e) {
            isEmergency = false;
            Crashlytics.getInstance().core.logException(e);
            throw new DAOException(e);
        } finally {
            db.endTransaction();
            db.close();
        }
        return isEmergency;
    }


}
