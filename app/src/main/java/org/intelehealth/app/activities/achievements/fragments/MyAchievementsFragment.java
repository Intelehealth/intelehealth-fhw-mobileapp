package org.intelehealth.app.activities.achievements.fragments;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.achievements.adapters.MyAchievementsPagerAdapter;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;

import java.util.Objects;

public class MyAchievementsFragment extends Fragment {
    private static final String TAG = "MyAchievementsFragmentN";
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_achievements_ui2, container, false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initUI();

    }

    private void initUI() {
        View layoutToolbar = requireActivity().findViewById(R.id.toolbar_home);
        layoutToolbar.setVisibility(View.GONE);
        TextView tvTitle = view.findViewById(R.id.tv_achievements_title);
        tvTitle.setText(getResources().getString(R.string.my_achievements));
        ImageView ivInternet = view.findViewById(R.id.iv_achievements_internet);
        if (CheckInternetAvailability.isNetworkAvailable(getActivity())) {
            ivInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        } else {
            ivInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

        }

        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav_home);
        bottomNav.setVisibility(View.VISIBLE);
        bottomNav.getMenu().findItem(R.id.bottom_nav_achievements).setChecked(true);
        configureTabLayout();

    }

    public void configureTabLayout() {
        TabLayout tabLayout = view.findViewById(R.id.tablayout_achievements);

        tabLayout.addTab(tabLayout.newTab().setText("Overall"));
        tabLayout.addTab(tabLayout.newTab().setText("Daily"));
        tabLayout.addTab(tabLayout.newTab().setText("Date range"));

        ViewPager viewPager = view.findViewById(R.id.pager_achievements);
        PagerAdapter adapter = new MyAchievementsPagerAdapter
                (getChildFragmentManager(), tabLayout.getTabCount(), getActivity());
        viewPager.setAdapter(adapter);
        int limit = (adapter.getCount() > 1 ? adapter.getCount() - 1 : 1);

        viewPager.setOffscreenPageLimit(limit);

        viewPager.addOnPageChangeListener(new
                TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            public void onTabReselected(TabLayout.Tab tab) {

            }

        });
    }

}
