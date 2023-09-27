package org.intelehealth.ezazi.partogram.model;

import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.utilities.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kaveri Zaware on 27-09-2023
 * email - kaveri@intelehealth.org
 **/
public class GetMedicineData {
    private static final String TAG = "GetMedicineData";

    public List<Medicine> getMedicineDetails(Context mContext) {

        String json = FileUtils.getJsonFromAssets(mContext, "medicines.json");
        if (json != null && json.length() > 0) {
            Type listType = new TypeToken<ArrayList<Medicine>>() {
            }.getType();
            return new Gson().fromJson(json, listType);
        } else return new ArrayList<>();
    }
}
