package org.intelehealth.ezazi.utilities;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Kaveri Zaware on 25-01-2024
 * email - kaveri@intelehealth.org
 **/
public class DateOfBirthCalculator {
    public static String getDobFromAge(int age,String selectedDobMonth,String selectedDobDay,String selectedDobYear) {
            Log.d("TAG", "getDobFromAge: selectedDobDay : "+selectedDobDay);
            Log.d("TAG", "getDobFromAge: selectedDobMonth : "+selectedDobMonth);
            Log.d("TAG", "getDobFromAge: selectedDobYear : "+selectedDobYear);
            Calendar currentDate = Calendar.getInstance();

            // Step 2: Subtract age from current year
            int birthYear = currentDate.get(Calendar.YEAR) - age;

            // Step 3: Create a Calendar object with the calculated birth year
            Calendar birthDate = Calendar.getInstance();
            birthDate.set(Calendar.YEAR, birthYear);

      /*  // Step 4: Set default values for month and day (using current month and day)
        birthDate.set(Calendar.MONTH, currentDate.get(Calendar.MONTH));
        birthDate.set(Calendar.DAY_OF_MONTH, currentDate.get(Calendar.DAY_OF_MONTH));*/


            birthDate.set(Calendar.YEAR, birthYear);
            birthDate.set(Calendar.MONTH, Integer.parseInt(selectedDobMonth));
            birthDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(selectedDobDay));

            // Print the calculated birthdate
            System.out.println("Date of Birth: " + birthDate.getTime());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            String birthDateString = dateFormat.format(birthDate.getTime());

            // Print the calculated birthdate as a string
            System.out.println("Date of Birth: " + birthDateString);
            return birthDateString;

    }
}
