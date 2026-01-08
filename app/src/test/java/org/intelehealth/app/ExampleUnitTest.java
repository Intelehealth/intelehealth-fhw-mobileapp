package org.intelehealth.app;

import org.intelehealth.app.activities.prescription.PrescriptionBuilder;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.graphics.Typeface;
import android.util.Log;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void  datetime_format_test(){
        //to test firebase crash (AEAT-1979)
        String formattedDate = DateAndTimeUtils.date_formatter("", "yyyy-dd-mm", "yyyy-dd-mm");
        assertNull(formattedDate);
    }
}