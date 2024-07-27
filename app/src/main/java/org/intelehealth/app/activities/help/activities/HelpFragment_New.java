package org.intelehealth.app.activities.help.activities;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
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

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.help.adapter.FAQExpandableAdapter;
import org.intelehealth.app.activities.help.adapter.MostSearchedVideosAdapter_New;
import org.intelehealth.app.activities.help.models.QuestionModel;
import org.intelehealth.app.activities.help.models.YoutubeVideoList;
import org.intelehealth.app.database.dao.ProviderDAO;
import org.intelehealth.app.models.dto.ProviderDTO;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HelpFragment_New extends Fragment implements View.OnClickListener, NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = "HelpFragment";
    View view;
    ImageView ivInternet;
    private ObjectAnimator syncAnimator;
    private RecyclerView rvSearchedVideos;
    private TextView tvOfflineHintVideosHelpFragment;
    private NetworkUtils networkUtils;
    private LinearLayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_help_ui2, container, false);
        setLocale(getContext());
        initUI();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setLocale(getContext());
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
        //View layoutToolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar_home);
        //TextView tvLocation = layoutToolbar.findViewById(R.id.tv_user_location_home);
        //TextView tvLastSyncApp = layoutToolbar.findViewById(R.id.tv_app_sync_time);
        //ImageView ivNotification = layoutToolbar.findViewById(R.id.imageview_notifications_home);
        //ImageView ivBackArrow = layoutToolbar.findViewById(R.id.iv_hamburger);
        // ivBackArrow.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_arrow_back_new));
        /*ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HomeScreenActivity_New.class);
                startActivity(intent);
            *//*    FragmentManager fm = Objects.requireNonNull(getActivity()).getFragmentManager();
                fm.popBackStack();*//*
            }
        });*/
        //tvLocation.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        //tvLastSyncApp.setVisibility(View.GONE);
        // ivNotification.setVisibility(View.GONE);
      /*  RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ivIsInternet.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        ivIsInternet.setLayoutParams(params);*/
        // tvLocation.setText(getResources().getString(R.string.help));

        networkUtils = new NetworkUtils(getActivity(), this);

        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav_home);
        bottomNav.getMenu().findItem(R.id.bottom_nav_help).setChecked(true);
        bottomNav.setVisibility(View.VISIBLE);


        View optionsView = view.findViewById(R.id.layout_buttons_options_help);
        TextView btnAll = optionsView.findViewById(R.id.btn_all_help);
        rvSearchedVideos = view.findViewById(R.id.rv_most_searched_videos);
        tvOfflineHintVideosHelpFragment = view.findViewById(R.id.tvOfflineHintVideosHelpFragment);
        RecyclerView rvFaq = view.findViewById(R.id.rv_faq1);
        TextView tvMoreVideos = view.findViewById(R.id.tv_more_videos);
        TextView tvMoreFaq = view.findViewById(R.id.tv_faq_more);
        FloatingActionButton fabHelp = view.findViewById(R.id.fab_chat_help);
        ivInternet = view.findViewById(R.id.iv_help_internet);
        ivInternet.setOnClickListener(v -> SyncUtils.syncNow(requireActivity(), ivInternet, syncAnimator));

        fabHelp.setOnClickListener(v -> {
            //Intent intent = new Intent(getActivity(), ChatSupportHelpActivity_New.class);
            //startActivity(intent);

            String phoneNumber = getString(R.string.support_mobile_no_1);
            String providerId = new SessionManager(requireContext()).getProviderID();
            try {
                String nurseName = new ProviderDAO().getProviderName(providerId, ProviderDTO.Columns.PROVIDER_UUID.value);
                String message = String.format(getString(R.string.help_whatsapp_string), nurseName);
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(
                                String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                                        phoneNumber, message))));
            } catch (DAOException e) {
                throw new RuntimeException(e);
            }

        });

        tvMoreVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MostSearchedVideosActivity_New.class);
                startActivity(intent);
            }
        });
        tvMoreFaq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FAQActivity_New.class);
                startActivity(intent);
            }
        });

        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rvSearchedVideos.setLayoutManager(layoutManager);

        if (NetworkConnection.isOnline(getActivity().getApplicationContext())) {
            tvOfflineHintVideosHelpFragment.setVisibility(View.GONE);
            MostSearchedVideosAdapter_New mostSearchedVideosAdapter_new = new MostSearchedVideosAdapter_New(getActivity(), getVideoList());
            rvSearchedVideos.setAdapter(mostSearchedVideosAdapter_new);
        } else {
            tvOfflineHintVideosHelpFragment.setVisibility(View.VISIBLE);
            List<YoutubeVideoList> list = new ArrayList<>();
            MostSearchedVideosAdapter_New mostSearchedVideosAdapter_new = new MostSearchedVideosAdapter_New(getActivity(), list);
            rvSearchedVideos.setAdapter(mostSearchedVideosAdapter_new);
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rvFaq.setLayoutManager(linearLayoutManager);
        FAQExpandableAdapter faqExpandableAdapter = new FAQExpandableAdapter(getActivity(), getQuestionsList());
        rvFaq.setAdapter(faqExpandableAdapter);

        btnAll.setOnClickListener(this);

    }

    public List<YoutubeVideoList> getVideoList() {
        String[] namesArr = {"<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/TqNiRWOBNTs\" frameborder=\"0\" allowfullscreen></iframe>",
                "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/LCG6eJ0j-Cg\" frameborder=\"0\" allowfullscreen></iframe>",
                "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/qbDHSwMOYg4\" frameborder=\"0\" allowfullscreen></iframe>",
                "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/E0UAHVoqcm0\" frameborder=\"0\" allowfullscreen></iframe>"};
        String[] descArr = {getResources().getString(R.string.treat_mild_fever), getResources().getString(R.string.what_is_anemia), getResources().getString(R.string.treat_cough_at_home), getResources().getString(R.string.benefits_of_walking)};


        List<YoutubeVideoList> videoList = new ArrayList<>();
        for (int i = 0; i < namesArr.length; i++) {
            YoutubeVideoList youtubeVideoList = new YoutubeVideoList(namesArr[i], descArr[i]);
            videoList.add(youtubeVideoList);
        }

        return videoList;
    }

    public List<QuestionModel> getQuestionsList() {
        String[] namesArr = {getResources().getString(R.string.how_intelehealth_work), getResources().getString(R.string.why_intelehealth_exist), getResources().getString(R.string.how_intelehealth_help), getResources().getString(R.string.how_to_register),
                getResources().getString(R.string.how_to_add_new_visit), getResources().getString(R.string.how_to_book_an_appointment)};
        String[] descArr = {getResources().getString(R.string.how_intelehealth_work_ans), getResources().getString(R.string.why_intelehealth_exist_ans), getResources().getString(R.string.how_intelehealth_help_ans),
                getResources().getString(R.string.how_to_register_ans), getResources().getString(R.string.how_to_add_new_visit_ans), getResources().getString(R.string.how_to_book_an_appointment_ans)};


        List<QuestionModel> questionsList = new ArrayList<>();
        for (int i = 0; i < namesArr.length; i++) {
            QuestionModel questionModel = new QuestionModel(namesArr[i], descArr[i]);
            questionsList.add(questionModel);
        }

        return questionsList;

    }

    @Override
    public void onStart() {
        super.onStart();
        networkUtils.callBroadcastReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        CustomLog.d(TAG, "updateUIForInternetAvailability: ");
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rvSearchedVideos.setLayoutManager(layoutManager);

        if (isInternetAvailable) {
            rvSearchedVideos.setVisibility(View.VISIBLE);
            tvOfflineHintVideosHelpFragment.setVisibility(View.GONE);
            MostSearchedVideosAdapter_New mostSearchedVideosAdapter_new = new MostSearchedVideosAdapter_New(getActivity(), getVideoList());
            rvSearchedVideos.setAdapter(mostSearchedVideosAdapter_new);

            ivInternet.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ui2_ic_internet_available));

        } else {
            rvSearchedVideos.setVisibility(View.GONE);
            tvOfflineHintVideosHelpFragment.setVisibility(View.VISIBLE);
            List<YoutubeVideoList> list = new ArrayList<>();
            MostSearchedVideosAdapter_New mostSearchedVideosAdapter_new = new MostSearchedVideosAdapter_New(getActivity(), list);
            rvSearchedVideos.setAdapter(mostSearchedVideosAdapter_new);

            ivInternet.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ui2_ic_no_internet));

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_all_help:
                break;
        }
    }
}