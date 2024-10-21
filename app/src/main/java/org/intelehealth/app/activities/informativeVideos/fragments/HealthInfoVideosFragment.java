package org.intelehealth.app.activities.informativeVideos.fragments;

import android.content.Context;
import android.content.Intent;
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

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.informativeVideos.adapters.HealthInfoMoreVideosAdapter_New;
import org.intelehealth.app.activities.informativeVideos.adapters.HealthInfoMostSearchedVideosAdapter_New;
import org.intelehealth.app.utilities.SessionManager;

import java.util.Locale;
import java.util.Objects;

public class HealthInfoVideosFragment extends Fragment {
    View view;
    public HomeScreenActivity_New activity1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_health_info_videos_ui2, container, false);
        setLocale(getContext());
        initUI();
        return view;
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
        ImageView ivBackArrow = layoutToolbar.findViewById(R.id.iv_hamburger);
        ivBackArrow.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_arrow_back_new));
        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              /*  FragmentManager fm = Objects.requireNonNull(getActivity()).getFragmentManager();
                fm.popBackStack();*/
                Intent intent = new Intent(getActivity(), HomeScreenActivity_New.class);
                startActivity(intent);
            }
        });

        RecyclerView rvSearchedVideos = view.findViewById(R.id.rv_most_searched_info_health);
        RecyclerView rvMoreVideos = view.findViewById(R.id.rv_more_videos_info_health);


        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rvSearchedVideos.setLayoutManager(layoutManager);
        HealthInfoMostSearchedVideosAdapter_New mostSearchedVideosAdapter_new = new HealthInfoMostSearchedVideosAdapter_New(getActivity());
        rvSearchedVideos.setAdapter(mostSearchedVideosAdapter_new);

        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getActivity());
        rvMoreVideos.setLayoutManager(layoutManager1);
        HealthInfoMoreVideosAdapter_New adapter_new = new HealthInfoMoreVideosAdapter_New(getActivity());
        rvMoreVideos.setAdapter(adapter_new);


    }
    @Override
    public void onResume() {
        super.onResume();
        initUI();

    }
}
