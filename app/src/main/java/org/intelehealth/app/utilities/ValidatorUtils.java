package org.intelehealth.app.utilities;

import static android.util.Patterns.PHONE;
import static android.util.Patterns.EMAIL_ADDRESS;

import java.util.regex.Pattern;

/**
 * Created by Vaghela Mithun R. on 24-02-2024 - 11:21.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class ValidatorUtils {
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@[a-z]+\\.+[a-z]+");

    public static boolean isValidPhoneNumber(String number) {
        return PHONE.matcher(number).matches();
    }

    public static boolean isValidEmail(String email) {
        return EMAIL_REGEX.matcher(email).matches();
    }
}
