package org.intelehealth.ekalarogya.activities.chmProfileActivity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.cameraActivity.CameraActivity;
import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.models.UserProfileModel.HwPersonalInformationModel;
import org.intelehealth.ekalarogya.models.UserProfileModel.HwProfileModel;
import org.intelehealth.ekalarogya.models.UserProfileModel.MainProfileModel;
import org.intelehealth.ekalarogya.models.UserProfileModel.UserAttributeModel;
import org.intelehealth.ekalarogya.models.UserProfileModel.UserDataModel;
import org.intelehealth.ekalarogya.models.UserProfileModel.UserInfoUpdateModel;
import org.intelehealth.ekalarogya.models.UserProfileModel.UserPersonModel;
import org.intelehealth.ekalarogya.models.patientImageModelRequest.PatientProfile;
import org.intelehealth.ekalarogya.utilities.Base64Utils;
import org.intelehealth.ekalarogya.utilities.DownloadFilesUtils;
import org.intelehealth.ekalarogya.utilities.Logger;
import org.intelehealth.ekalarogya.utilities.NetworkConnection;
import org.intelehealth.ekalarogya.utilities.SessionManager;
import org.intelehealth.ekalarogya.utilities.UrlModifiers;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.service.autofill.UserData;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class HwProfileActivity extends AppCompatActivity {
    public static String TAG = "HwProfileUpdate";
    private static final int PICK_IMAGE_FROM_GALLERY = 2001;
    SessionManager sessionManager = null;
    String mCurrentPhotoPath;

    EditText hw_designation_value, hw_aboutme_value, hw_gender_value, hw_mobile_value, hw_whatsapp_value,
            hw_email_value;
    TextView hw_name_value, total_patregistered_value, total_visitprogress_value,
            total_consultaion_value, hw_state_value, save_hw_detail;
    CircularImageView hw_profile_image;
    MainProfileModel mainProfileModel;

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

        hw_profile_image = (CircularImageView) findViewById(R.id.hw_profile_image);

        hw_name_value = (TextView) findViewById(R.id.hw_name_value);
        hw_designation_value = (EditText) findViewById(R.id.hw_designation_value);
        hw_aboutme_value = (EditText) findViewById(R.id.hw_aboutme_value);

        total_patregistered_value = (TextView) findViewById(R.id.total_patregistered_value);
        total_visitprogress_value = (TextView) findViewById(R.id.total_visitprogress_value);
        total_consultaion_value = (TextView) findViewById(R.id.total_consultaion_value);

        hw_gender_value = (EditText) findViewById(R.id.hw_gender_value);
        hw_state_value = (TextView) findViewById(R.id.hw_state_value);
        hw_mobile_value = (EditText) findViewById(R.id.hw_mobile_value);
        hw_whatsapp_value = (EditText) findViewById(R.id.hw_whatsapp_value);
        hw_email_value = (EditText) findViewById(R.id.hw_email_value);

        save_hw_detail = (TextView) findViewById(R.id.save_hw_detail);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        if (NetworkConnection.isOnline(this)) {
            getHw_Information();
        } else {
            DisplayUserDetail();
        }
        super.onResume();
    }

    public void getHw_Information() {
        Dialog progressDialog = new Dialog(this, android.R.style.Theme_Black);
        View view = LayoutInflater.from(HwProfileActivity.this).inflate(
                R.layout.custom_progress_dialog, null);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.getWindow().setBackgroundDrawableResource(
                R.color.transparent);
        progressDialog.setContentView(view);
        progressDialog.show();

        String url = "https://" + sessionManager.getServerUrl() + ":3004/api/user/profile/" + sessionManager.getCreatorID() + "?type=hw";
        Logger.logD("Profile", "get profile Info url" + url);

        Observable<MainProfileModel> profilePicDownload = AppConstants.apiInterface.PERSON_PROFILE_INFO(url, "Basic " + sessionManager.getEncoded());
        profilePicDownload.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<MainProfileModel>() {
                    @Override
                    public void onNext(MainProfileModel mainProfileModel1) {
                        String response = mainProfileModel1.toString();
                        if (mainProfileModel1 != null && mainProfileModel1.getStatus() == true) {
                            Gson gson = new Gson();
                            String userprofile = gson.toJson(mainProfileModel1);
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
        inflater.inflate(R.menu.menu_edit_hw_profile, menu);
        inflater.inflate(R.menu.today_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.hw_profile_image_edit:

                hw_designation_value.setClickable(true);
                hw_designation_value.setFocusable(true);
                hw_designation_value.setCursorVisible(true);
                hw_designation_value.setFocusableInTouchMode(true);
                hw_designation_value.requestFocus();
                hw_designation_value.setSelection(hw_designation_value.getText().length());

//                hw_aboutme_value.setClickable(true);
//                hw_aboutme_value.setFocusable(true);
//                hw_aboutme_value.setCursorVisible(true);
//                hw_aboutme_value.setFocusableInTouchMode(true);
//                hw_aboutme_value.setVisibility(View.VISIBLE);

              /* This is not needed to edit as per: https://intelehealthwiki.atlassian.net/browse/AEAT-473
                hw_gender_value.setClickable(true);
                hw_gender_value.setFocusable(true);
                hw_gender_value.setCursorVisible(true);
                hw_gender_value.setFocusableInTouchMode(true);*/

                hw_mobile_value.setClickable(true);
                hw_mobile_value.setFocusable(true);
                hw_mobile_value.setCursorVisible(true);
                hw_mobile_value.setFocusableInTouchMode(true);

                hw_whatsapp_value.setClickable(true);
                hw_whatsapp_value.setFocusable(true);
                hw_whatsapp_value.setCursorVisible(true);
                hw_whatsapp_value.setFocusableInTouchMode(true);

                hw_email_value.setClickable(true);
                hw_email_value.setFocusable(true);
                hw_email_value.setCursorVisible(true);
                hw_email_value.setFocusableInTouchMode(true);

                save_hw_detail.setVisibility(View.VISIBLE);
                save_hw_detail.setClickable(true);
                save_hw_detail.setEnabled(true);
                save_hw_detail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (NetworkConnection.isOnline(HwProfileActivity.this)) {
                            CheckValidation();
                        } else {
                            Toast.makeText(HwProfileActivity.this, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                hw_profile_image.setClickable(true);
                hw_profile_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectImage();
                    }
                });

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void selectImage() {
        final CharSequence[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(HwProfileActivity.this);
        builder.setTitle(R.string.hw_profile_image_picker_title);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    Intent cameraIntent = new Intent(HwProfileActivity.this, CameraActivity.class);
                    String imageName = UUID.randomUUID().toString();
                    cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
                    cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, AppConstants.IMAGE_PATH);
                    startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
                } else if (item == 1) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_IMAGE_FROM_GALLERY);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void DisplayUserDetail() {
        Gson gson = new Gson();
        String userDetail = sessionManager.getUserProfileDetail();
        if (userDetail != null && !userDetail.isEmpty()) {
            mainProfileModel = gson.fromJson(userDetail, MainProfileModel.class);
            String profile_image_url = "https://" + sessionManager.getServerUrl() + "/openmrs/ws/rest/v1/personimage/" + sessionManager.getHwID();

            HwProfileModel hwProfileModel = mainProfileModel.getHwProfileModel();
            hw_name_value.setText(hwProfileModel.getUserName());
            hw_designation_value.setText(hwProfileModel.getDesignation());
            if (!hwProfileModel.getAboutMe().isEmpty() && hwProfileModel.getAboutMe() != null) {
                hw_aboutme_value.setText(hwProfileModel.getAboutMe());
            } else {
                hw_aboutme_value.setVisibility(View.GONE);
            }

            total_patregistered_value.setText(hwProfileModel.getPatientRegistered() + "");
            total_visitprogress_value.setText(hwProfileModel.getVisitInProgress() + "");
            total_consultaion_value.setText(hwProfileModel.getCompletedConsultation() + "");

            HwPersonalInformationModel personalInformationModel = hwProfileModel.getPersonalInformation();
            Log.v("lang", "langModel: " + personalInformationModel.getGender());

            if (personalInformationModel.getGender().equalsIgnoreCase("F") ||
                    personalInformationModel.getGender().equalsIgnoreCase("Female")) {
                hw_gender_value.setText(getResources().getString(R.string.identification_screen_checkbox_female));
            } else if (personalInformationModel.getGender().equalsIgnoreCase("M") ||
                    personalInformationModel.getGender().equalsIgnoreCase("Male")) {
                hw_gender_value.setText(getResources().getString(R.string.identification_screen_checkbox_male));
            } else {
                hw_gender_value.setText(personalInformationModel.getGender());
            }
            Log.v("lang", "langModel: " + hw_gender_value.getText().toString());
            hw_state_value.setText(personalInformationModel.getState());
            hw_mobile_value.setText(personalInformationModel.getMobile());
            hw_whatsapp_value.setText(personalInformationModel.getWhatsApp());
            hw_email_value.setText(personalInformationModel.getEmail());
            sessionManager.setHwPhone(personalInformationModel.getMobile());
            sessionManager.setHwWhatsApp(personalInformationModel.getWhatsApp());

            profilePicDownloaded();
        } else {
            Toast.makeText(HwProfileActivity.this, HwProfileActivity.this.getString(R.string.please_connect_to_internet), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CameraActivity.TAKE_IMAGE) {
            if (resultCode == RESULT_OK) {
                mCurrentPhotoPath = data.getStringExtra("RESULT");
                File photo = new File(mCurrentPhotoPath);
                Glide.with(HwProfileActivity.this)
                        .load(photo)
                        .thumbnail(0.3f)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(hw_profile_image);

                UploadHW_ProfileImage();
            }
        } else if (requestCode == PICK_IMAGE_FROM_GALLERY && null != data) {
            if (resultCode == RESULT_OK) {
                Log.v("HWProfile", "HWProfile: " + "Result OK");
                Uri selectedImage = data.getData();
                if (selectedImage == null) {    // AEAT-471
                    Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mCurrentPhotoPath = cursor.getString(columnIndex);
                cursor.close();
                File file = new File(mCurrentPhotoPath);
                Glide.with(HwProfileActivity.this)
                        .load(file)
                        .thumbnail(0.3f)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(hw_profile_image);
                // String picturePath contains the path of selected Image
                UploadHW_ProfileImage();
            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    public boolean UploadHW_ProfileImage() {
        String encoded = sessionManager.getEncoded();
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.setPatientProfileImageUrl();
        Base64Utils base64Utils = new Base64Utils();
        PatientProfile p = new PatientProfile();
        p.setPerson(sessionManager.getHwID());
        p.setBase64EncodedImage(base64Utils.getBase64FromFileWithConversion(mCurrentPhotoPath));
        Single<ResponseBody> personProfilePicUpload = AppConstants.apiInterface.PERSON_PROFILE_PIC_UPLOAD(url, "Basic " + encoded, p);
        personProfilePicUpload.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody responseBody) {
                        Logger.logD("HwProfileImage", "success" + responseBody);
//                      AppConstants.notificationUtils.DownloadDone("Patient Profile", "Uploaded Patient Profile", 4, IntelehealthApplication.getAppContext());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.logD("HwProfileImage", "Onerror " + e.getMessage());
//                            AppConstants.notificationUtils.DownloadDone("Patient Profile", "Error Uploading Patient Profile", 4, IntelehealthApplication.getAppContext());
                    }
                });

        sessionManager.setPullSyncFinished(true);
        IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_PATIENT_PROFILE_IMAGE_PUSH_DONE));
//        AppConstants.notificationUtils.DownloadDone("Patient Profile", "Completed Uploading Patient Profile", 4, IntelehealthApplication.getAppContext());
        return true;
    }

    public void profilePicDownloaded() {
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.patientProfileImageUrl(sessionManager.getHwID());
        Logger.logD("URL", "profileimage url" + url);
        Observable<ResponseBody> profilePicDownload = AppConstants.apiInterface.PERSON_PROFILE_PIC_DOWNLOAD(url, "Basic " + sessionManager.getEncoded());
        profilePicDownload.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody file) {
                        DownloadFilesUtils downloadFilesUtils = new DownloadFilesUtils();
                        downloadFilesUtils.saveToDisk(file, sessionManager.getHwID());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.logD("Error", e.getMessage());
                        // AEAT-552: You cannot start a load for a destroyed activity
                        // This crash will happen in cases if the activity is destroyed and since below we are using Glide.with(activity) it will crash
                        // instead either use applciationContenxt or check before loading if the context is still alive.

                     //   Glide.with(HwProfileActivity.this)
                        Glide.with(getApplicationContext())
                                .load("")
                                .error(R.drawable.ic_person_black_24dp)
                                .thumbnail(0.3f)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(hw_profile_image);
                    }

                    @Override
                    public void onComplete() {
                        Glide.with(HwProfileActivity.this)
                                .load(AppConstants.IMAGE_PATH + sessionManager.getHwID() + ".jpg")
                                .thumbnail(0.3f)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(hw_profile_image);
                    }

                });
    }

    public void CheckValidation() {
        int errorColor;
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            errorColor = ContextCompat.getColor(getApplicationContext(), R.color.white);
        } else {
            errorColor = getResources().getColor(R.color.white);
        }

        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(errorColor);
        String name = hw_gender_value.getText().toString().trim();
        Log.v("lang", "langModel: " + name);

        if(name.equalsIgnoreCase("ପୁରୁଷ"))
            name = "Male";
        else if(name.equalsIgnoreCase("पुरुष"))
            name = "Male";
        else if(name.equalsIgnoreCase("પુરુષ"))
            name = "Male";
        else if(name.equalsIgnoreCase("পুৰুষ"))
            name = "Male";

        if(name.equalsIgnoreCase("ମହିଳା"))
            name = "Female";
        else if(name.equalsIgnoreCase("महिला"))
            name = "Female";
        else if(name.equalsIgnoreCase("સ્ત્રી"))
            name = "Female";
        else if(name.equalsIgnoreCase("মহিলা"))
            name = "Female";


        if(name.length()>0){
            if(!name.equalsIgnoreCase("Male") &&
                    !name.equalsIgnoreCase("Female")){
                hw_gender_value.requestFocus();
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getString(R.string.enter_valid_gender));
                spannableStringBuilder.setSpan(foregroundColorSpan, 0, getString(R.string.enter_valid_gender).length(), 0);
                hw_gender_value.setError(spannableStringBuilder);
                return;
            }
        }

        if (hw_mobile_value.getText().toString().trim().length() > 0) {
            if (hw_mobile_value.getText().toString().trim().length() < 10) {
                hw_mobile_value.requestFocus();
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getString(R.string.enter_10_digits));
                spannableStringBuilder.setSpan(foregroundColorSpan, 0, getString(R.string.enter_10_digits).length(), 0);
                hw_mobile_value.setError(spannableStringBuilder);
                return;
            }
        }

        if (hw_whatsapp_value.getText().toString().trim().length() > 0) {
            if (hw_whatsapp_value.getText().toString().trim().length() < 10) {
                hw_whatsapp_value.requestFocus();
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getString(R.string.enter_10_digits));
                spannableStringBuilder.setSpan(foregroundColorSpan, 0, getString(R.string.enter_10_digits).length(), 0);
                hw_whatsapp_value.setError(spannableStringBuilder);
                return;
            }
        }

        if (hw_email_value.getText().toString().trim().length() > 0) {
            if (!emailValidation(hw_email_value.getText().toString().trim())) {
                hw_email_value.requestFocus();
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getString(R.string.enter_valid_email));
                spannableStringBuilder.setSpan(foregroundColorSpan, 0, getString(R.string.enter_valid_email).length(), 0);
                hw_email_value.setError(spannableStringBuilder);
                return;
            }
        }

        updateHwDetail();
    }

    public boolean emailValidation(String email)
    {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public void updateHwDetail(){

        /*{
            "phoneNumber": "7867",
                "qualification": "MBBS, MD",
                "fontOfSign": "almondita",
                "whatsapp": "7972269174",
                "registrationNumber": "MAR1208632",
                "emailId": "hiren@elxrsmarthealth.com",
                "address": "14, Shreeji Arcade, Opp Nitin Company, 400601",
                "textOfSign": "DrGenPhy",
                "specialization": "Allopathy",
                "visitState": "All",
                "aboutMe": "test",
                "timings": "12:00 AM - 3:00 AM"
                "gender":"female"
        }*/
        try{
            if(mainProfileModel!=null){
                HwProfileModel hwProfileModel = mainProfileModel.getHwProfileModel();
                UserAttributeModel userAttributeModel = new UserAttributeModel();
                if(hwProfileModel!=null) {
                    if (!hw_designation_value.getText().toString().equalsIgnoreCase(hwProfileModel.getDesignation())){
                        userAttributeModel.setQualification(hw_designation_value.getText().toString().trim());
                    }

                    if (!hw_aboutme_value.getText().toString().equalsIgnoreCase(hwProfileModel.getAboutMe())){
                        userAttributeModel.setAboutMe(hw_aboutme_value.getText().toString());
                    }

                    HwPersonalInformationModel personalInformationModel = hwProfileModel.getPersonalInformation();

                    if(personalInformationModel!=null) {
                        if (!hw_gender_value.getText().toString().equalsIgnoreCase(personalInformationModel.getGender())){
                            userAttributeModel.setGender(hw_gender_value.getText().toString().trim());
                        }

                        if (!hw_mobile_value.getText().toString().equalsIgnoreCase(personalInformationModel.getMobile())){
                            userAttributeModel.setPhoneNumber(hw_mobile_value.getText().toString().trim());
                            sessionManager.setHwPhone(hw_mobile_value.getText().toString().trim());
                        }

                        if (!hw_whatsapp_value.getText().toString().equalsIgnoreCase(personalInformationModel.getWhatsApp())){
                            userAttributeModel.setWhatsapp(hw_whatsapp_value.getText().toString().trim());
                            sessionManager.setHwWhatsApp(hw_whatsapp_value.getText().toString().trim());
                        }

                        if (!hw_email_value.getText().toString().equalsIgnoreCase(personalInformationModel.getEmail())){
                            userAttributeModel.setEmailId(hw_email_value.getText().toString().trim());
                        }

                    }
                    if(userAttributeModel!=null)
                        updateOnServer(userAttributeModel);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updateOnServer(UserAttributeModel obj){
        //https://afitraining.ekalarogya.org:3004/api/user/profile/a4ac4fee-538f-11e6-9cfe-86f436325720
        String url = "https://" + sessionManager.getServerUrl() + ":3004/api/user/profile/"+sessionManager.getCreatorID();
        String encoded = sessionManager.getEncoded();

        Single<UserInfoUpdateModel> hwUpdateApiCallObservable = AppConstants.apiInterface.HwUpdateInfo_API_CALL_OBSERVABLE(url, "Basic " + encoded, obj);
        hwUpdateApiCallObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<UserInfoUpdateModel>() {
                    @Override
                    public void onSuccess(UserInfoUpdateModel responseBody) {
                        Logger.logD(TAG, "success" + responseBody.toString());
                        Gson gson=new Gson();
                        String response= gson.toJson(responseBody);
                        Toast.makeText(HwProfileActivity.this,getString(R.string.update_hw_profile),Toast.LENGTH_SHORT).show();
                        setEnabledProfile();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.logD(TAG, "Onerror " + e.getMessage());
                    }
                });
    }

    public void setEnabledProfile(){
        hw_gender_value.setClickable(false);
        hw_gender_value.setFocusable(false);
        hw_gender_value.setCursorVisible(false);
        hw_gender_value.setFocusableInTouchMode(false);

        hw_mobile_value.setClickable(false);
        hw_mobile_value.setFocusable(false);
        hw_mobile_value.setCursorVisible(false);
        hw_mobile_value.setFocusableInTouchMode(false);

        hw_whatsapp_value.setClickable(false);
        hw_whatsapp_value.setFocusable(false);
        hw_whatsapp_value.setCursorVisible(false);
        hw_whatsapp_value.setFocusableInTouchMode(false);

        hw_email_value.setClickable(false);
        hw_email_value.setFocusable(false);
        hw_email_value.setCursorVisible(false);
        hw_email_value.setFocusableInTouchMode(false);

        save_hw_detail.setVisibility(View.GONE);
        save_hw_detail.setClickable(false);
        save_hw_detail.setEnabled(false);

        hw_profile_image.setClickable(false);
    }
}