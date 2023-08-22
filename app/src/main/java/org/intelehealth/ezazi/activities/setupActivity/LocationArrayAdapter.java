package org.intelehealth.ezazi.activities.setupActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import java.util.List;

import org.intelehealth.ezazi.R;

public class LocationArrayAdapter extends ArrayAdapter<String> {

    public LocationArrayAdapter(Context context, List<String> objects) {
        super(context, R.layout.spinner_textview, objects);
        setDropDownViewResource(R.layout.spinner_textview);
    }

    @SuppressLint("ResourceType")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (view != null && view.getId() > 0) {
            view.setContentDescription(getItem(position));
        }
        assert view != null;
        return view;
    }

    //    @Override
//    public int getCount() {
//        return super.getCount();
//    }
//
//    @Override
//    public View getDropDownView(int position, View convertView, ViewGroup parent) {
//        View v;
//        if (position == 0) {
//            TextView tv = new TextView(getContext());
//            tv.setHeight(0);
//            tv.setVisibility(View.GONE);
//            v = tv;
//        } else {
//            v = super.getDropDownView(position, null, parent);
//        }
//        int id = ViewCompat.generateViewId();
//        Log.e("DropDownAdapter", "getDropDownView: id =>" + id);
//        v.setId(id);
//        parent.setVerticalScrollBarEnabled(true);
//        return v;
//    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}


