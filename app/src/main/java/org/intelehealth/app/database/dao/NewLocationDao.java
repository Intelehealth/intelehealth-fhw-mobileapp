package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.statewise_location.Setup_DistrictModel;
import org.intelehealth.app.models.statewise_location.Setup_LocationModel;
import org.intelehealth.app.models.statewise_location.Setup_StateModel;
import org.intelehealth.app.models.statewise_location.Setup_VillageModel;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;

public class NewLocationDao {

    long createdRecordsCount = 0;

    public boolean insertSetupLocations(Setup_LocationModel location) throws DAOException {
        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            List<Setup_StateModel> stateList = location.getStates();
            if (stateList.size() > 0) {
                for (int i = 0; i < stateList.size(); i++) {
                    String stateName = stateList.get(i).getName();
                    String uuid = stateList.get(i).getUuid();
                    List<Setup_DistrictModel> districtList = stateList.get(i).getDistricts();
                    if (districtList != null && districtList.size() > 0) {
                        for (int j = 0; j < districtList.size(); j++) {
                            String districtName = districtList.get(j).getName();
                            uuid = districtList.get(j).getUuid();
                            List<Setup_VillageModel> villageList = districtList.get(j).getVillages();
                            if (villageList != null && villageList.size() > 0) {
                                for (int n = 0; n < villageList.size(); n++) {
                                    String villageName = villageList.get(n).getName();
                                    uuid = villageList.get(n).getUuid();
                                    //------enter into db with name and state, district and village only----
                                    createSetupLocation(stateName, districtName, villageName, uuid, db);
                                }
                            } else {
                                //------enter into db with name and state and district only----
                                createSetupLocation(stateName, districtName, "", uuid, db);
                            }
                        }
                    } else {
                        //------enter into db with name and stateonly----
                        createSetupLocation(stateName, "", "", uuid, db);
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

    //------------------sanch and tehsil both are same--------------------------
    private boolean createSetupLocation(String stateName, String districtName, String villageName, String uuid, SQLiteDatabase db) throws DAOException {
        boolean isCreated = true;
        ContentValues values = new ContentValues();
        try {
            values.put("country", "India");
            if (villageName.length() > 0) {
                values.put("name", villageName);
                values.put("state", stateName);
                values.put("district", districtName);
                values.put("village", villageName);
            } else if (districtName.length() > 0) {
                values.put("name", stateName);
                values.put("state", stateName);
                values.put("district", districtName);
            } else if (stateName.length() > 0) {
                values.put("name", stateName);
                values.put("state", stateName);
            }
            values.put("locationuuid", uuid);
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            createdRecordsCount = db.insertWithOnConflict("tbl_location_new", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        }
        return isCreated;
    }

    public List<String> getStateList(Context context) {
        List<String> state_locations = new ArrayList<String>();
        state_locations.add(context.getResources().getString(R.string.setup_select_state_str));
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Cursor cursor = db.rawQuery("SELECT DISTINCT state FROM tbl_location_new", null);
        Log.d("count", "count: " + cursor.getCount());

        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                state_locations.add(cursor.getString(cursor.getColumnIndex("state")));
            }
        }
        cursor.close();
        return state_locations;
    }

    public List<String> getDistrictList(String stateName,Context context) {
        List<String> district_locations = new ArrayList<String>();
        district_locations.add(context.getResources().getString(R.string.setup_select_district_str));
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Cursor cursor = db.rawQuery("SELECT DISTINCT district FROM tbl_location_new where state = ? COLLATE NOCASE", new String[]{stateName});
        Log.d("count", "count: " + cursor.getCount());

        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                if(cursor.getString(cursor.getColumnIndex("district"))!=null)
                    district_locations.add(cursor.getString(cursor.getColumnIndex("district")));
            }
        }
        cursor.close();
        return district_locations;
    }

    /*public List<String> getSanchList(String stateName, String districtName,Context context) {
        List<String> sanch_locations = new ArrayList<String>();
        sanch_locations.add(context.getResources().getString(R.string.setup_select_sanch_str));
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Cursor cursor = db.rawQuery("SELECT DISTINCT tehsil FROM tbl_location_new where state = ? AND district=? COLLATE NOCASE", new String[]{stateName, districtName});
        Log.d("count", "count: " + cursor.getCount());
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                if (cursor.getString(cursor.getColumnIndex("tehsil")) != null)
                    sanch_locations.add(cursor.getString(cursor.getColumnIndex("tehsil")));
            }
        }
        cursor.close();
        return sanch_locations;
    }*/

    public List<String> getVillageList(String stateName, String districtName/*, String sanchName*/, Context context) {
        List<String> village_locations = new ArrayList<String>();
        village_locations.add(context.getResources().getString(R.string.setup_select_village_str));
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Cursor cursor = db.rawQuery("SELECT DISTINCT village FROM tbl_location_new where state = ? AND district=? /*AND tehsil=?*/ COLLATE NOCASE", new String[]{stateName, districtName/*, sanchName*/});
        Log.d("count", "count: " + cursor.getCount());

        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                if(cursor.getString(cursor.getColumnIndex("village"))!=null)
                    village_locations.add(cursor.getString(cursor.getColumnIndex("village")));
            }
        }
        cursor.close();
        return village_locations;
    }

    public String getVillageUuid(String stateName, String districtName/*, String sanchName*/,String villageName) {
        String villageUuid = "";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Cursor cursor = db.rawQuery("SELECT DISTINCT locationuuid FROM tbl_location_new where state = ? AND district=? /*AND tehsil=?*/ AND village=? COLLATE NOCASE",
                new String[]{stateName, districtName/*, sanchName*/,villageName});
        Log.d("count", "count: " + cursor.getCount());

        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                villageUuid=cursor.getString(cursor.getColumnIndex("locationuuid"));
            }
        }
        cursor.close();
        return villageUuid;
    }

}
