package org.intelehealth.app.activities.visit;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Created by: Prajwal Waingankar On: 2/Nov/2022
 * Github: prajwalmw
 */
public class VisitPagerAdapter extends FragmentStateAdapter {
    int tabCount;
    Context context;

    public VisitPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

/*
    public VisitPagerAdapter(FragmentManager fm, int numberOfTabs, Context context) {
        super(fm);
        this.tabCount = numberOfTabs;
        this.context = context;
    }
*/

/*    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return new VisitPendingFragment();

            case 0:

            default:
                return new VisitReceivedFragment();
        }
    }*/

    /*@Override
    public int getCount() {
        return tabCount;
    }*/

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new VisitReceivedFragment();

            case 1:
                return new VisitPendingFragment();

            default:
                return null;


        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
