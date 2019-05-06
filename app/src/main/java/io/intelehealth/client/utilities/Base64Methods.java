package io.intelehealth.client.utilities;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

public class Base64Methods {
    private String TAG = Base64Methods.class.getSimpleName();

    public String encoded(String USERNAME, String PASSWORD) {
        String encoded = null;
        encoded = Base64.encodeToString((USERNAME + ":" + PASSWORD).getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
        return encoded;
    }
}
