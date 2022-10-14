package org.intelehealth.app.activities.informativeVideos.adapters;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.intelehealth.app.activities.achievements.fragments.DailyAchievementsFragment;
import org.intelehealth.app.activities.achievements.fragments.DateRangeAchievementsFragment;
import org.intelehealth.app.activities.achievements.fragments.OverallAchievementsFragment;
import org.intelehealth.app.activities.informativeVideos.fragments.AboutAppInfoFragment;
import org.intelehealth.app.activities.informativeVideos.fragments.HealthInfoVideosFragment;
import org.intelehealth.app.activities.informativeVideos.fragments.InformativeVideosFragment_New;
import org.intelehealth.app.activities.informativeVideos.fragments.TrainingInfoVideosFragment;

public class InformativeVideosPagerAdapter extends FragmentPagerAdapter {

    int tabCount;
    Context context;

    public InformativeVideosPagerAdapter(FragmentManager fm, int numberOfTabs, Context context) {
        super(fm);
        this.tabCount = numberOfTabs;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
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
    public int getCount() {
        return tabCount;
    }
}