package org.intelehealth.ezazi.activities.addNewPatient;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.intelehealth.ezazi.R;

import java.util.List;

public class CountryArrayAdapter extends ArrayAdapter<String> {

    public CountryArrayAdapter(Context context, List<String> objects) {
        super(context, R.layout.spinner_textview, objects);
        setDropDownViewResource(R.layout.spinner_textview);
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v;
        if (position == 0) {
            TextView tv = new TextView(getContext());
            tv.setHeight(0);
            tv.setVisibility(View.GONE);
            v = tv;
        } else {
            v = super.getDropDownView(position, null, parent);
        }
        parent.setVerticalScrollBarEnabled(true);
        return v;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}


