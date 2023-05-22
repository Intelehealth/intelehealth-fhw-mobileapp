package org.intelehealth.unicef.activities.visit;

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
