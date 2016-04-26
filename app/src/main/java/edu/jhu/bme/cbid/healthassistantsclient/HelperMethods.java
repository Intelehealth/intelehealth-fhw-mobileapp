package edu.jhu.bme.cbid.healthassistantsclient;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/**
 * Created by tusharjois on 3/22/16.
 */
public class HelperMethods {

    public static int getAge(String s) {
        if (s == null) return 0;

        String[] components = s.split("\\-");

        int year = Integer.parseInt(components[0]);
        int month = Integer.parseInt(components[1]);
        int day = Integer.parseInt(components[2]);

        LocalDate birthdate = new LocalDate (year, month, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());

        return period.getYears();
    }


}
