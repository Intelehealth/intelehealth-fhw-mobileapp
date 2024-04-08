package org.intelehealth.app.appointmentNew;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.Objects;

public class MyAppointmentsPagerAdapter extends FragmentStateAdapter {

    int tabCount;
    Context context;

    public MyAppointmentsPagerAdapter(FragmentManager fm, int numberOfTabs, Context context) {
        super((FragmentActivity) context);
        this.tabCount = numberOfTabs;
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new AllAppointmentsFragment();

            case 0:

            default:
                return new TodaysMyAppointmentsFragment();
        }
    }


    @Override
    public int getItemCount() {
        return tabCount;
    }
}