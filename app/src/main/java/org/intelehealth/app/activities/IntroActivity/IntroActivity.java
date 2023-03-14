package org.intelehealth.app.activities.IntroActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.parse.Parse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.activities.setupActivity.LocationArrayAdapter;
import org.intelehealth.app.activities.setupActivity.SetupActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.NewLocationDao;
import org.intelehealth.app.models.Location;
import org.intelehealth.app.models.Results;
import org.intelehealth.app.models.loginModel.LoginModel;
import org.intelehealth.app.models.loginProviderModel.LoginProviderModel;
import org.intelehealth.app.models.statewise_location.Setup_LocationModel;
import org.intelehealth.app.networkApiCalls.ApiClient;
import org.intelehealth.app.networkApiCalls.ApiInterface;
import org.intelehealth.app.utilities.AdminPassword;
import org.intelehealth.app.utilities.Base64Utils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.LocaleHelper;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringEncryption;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.apprtc.data.Manager;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class IntroActivity extends AppCompatActivity {

    Context context;

    private static final String TAG = IntroActivity.class.getSimpleName();
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnSkip, btnNext;
    private List<Location> mLocations = new ArrayList<>();
    Location location = null;
    ProgressDialog progress;
    Base64Utils base64Utils = new Base64Utils();
    String encoded = null;
    UrlModifiers urlModifiers = new UrlModifiers();
    SessionManager sessionManager = null;
    String BASE_URL = "";
    private long createdRecordsCount = 0;
    Map.Entry<String, String> village_name;
    private String selectedState = "", selectedDistrict = "", selectedVillage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        btnNext = findViewById(R.id.btn_next);

        context = IntroActivity.this;
        sessionManager = new SessionManager(this);
        String appLanguage = sessionManager.getAppLanguage();

        if (!appLanguage.equalsIgnoreCase("")) {
            Locale locale = new Locale(appLanguage);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        btnNext.setText(R.string.next);

        BASE_URL = "https://services.sila.care:3004/api/openmrs/";

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        btnSkip = (Button) findViewById(R.id.btn_skip);

        layouts = new int[]{
                R.layout.welcome_slide1,
                R.layout.welcome_slide2
        };

        // adding bottom dots
        addBottomDots(0);

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                launchHomeScreen();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                int current = getItem(+1);
                if (current < layouts.length) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                } else {

                    launchHomeScreen();

                }
            }
        });
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.font_black_4));
            dotsLayout.addView(dots[i]);
        }

        //Changed color of Dot
        if (dots.length > 0)
            dots[currentPage].setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {

//        Intent intent = new Intent(context, LoginActivity.class);
//        startActivity(intent);
//        finish();

    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.start));
                btnNext.setVisibility(View.GONE);
                btnSkip.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
                btnNext.setVisibility(View.VISIBLE);
                btnSkip.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;
        private TextView tvIntroOne;
        private TextView tvIntroTwo;
        private Button button_demo_visit;
        private Button button_custom_visit;


        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            tvIntroOne = findViewById(R.id.tv_intro_one);
            tvIntroTwo = findViewById(R.id.tv_intro_two);
            button_demo_visit = findViewById(R.id.button_demo_visit);
            button_custom_visit = findViewById(R.id.button_custom_visit);

            //Highlighting Text
            String introOne = getString(R.string.arogya_sampada_a_telemedicine_platform);
            String textToHighlightOne = getString(R.string.app_name);
            String newString = introOne.replaceAll(textToHighlightOne, "<font color='blue'>" + textToHighlightOne + "</font>");
            tvIntroOne.setText(Html.fromHtml(newString));

            if (position == 1) {
                String two = getString(R.string.sila_intro_text);
                String textHighlight = getString(R.string.app_name);
                String newhighlight = two.replaceAll(textHighlight,
                                "<font color='blue'>" + textHighlight + "</font>")
                        .replaceAll("\n", "<br>");
                tvIntroTwo.setText(Html.fromHtml(newhighlight));

                button_demo_visit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progress = new ProgressDialog(IntroActivity.this, R.style.AlertDialogStyle);
                        progress.setTitle(getString(R.string.please_wait_progress));
                        progress.setMessage(getString(R.string.logging_in));
                        progress.show();
                        getLocationFromServer(BASE_URL);

                    }
                });

                button_custom_visit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(context, SetupActivity.class);
                        startActivity(intent);
                        finish();

                    }
                });
            }

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    private void getLocationFromServer(String url) {
        ApiClient.changeApiBaseUrl(url);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        try {
            Observable<Setup_LocationModel> resultsObservable = apiService.SETUP_LOCATIONOBSERVABLE();
            resultsObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<Setup_LocationModel>() {
                        @Override
                        public void onNext(Setup_LocationModel location) {
                            if (location.getStates() != null) {
                                try {
                                    NewLocationDao newLocationDao = new NewLocationDao();
                                    newLocationDao.insertSetupLocations(location);
                                    List<String> state_locations = newLocationDao.getStateList(context);
                                    if (state_locations.size() != 0 && state_locations.size() > 1) {
                                        selectedState = state_locations.get(1);
                                        List<String> district_locations = newLocationDao.getDistrictList(state_locations.get(1), context);
                                        if (district_locations.size() != 0 && district_locations.size() > 1) {
                                            selectedDistrict = district_locations.get(1);
                                            List<String> village_locations = newLocationDao.getVillageList(state_locations.get(1), district_locations.get(1), context);
                                            if (village_locations.size() != 0 && village_locations.size() > 1) {
                                                selectedVillage = village_locations.get(1);
                                                HashMap<String, String> hashMap4;
                                                String village_uuid = newLocationDao.getVillageUuid(state_locations.get(1), district_locations.get(1), village_locations.get(1));
                                                hashMap4 = new HashMap<>();
                                                hashMap4.put(village_uuid, village_locations.get(1));
                                                for (Map.Entry<String, String> entry : hashMap4.entrySet()) {
                                                    village_name = entry;
                                                }
                                                TestSetup("services.sila.care", "nurse1", "Nurse@123", "", village_name);
                                            }
                                        }
                                    } else {
                                    }

                                } catch (DAOException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                progress.dismiss();
                                Toast.makeText(IntroActivity.this, getString(R.string.error_location_not_fetched), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            progress.dismiss();
                            Toast.makeText(IntroActivity.this, getString(R.string.error_location_not_fetched), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } catch (IllegalArgumentException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private List<String> getLocationStringList(List<Location> locationList) {
        List<String> list = new ArrayList<String>();
        list.add(getString(R.string.login_location_select));
        for (int i = 0; i < locationList.size(); i++) {
            list.add(locationList.get(i).getDisplay());
        }
        return list;
    }

    /*public void TestSetup(String CLEAN_URL, String USERNAME, String PASSWORD, String ADMIN_PASSWORD, Location location) {

        String urlString = urlModifiers.loginUrl(CLEAN_URL);
        Logger.logD(TAG, "usernaem and password" + USERNAME + PASSWORD);
        encoded = base64Utils.encoded(USERNAME, PASSWORD);
        sessionManager.setEncoded(encoded);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Observable<LoginModel> loginModelObservable = AppConstants.apiInterface.LOGIN_MODEL_OBSERVABLE(urlString, "Basic " + encoded);
        loginModelObservable.subscribe(new Observer<LoginModel>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(LoginModel loginModel) {
                Boolean authencated = loginModel.getAuthenticated();
                Gson gson = new Gson();
                sessionManager.setChwname(loginModel.getUser().getDisplay());
                sessionManager.setCreatorID(loginModel.getUser().getUuid());
                sessionManager.setSessionID(loginModel.getSessionId());
                sessionManager.setProviderID(loginModel.getUser().getPerson().getUuid());
                UrlModifiers urlModifiers = new UrlModifiers();
                String url = urlModifiers.loginUrlProvider(CLEAN_URL, loginModel.getUser().getUuid());
                if (authencated) {
                    Observable<LoginProviderModel> loginProviderModelObservable = AppConstants.apiInterface.LOGIN_PROVIDER_MODEL_OBSERVABLE(url, "Basic " + encoded);
                    loginProviderModelObservable
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DisposableObserver<LoginProviderModel>() {
                                @Override
                                public void onNext(LoginProviderModel loginProviderModel) {
                                    if (loginProviderModel.getResults().size() != 0) {
                                        for (int i = 0; i < loginProviderModel.getResults().size(); i++) {
                                            Log.i(TAG, "doInBackground: " + loginProviderModel.getResults().get(i).getUuid());
                                            sessionManager.setProviderID(loginProviderModel.getResults().get(i).getUuid());
//                                                responsecode = 200;
//                                           final Account account = new Account(USERNAME, "io.intelehealth.openmrs");
//                                            manager.addAccountExplicitly(account, PASSWORD, null);

                                            sessionManager.setLocationName(location.getDisplay());
                                            sessionManager.setLocationUuid(location.getUuid());
                                            sessionManager.setLocationDescription(location.getDescription());
                                            sessionManager.setServerUrl(CLEAN_URL);
                                            sessionManager.setServerUrlRest(BASE_URL);
                                            sessionManager.setServerUrlBase("https://" + CLEAN_URL + "/openmrs");
                                            sessionManager.setBaseUrl(BASE_URL);
                                            sessionManager.setSetupComplete(true);
                                            // keeping the base url in one singleton object for using in apprtc module
                                            Manager.getInstance().setBaseUrl("https://" + sessionManager.getServerUrl());

                                            // OfflineLogin.getOfflineLogin().setUpOfflineLogin(USERNAME, PASSWORD);
                                            AdminPassword.getAdminPassword().setUp(ADMIN_PASSWORD);

                                            Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                                                    .applicationId(AppConstants.IMAGE_APP_ID)
                                                    .server("https://" + CLEAN_URL + ":1337/parse/")
                                                    .build()
                                            );

                                            SQLiteDatabase sqLiteDatabase = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
                                            //SQLiteDatabase read_db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();

                                            sqLiteDatabase.beginTransaction();
                                            //read_db.beginTransaction();
                                            ContentValues values = new ContentValues();

                                            //StringEncryption stringEncryption = new StringEncryption();
                                            String random_salt = getSalt_DATA();

                                            //String random_salt = stringEncryption.getRandomSaltString();
                                            Log.d("salt", "salt: " + random_salt);
                                            //Salt_Getter_Setter salt_getter_setter = new Salt_Getter_Setter();
                                            //salt_getter_setter.setSalt(random`_salt);


                                            String hash_password = null;
                                            try {
                                                //hash_email = StringEncryption.convertToSHA256(random_salt + mEmail);
                                                hash_password = StringEncryption.convertToSHA256(random_salt + PASSWORD);
                                            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                                                FirebaseCrashlytics.getInstance().recordException(e);
                                            }

                                            try {
                                                values.put("username", USERNAME);
                                                values.put("password", hash_password);
                                                values.put("creator_uuid_cred", loginModel.getUser().getUuid());
                                                values.put("chwname", loginModel.getUser().getDisplay());
                                                values.put("provider_uuid_cred", sessionManager.getProviderID());
                                                createdRecordsCount = sqLiteDatabase.insertWithOnConflict("tbl_user_credentials", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                                sqLiteDatabase.setTransactionSuccessful();

                                                Logger.logD("values", "values" + values);
                                                Logger.logD("created user credentials", "create user records" + createdRecordsCount);
                                            } catch (SQLException e) {
                                                Log.d("SQL", "SQL user credentials: " + e);
                                            } finally {
                                                sqLiteDatabase.endTransaction();
                                            }

                                            Log.i(TAG, "onPostExecute: Parse init");
                                            Intent intent = new Intent(IntroActivity.this, HomeActivity.class);
                                            intent.putExtra("setup", true);
                                            sessionManager.setTriggerNoti("no");
                                            startActivity(intent);
                                            finish();
                                            progress.dismiss();
                                        }
                                    }

                                }

                                @Override
                                public void onError(Throwable e) {
                                    Logger.logD(TAG, "handle provider error" + e.getMessage());
                                    progress.dismiss();
                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                }
            }

            @Override
            public void onError(Throwable e) {
                Logger.logD(TAG, "Login Failure" + e.getMessage());
                progress.dismiss();
                DialogUtils dialogUtils = new DialogUtils();
                dialogUtils.showerrorDialog(IntroActivity.this, "Error Login", getString(R.string.error_incorrect_password), "ok");
            }

            @Override
            public void onComplete() {
                Logger.logD(TAG, "completed");
            }
        });


    }*/


    /**
     * Attempts login to the OpenMRS server.
     * If successful cretes a new {@link Account}
     * If unsuccessful details are saved in SharedPreferences.
     */
    public void TestSetup(String CLEAN_URL, String USERNAME, String PASSWORD, String ADMIN_PASSWORD, Map.Entry<String, String> location) {

        String urlString = urlModifiers.loginUrl(CLEAN_URL);
        Logger.logD(TAG, "usernaem and password" + USERNAME + PASSWORD);
        encoded = base64Utils.encoded(USERNAME, PASSWORD);
        sessionManager.setEncoded(encoded);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Observable<LoginModel> loginModelObservable = AppConstants.apiInterface.LOGIN_MODEL_OBSERVABLE(urlString, "Basic " + encoded);
        loginModelObservable.subscribe(new Observer<LoginModel>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(LoginModel loginModel) {
                Boolean authencated = loginModel.getAuthenticated();
                Gson gson = new Gson();
                sessionManager.setChwname(loginModel.getUser().getDisplay());
                sessionManager.setCreatorID(loginModel.getUser().getUuid());
                sessionManager.setSessionID(loginModel.getSessionId());
                sessionManager.setProviderID(loginModel.getUser().getPerson().getUuid());
                UrlModifiers urlModifiers = new UrlModifiers();
                String url = urlModifiers.loginUrlProvider(CLEAN_URL, loginModel.getUser().getUuid());
                if (authencated) {
                    Observable<LoginProviderModel> loginProviderModelObservable = AppConstants.apiInterface.LOGIN_PROVIDER_MODEL_OBSERVABLE(url, "Basic " + encoded);
                    loginProviderModelObservable
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DisposableObserver<LoginProviderModel>() {
                                @Override
                                public void onNext(LoginProviderModel loginProviderModel) {
                                    if (loginProviderModel.getResults().size() != 0) {
                                        for (int i = 0; i < loginProviderModel.getResults().size(); i++) {
                                            Log.i(TAG, "doInBackground: " + loginProviderModel.getResults().get(i).getUuid());
                                            sessionManager.setProviderID(loginProviderModel.getResults().get(i).getUuid());
//                                                responsecode = 200;
                                          /*  final Account account = new Account(USERNAME, "io.intelehealth.openmrs");
                                            manager.addAccountExplicitly(account, PASSWORD, null);*/

                                            sessionManager.setLocationName(location.getValue());
                                            sessionManager.setLocationUuid(location.getKey());
                                            //  sessionManager.setLocationDescription(location.getDescription());
                                            sessionManager.setServerUrl(CLEAN_URL);
                                            sessionManager.setServerUrlRest(BASE_URL);
                                            sessionManager.setServerUrlBase("https://" + CLEAN_URL + "/openmrs");
                                            sessionManager.setBaseUrl(BASE_URL);
                                            sessionManager.setSetupComplete(true);

                                            //Storing State Name
                                            sessionManager.setCountryName("Syria");
                                            sessionManager.setStateName(selectedState);
                                            sessionManager.setDistrictName(selectedDistrict);
                                            sessionManager.setVillageName(selectedVillage);

                                            // OfflineLogin.getOfflineLogin().setUpOfflineLogin(USERNAME, PASSWORD);
                                            AdminPassword.getAdminPassword().setUp(ADMIN_PASSWORD);

                                            Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                                                    .applicationId(AppConstants.IMAGE_APP_ID)
                                                    .server("https://" + CLEAN_URL + ":1337/parse/")
                                                    .build()
                                            );

                                            SQLiteDatabase sqLiteDatabase = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
                                            //SQLiteDatabase read_db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();

                                            sqLiteDatabase.beginTransaction();
                                            //read_db.beginTransaction();
                                            ContentValues values = new ContentValues();

                                            //StringEncryption stringEncryption = new StringEncryption();
                                            String random_salt = getSalt_DATA();

                                            //String random_salt = stringEncryption.getRandomSaltString();
                                            Log.d("salt", "salt: " + random_salt);
                                            //Salt_Getter_Setter salt_getter_setter = new Salt_Getter_Setter();
                                            //salt_getter_setter.setSalt(random`_salt);


                                            String hash_password = null;
                                            try {
                                                //hash_email = StringEncryption.convertToSHA256(random_salt + mEmail);
                                                hash_password = StringEncryption.convertToSHA256(random_salt + PASSWORD);
                                            } catch (NoSuchAlgorithmException |
                                                     UnsupportedEncodingException e) {
                                                FirebaseCrashlytics.getInstance().recordException(e);
                                            }

                                            try {
                                                values.put("username", USERNAME);
                                                values.put("password", hash_password);
                                                values.put("creator_uuid_cred", loginModel.getUser().getUuid());
                                                values.put("chwname", loginModel.getUser().getDisplay());
                                                values.put("provider_uuid_cred", sessionManager.getProviderID());
                                                createdRecordsCount = sqLiteDatabase.insertWithOnConflict("tbl_user_credentials", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                                sqLiteDatabase.setTransactionSuccessful();

                                                Logger.logD("values", "values" + values);
                                                Logger.logD("created user credentials", "create user records" + createdRecordsCount);
                                            } catch (SQLException e) {
                                                Log.d("SQL", "SQL user credentials: " + e);
                                            } finally {
                                                sqLiteDatabase.endTransaction();
                                            }

                                            Log.i(TAG, "onPostExecute: Parse init");
                                            Intent intent = new Intent(IntroActivity.this, HomeActivity.class);
                                            intent.putExtra("setup", true);
                                            sessionManager.setTriggerNoti("no");
                                            startActivity(intent);
                                            finish();
                                            progress.dismiss();
                                        }
                                    }

                                }

                                @Override
                                public void onError(Throwable e) {
                                    Logger.logD(TAG, "handle provider error" + e.getMessage());
                                    progress.dismiss();
                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                }
            }

            @Override
            public void onError(Throwable e) {
                Logger.logD(TAG, "Login Failure" + e.getMessage());
                progress.dismiss();
            }

            @Override
            public void onComplete() {
                Logger.logD(TAG, "completed");
            }
        });


    }

    public String getSalt_DATA() {
        BufferedReader reader = null;
        String salt = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("salt.env")));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                salt = mLine;
                Log.d("SA", "SA " + salt);
            }
        } catch (Exception e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    //log the exception
                }
            }
        }
        return salt;

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

}