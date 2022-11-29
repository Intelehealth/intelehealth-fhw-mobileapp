package org.intelehealth.app.activities.settingsActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.chooseLanguageActivity.SplashScreenActivity;
import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.setupActivity.SetupActivity;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.DownloadMindMapRes;
import org.intelehealth.app.networkApiCalls.ApiClient;
import org.intelehealth.app.networkApiCalls.ApiInterface;
import org.intelehealth.app.utilities.DownloadMindMaps;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;

import java.io.File;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class Language_ProtocolsActivity extends AppCompatActivity {
    private Spinner lang_spinner;
    private ImageButton reset_btn, update_protocols_btn;
    private String selected_language = "English";
    private Context context;
    private ArrayAdapter<String> langAdapter;
    private SessionManager sessionManager = null;
    private CardView snackbar_cv;
    private TextView snackbar_text;
    String key = null;
    String licenseUrl = null;
  //  private CustomProgressDialog customProgressDialog;
    private DownloadMindMaps mTask;
    private ProgressDialog mProgressDialog;
    private String mindmapURL = "";
    private AlertDialog alertDialog;
    private String appLanguage;
    EditText text, url;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_protocols);

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        initUI();
        clickListeners();
    }

    private void initUI() {
        context = Language_ProtocolsActivity.this;
        sessionManager = new SessionManager(context);
        lang_spinner = findViewById(R.id.lang_spinner);
        reset_btn = findViewById(R.id.reset_btn);
        snackbar_cv = findViewById(R.id.snackbar_cv);
        snackbar_text = findViewById(R.id.snackbar_text);
        update_protocols_btn = findViewById(R.id.update_protocols_btn);
    }

    private void clickListeners() {
        // language spinner - start
        langAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.language_names));
        lang_spinner.setAdapter(langAdapter); // setting up language spinners.

        String l = sessionManager.getCurrentLang();
        if (l.equalsIgnoreCase("en"))
            l = "English";
        if (l.equalsIgnoreCase("hi"))
            l = "हिंदी";

        int i = langAdapter.getPosition(l);
        if (!l.equalsIgnoreCase(""))
            lang_spinner.setSelection(langAdapter.getPosition(l));
        else
            lang_spinner.setSelection(langAdapter.getPosition("English"));

        lang_spinner.setSelection(i,false);
        lang_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                if (index >= 0) {
                    selected_language = adapterView.getItemAtPosition(index).toString();
                    Log.v("Langauge", "selection: " + selected_language);
                    String message = "Are you sure you want to change language to " + selected_language + "?";
                    dialog(context, getResources().getDrawable(R.drawable.close_patient_svg), "Change language?",
                            message, "Yes", "No", false);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Reset button.
        reset_btn.setOnClickListener(v -> {
            sessionManager.setAppLanguage("en");
            lang_spinner.setSelection(0, false);
          //  showSnackBarAndRemoveLater("Language successfully changed to English!");
        });

        // language spinner - end

        // update protocols - start
        update_protocols_btn.setOnClickListener(v -> {
            updateProtocols();
        });
        // update protocols - end
    }

    private void updateProtocols() {
        if (NetworkConnection.isOnline(Language_ProtocolsActivity.this)) {
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(Language_ProtocolsActivity.this);
            LayoutInflater li = LayoutInflater.from(Language_ProtocolsActivity.this);
            View promptsView = li.inflate(R.layout.dialog_mindmap_cred, null);
            text = promptsView.findViewById(R.id.licensekey);
            url = promptsView.findViewById(R.id.licenseurl);

            if (!sessionManager.getLicenseKey().isEmpty()) {

                text.setText(sessionManager.getLicenseKey());
                url.setText(sessionManager.getMindMapServerUrl());

            } else {
                url.setText("");
                text.setText("");
            }

            dialog.setTitle(getString(R.string.enter_license_key))
                    .setView(promptsView)
                    .setPositiveButton(getString(R.string.button_ok), null)
                    .setNegativeButton(getString(R.string.button_cancel), null);

            AlertDialog alertDialog = dialog.create();
            alertDialog.setView(promptsView, 20, 0, 20, 0);
            alertDialog.show();
            alertDialog.setCanceledOnTouchOutside(false); //dialog wont close when clicked outside...


            // Get the alert dialog buttons reference
            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            // Change the alert dialog buttons text and background color
            positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));

            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                                /* text = promptsView.findViewById(R.id.licensekey);
                                 url = promptsView.findViewById(R.id.licenseurl);*/

                    url.setError(null);
                    text.setError(null);

                    //If both are not entered...
                    if (url.getText().toString().trim().isEmpty() && text.getText().toString().trim().isEmpty()) {
                        url.requestFocus();
                        url.setError(getResources().getString(R.string.enter_server_url));
                        text.setError(getResources().getString(R.string.enter_license_key));
                        return;
                    }

                    //If Url is empty...key is not empty...
                    if (url.getText().toString().trim().isEmpty() && !text.getText().toString().trim().isEmpty()) {
                        url.requestFocus();
                        url.setError(getResources().getString(R.string.enter_server_url));
                        return;
                    }

                    //If Url is not empty...key is empty...
                    if (!url.getText().toString().trim().isEmpty() && text.getText().toString().trim().isEmpty()) {
                        text.requestFocus();
                        text.setError(getResources().getString(R.string.enter_license_key));
                        return;
                    }

                    //If Url has : in it...
                    if (url.getText().toString().trim().contains(":")) {
                        url.requestFocus();
                        url.setError(getResources().getString(R.string.invalid_url));
                        return;
                    }

                    //If url entered is Invalid...
                    if (!url.getText().toString().trim().isEmpty()) {
                        if (Patterns.WEB_URL.matcher(url.getText().toString().trim()).matches()) {
                            String url_field = "https://" + url.getText().toString() + ":3004/";
                            if (URLUtil.isValidUrl(url_field)) {
                                key = text.getText().toString().trim();
                                licenseUrl = url.getText().toString().trim();

                                sessionManager.setMindMapServerUrl(licenseUrl);

                                sessionManager.setLicenseKey(key);

                                if (keyVerified(key)) {
                                    getMindmapDownloadURL("https://" + licenseUrl + ":3004/", key);
                                    alertDialog.dismiss();
                                }
                            } else {
                                Toast.makeText(Language_ProtocolsActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            //invalid url || invalid url and key.
                            Toast.makeText(Language_ProtocolsActivity.this, R.string.invalid_url, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Language_ProtocolsActivity.this, R.string.please_enter_url_and_key, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

            IntelehealthApplication.setAlertDialogCustomTheme(Language_ProtocolsActivity.this, alertDialog);

//                      }

        } else {
            Toast.makeText(context, getString(R.string.mindmap_internect_connection), Toast.LENGTH_SHORT).show();
        }
/*
        if (NetworkConnection.isOnline(this)) {
                MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
                LayoutInflater li = LayoutInflater.from(this);
                View promptsView = li.inflate(R.layout.dialog_mindmap_cred, null);

                dialog.setTitle(getString(R.string.enter_license_key))
                        .setView(promptsView)
                        .setPositiveButton(getString(R.string.button_ok), null)
                        .setNegativeButton(getString(R.string.button_cancel), null);

                AlertDialog alertDialog = dialog.create();
                alertDialog.setView(promptsView, 20, 0, 20, 0);
                alertDialog.show();
                alertDialog.setCanceledOnTouchOutside(false); //dialog wont close when clicked outside...

                // Get the alert dialog buttons reference
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                // Change the alert dialog buttons text and background color
                positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));

                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText text = promptsView.findViewById(R.id.licensekey);
                        EditText url = promptsView.findViewById(R.id.licenseurl);

                        url.setError(null);
                        text.setError(null);

                        //If both are not entered...
                        if (url.getText().toString().trim().isEmpty() && text.getText().toString().trim().isEmpty()) {
                            url.requestFocus();
                            url.setError(getResources().getString(R.string.enter_server_url));
                            text.setError(getResources().getString(R.string.enter_license_key));
                            return;
                        }

                        //If Url is empty...key is not empty...
                        if (url.getText().toString().trim().isEmpty() && !text.getText().toString().trim().isEmpty()) {
                            url.requestFocus();
                            url.setError(getResources().getString(R.string.enter_server_url));
                            return;
                        }

                        //If Url is not empty...key is empty...
                        if (!url.getText().toString().trim().isEmpty() && text.getText().toString().trim().isEmpty()) {
                            text.requestFocus();
                            text.setError(getResources().getString(R.string.enter_license_key));
                            return;
                        }

                        //If Url has : in it...
                        if (url.getText().toString().trim().contains(":")) {
                            url.requestFocus();
                            url.setError(getResources().getString(R.string.invalid_url));
                            return;
                        }

                        //If url entered is Invalid...
                        if (!url.getText().toString().trim().isEmpty()) {
                            if (Patterns.WEB_URL.matcher(url.getText().toString().trim()).matches()) {
                                String url_field = "https://" + url.getText().toString() + ":3004/";
                                if (URLUtil.isValidUrl(url_field)) {
                                    key = text.getText().toString().trim();
                                    licenseUrl = url.getText().toString().trim();
                                    sessionManager.setMindMapServerUrl(licenseUrl);

                                    if (keyVerified(key)) {
                                        getMindmapDownloadURL("https://" + licenseUrl + ":3004/", key);
                                        alertDialog.dismiss();
                                    }
                                } else {
                                    Toast.makeText(context, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                //invalid url || invalid url and key.
                                Toast.makeText(context, R.string.invalid_url, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, R.string.please_enter_url_and_key, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
        }
*/
    }


    // Dialog - start
    public void dialog(Context context, Drawable drawable, String title, String subTitle,
                                                 String positiveBtnTxt, String negativeBtnTxt, boolean toDisable) {

        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_patient_registration, null);
        alertdialogBuilder.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.dialog_icon);
        TextView dialog_title = convertView.findViewById(R.id.dialog_title);
        TextView dialog_subtitle = convertView.findViewById(R.id.dialog_subtitle);
        Button positive_btn = convertView.findViewById(R.id.positive_btn);
        Button negative_btn = convertView.findViewById(R.id.negative_btn);

        icon.setImageDrawable(drawable);
        dialog_title.setText(title);
        dialog_subtitle.setText(subTitle);
        positive_btn.setText(positiveBtnTxt);
        negative_btn.setText(negativeBtnTxt);

        if (toDisable) {
            positive_btn.setVisibility(View.GONE);
            negative_btn.setVisibility(View.GONE);
        }
        else {
            positive_btn.setVisibility(View.VISIBLE);
            negative_btn.setVisibility(View.VISIBLE);
        }

        alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        negative_btn.setOnClickListener(v -> {
            lang_spinner.setSelection(langAdapter.getPosition("English"));
            alertDialog.dismiss();
        });

        positive_btn.setOnClickListener(v -> {
            // setting app language here...
            if (!selected_language.equalsIgnoreCase("")) {
                String locale = selected_language;
                if (locale.equalsIgnoreCase("English"))
                    locale = "en";
                if (locale.equalsIgnoreCase("हिंदी"))
                    locale = "hi";

                setLocale(locale, selected_language);
            }
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    // Dialog - end

    /**
     * Setting the language selected by user as app language.
     * @param // appLanguage
     */
    public void setLocale(String locale_code, String language) {
        sessionManager.setAppLanguage(locale_code);
        // here comes en, hi, mr
        Locale locale = new Locale(locale_code);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        // show snackbar view
        showSnackBarAndRemoveLater("Language successfully changed to " + language + "!");
    }

    // show snackbar
    private void showSnackBarAndRemoveLater(String appLanguage) {
        snackbar_cv.setVisibility(View.VISIBLE);
        snackbar_text.setText(appLanguage);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                snackbar_cv.setVisibility(View.GONE);
            }
        }, 4000);
    }

    private boolean keyVerified(String key) {
        //TODO: Verify License Key
        return true;
    }

    private void getMindmapDownloadURL(String url, String key) {
       // customProgressDialog.show();
        dialog(context, getResources().getDrawable(R.drawable.ui2_icon_logging_in),
                "Changing protocols", "Please wait while the protocols are being changed.",
                "Yes", "No", true);

        ApiClient.changeApiBaseUrl(url);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        try {
            Observable<DownloadMindMapRes> resultsObservable = apiService.DOWNLOAD_MIND_MAP_RES_OBSERVABLE(key);
            resultsObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<DownloadMindMapRes>() {
                        @Override
                        public void onNext(DownloadMindMapRes res) {
                          //  customProgressDialog.dismiss();
                            if (res.getMessage() != null && res.getMessage().equalsIgnoreCase("Success")) {

                                Log.e("MindMapURL", "Successfully get MindMap URL");
                                mTask = new DownloadMindMaps(context, alertDialog,"home", true);
                                mindmapURL = res.getMindmap().trim();
                                sessionManager.setLicenseKey(key);
                                /**
                                 * Showing snackbar custom view on success of Protocols udpated...
                                 */
                                showSnackBarAndRemoveLater("Protocols have been successfully changed!");
                                checkExistingMindMaps();
                              //  alertDialog.dismiss();

                            } else {
                                Toast.makeText(context, getResources().getString(R.string.no_protocols_found), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                          //  customProgressDialog.dismiss();
                            alertDialog.dismiss();
                            Toast.makeText(context, getResources().getString(R.string.unable_to_get_proper_response), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } catch (IllegalArgumentException e) {
            Log.e("TAG", "changeApiBaseUrl: " + e.getMessage());
            Log.e("TAG", "changeApiBaseUrl: " + e.getStackTrace());
        }
    }

//    private void getMindmapDownloadURL(String url, String key) {
//      //  customProgressDialog.show();
//        dialog(context, getResources().getDrawable(R.drawable.ui2_icon_logging_in),
//                "Changing protocols", "Please wait while the protocols are being changed.",
//                "Yes", "No", true);
//
//        ApiClient.changeApiBaseUrl(url);
//        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
//        try {
//            Observable<DownloadMindMapRes> resultsObservable = apiService.DOWNLOAD_MIND_MAP_RES_OBSERVABLE(key);
//            resultsObservable
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new DisposableObserver<DownloadMindMapRes>() {
//                        @Override
//                        public void onNext(DownloadMindMapRes res) {
//                          //  customProgressDialog.dismiss();
//                            alertDialog.dismiss();
//                            if (res.getMessage() != null && res.getMessage().equalsIgnoreCase("Success")) {
//                                Log.e("MindMapURL", "Successfully get MindMap URL");
//                                mTask = new DownloadMindMaps(context, mProgressDialog,"setup");
//                                mindmapURL = res.getMindmap().trim();
//                                sessionManager.setLicenseKey(key);
//                                /**
//                                 * Showing snackbar custom view on success of Protocols udpated...
//                                  */
//                                showSnackBarAndRemoveLater("Protocols have been successfully changed!");
//                                checkExistingMindMaps();
//
//                            } else {
////                                Toast.makeText(context, res.getMessage(), Toast.LENGTH_LONG).show();
//                                Toast.makeText(context, getResources().getString(R.string.no_protocols_found), Toast.LENGTH_LONG).show();
//                            }
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                          //  customProgressDialog.dismiss();
//                            alertDialog.dismiss();
//                            Log.e("MindMapURL", " " + e);
//                            Toast.makeText(context, getResources().getString(R.string.unable_to_get_proper_response), Toast.LENGTH_LONG).show();
//                        }
//
//                        @Override
//                        public void onComplete() {
//
//                        }
//                    });
//        } catch (IllegalArgumentException e) {
//            Log.e("TAG", "changeApiBaseUrl: " + e.getMessage());
//            Log.e("TAG", "changeApiBaseUrl: " + e.getStackTrace());
//        }
//    }

    private void showProgressbar() {
        mProgressDialog = new ProgressDialog(Language_ProtocolsActivity.this);
        mProgressDialog.setMessage(getString(R.string.download_protocols));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
    }

    private void checkExistingMindMaps() {
        //Check is there any existing mindmaps are present, if yes then delete.
        File engines = new File(context.getFilesDir().getAbsolutePath(), "/Engines");
        Log.e("TAG", "Engines folder=" + engines.exists());
        if (engines.exists()) {
            engines.delete();
        }
        File logo = new File(context.getFilesDir().getAbsolutePath(), "/logo");
        Log.e("TAG", "Logo folder=" + logo.exists());
        if (logo.exists()) {
            logo.delete();
        }
        File physicalExam = new File(context.getFilesDir().getAbsolutePath() + "/physExam.json");
        Log.e("TAG", "physExam.json=" + physicalExam.exists());
        if (physicalExam.exists()) {
            physicalExam.delete();
        }
        File familyHistory = new File(context.getFilesDir().getAbsolutePath() + "/famHist.json");
        Log.e("TAG", "famHist.json=" + familyHistory.exists());
        if (familyHistory.exists()) {
            familyHistory.delete();
        }
        File pastMedicalHistory = new File(context.getFilesDir().getAbsolutePath() + "/patHist.json");
        Log.e("TAG", "patHist.json=" + pastMedicalHistory.exists());
        if (pastMedicalHistory.exists()) {
            pastMedicalHistory.delete();
        }
        File config = new File(context.getFilesDir().getAbsolutePath() + "/config.json");
        Log.e("TAG", "config.json=" + config.exists());
        if (config.exists()) {
            config.delete();
        }

        //Start downloading mindmaps
        mTask.execute(mindmapURL, context.getFilesDir().getAbsolutePath() + "/mindmaps.zip");
        Log.e("DOWNLOAD", "isSTARTED");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}