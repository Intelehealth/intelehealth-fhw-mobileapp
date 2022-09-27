package org.intelehealth.app.help.fragment;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.help.activities.FAQActivity_New;
import org.intelehealth.app.help.activities.MostSearchedVideosActivity;
import org.intelehealth.app.help.adapter.ExpendableRecyclerViewAdapter;
import org.intelehealth.app.help.adapter.FAQExpandableAdapter;
import org.intelehealth.app.help.adapter.MostSearchedVideosAdapter_New;
import org.intelehealth.app.help.models.Person;
import org.intelehealth.app.help.models.QuestionModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HelpFragment_New extends Fragment implements View.OnClickListener {
    private static final String TAG = "HelpFragment";
    View view;

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
        View layoutToolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar_home);
        TextView tvLocation = layoutToolbar.findViewById(R.id.tv_user_location_home);
        TextView tvLastSyncApp = layoutToolbar.findViewById(R.id.tv_app_sync_time);
        ImageView ivNotification = layoutToolbar.findViewById(R.id.imageview_notifications_home);
        ImageView ivBackArrow = layoutToolbar.findViewById(R.id.iv_hamburger);
        ivBackArrow.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_arrow_back_new));
        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HomeScreenActivity_New.class);
                startActivity(intent);
            /*    FragmentManager fm = Objects.requireNonNull(getActivity()).getFragmentManager();
                fm.popBackStack();*/
            }
        });
        tvLocation.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        tvLastSyncApp.setVisibility(View.GONE);
        ivNotification.setVisibility(View.GONE);
      /*  RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ivIsInternet.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        ivIsInternet.setLayoutParams(params);*/
        tvLocation.setText(getResources().getString(R.string.help));
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav_home);
        bottomNav.getMenu().findItem(R.id.bottom_nav_help).setChecked(true);


        View optionsView = view.findViewById(R.id.layout_buttons_options_help);
        TextView btnAll = optionsView.findViewById(R.id.btn_all_help);
        RecyclerView rvSearchedVideos = view.findViewById(R.id.rv_most_searched_videos);
        RecyclerView rvFaq = view.findViewById(R.id.rv_faq1);
        TextView tvMoreVideos = view.findViewById(R.id.tv_more_videos);
        TextView tvMoreFaq = view.findViewById(R.id.tv_faq_more);


        tvMoreVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MostSearchedVideosActivity.class);
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
        MostSearchedVideosAdapter_New mostSearchedVideosAdapter_new = new MostSearchedVideosAdapter_New(getActivity());
        rvSearchedVideos.setAdapter(mostSearchedVideosAdapter_new);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rvFaq.setLayoutManager(linearLayoutManager);
        FAQExpandableAdapter faqExpandableAdapter = new FAQExpandableAdapter(getActivity(), getQuestionsList());
        rvFaq.setAdapter(faqExpandableAdapter);

        btnAll.setOnClickListener(this);

    }

    public List<QuestionModel> getQuestionsList() {
        String[] namesArr = {"How intelehealth works?", "How intelehealth help patients?", "How to register new patient?",
                "How to add a new visit?", "How to book an appointment?"};
        String[] descArr = {"Telemedicine app makes specialist doctor consultations available to the rural populations coming to primary healthcare . It helps in saving patients from traveling miles for healthcare..Telemedicine app allows Health Officers to collect detailed patient complaints and symptoms and generates a comprehensive clinical case history. The app also allows them to capture details of previous medications, diagnostics, prescriptions and treatment."};

        List<QuestionModel> questionsList = new ArrayList<>();
        for (int i = 0; i < namesArr.length; i++) {
            QuestionModel questionModel = new QuestionModel(namesArr[i], descArr[0]);
            questionsList.add(questionModel);
        }

        return questionsList;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_all_help:
                break;
        }
    }
}