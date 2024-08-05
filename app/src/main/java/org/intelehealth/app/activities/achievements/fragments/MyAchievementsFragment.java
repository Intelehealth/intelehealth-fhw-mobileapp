package org.intelehealth.app.activities.achievements.fragments;

import android.animation.ObjectAnimator;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import org.intelehealth.app.utilities.CustomLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.achievements.adapters.MyAchievementsPagerAdapter;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.SessionManager;

import java.util.Locale;

public class MyAchievementsFragment extends Fragment implements NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = "MyAchievementsFragmentN";
    View view;
    ImageView ivInternet;
    NetworkUtils networkUtils;
    private ObjectAnimator syncAnimator;

    protected SessionManager sessionManager;
    public UsageStatsManager usageStatsManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale(getContext());
        sessionManager = new SessionManager(requireActivity());
        usageStatsManager = (UsageStatsManager) (requireActivity().getSystemService(Context.USAGE_STATS_SERVICE));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setLocale(getContext());
        view = inflater.inflate(R.layout.fragment_my_achievements_ui2, container, false);
        ((TextView) view.findViewById(R.id.tv_user_level)).setText(String.format(getString(R.string.level), 1));
        networkUtils = new NetworkUtils(getActivity(), this);
        setLocale(getContext());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initUI();
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        context.createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }

    private void initUI() {
        //View layoutToolbar = requireActivity().findViewById(R.id.toolbar_home);
        //layoutToolbar.setVisibility(View.GONE);
        TextView tvTitle = view.findViewById(R.id.tv_achievements_title);
        tvTitle.setText(getResources().getString(R.string.my_achievements));
        ivInternet = view.findViewById(R.id.iv_achievements_internet);

        ivInternet.setOnClickListener(v -> SyncUtils.syncNow(requireActivity(), ivInternet, syncAnimator));
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav_home);
        bottomNav.setVisibility(View.VISIBLE);
        bottomNav.getMenu().findItem(R.id.bottom_nav_achievements).setChecked(true);
        configureTabLayout();

    }

    public void configureTabLayout() {
        TabLayout tabLayout = view.findViewById(R.id.tablayout_achievements);
        tabLayout.removeAllTabs();

        ViewPager2 viewPager = view.findViewById(R.id.pager_achievements);
        MyAchievementsPagerAdapter adapter = new MyAchievementsPagerAdapter(getChildFragmentManager(), 3, getActivity());
        viewPager.setAdapter(adapter);
        int limit = (adapter.getItemCount() > 1 ? adapter.getItemCount() - 1 : 1);

        viewPager.setOffscreenPageLimit(limit);

        //viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0 -> tab.setText(getResources().getString(R.string.overall));
                    case 1 -> tab.setText(getResources().getString(R.string.daily));
                    default -> tab.setText(getResources().getString(R.string.date_range));
                }
            }
        }).attach();


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

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        CustomLog.d(TAG, "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            ivInternet.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ui2_ic_internet_available));

        } else {
            ivInternet.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ui2_ic_no_internet));

        }
    }

    @Override
    public void onStart() {
        super.onStart();

        //register receiver for internet check
        networkUtils.callBroadcastReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}