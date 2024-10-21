package org.intelehealth.app.activities.informativeVideos.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.informativeVideos.adapters.InformativeVideosPagerAdapter;
import org.intelehealth.app.utilities.SessionManager;

import java.util.Locale;
import java.util.Objects;

public class InformativeVideosFragment_New extends Fragment {
    public static final String TAG = "InformativeVideosFragme";
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_informative_videos_ui2, container, false);
        setLocale(getContext());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setLocale(getContext());
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
        View layoutToolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar_home);
        TextView tvLocation = layoutToolbar.findViewById(R.id.tv_user_location_home);
        TextView tvLastSyncApp = layoutToolbar.findViewById(R.id.tv_app_sync_time);
        ImageView ivNotification = layoutToolbar.findViewById(R.id.imageview_notifications_home);
        ImageView ivBackArrow = layoutToolbar.findViewById(R.id.iv_hamburger);
        ivBackArrow.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_arrow_back_new));
        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent(getActivity(), HomeScreenActivity_New.class);
                startActivity(intent);*/
                androidx.fragment.app.FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.popBackStack(TAG,androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
        tvLocation.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        tvLastSyncApp.setVisibility(View.GONE);
        ivNotification.setVisibility(View.GONE);
        tvLocation.setText(getResources().getString(R.string.videos));
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav_home);
        bottomNav.setVisibility(View.GONE);

        configureTabLayout();

    }

    public void configureTabLayout() {
        TabLayout tabLayout = view.findViewById(R.id.tablayout_videos);

        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.health)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.training)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.about_app)));

        ViewPager2 viewPager = view.findViewById(R.id.pager_videos);
        InformativeVideosPagerAdapter adapter = new InformativeVideosPagerAdapter
                (getChildFragmentManager(), 3, getActivity());
        viewPager.setAdapter(adapter);
        int limit = (adapter.getItemCount() > 1 ? adapter.getItemCount() - 1 : 1);

        viewPager.setOffscreenPageLimit(limit);

        //viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0 -> tab.setText(getResources().getString(R.string.health));
                    case 1 -> tab.setText(getResources().getString(R.string.training));
                    default -> tab.setText(getResources().getText(R.string.about_app));
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

}
