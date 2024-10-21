package org.intelehealth.app.activities.setupActivity;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import org.intelehealth.app.R;

public class LocationArrayAdapter extends ArrayAdapter<String> {

    public LocationArrayAdapter(Context context, List<String> objects) {
        super(context, R.layout.spinner_textview, R.id.text1, objects);
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
        }
        else {
            v = super.getDropDownView(position, null, parent);
        }

        parent.setVerticalScrollBarEnabled(false);
        return v;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (view != null) {
//            if(getItem(position).equalsIgnoreCase("Telemedicine Clinic 1") /*|| getItem(position).equalsIgnoreCase("Telemedicine Clinic 2")*/)
            if(position==0)
            {
                View divider = view.findViewById(R.id.spinner_divider);
                divider.setVisibility(View.GONE);
            }
        }
        assert view != null;
        return view;
    }
}


