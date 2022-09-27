package org.intelehealth.app.help.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.help.adapter.FAQExpandableAdapter;
import org.intelehealth.app.help.models.QuestionModel;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;

import java.util.ArrayList;
import java.util.List;

public class FAQActivity_New extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faqactivity_ui2);

        initUI();

    }

    private void initUI() {
        View toolbar = findViewById(R.id.toolbar_faq);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ImageView ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);

        tvTitle.setText(getResources().getString(R.string.faq));
        if (CheckInternetAvailability.isNetworkAvailable(this)) {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        } else {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

        }

        RecyclerView rvFaq = findViewById(R.id.rv_faq1);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvFaq.setLayoutManager(linearLayoutManager);
        FAQExpandableAdapter faqExpandableAdapter = new FAQExpandableAdapter(this, getQuestionsList());
        rvFaq.setAdapter(faqExpandableAdapter);

    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
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

}