package org.intelehealth.ezazi.partogram.model;

import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import org.intelehealth.ezazi.R;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kaveri Zaware on 27-09-2023
 * email - kaveri@intelehealth.org
 **/
public class GetMedicineData {
    private static final String TAG = "GetMedicineData";
    private List<Medicine> medicineDetailsList;

    public List<Medicine> getMedicineDetails(Context mContext) {
        medicineDetailsList = new ArrayList<>();
        InputStream inputStream = null;
        try {
            inputStream = mContext.getAssets().open("medicines.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String jsonData = new String(buffer, "UTF-8");
            JSONObject mainObject = new JSONObject(jsonData);
            JSONArray routesDataArr = mainObject.getJSONArray("medicineDetails");


            for (int i = 0; i < routesDataArr.length(); i++) {
                JSONObject jsonObject = routesDataArr.getJSONObject(i);
                String name = jsonObject.getString("name");
                String form = jsonObject.getString("form");
                String strength = jsonObject.getString("strength");
                String doseUnit = jsonObject.getString("doseUnit");
                String route = jsonObject.getString("route");
                //medicine name =  form name strength
                String medicineName = form + " " + name + " " + strength;
                Log.d(TAG, "getMedicineDetails: medicineName :: "+medicineName);
                Medicine medicine = new Medicine();
                medicine.setMedicineFullName(medicineName);
                medicine.setName(name);
                medicine.setForm(form);
                medicine.setStrength(strength);
                medicine.setDosageUnit(doseUnit);
                medicine.setRoute(route);
                medicineDetailsList.add(medicine);
            }
           /* autoCompleteTextView.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
            autoCompleteTextView.setAdapter(new ArrayAdapter(mContext, R.layout.spinner_textview, routesArray));
            autoCompleteTextView.setThreshold(1);*/


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return medicineDetailsList;
    }
}
