package org.intelehealth.app.activities.help.activities;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.help.adapter.ChatSupportAdapter_New;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;
import org.intelehealth.app.utilities.SessionManager;

import java.util.Locale;

public class ChatSupportHelpActivity_New extends BaseActivity {

    ImageView ivBackArrow, ivIsInternet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_support_help_ui2);

         ivIsInternet = findViewById(R.id.iv_is_internet);
         ivBackArrow = findViewById(R.id.iv_back_arrow_chat);

        if (CheckInternetAvailability.isNetworkAvailable(this)) {
            ivIsInternet.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_internet_available));
        } else {
            ivIsInternet.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_no_internet));

        }

        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        RecyclerView rvChatSupport = findViewById(R.id.rv_chatting);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvChatSupport.setLayoutManager(layoutManager);
        ChatSupportAdapter_New chatSupportAdapter_new = new ChatSupportAdapter_New(this);
        rvChatSupport.setAdapter(chatSupportAdapter_new);
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
}