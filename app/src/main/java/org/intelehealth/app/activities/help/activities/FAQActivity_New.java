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
        String[] namesArr = {getResources().getString(R.string.how_intelehealth_work),getResources().getString(R.string.why_intelehealth_exist), getResources().getString(R.string.how_intelehealth_help), getResources().getString(R.string.how_to_register),
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

}