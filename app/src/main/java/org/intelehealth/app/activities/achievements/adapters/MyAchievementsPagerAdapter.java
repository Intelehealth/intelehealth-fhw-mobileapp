package org.intelehealth.app.activities.achievements.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.intelehealth.app.activities.achievements.fragments.DailyAchievementsFragment;
import org.intelehealth.app.activities.achievements.fragments.DailyAchievementsFragmentNew;
import org.intelehealth.app.activities.achievements.fragments.DateRangeAchievementsFragment;
import org.intelehealth.app.activities.achievements.fragments.DateRangeAchievementsFragmentNew;
import org.intelehealth.app.activities.achievements.fragments.OverallAchievementsFragment;

public class MyAchievementsPagerAdapter extends FragmentStateAdapter {

    int tabCount;
    Context context;

    public MyAchievementsPagerAdapter(FragmentManager fm, int numberOfTabs, Context context) {
        super(fm,((FragmentActivity) context).getLifecycle());
        this.tabCount = numberOfTabs;
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DailyAchievementsFragmentNew();

            case 1:
                return new DateRangeAchievementsFragmentNew();
          /*  case 0:

            default:
                return new OverallAchievementsFragment();*/
            default:
                return new DailyAchievementsFragmentNew();
        }
    }

    @Override
    public int getItemCount() {
        return tabCount;
    }
}