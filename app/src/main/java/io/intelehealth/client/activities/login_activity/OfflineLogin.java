package io.intelehealth.client.activities.login_activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import io.intelehealth.client.activities.home_activity.HomeActivity;
import io.intelehealth.client.application.IntelehealthApplication;
import io.intelehealth.client.R;
import io.intelehealth.client.security.StringEncryption;


/**
 * Provides necessary login alternative if offline.
 * <p>
 * Created by Dexter Barretto on 5/24/17.
 * Github : @dbarretto
 */

public class OfflineLogin {

    private static final String TAG = OfflineLogin.class.getSimpleName();

    private Context mContext;
    private SharedPreferences mSharedPreference;
    private static OfflineLogin mOfflineLogin;

    private OfflineLogin(Context context) {
        mContext = context;
        mSharedPreference = mContext.getSharedPreferences(
                context.getString(R.string.offline_login_shared_preference_key), Context.MODE_PRIVATE);
    }

    /**
     * Stores login credentials in shared preferences after hashing.
     *
     * @param username The username
     * @param password The password
     */
    public void setUpOfflineLogin(String username, String password) {
        StringEncryption stringEncryption = new StringEncryption();
        String random_salt = stringEncryption.getRandomSaltString();
        String hash = null;
        try {
            hash = stringEncryption.convertToSHA256(random_salt + password);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        SharedPreferences.Editor editor = mSharedPreference.edit();

        if (username != null && !TextUtils.isEmpty(username)) editor.putString(
                mContext.getString(R.string.offline_login_username_key), username);

        if (random_salt != null && !TextUtils.isEmpty(random_salt)) editor.putString(
                mContext.getString(R.string.offline_login_salt_key), random_salt);

        if (hash != null && !TextUtils.isEmpty(hash)) editor.putString(
                mContext.getString(R.string.offline_login_password_key), hash);

        editor.putBoolean(
                mContext.getString(R.string.offline_login_status), true);

        editor.commit();

        Log.i(TAG, "Created Offline Login!");
        Log.i(TAG, "Username: " + username);
        Log.i(TAG, "Salt: " + random_salt);
        Log.i(TAG, "Password Hash: " + hash);
    }

    public void login(String username, String password) {
        Log.i(TAG, "Checking Offline Login!");
        if (mSharedPreference.contains(mContext.getString(R.string.offline_login_username_key)) &&
                mSharedPreference.contains(mContext.getString(R.string.offline_login_salt_key)) &&
                mSharedPreference.contains(mContext.getString(R.string.offline_login_password_key))) {

            String stored_username = mSharedPreference.getString(mContext.getString(R.string.offline_login_username_key), null);

            if (stored_username != null && stored_username.equals(username)) {
                Log.i(TAG, "Username: " + username);
                Log.i(TAG, "Stored Username: " + stored_username);
                String stored_password = mSharedPreference.getString(mContext.getString(R.string.offline_login_password_key), null);
                String stored_salt = mSharedPreference.getString(mContext.getString(R.string.offline_login_salt_key), null);

                if (stored_password != null && stored_salt != null) {
                    Log.i(TAG, "Stored Salt: " + stored_salt);
                    Log.i(TAG, "Stored Password : " + stored_password);
                    StringEncryption stringEncryption = new StringEncryption();
                    String hash = null;
                    try {
                        hash = stringEncryption.convertToSHA256(stored_salt + password);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "Hashed Password : " + hash);
                    if (hash != null && stored_password.equals(hash)) {
                        Toast.makeText(mContext, "Offline Login Successful", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(mContext, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        setOfflineLoginStatus(true);
                        mContext.startActivity(intent);
                    } else {
                        Toast.makeText(mContext, "Offline Login Unsuccessful", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.offline_authentication_not_possible), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.offline_credentials_unavailable), Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(mContext, mContext.getString(R.string.offline_credentials_unavailable), Toast.LENGTH_LONG).show();
        }
    }

    public void invalidateLoginCredentials() {
        mSharedPreference.edit().clear().commit();
        Log.i(TAG, "Offline Login Invalidated");
    }

    /**
     * Provides application context.
     *
     * @return {@link OfflineLogin}
     */
    public static OfflineLogin getOfflineLogin() {
        if (mOfflineLogin == null)
            mOfflineLogin = new OfflineLogin(IntelehealthApplication.getAppContext());
        return mOfflineLogin;
    }

    public void setOfflineLoginStatus(Boolean status) {
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putBoolean(
                mContext.getString(R.string.offline_login_status), status);
        editor.commit();
    }

    public Boolean getOfflineLoginStatus() {
        if (mSharedPreference.contains(mContext.getString(R.string.offline_login_status))) {
            return mSharedPreference.getBoolean(mContext.getString(R.string.offline_login_status), false);
        }
        return false;
    }
}
