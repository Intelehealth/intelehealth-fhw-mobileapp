package org.intelehealth.app.activities.chooseLanguageActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.IntroActivity.IntroScreensActivity_New;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.utilities.SessionManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = "SplashScreenActivity";
    RecyclerView rvSelectLanguage;
    View layoutLanguage;
    ViewGroup layoutParent;
    ConstraintLayout layoutHeader;
    String appLanguage;
    SessionManager sessionManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screenactivity_ui2);

        sessionManager = new SessionManager(SplashScreenActivity.this);

        rvSelectLanguage = findViewById(R.id.rv_select_language);
        layoutLanguage = findViewById(R.id.layout_panel);
        layoutParent = findViewById(R.id.layout_parent);
        layoutHeader = findViewById(R.id.layout_child1);

        animateViews();
        populatingLanguages();
        saveLanguage();

    }

    private void saveLanguage() {
        appLanguage = sessionManager.getAppLanguage();
        if (!appLanguage.equalsIgnoreCase("")) {
            setLocale(appLanguage);
        }

        Button btnNextToIntro = findViewById(R.id.btn_next_to_intro);
        btnNextToIntro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocale(sessionManager.getAppLanguage());
                if (sessionManager.isFirstTimeLaunch()) {
                    Intent intent = new Intent(SplashScreenActivity.this, IntroScreensActivity_New.class);
                    startActivity(intent);
                    sessionManager.setFirstTimeLaunch(false);
                } else {
                    Intent intent = new Intent(SplashScreenActivity.this, IntroScreensActivity_New.class);
                    intent.putExtra("from", "splash");
                    intent.putExtra("username", "");
                    intent.putExtra("password", "");
                    startActivity(intent);
                }
                finish(); // TODO: uncomment

                // testing...
              /*  sessionManager.setServerUrl(AppConstants.DEMO_URL);
                Intent intent = new Intent(SplashScreenActivity.this, HomeScreenActivity_New.class);
                startActivity(intent); // TODO: remove this block code later.*/
            }
        });

    }


    private void animateViews() {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation translateAnim = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.ui2_new_center_to_top);
                translateAnim.setFillAfter(true);
                translateAnim.setFillEnabled(true);
                translateAnim.setFillBefore(false);
                translateAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        showChooseLanguageUI(true);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                layoutHeader.startAnimation(translateAnim);

            }
        }, 500);


    }

    private void showChooseLanguageUI(boolean show) {
        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(2000);
        transition.addTarget(R.id.layout_panel);

        TransitionManager.beginDelayedTransition(layoutParent, transition);
        layoutLanguage.setVisibility(show ? View.VISIBLE : View.GONE);

    }

    public void populatingLanguages() {
        try {
            List<JSONObject> itemList = new ArrayList<JSONObject>();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", "हिंदी");
            jsonObject.put("code", "hi");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("hi"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "English");
            jsonObject.put("code", "en");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("en"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "ଓଡିଆ");
            jsonObject.put("code", "or");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("or"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "తెలుగు");
            jsonObject.put("code", "te");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("te"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "ગુજરાતી");
            jsonObject.put("code", "gu");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("gu"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "मराठी");
            jsonObject.put("code", "mr");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("mr"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "ಕನ್ನಡ");
            jsonObject.put("code", "kn");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("kn"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "অসমীয়া");
            jsonObject.put("code", "as");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("as"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "മലയാളം");
            jsonObject.put("code", "ml");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("ml"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "русский");
            jsonObject.put("code", "ru");

            jsonObject.put("selected", sessionManager.getAppLanguage().equalsIgnoreCase("ru"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "বাংলা");
            jsonObject.put("code", "bn");

             jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("bn"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "தமிழ்");
            jsonObject.put("code", "ta");

             jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("ta"));
            itemList.add(jsonObject);

            ChooseLanguageAdapterNew languageListAdapter = new ChooseLanguageAdapterNew(SplashScreenActivity.this,
                    itemList, new ItemSelectionListener() {
                @Override
                public void onSelect(JSONObject jsonObject, int index) {
                    try {
                        sessionManager.setAppLanguage(jsonObject.getString("code"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            rvSelectLanguage.setLayoutManager(layoutManager);
            rvSelectLanguage.setItemAnimator(new DefaultItemAnimator());
            rvSelectLanguage.setAdapter(languageListAdapter);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface ItemSelectionListener {
        void onSelect(JSONObject jsonObject, int index);
    }

    public void setLocale(String appLanguage) {
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
}