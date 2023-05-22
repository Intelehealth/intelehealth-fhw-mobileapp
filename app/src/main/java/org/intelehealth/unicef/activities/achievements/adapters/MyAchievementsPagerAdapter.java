package org.intelehealth.unicef.activities.achievements.adapters;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.intelehealth.unicef.activities.achievements.fragments.DailyAchievementsFragment;
import org.intelehealth.unicef.activities.achievements.fragments.DateRangeAchievementsFragment;
import org.intelehealth.unicef.activities.achievements.fragments.OverallAchievementsFragment;

public class MyAchievementsPagerAdapter extends FragmentPagerAdapter {

    int tabCount;
    Context context;

    public MyAchievementsPagerAdapter(FragmentManager fm, int numberOfTabs, Context context) {
        super(fm);
        this.tabCount = numberOfTabs;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return new DailyAchievementsFragment();

            case 2:
                return new DateRangeAchievementsFragment();
            case 0:

            default:
                return new OverallAchievementsFragment();
        }

    }

    @Override
    public int getCount() {
        return tabCount;
    }
}