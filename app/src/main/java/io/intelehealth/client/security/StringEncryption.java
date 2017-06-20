package io.intelehealth.client.security;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by Dexter Barretto on 5/24/17.
 * Github : @dbarretto
 */


public class StringEncryption {

    SecureRandom secureRandom = null;

    public String getRandomSaltString() {
        if (secureRandom == null) secureRandom = new SecureRandom();
        return new BigInteger(130, secureRandom).toString(32);
    }

    public static String convertToSHA256(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        byte[] digest = md.digest();
        return String.format("%064x", new BigInteger(1, digest));
    }
}
