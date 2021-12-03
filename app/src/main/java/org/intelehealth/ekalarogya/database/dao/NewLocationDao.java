package org.intelehealth.ekalarogya.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.models.dto.LocationDTO;
import org.intelehealth.ekalarogya.models.statewise_location.Setup_DistrictModel;
import org.intelehealth.ekalarogya.models.statewise_location.Setup_LocationModel;
import org.intelehealth.ekalarogya.models.statewise_location.Setup_SanchModel;
import org.intelehealth.ekalarogya.models.statewise_location.Setup_StateModel;
import org.intelehealth.ekalarogya.models.statewise_location.Setup_VillageModel;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NewLocationDao {

    long createdRecordsCount = 0;

    public boolean insertSetupLocations(Setup_LocationModel location) throws DAOException {
        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            List<Setup_StateModel> stateList=location.getStates();
            if(stateList.size()>0) {
                for (int i = 0; i < stateList.size(); i++) {
                    String stateName = stateList.get(i).getName();
                    List<Setup_DistrictModel> districtList = stateList.get(i).getDistricts();
                    if (districtList != null && districtList.size()>0) {


                        for(int j=0;j<districtList.size();j++) {
                            String districtName = districtList.get(j).getName();
                            List<Setup_SanchModel> sanchList = districtList.get(j).getSanchs();
                            if (sanchList != null && sanchList.size() > 0) {
                                for(int k=0;k<sanchList.size();k++) {
                                    String sanchName = sanchList.get(k).getName();
                                    List<Setup_VillageModel> villageList = sanchList.get(k).getVillages();
                                    if (villageList != null && villageList.size() > 0) {
                                        for(int n=0;n<villageList.size();n++){
                                            String villageName = villageList.get(n).getName();
                                            //------enter into db with name and state, district, sanch and village only----
                                            createSetupLocation(stateName, districtName, sanchName, villageName, db);
                                        }
                                    }else {
                                        //------enter into db with name and state, district and sanch only----
                                        createSetupLocation(stateName, districtName, sanchName,"", db);
                                    }
                                }
                            } else {
                                //------enter into db with name and state and district only----
                                createSetupLocation(stateName, districtName, "", "", db);
                            }
                        }


                    } else {
                        //------enter into db with name and stateonly----
                        createSetupLocation(stateName,"","", "",db);
                    }
                }
            }
            //values.put("uuid", UUID.randomUUID().toString());
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();

        }

        return isInserted;
    }

    private boolean createSetupLocation(String stateName,String districtName, String sanchName, String villageName, SQLiteDatabase db) throws DAOException {
        boolean isCreated = true;
        ContentValues values = new ContentValues();
        try {
            values.put("country", "India");
            if(villageName.length()>0){
                values.put("name", villageName);
                values.put("state", stateName);
                values.put("district", districtName);
                values.put("tehsil", sanchName);
                values.put("village", villageName);
            }else if(sanchName.length()>0){
                values.put("name", districtName);
                values.put("state", stateName);
                values.put("district", districtName);
                values.put("tehsil", sanchName);
            }else if(districtName.length()>0){
                values.put("name", stateName);
                values.put("state", stateName);
                values.put("district", districtName);
            }else if(stateName.length()>0){
                values.put("name", stateName);
                values.put("state", stateName);
            }
            values.put("locationuuid", UUID.randomUUID().toString());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            createdRecordsCount = db.insertWithOnConflict("tbl_location_new", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        }
        return isCreated;
    }

    public List<String> getStateList() {
        List<String> state_locations=new ArrayList<String>();
        state_locations.add("Select State");
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Cursor cursor = db.rawQuery("SELECT DISTINCT state FROM tbl_location_new",null);
        Log.d("count", "count: "+cursor.getCount());

        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                state_locations.add(cursor.getString(cursor.getColumnIndex("state")));
            }
        }
        cursor.close();
        return state_locations;
    }

    public List<String> getDistrictList(String stateName) {
        List<String> district_locations=new ArrayList<String>();
        district_locations.add("Select District");
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Cursor cursor = db.rawQuery("SELECT DISTINCT district FROM tbl_location_new where state = ? COLLATE NOCASE", new String[]{stateName});
        Log.d("count", "count: "+cursor.getCount());

        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                district_locations.add(cursor.getString(cursor.getColumnIndex("district")));
            }
        }
        cursor.close();
        return district_locations;
    }

    public List<String> getSanchList(String stateName, String districtName){
        List<String> sanch_locations=new ArrayList<String>();
        sanch_locations.add("Select Sanch");
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Cursor cursor = db.rawQuery("SELECT DISTINCT tehsil FROM tbl_location_new where state = ? AND district=? COLLATE NOCASE", new String[]{stateName,districtName});
        Log.d("count", "count: "+cursor.getCount());

        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                sanch_locations.add(cursor.getString(cursor.getColumnIndex("tehsil")));
            }
        }
        cursor.close();
        return sanch_locations;
    }

    public List<String> getVillageList(String stateName, String districtName, String sanchName){
        List<String> village_locations=new ArrayList<String>();
        village_locations.add("Select Village");
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Cursor cursor = db.rawQuery("SELECT DISTINCT village FROM tbl_location_new where state = ? AND district=? AND tehsil=? COLLATE NOCASE", new String[]{stateName,districtName,sanchName});
        Log.d("count", "count: "+cursor.getCount());

        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                village_locations.add(cursor.getString(cursor.getColumnIndex("village")));
            }
        }
        cursor.close();
        return village_locations;
    }

}
