package org.intelehealth.app.activities.help.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.help.adapter.FAQExpandableAdapter;
import org.intelehealth.app.activities.help.models.QuestionModel;
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

        FloatingActionButton fabHelp = findViewById(R.id.fab_help_faq);
        fabHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FAQActivity_New.this, ChatSupportHelpActivity_New.class);
                startActivity(intent);
            }
        });
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
        String[] namesArr = {"How intelehealth works?","Why Intelehealth exists?", "How intelehealth help patients?", "How to register new patient?",
                "How to add a new visit?", "How to book an appointment?"};
        String[] descArr = {"Intelehealth has developed a comprehensive technology platform that Governments, NGO’s and Hospitals can use to deliver telemedicine-based care to their beneficiaries. Built with powerful features like a digital assistant with 80+ care protocols makes it easy for any organization to use and adapt it to meet their needs!",
                "Team at Intelehealth has a vision of \"Health for all\". Thus, Intelehealth exists to keep this vision of universal health coverage alive. It strive to achieve that every single citizen in this world should be able to receive the health services they need, when and where they need them, without facing any financial hardship.",
                "Our Telemedicine app makes specialist doctor consultations available to the rural populations coming to primary healthcare. Using app, the HWs are able to capture details of patient\'s medication history, diagnostics, prescriptions and treatment. All these details our then shared with the remote doctors to provide consultation. It helps in saving patients from traveling miles for healthcare.",
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

}