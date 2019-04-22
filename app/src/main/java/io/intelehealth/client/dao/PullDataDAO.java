package io.intelehealth.client.dao;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.dto.ResponseDTO;
import io.intelehealth.client.utilities.Logger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PullDataDAO {


    public boolean pullData(final Context context) {

        String url = "http://142.93.221.37:8080/EMR-Middleware/webapi/pull/pulldata/1eaa9a54-0fcb-4d5c-9ec7-501d2e5bcf2a/" + AppConstants.sessionManager.getPullExcutedTime();
        Call<ResponseDTO> middleWarePullResponseCall = AppConstants.apiInterface.RESPONSE_DTO_CALL(url);

        middleWarePullResponseCall.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {

                if (response.isSuccessful()) {
                    pullDataExecutedTime(response.body(), context);
                }
                if (response.body() != null && response.body().getData() != null) {
                    AppConstants.sessionManager.setPullExcutedTime(response.body().getData().getPullexecutedtime());
                }
            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                Logger.logD("pull data", "exception" + t.getMessage());
            }
        });

       /* try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            ResponseDTO responseDTO = middleWarePullResponseCall.execute().body();
            if (responseDTO != null) {

                pullDataExecutedTime(responseDTO, context);
//                sessionManager.setPullExcutedTime(responseDTO.getData().getPullexecutedtime());

                Gson gson = new Gson();
                Logger.logD("PuldataDao", "pulled data" + gson.toJson(responseDTO));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        return true;
    }

    public void pullDataExecutedTime(final ResponseDTO responseDTO, final Context context) {
        class dataInserted extends AsyncTask<Void, Void, Void> {
            private ProgressDialog dialog = new ProgressDialog(context);

            @Override
            protected Void doInBackground(Void... voids) {
                SyncDAO syncDAO = new SyncDAO();
                try {
                    syncDAO.SyncData(responseDTO);
                } catch (DAOException e) {
                    Logger.logE("Dao exception", "exception", e);
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                this.dialog.setMessage("Please wait");
                this.dialog.setCancelable(false);
                this.dialog.show();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }
        dataInserted dataInserted = new dataInserted();
        dataInserted.execute();

    }
}
   /* private void dataInsert(List<PatientDTO> patientDTO) {


        inteleHealthDatabaseHelper = new InteleHealthDatabaseHelper(IntelehealthApplication.getAppContext());
        SQLiteDatabase db = inteleHealthDatabaseHelper.getWritableDatabase();
        inteleHealthDatabaseHelper.onCreate(db);
        ContentValues values = new ContentValues();
        for (int i = 0; i < patientDTO.size(); i++) {
            values.put("openmrs_uuid", patientDTO.get(i).getOpenmrsUuid());
            values.put("openmrs_id", patientDTO.get(i).getOpenmrsId());
            values.put("first_name", patientDTO.get(i).getFirstname());
            values.put("middle_name", patientDTO.get(i).getMiddlename());
            values.put("last_name", patientDTO.get(i).getLastname());
            values.put("address1", patientDTO.get(i).getAddress1());
            values.put("country", patientDTO.get(i).getCountry());
            values.put("date_of_birth", patientDTO.get(i).getDateofbirth());
            values.put("gender", patientDTO.get(i).getGender());
            Logger.logD("pulldata", "datadumper" + values);
            db.insertWithOnConflict("patient_temp", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }

        db.close();

    }

    private void visitInsert(List<VisitDTO> visitDTOS) {
        inteleHealthDatabaseHelper = new InteleHealthDatabaseHelper(IntelehealthApplication.getAppContext());
        SQLiteDatabase db = inteleHealthDatabaseHelper.getWritableDatabase();
        inteleHealthDatabaseHelper.onCreate(db);
        ContentValues values = new ContentValues();
        for (int i = 0; i < visitDTOS.size(); i++) {
            values.put("openmrs_patientuuid", visitDTOS.get(i).getOpenmrsPatientuuid());
            values.put("openmrs_visituuid", visitDTOS.get(i).getOpenmrsVisituuid());
            values.put("locationuuid", visitDTOS.get(i).getLocationuuid());
            values.put("visit_type_id", visitDTOS.get(i).getVisitTypeId());
            values.put("visit_creator", visitDTOS.get(i).getCreator());
            values.put("end_datetime", visitDTOS.get(i).getEnddate());
            values.put("start_datetime", visitDTOS.get(i).getStartdate());
            Logger.logD("pulldata", "datadumper" + values);
            db.insertWithOnConflict("visit_temp", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }

        db.close();
    }

    private void encounterInsert(List<EncounterDTO> encounterDTOS) {
        inteleHealthDatabaseHelper = new InteleHealthDatabaseHelper(IntelehealthApplication.getAppContext());
        SQLiteDatabase db = inteleHealthDatabaseHelper.getWritableDatabase();
        inteleHealthDatabaseHelper.onCreate(db);
        ContentValues values = new ContentValues();
        for (int i = 0; i < encounterDTOS.size(); i++) {
            values.put("openmrs_encounteruuid", encounterDTOS.get(i).getOpenmrsEncounteruuid());
            values.put("openmrs_visit_uuid", encounterDTOS.get(i).getOpenmrsVisituuid());
            values.put("encounter_type", encounterDTOS.get(i).getEncounterType());
            Logger.logD("pulldata", "datadumper" + values);
            db.insertWithOnConflict("encounter_temp", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }

        db.close();
    }

    private void obsInsert(List<ObsDTO> obsDTOS) {
        inteleHealthDatabaseHelper = new InteleHealthDatabaseHelper(IntelehealthApplication.getAppContext());
        SQLiteDatabase db = inteleHealthDatabaseHelper.getWritableDatabase();
        inteleHealthDatabaseHelper.onCreate(db);
        ContentValues values = new ContentValues();
        for (int i = 0; i < obsDTOS.size(); i++) {
            values.put("openmrs_encounteruuid", obsDTOS.get(i).getOpenmrsEncounteruuid());
            values.put("openmrs_obsuuid", obsDTOS.get(i).getOpenmrsObsuuid());
            values.put("creator", obsDTOS.get(i).getCreator());
            values.put("concept_id", obsDTOS.get(i).getConceptid());
            values.put("value", obsDTOS.get(i).getValue());
            Logger.logD("pulldata", "datadumper" + values);
            db.insertWithOnConflict("obs_temp", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }

        db.close();
    }



 private void pullDataToDb(final List<PatientDTO> patientDTO) {

        class SaveTaskattendanceData extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

//adding to database
                InteleHealthRoomDatabase.getInteleHealthRoomDatabase(IntelehealthApplication.getAppContext()).inteleHealthDao().PullDatainsert(patientDTO);
//                GurukulRoomDatabase.getDatabase(getApplication()).gurukulDAO().insertgurukul(gurukulLocalDataModel);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        }

        SaveTaskattendanceData st = new SaveTaskattendanceData();
        st.execute();
    }
*/



