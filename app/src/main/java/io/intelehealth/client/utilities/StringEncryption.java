package io.intelehealth.client.utilities;

import android.content.res.AssetManager;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import io.intelehealth.client.activities.complaintNodeActivity.ComplaintNodeActivity;
import io.intelehealth.client.knowledgeEngine.Node;

/**
 * Created by Dexter Barretto on 5/24/17.
 * Github : @dbarretto
 */


public class StringEncryption {

    private SecureRandom secureRandom = null;

    public static String convertToSHA256(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(text.getBytes(StandardCharsets.ISO_8859_1), 0, text.length());
        byte[] digest = md.digest();
        return String.format("%064x", new BigInteger(1, digest));
    }

    public String getRandomSaltString() {
        if (secureRandom == null) secureRandom = new SecureRandom();
        return new BigInteger(130, secureRandom).toString(32);
    }

    public String getSaltString()
    {
        String salt = "123456789";
        return salt;
    }
}
