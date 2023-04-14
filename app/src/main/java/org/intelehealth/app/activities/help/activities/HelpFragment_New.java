package org.intelehealth.app.activities.help.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class HelpFragment_New extends Fragment implements View.OnClickListener, NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = "HelpFragment";
    View view;
    ImageView ivInternet;
    private ObjectAnimator syncAnimator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_help_ui2, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initUI();

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
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav_home);
        bottomNav.getMenu().findItem(R.id.bottom_nav_help).setChecked(true);
        bottomNav.setVisibility(View.VISIBLE);


        View optionsView = view.findViewById(R.id.layout_buttons_options_help);
        TextView btnAll = optionsView.findViewById(R.id.btn_all_help);
        RecyclerView rvSearchedVideos = view.findViewById(R.id.rv_most_searched_videos);
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
            String message = String.format(getString(R.string.help_whatsapp_string), new SessionManager(getActivity()).getChwname());
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(
                            String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                                    phoneNumber, message))));
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

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rvSearchedVideos.setLayoutManager(layoutManager);
        MostSearchedVideosAdapter_New mostSearchedVideosAdapter_new = new MostSearchedVideosAdapter_New(getActivity(), getVideoList());
        rvSearchedVideos.setAdapter(mostSearchedVideosAdapter_new);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rvFaq.setLayoutManager(linearLayoutManager);
        FAQExpandableAdapter faqExpandableAdapter = new FAQExpandableAdapter(getActivity(), getQuestionsList());
        rvFaq.setAdapter(faqExpandableAdapter);

        btnAll.setOnClickListener(this);

    }

    public List<YoutubeVideoList> getVideoList()
    {
        String[] namesArr = {"<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/TqNiRWOBNTs\" frameborder=\"0\" allowfullscreen></iframe>",
                "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/LCG6eJ0j-Cg\" frameborder=\"0\" allowfullscreen></iframe>",
                "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/qbDHSwMOYg4\" frameborder=\"0\" allowfullscreen></iframe>",
                "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/E0UAHVoqcm0\" frameborder=\"0\" allowfullscreen></iframe>"};
        String[] descArr = {"Treat mild fever at home", "What is Anemia?","Treat cough at home","Benefits of walking"};


        List<YoutubeVideoList> videoList = new ArrayList<>();
        for (int i = 0; i < namesArr.length; i++) {
            YoutubeVideoList youtubeVideoList = new YoutubeVideoList(namesArr[i], descArr[i]);
            videoList.add(youtubeVideoList);
        }

        return videoList;
    }

    public List<QuestionModel> getQuestionsList() {
        String[] namesArr = {"How intelehealth works?", "How intelehealth help patients?", "How to register new patient?",
                "How to add a new visit?", "How to book an appointment?"};
        String[] descArr = {"Intelehealth has developed a comprehensive technology platform that Governments, NGO’s and Hospitals can use to deliver telemedicine-based care to their beneficiaries. Built with powerful features like a digital assistant with 80+ care protocols makes it easy for any organization to use and adapt it to meet their needs!",
                "Our Telemedicine app makes specialist doctor consultations available to the rural populations coming to primary healthcare. Using app, the HWs are able to capture details of patient\'s medication history, diagnostics, prescriptions and treatment. All these details are then shared with the remote doctors to provide consultation. It helps in saving patients from traveling miles for healthcare.",
                "To register a patient, click on the \"Add Patient\" tab on the home scree. Read out the privacy policy to the patient. If they accept, fill out all the details to successfully register a patient. ",
                "Once the patient is registered, on patient details screen, click \"Start Visit\" button to create a new visit for the patient.",
                "Once the patient is registered and the visit is created, on visit summary screen, click \"Appointment\" button. Select the date and time (from available slots) which is suitable to the patient. Click on \"Book Appointment\" to complete booking an appointment with the specialist doctor."};


        List<QuestionModel> questionsList = new ArrayList<>();
        for (int i = 0; i < namesArr.length; i++) {
            QuestionModel questionModel = new QuestionModel(namesArr[i], descArr[i]);
            questionsList.add(questionModel);
        }

        return questionsList;

    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        Log.d(TAG, "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            ivInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));

        } else {
            ivInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

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