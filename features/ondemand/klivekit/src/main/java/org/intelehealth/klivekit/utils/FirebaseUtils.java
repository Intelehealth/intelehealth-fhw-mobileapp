package org.intelehealth.klivekit.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class FirebaseUtils {
    private static final String TAG = FirebaseUtils.class.getName();

    public static void saveToken(Context context, String userUUID, String fcmToken, String lang) {
        Log.v(TAG, userUUID);
        Log.v(TAG, fcmToken);
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        // Start the queue
        requestQueue.start();
        try {
            if (userUUID == null || userUUID.isEmpty() || fcmToken == null || fcmToken.isEmpty()) {
                return;
            }
            JSONObject inputJsonObject = new JSONObject();
            inputJsonObject.put("user_uuid", userUUID);
            inputJsonObject.put("data", new JSONObject().put("device_reg_token", fcmToken));
            inputJsonObject.put("locale", lang);

            String url = Constants.SAVE_FCM_TOKEN_URL;
            Log.v(TAG, url);
            Log.v(TAG, inputJsonObject.toString());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, inputJsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.v(TAG, "saveToken -response - " + response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v(TAG, "saveToken - onErrorResponse - " + error.getMessage());

                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    7 * 1000,
                    3,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
