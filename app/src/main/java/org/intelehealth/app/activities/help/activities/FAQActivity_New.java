package org.intelehealth.app.activities.help.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.help.adapter.FAQExpandableAdapter;
import org.intelehealth.app.activities.help.models.QuestionModel;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;
import org.intelehealth.app.utilities.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FAQActivity_New extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faqactivity_ui2);

        handleBackPress();
        initUI();

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(setLocale(newBase));
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
        View toolbar = findViewById(R.id.toolbar_faq);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ImageView ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);

        tvTitle.setText(getResources().getString(R.string.faq));
        if (CheckInternetAvailability.isNetworkAvailable(this)) {
            ivIsInternet.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_internet_available));
        } else {
            ivIsInternet.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_no_internet));

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

    /**
     * removed onBackPressed function due to deprecation
     * and added this one to handle onBackPressed
     */
    private void handleBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
            }
        });
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