package org.intelehealth.ekalarogya.activities.chmProfileActivity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.database.dao.ImagesDAO;
import org.intelehealth.ekalarogya.database.dao.PatientsDAO;
import org.intelehealth.ekalarogya.models.UserProfileModel.HwPersonalInformationModel;
import org.intelehealth.ekalarogya.models.UserProfileModel.HwProfileModel;
import org.intelehealth.ekalarogya.models.UserProfileModel.MainProfileModel;
import org.intelehealth.ekalarogya.utilities.DownloadFilesUtils;
import org.intelehealth.ekalarogya.utilities.Logger;
import org.intelehealth.ekalarogya.utilities.NetworkConnection;
import org.intelehealth.ekalarogya.utilities.SessionManager;
import org.intelehealth.ekalarogya.utilities.UrlModifiers;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class HwProfileActivity extends AppCompatActivity {
    SessionManager sessionManager = null;
    TextView hw_name_value, hw_designation_value, total_patregistered_value, total_visitprogress_value,
            total_consultaion_value, hw_gender_value, hw_state_value, hw_mobile_value,
            hw_whatsapp_value, hw_email_value, hw_aboutme_value;
    CircularImageView hw_profile_image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hw_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Get the intent, verify the action and get the query
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        hw_profile_image=(CircularImageView)findViewById(R.id.hw_profile_image);

        hw_name_value=(TextView)findViewById(R.id.hw_name_value);
        hw_designation_value=(TextView)findViewById(R.id.hw_designation_value);
        hw_aboutme_value=(TextView)findViewById(R.id.hw_aboutme_value);

        total_patregistered_value=(TextView)findViewById(R.id.total_patregistered_value);
        total_visitprogress_value=(TextView)findViewById(R.id.total_visitprogress_value);
        total_consultaion_value=(TextView)findViewById(R.id.total_consultaion_value);

        hw_gender_value=(TextView)findViewById(R.id.hw_gender_value);
        hw_state_value=(TextView)findViewById(R.id.hw_state_value);
        hw_mobile_value=(TextView)findViewById(R.id.hw_mobile_value);
        hw_whatsapp_value=(TextView)findViewById(R.id.hw_whatsapp_value);
        hw_email_value=(TextView)findViewById(R.id.hw_email_value);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        if (NetworkConnection.isOnline(this)) {
            getHw_Information();
        }else{
            DisplayUserDetail();
        }
        super.onResume();
    }

    public void getHw_Information(){
        Dialog progressDialog = new Dialog(this, android.R.style.Theme_Black);
        View view = LayoutInflater.from(HwProfileActivity.this).inflate(
                R.layout.custom_progress_dialog, null);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.getWindow().setBackgroundDrawableResource(
                R.color.transparent);
        progressDialog.setContentView(view);
        progressDialog.show();

        String url = "https://" + sessionManager.getServerUrl() + ":3004/api/user/profile/"+sessionManager.getCreatorID()+"?type=hw";
        Logger.logD("Profile", "get profile Info url" + url);
        Observable<MainProfileModel> profilePicDownload = AppConstants.apiInterface.PERSON_PROFILE_INFO(url, "Basic " + sessionManager.getEncoded());
        profilePicDownload.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<MainProfileModel>() {
                    @Override
                    public void onNext(MainProfileModel mainProfileModel) {
                        System.out.println(mainProfileModel.toString()+"");
                        if(mainProfileModel!=null && mainProfileModel.getStatus()==true) {
                            Gson gson = new Gson();
                            String userprofile= gson.toJson(mainProfileModel);
                            sessionManager.setUserProfileDetail(userprofile);
                            DisplayUserDetail();
                       }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.logD("ProfileInfo", e.getMessage());
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onComplete() {
                        Logger.logD("ProfileInfo", "complete");
                       }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_today_patient, menu);
        inflater.inflate(R.menu.today_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void DisplayUserDetail(){
        Gson gson = new Gson();
        String userDetail = sessionManager.getUserProfileDetail();
        if(userDetail!=null && !userDetail.isEmpty()) {
            MainProfileModel mainProfileModel = gson.fromJson(userDetail, MainProfileModel.class);
        /* Glide.with(HwProfileActivity.this)
                                .load(hwProfileModel.getImage())
                                .thumbnail(0.3f)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(hw_profile_image);*/
            HwProfileModel hwProfileModel = mainProfileModel.getHwProfileModel();
            hw_name_value.setText(hwProfileModel.getUserName());
            hw_designation_value.setText(hwProfileModel.getDesignation());
            hw_aboutme_value.setText(hwProfileModel.getAboutMe());

            total_patregistered_value.setText(hwProfileModel.getPatientRegistered() + "+");
            total_visitprogress_value.setText(hwProfileModel.getVisitInProgress() + "+");
            total_consultaion_value.setText(hwProfileModel.getCompletedConsultation() + "+");

            HwPersonalInformationModel personalInformationModel = hwProfileModel.getPersonalInformation();

            if (personalInformationModel.getGender().equalsIgnoreCase("F")) {
                hw_gender_value.setText(getResources().getString(R.string.identification_screen_checkbox_female));
            } else if (personalInformationModel.getGender().equalsIgnoreCase("M")) {
                hw_gender_value.setText(getResources().getString(R.string.identification_screen_checkbox_male));
            } else {
                hw_gender_value.setText(personalInformationModel.getGender());
            }
            hw_state_value.setText(personalInformationModel.getState());
            hw_mobile_value.setText(personalInformationModel.getMobile());
            hw_whatsapp_value.setText(personalInformationModel.getWhatsApp());
            hw_email_value.setText(personalInformationModel.getEmail());
        }else{
            Toast.makeText(HwProfileActivity.this, HwProfileActivity.this.getString(R.string.please_connect_to_internet), Toast.LENGTH_LONG).show();
        }
    }
}