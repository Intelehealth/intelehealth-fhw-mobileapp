package org.intelehealth.app.profile;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.hbb20.CountryCodePicker;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.cameraActivity.CameraActivity;
import org.intelehealth.app.activities.forgotPasswordNew.ChangePasswordActivity_New;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ImagesPushDAO;
import org.intelehealth.app.database.dao.ProviderDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.models.dto.ProviderDTO;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;
import org.intelehealth.app.utilities.BitmapUtils;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DownloadFilesUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.exception.DAOException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class MyProfileActivity extends AppCompatActivity {
    private static final String TAG = "MyProfileActivity";
    TextInputEditText etUsername, etFirstName, etMiddleName, etLastName, etEmail, etMobileNo;
    TextView tvDob, tvAge;
    LinearLayout layoutParent;
    TextView tvChangePhoto;
    String selectedGender;
    ImageView ivProfileImage;
    private DatePickerDialog.OnDateSetListener mDateSetListener1;
    String dobToDb, dobToShow;
    SessionManager sessionManager;
    private static final int PICK_IMAGE_FROM_GALLERY = 2001;
    private Handler mBackgroundHandler;
    private static final int GROUP_PERMISSION_REQUEST = 1000;
    RadioButton rbMale, rbFemale, rbOther;
    String profileImagePAth = "";
    Button btnSave;
    SnackbarUtils snackbarUtils;
    // View layoutToolbar;
    private int mDOBYear, mDOBMonth, mDOBDay, mAgeYears = 0, mAgeMonths = 0, mAgeDays = 0;
    private CountryCodePicker countryCodePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile_ui2);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResume() {
        super.onResume();
        try {
            initUI();
        } catch (DAOException e) {
            e.printStackTrace();
        }


    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initUI() throws DAOException {
        snackbarUtils = new SnackbarUtils();
        sessionManager = new SessionManager(this);
        View toolbar = findViewById(R.id.toolbar_common);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ImageView ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);

        ImageView ivBack = toolbar.findViewById(R.id.iv_back_arrow_common);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyProfileActivity.this, HomeScreenActivity_New.class);
                startActivity(intent);
                finish();
            }
        });
        tvTitle.setText(getResources().getString(R.string.my_profile));
        if (CheckInternetAvailability.isNetworkAvailable(this)) {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        } else {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

        }

        //initialize all input fields

        etUsername = findViewById(R.id.et_username_profile);
        etFirstName = findViewById(R.id.et_first_name_profile);
        etMiddleName = findViewById(R.id.et_middle_name_profile);
        etLastName = findViewById(R.id.et_last_name_profile);
        etEmail = findViewById(R.id.et_email_profile);
        etMobileNo = findViewById(R.id.et_mobile_no_profile);
        tvDob = findViewById(R.id.tv_date_of_birth_profile);
        tvAge = findViewById(R.id.tv_age_profile);
        btnSave = findViewById(R.id.btn_save_profile);
        layoutParent = findViewById(R.id.layout_parent_profile);
        rbMale = findViewById(R.id.rb_male);
        rbFemale = findViewById(R.id.rb_female);
        rbOther = findViewById(R.id.rb_other);

        countryCodePicker = findViewById(R.id.countrycode_spinner_profile);
        countryCodePicker.registerCarrierNumberEditText(etMobileNo); // attaches the ccp spinner with the edittext


        ivProfileImage = findViewById(R.id.iv_profilePic);
        tvChangePhoto = findViewById(R.id.tv_change_photo_profile);


        RelativeLayout layoutChangePassword = findViewById(R.id.view_change_password);

        //all click listeners

        RadioGroup rgGroupGender = findViewById(R.id.radioGroup_gender_profile);
        rgGroupGender.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checkedRadioButton = group.findViewById(checkedId);
            boolean isChecked = checkedRadioButton.isChecked();
            if (isChecked) {
                String selectedGenderText = checkedRadioButton.getText().toString();

                switch (selectedGenderText) {
                    case "Male":
                        selectedGender = "M";
                        rbMale.setButtonDrawable(getDrawable(R.drawable.ui2_ic_selected_green));
                        rbFemale.setButtonDrawable(getDrawable(R.drawable.ui2_ic_circle));
                        rbOther.setButtonDrawable(getDrawable(R.drawable.ui2_ic_circle));

                        break;
                    case "Female":
                        rbMale.setButtonDrawable(getDrawable(R.drawable.ui2_ic_circle));
                        rbFemale.setButtonDrawable(getDrawable(R.drawable.ui2_ic_selected_green));
                        rbOther.setButtonDrawable(getDrawable(R.drawable.ui2_ic_circle));
                        selectedGender = "F";
                        break;
                    case "Other":
                        rbMale.setButtonDrawable(getDrawable(R.drawable.ui2_ic_circle));
                        rbFemale.setButtonDrawable(getDrawable(R.drawable.ui2_ic_circle));
                        rbOther.setButtonDrawable(getDrawable(R.drawable.ui2_ic_selected_green));
                        selectedGender = "O";
                        break;
                }


            }
        });

        layoutChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity_New.class);
            startActivity(intent);
        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    updateProfileDetailsToLocalDb();
                } catch (DAOException e) {
                    e.printStackTrace();
                }

            }
        });


        //date selector
        tvDob.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener1, year, month, day);
            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // datePickerDialog.getDatePicker().setMinDate(cal.getTimeInMillis());

            //  datePickerDialog.getDatePicker().setMinDate(cal.getTimeInMillis());
            datePickerDialog.show();
        });
        mDateSetListener1 = (datePicker, year, month, day) -> {
            month = month + 1;

            //String date = day + "-" + month + "-" + year;
            String sDay = "";
            if (day < 10) {
                sDay = "0" + day;
            } else {
                sDay = String.valueOf(day);
            }

            String sMonth = "";
            if (month < 10) {
                sMonth = "0" + month;
            } else {
                sMonth = String.valueOf(month);
            }
            dobToDb = year + "-" + sMonth + "-" + sDay;

            dobToShow = sDay + "-" + sMonth + "-" + year;
            tvDob.setText(dobToShow);
            tvDob.setText(DateAndTimeUtils.getDisplayDateForApp(dobToDb));

            String age = DateAndTimeUtils.getAge_FollowUp(dobToDb, this);
            tvAge.setText(age);
            //yyyy-mm-dd

        };

        tvChangePhoto.setOnClickListener(v -> checkPerm());

        // fetch user details if added
        fetchUserDetailsIfAdded();

    }

    private void fetchUserDetailsIfAdded() {
        try {
            ProviderDAO providerDAO = new ProviderDAO();
            ProviderDTO providerDTO = providerDAO.getLoginUserDetails(sessionManager.getProviderID());
            etUsername.setText(sessionManager.getChwname());
            etFirstName.setText(providerDTO.getFamilyName());
            etLastName.setText(providerDTO.getGivenName());
            etEmail.setText(providerDTO.getEmailId());
            etMiddleName.setText(providerDTO.getMiddle_name());

            tvDob.setText(DateAndTimeUtils.getDisplayDateForApp(providerDTO.getDateofbirth()));

            String age = DateAndTimeUtils.getAge_FollowUp(providerDTO.getDateofbirth(), this);
            tvAge.setText(age);
            String phoneWithCountryCode = providerDTO.getCountryCode() + providerDTO.getTelephoneNumber();

            countryCodePicker.setFullNumber(phoneWithCountryCode); // automatically assigns cc to spinner and number to edittext field.


            String gender = providerDTO.getGender();
            if (gender != null && !gender.isEmpty()) {

                if (gender.equalsIgnoreCase("m")) {
                    rbMale.setChecked(true);
                    rbFemale.setChecked(false);
                    rbOther.setChecked(false);

                } else if (gender.equalsIgnoreCase("f")) {
                    rbMale.setChecked(false);
                    rbFemale.setChecked(true);
                    rbOther.setChecked(false);
                } else if (gender.equalsIgnoreCase("o")) {
                    rbMale.setChecked(false);
                    rbFemale.setChecked(false);
                    rbOther.setChecked(true);
                }
            }

            if (providerDTO.getImagePath() != null && !providerDTO.getImagePath().isEmpty()) {
                Glide.with(this)
                        .load(providerDTO.getImagePath())
                        .thumbnail(0.3f)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(ivProfileImage);
            } else {
                ivProfileImage.setImageDrawable(getResources().getDrawable(R.drawable.avatar1));
            }

            Log.d(TAG, "fetchUserDetailsIfAdded: path : " + providerDTO.getImagePath());
            if (providerDTO.getImagePath() == null || providerDTO.getImagePath().equalsIgnoreCase("")) {
                if (NetworkConnection.isOnline(this)) {
                    profilePicDownloaded(providerDTO);
                }
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }


    private void selectImage() {
        final CharSequence[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_profile_image);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    Intent cameraIntent = new Intent(MyProfileActivity.this, CameraActivity.class);
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

    private void updateProfileDetailsToLocalDb() throws DAOException {
        String selectedCode = countryCodePicker.getSelectedCountryCodeWithPlus();
        ProviderDAO providerDAO = new ProviderDAO();
        ProviderDTO providerDTO = providerDAO.getLoginUserDetails(sessionManager.getProviderID());
        if (providerDTO != null) {
            ProviderDTO inputDTO = new ProviderDTO(providerDTO.getRole(),
                    providerDTO.getUseruuid(), etEmail.getText().toString().trim(),
                    etMobileNo.getText().toString().trim(), providerDTO.getProviderId(),
                    etLastName.getText().toString().trim(), etFirstName.getText().toString().trim(),
                    providerDTO.getVoided(), selectedGender, dobToDb,
                    providerDTO.getUuid(), providerDTO.getIdentifier(), selectedCode,
                    etMiddleName.getText().toString().trim());

            String imagePath = "";
            if (profileImagePAth != null && !profileImagePAth.isEmpty()) {
                imagePath = profileImagePAth;
            } else {
                imagePath = providerDTO.getImagePath();
            }

            if (imagePath != null && !imagePath.isEmpty())
                inputDTO.setImagePath(imagePath);

            try {
                boolean isUpdated = providerDAO.updateProfileDetails(inputDTO);
                if (isUpdated)

                    snackbarUtils.showSnackLinearLayoutParentSuccess(this, layoutParent, getResources().getString(R.string.profile_details_updated_new));


                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MyProfileActivity.this, HomeScreenActivity_New.class);
                        startActivity(intent);
                    }
                }, 2000);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void compressImageAndSave(final String filePath) {
        getBackgroundHandler().post(new Runnable() {
            @Override
            public void run() {
                boolean flag = BitmapUtils.fileCompressed(filePath);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (flag) {
                            saveImage(filePath);
                        } else
                            Toast.makeText(MyProfileActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    private void saveImage(String picturePath) {
        Log.v("saveImage", "picturePath = " + picturePath);
        File photo = new File(picturePath);
        if (photo.exists()) {
            try {

                long length = photo.length();
                length = length / 1024;
                Log.e("------->>>>", length + "");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("File not found : " + e.getMessage() + e);
            }

            //   recyclerViewAdapter.add(new DocumentObject(photo.getName(), photo.getAbsolutePath()));
            //   updateProfileImage(StringUtils.getFileNameWithoutExtension(photo));

        } else {
        }
        updateProfileImage(picturePath);


    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    private void updateProfileImage(String imagePath) {
        //update profile image to local db after its selected
        profileImagePAth = imagePath;
        ProviderDAO providerProfileDao = new ProviderDAO();
        try {
            boolean isUpdated = providerProfileDao.updateLoggedInUserProfileImage(imagePath, sessionManager.getProviderID());
            if (isUpdated) {
                //  snackbarUtils.showSnackLinearLayoutParentSuccess(this, layoutParent, getResources().getString(R.string.profile_photo_updated_new));

            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        if (NetworkConnection.isOnline(MyProfileActivity.this)) {
            ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
            imagesPushDAO.loggedInUserProfileImagesPush();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CameraActivity.TAKE_IMAGE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                Glide.with(MyProfileActivity.this)
                        .load(new File(mCurrentPhotoPath))
                        .thumbnail(0.25f).centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true).into(ivProfileImage);

                saveImage(mCurrentPhotoPath);
            }
        } else if (requestCode == PICK_IMAGE_FROM_GALLERY) {
            if (data != null) {
                try {
                    Uri selectedImage = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};
                    Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                    c.moveToFirst();
                    int columnIndex = c.getColumnIndex(filePath[0]);
                    String picturePath = c.getString(columnIndex);
                    c.close();
                    //Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                    Log.v("path", picturePath + "");

                    // copy & rename the file
                    String finalImageName = UUID.randomUUID().toString();
                    final String finalFilePath = AppConstants.IMAGE_PATH + finalImageName + ".jpg";

                    Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);

                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() //run on ui thread
                            {
                                public void run() {
                                    Glide.with(MyProfileActivity.this)
                                            .load(finalFilePath)
                                            .thumbnail(0.3f)
                                            .centerCrop()
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .skipMemoryCache(true)
                                            .into(ivProfileImage);
                                }
                            });
                        }
                    };
                    thread.start();

                    BitmapUtils.copyFile(picturePath, finalFilePath);
                    compressImageAndSave(finalFilePath);

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }
    }

    private void checkPerm() {
        if (checkAndRequestPermissions()) {
            selectImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GROUP_PERMISSION_REQUEST) {
            boolean allGranted = grantResults.length != 0;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                checkPerm();
            } else {
                showPermissionDeniedAlert(permissions);
            }

        }
    }

    private void showPermissionDeniedAlert(String[] permissions) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(MyProfileActivity.this);

        // AlertDialog.Builder alertdialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        alertdialogBuilder.setMessage(R.string.reject_permission_results);
        alertdialogBuilder.setPositiveButton(R.string.retry_again, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                checkPerm();
            }
        });
        alertdialogBuilder.setNegativeButton(R.string.ok_close_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setTextColor(getResources().getColor(org.intelehealth.apprtc.R.color.colorPrimary));
        //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        negativeButton.setTextColor(getResources().getColor(org.intelehealth.apprtc.R.color.colorPrimary));
        //negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    private boolean checkAndRequestPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int getAccountPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int phoneStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (getAccountPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.GET_ACCOUNTS);
        }
        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (phoneStatePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);

        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), GROUP_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    public void profilePicDownloaded(ProviderDTO providerDTO) throws DAOException {
        Log.d(TAG, "profilePicDownloaded: ");
        SessionManager sessionManager = new SessionManager(MyProfileActivity.this);
        UrlModifiers urlModifiers = new UrlModifiers();
        String uuid = sessionManager.getProviderID();
        String url = urlModifiers.getProviderProfileImageUrl(uuid);
        Log.d(TAG, "profilePicDownloaded:: url : " + url);


        Observable<ResponseBody> profilePicDownload =
                AppConstants.apiInterface.PROVIDER_PROFILE_PIC_DOWNLOAD(url, "Basic " + sessionManager.getEncoded());

        profilePicDownload.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody file) {
                        Log.d(TAG, "onNext: ");
                        DownloadFilesUtils downloadFilesUtils = new DownloadFilesUtils();
                        downloadFilesUtils.saveToDisk(file, uuid);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Logger.logD(TAG, e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        ProviderDAO providerDAO = new ProviderDAO();
                        boolean updated = false;
                        try {
                            updated = providerDAO.updateLoggedInUserProfileImage(profileImagePAth,
                                    sessionManager.getProviderID());

                        } catch (DAOException e) {
                            e.printStackTrace();
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                        if (updated) {
                            Glide.with(MyProfileActivity.this)
                                    .load(AppConstants.IMAGE_PATH + uuid + ".jpg")
                                    .thumbnail(0.3f)
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .into(ivProfileImage);
                        }
                        ImagesDAO imagesDAO = new ImagesDAO();
                        boolean isImageDownloaded = false;
                        try {
                            isImageDownloaded = imagesDAO.updateLoggedInUserProfileImage(profileImagePAth,
                                    sessionManager.getProviderID());

                        } catch (DAOException e) {
                            e.printStackTrace();
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                });
    }


}