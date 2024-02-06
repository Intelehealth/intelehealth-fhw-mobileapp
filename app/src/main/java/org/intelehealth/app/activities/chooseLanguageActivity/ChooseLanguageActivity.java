package org.intelehealth.app.activities.chooseLanguageActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChooseLanguageActivity extends BaseActivity {


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

        appLanguage = sessionManager.getAppLanguage();
        if (!appLanguage.equalsIgnoreCase("")) {
            setLocale(appLanguage);
        }
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
                setLocale(sessionManager.getAppLanguage());
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
//            jsonObject.put("name", "हिंदी");
//            jsonObject.put("code", "hi");
//            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("hi"));
//            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "English");
            jsonObject.put("code", "en");
            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("en"));
            itemList.add(jsonObject);

//            jsonObject = new JSONObject();
//            jsonObject.put("name", "ଓଡିଆ");
//            jsonObject.put("code", "or");
//            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("or"));
//            itemList.add(jsonObject);
//
//            jsonObject = new JSONObject();
//            jsonObject.put("name", "తెలుగు");
//            jsonObject.put("code", "te");
//            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("te"));
//            itemList.add(jsonObject);
//
//            jsonObject = new JSONObject();
//            jsonObject.put("name", "ગુજરાતી");
//            jsonObject.put("code", "gu");
//            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("gu"));
//            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "मराठी");
            jsonObject.put("code", "mr");
            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("mr"));
            itemList.add(jsonObject);

//            jsonObject = new JSONObject();
//            jsonObject.put("name", "Kannada");
//            jsonObject.put("code", "kn");
//            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("kn"));
//            itemList.add(jsonObject);
//
//            jsonObject = new JSONObject();
//            jsonObject.put("name", "Assamese");
//            jsonObject.put("code", "as");
//            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("as"));
//            itemList.add(jsonObject);
//
//            jsonObject = new JSONObject();
//            jsonObject.put("name", "Malyalam");
//            jsonObject.put("code", "ml");
//            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("ml"));
//            itemList.add(jsonObject);
//
//            jsonObject = new JSONObject();
//            jsonObject.put("name", "русский");
//            jsonObject.put("code", "ru");
//            jsonObject.put("selected", sessionManager.getAppLanguage().equalsIgnoreCase("ru"));
//            itemList.add(jsonObject);
//
//            jsonObject = new JSONObject();
//            jsonObject.put("name", "বাংলা");
//            jsonObject.put("code", "bn");
//            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("bn"));
//            itemList.add(jsonObject);
//
//            jsonObject = new JSONObject();
//            jsonObject.put("name", "தமிழ்");
//            jsonObject.put("code", "ta");
//            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("ta"));
//            itemList.add(jsonObject);

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

    public void setLocale(String appLanguage) {
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    public interface ItemSelectionListener {
        void onSelect(JSONObject jsonObject, int index);
    }
}