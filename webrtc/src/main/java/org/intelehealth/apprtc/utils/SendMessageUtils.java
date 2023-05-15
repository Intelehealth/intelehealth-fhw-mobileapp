package org.intelehealth.apprtc.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.intelehealth.apprtc.data.Constants;
import org.json.JSONException;
import org.json.JSONObject;

public class SendMessageUtils {
    private static final String TAG = SendMessageUtils.class.getName();

    public static void postMessages(Context context, String visitId, String patientName, String fromUUId, String toUUId, String patientUUId, String message) {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            // Start the queue
            requestQueue.start();
            JSONObject inputJsonObject = new JSONObject();
            inputJsonObject.put("visitId", visitId);
            inputJsonObject.put("patientName", patientName);
            inputJsonObject.put("fromUser", fromUUId);
            inputJsonObject.put("toUser", toUUId);
            inputJsonObject.put("patientId", patientUUId);
            inputJsonObject.put("message", message);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Constants.SEND_MESSAGE_URL, inputJsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.v(TAG, "postMessages - response - " + response.toString());

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
