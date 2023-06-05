package org.intelehealth.ezazi.activities.addNewPatient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Created by Vaghela Mithun R. on 04-06-2023 - 00:01.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class PatientTabPagerAdapter extends FragmentStateAdapter {
    public PatientTabPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) PatientPersonalInfoFragment.getInstance();
        else if (position == 1) PatientAddressInfoFragment.getInstance();
        else if (position == 2) PatientOtherInfoFragment.getInstance();
        return PatientPersonalInfoFragment.getInstance();
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
