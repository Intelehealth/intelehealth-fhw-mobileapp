package org.intelehealth.app.activities.chooseLanguageActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.IntroActivity.IntroActivity;
import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.utilities.LocaleHelper;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class ChooseLanguageActivity extends AppCompatActivity {


    Button SaveButton;
    ImageView BackImage;

    SessionManager sessionManager = null;

    String LOG_TAG = "ChooseLanguageActivity";
    String systemLanguage = Resources.getSystem().getConfiguration().locale.getLanguage();

    String appLanguage;
    private RecyclerView mRecyclerView;
    private List<JSONObject> mItemList = new ArrayList<JSONObject>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_language);
        initViews();

        //this language code is no longer required as we are moving towards more optimised as well as generic code for localisation. Check "attachBaseContext".
//        appLanguage = sessionManager.getAppLanguage();
//        if (!appLanguage.equalsIgnoreCase("")) {
//            setLocale(appLanguage);
//        }

        if (!sessionManager.isFirstTimeLaunch()) {
            BackImage.setVisibility(View.VISIBLE);
            BackImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });

        }

        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                setLocale(sessionManager.getAppLanguage());
                if (sessionManager.isFirstTimeLaunch()) {
                    Logger.logD(LOG_TAG, "Starting setup");
//                    Intent intent = new Intent(ChooseLanguageActivity.this, IntroActivity.class);
                    Intent intent = new Intent(ChooseLanguageActivity.this, IntroActivity.class);
                    startActivity(intent);
                    sessionManager.setFirstTimeLaunch(false);
                } else {
                    Intent intent = new Intent(ChooseLanguageActivity.this, HomeActivity.class);
                    intent.putExtra("from", "splash");
                    intent.putExtra("username", "");
                    intent.putExtra("password", "");
                    startActivity(intent);
                }
                finish();
            }
        });
        populatingLanguages();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    public void initViews() {
        sessionManager = new SessionManager(ChooseLanguageActivity.this);
        mRecyclerView = findViewById(R.id.language_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        SaveButton = findViewById(R.id.save_button);
        BackImage = findViewById(R.id.backButton);

    }

    public void populatingLanguages() {
        try {
            List<JSONObject> itemList = new ArrayList<JSONObject>();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", "عربى");
            jsonObject.put("code", "ar");
            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("ar"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "English");
            jsonObject.put("code", "en");
            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("en"));
            itemList.add(jsonObject);





            LanguageListAdapter languageListAdapter = new LanguageListAdapter(ChooseLanguageActivity.this, itemList, new ItemSelectionListener() {
                @Override
                public void onSelect(JSONObject jsonObject, int index) {
                    try {
                        sessionManager.setAppLanguage(jsonObject.getString("code"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            mRecyclerView.setAdapter(languageListAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //this language code is no longer required as we are moving towards more optimised as well as generic code for localisation. Check "attachBaseContext".
    /*public void setLocale(String appLanguage) {
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }*/

    public interface ItemSelectionListener {
        void onSelect(JSONObject jsonObject, int index);
    }
}