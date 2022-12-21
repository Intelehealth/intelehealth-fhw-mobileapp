package org.intelehealth.app.activities.chooseLanguageActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.IntroActivity.IntroActivity;
import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.app.IntelehealthApplication;
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
    Toolbar mToolbar;
    SessionManager sessionManager = null;
    String LOG_TAG = "ChooseLanguageActivity";
    String systemLanguage = Resources.getSystem().getConfiguration().locale.getLanguage();
    String appLanguage;
    private RecyclerView mRecyclerView;
    private List<JSONObject> mItemList = new ArrayList<JSONObject>();
    Intent intent;
    String intentType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_language);
        initViews();

        /*if (!sessionManager.isFirstTimeLaunch()) {
            BackImage.setVisibility(View.VISIBLE);
            BackImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });

        }*/

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

    public void saveLocale(String lang) {
        String langPref = "Language";
        SharedPreferences prefs = this.getSharedPreferences("Intelehealth", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(langPref, lang);
        prefs.getAll();
        editor.apply();

        SessionManager sessionManager =  new SessionManager(ChooseLanguageActivity.this);
        sessionManager.setCurrentLang(lang);
        sessionManager.setAppLanguage(lang);

    }

    /*@Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }*/

    public void initViews() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        mToolbar.setTitleTextColor(Color.WHITE);
        sessionManager = new SessionManager(ChooseLanguageActivity.this);
        appLanguage = sessionManager.getAppLanguage();
        setLocale(appLanguage);
        mRecyclerView = findViewById(R.id.language_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        SaveButton = findViewById(R.id.save_button);
        BackImage = findViewById(R.id.backButton);
        intent = getIntent();
        intentType = intent.getStringExtra("intentType");

        if(intentType.equalsIgnoreCase("home")) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            setTitle(R.string.languages);
        }
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
    public void setLocale(String appLanguage) {
        final Locale myLocale = new Locale(appLanguage);
        Locale.setDefault(myLocale);
        saveLocale(appLanguage);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    public interface ItemSelectionListener {
        void onSelect(JSONObject jsonObject, int index);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

}