package org.intelehealth.app.activities.informativeVideos.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.intelehealth.app.activities.achievements.fragments.DailyAchievementsFragment;
import org.intelehealth.app.activities.achievements.fragments.DateRangeAchievementsFragment;
import org.intelehealth.app.activities.achievements.fragments.OverallAchievementsFragment;
import org.intelehealth.app.activities.informativeVideos.fragments.AboutAppInfoFragment;
import org.intelehealth.app.activities.informativeVideos.fragments.HealthInfoVideosFragment;
import org.intelehealth.app.activities.informativeVideos.fragments.InformativeVideosFragment_New;
import org.intelehealth.app.activities.informativeVideos.fragments.TrainingInfoVideosFragment;

public class InformativeVideosPagerAdapter extends FragmentStateAdapter {

    int tabCount;
    Context context;

    public InformativeVideosPagerAdapter(FragmentManager fm, int numberOfTabs, Context context) {
        super(fm,((FragmentActivity) context).getLifecycle());
        this.tabCount = numberOfTabs;
        this.context = context;
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new TrainingInfoVideosFragment();

            case 2:
                return new AboutAppInfoFragment();
            case 0:

            default:
                return new HealthInfoVideosFragment();
        }

    }

    @Override
    public int getItemCount() {
        return tabCount;
    }
}