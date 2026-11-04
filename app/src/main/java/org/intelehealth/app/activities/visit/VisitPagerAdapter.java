package org.intelehealth.app.activities.visit;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.intelehealth.app.utilities.PrescriptionLoadingListeners;

/**
 * Created by: Prajwal Waingankar On: 2/Nov/2022
 * Github: prajwalmw
 */
public class VisitPagerAdapter extends FragmentStateAdapter {
    int tabCount;
    Context context;
    PrescriptionLoadingListeners prescriptionLoadingListeners;

    public VisitPagerAdapter(@NonNull FragmentActivity fragmentActivity,PrescriptionLoadingListeners prescriptionLoadingListeners) {
        super(fragmentActivity);
        this.prescriptionLoadingListeners = prescriptionLoadingListeners;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new VisitReceivedFragment(prescriptionLoadingListeners);

            case 1:
                return new VisitPendingFragment(prescriptionLoadingListeners);

            default:
                return null;


        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
