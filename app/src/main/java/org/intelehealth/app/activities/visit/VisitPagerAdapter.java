package org.intelehealth.app.activities.visit;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.intelehealth.app.activities.appointment.AllAppointmentsFragment;
import org.intelehealth.app.activities.appointment.TodaysMyAppointmentsFragment;

/**
 * Created by: Prajwal Waingankar On: 2/Nov/2022
 * Github: prajwalmw
 */
public class VisitPagerAdapter extends FragmentPagerAdapter {
    int tabCount;
    Context context;

    public VisitPagerAdapter(FragmentManager fm, int numberOfTabs, Context context) {
        super(fm);
        this.tabCount = numberOfTabs;
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return new VisitPendingFragment();

            case 0:

            default:
                return new VisitReceivedFragment();
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
