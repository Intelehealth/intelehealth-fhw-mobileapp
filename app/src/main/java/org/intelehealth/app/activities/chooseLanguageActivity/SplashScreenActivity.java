package org.intelehealth.app.activities.chooseLanguageActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import org.intelehealth.app.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = "SplashScreenActivity";
    private boolean isPanelShown;
    RecyclerView rvSelectLanguage;
    ChooseLanguageAdapterNew.ItemClickListener itemClickListener;
    ChooseLanguageAdapterNew chooseLanguageAdapterNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screenactivity_ui2);
        isPanelShown = false;

        rvSelectLanguage = findViewById(R.id.rv_select_language);
        populatingLanguages();


        //show after 2 seconds
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showChooseLanguageUI(true);
            }
        }, 2000);

    }

    private void showChooseLanguageUI(boolean show) {
        View layoutLanguage = findViewById(R.id.layout_panel);
        ViewGroup parent = findViewById(R.id.layout_parent);
        LinearLayout child = findViewById(R.id.layout_child1);

        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(2000);
        transition.addTarget(R.id.layout_panel);

        TransitionManager.beginDelayedTransition(parent, transition);
        layoutLanguage.setVisibility(show ? View.VISIBLE : View.GONE);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 50, 0, 0);
        child.setLayoutParams(params);



    }

    public void populatingLanguages() {
        try {
            List<JSONObject> itemList = new ArrayList<JSONObject>();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", "हिंदी");
            jsonObject.put("code", "hi");
            //  jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("hi"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "English");
            jsonObject.put("code", "en");
            // jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("en"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "ଓଡିଆ");
            jsonObject.put("code", "or");
            //  jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("or"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "తెలుగు");
            jsonObject.put("code", "te");
            //  jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("te"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "ગુજરાતી");
            jsonObject.put("code", "gu");
            // jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("gu"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "मराठी");
            jsonObject.put("code", "mr");
            //  jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("mr"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "ಕನ್ನಡ");
            jsonObject.put("code", "kn");
            //  jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("kn"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "অসমীয়া");
            jsonObject.put("code", "as");
            //   jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("as"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "മലയാളം");
            jsonObject.put("code", "ml");
            // jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("ml"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "русский");
            jsonObject.put("code", "ru");
            // jsonObject.put("selected", sessionManager.getAppLanguage().equalsIgnoreCase("ru"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "বাংলা");
            jsonObject.put("code", "bn");
            //  jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("bn"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "தமிழ்");
            jsonObject.put("code", "ta");
            // jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("ta"));
            itemList.add(jsonObject);

           /* LanguageListAdapter languageListAdapter = new LanguageListAdapter(SplashScreenActivity.this,
                    itemList, new ChooseLanguageActivity.ItemSelectionListener() {
                @Override
                public void onSelect(JSONObject jsonObject, int index) {
                    try {
                        //sessionManager.setAppLanguage(jsonObject.getString("code"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });*/
            itemClickListener = new ChooseLanguageAdapterNew.ItemClickListener() {
                @Override
                public void onClick(String s, int position) {
                    rvSelectLanguage.post(new Runnable() {
                        @Override
                        public void run() {

                            chooseLanguageAdapterNew.notifyItemChanged(position);
                        }
                    });

                }
            };
            chooseLanguageAdapterNew = new ChooseLanguageAdapterNew(this, itemList, itemClickListener);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            rvSelectLanguage.setLayoutManager(layoutManager);
            rvSelectLanguage.setItemAnimator(new DefaultItemAnimator());
            rvSelectLanguage.setAdapter(chooseLanguageAdapterNew);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}