package org.intelehealth.app.appointmentNew;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class MyAppointmentsPagerAdapter extends FragmentPagerAdapter {

    int tabCount;
    Context context;

    public MyAppointmentsPagerAdapter(FragmentManager fm, int numberOfTabs, Context context) {
        super(fm);
        this.tabCount = numberOfTabs;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return new AllAppointmentsFragment();

            case 0:

            default:
                return new TodaysMyAppointmentsFragment();
        }

    }

    @Override
    public int getCount() {
        return tabCount;
    }
}