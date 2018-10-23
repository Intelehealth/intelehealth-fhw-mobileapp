
package io.intelehealth.client.activities.search_patient_activity;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import io.intelehealth.client.R;
import io.intelehealth.client.utilities.HelperMethods;

/**
 * Class to populate the patient search system with information from the database
 */
public class SearchCursorAdapter extends CursorAdapter {

    public SearchCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);
    }


    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_search, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
            TextView tvBody = (TextView) view.findViewById(R.id.list_item_body);
            TextView tvHead = (TextView) view.findViewById(R.id.list_item_head);

            // Extract properties from cursor
            String openmrsID = cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id"));
            String fName = cursor.getString(cursor.getColumnIndexOrThrow("first_name"));
            String mName = cursor.getString(cursor.getColumnIndexOrThrow("middle_name"));
            char mInitial = '\0';
            if (!mName.equals("")) mInitial = mName.charAt(0);
            String phoneNum = cursor.getString(cursor.getColumnIndexOrThrow("phone_number"));

            String lName = cursor.getString(cursor.getColumnIndexOrThrow("last_name"));
            String dob = cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth"));
            int age = HelperMethods.getAge(dob);
            //for converting Date format in dd-MMMM-yyyy
            dob=HelperMethods.SimpleDatetoLongDate(dob);
            String header, body;
            if (openmrsID == null) {
                header = String.format("%s %s", fName, lName);
                body = String.format(context.getString(R.string.identification_screen_prompt_phone_number) + ": %s\n" +
                        context.getString(R.string.identification_screen_prompt_birthday) + ": %s (" +
                        context.getString(R.string.identification_screen_prompt_age) + " %d)", phoneNum, dob, age);
            } else {
                header = String.format("%s %s - " + context.getString(R.string.visit_summary_heading_id) + ": %s", fName, lName, openmrsID);
                body = String.format(context.getString(R.string.id_number) + ": %s\n "+
                        context.getString(R.string.identification_screen_prompt_phone_number) + ": %s\n" +
                        context.getString(R.string.identification_screen_prompt_birthday) + ": %s (" +
                        context.getString(R.string.identification_screen_prompt_age) + " %d)", openmrsID, phoneNum, dob, age);
            }


            // String gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"));


            // Populate fields with extracted properties
            tvHead.setText(header);
            tvBody.setText(body);
        }

}

