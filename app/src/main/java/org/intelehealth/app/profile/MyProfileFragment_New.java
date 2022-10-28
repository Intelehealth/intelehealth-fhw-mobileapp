package org.intelehealth.app.profile;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
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
import android.provider.Settings;
import android.text.TextUtils;
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
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.additionalDocumentsActivity.AdditionalDocumentsActivity;
import org.intelehealth.app.activities.cameraActivity.CameraActivity;
import org.intelehealth.app.activities.forgotPasswordNew.ChangePasswordActivity_New;
import org.intelehealth.app.activities.forgotPasswordNew.ForgotPasswordActivity_New;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity;
import org.intelehealth.app.activities.splash_activity.SplashActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.dataMigration.SmoothUpgrade;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ProviderDAO;
import org.intelehealth.app.database.dao.ProviderProfileDao;
import org.intelehealth.app.models.DocumentObject;
import org.intelehealth.app.models.dto.ProviderProfileDTO;
import org.intelehealth.app.services.firebase_services.CallListenerBackgroundService;
import org.intelehealth.app.utilities.BitmapUtils;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MyProfileFragment_New extends Fragment {
    private static final String TAG = "MyProfileFragment_New";
    View view;
    String[] textArray = {"+91", "+00", "+20", "+22"};
    Integer[] imageArray = {R.drawable.ui2_ic_country_flag_india, R.drawable.ic_flag_black_24dp,
            R.drawable.ic_account_box_black_24dp, R.drawable.ic_done_24dp};
    TextInputEditText etUsername, etFirstName, etMiddleName, etLastName, etEmail, etMobileNo;
    TextView tvDob, tvAge;
    LinearLayout layoutParent;
    TextView tvChangePhoto;
    String selectedGender;
    Spinner spinnerCountries;
    ImageView ivProfileImage;
    private DatePickerDialog.OnDateSetListener mDateSetListener1;
    String dobToDb, dobToShow;
    SessionManager sessionManager;
    private static final int PICK_IMAGE_FROM_GALLERY = 2001;
    private Handler mBackgroundHandler;
    private static final int GROUP_PERMISSION_REQUEST = 1000;
    RadioButton rbMale, rbFemale, rbOther;

    @Override
    public void onResume() {
        super.onResume();
        initUI();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_profile_ui2, container, false);
        return view;
    }

    private void initUI() {
        sessionManager = new SessionManager(getActivity());
        View layoutToolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar_home);
        TextView tvLocation = layoutToolbar.findViewById(R.id.tv_user_location_home);
        TextView tvLastSyncApp = layoutToolbar.findViewById(R.id.tv_app_sync_time);
        ImageView ivNotification = layoutToolbar.findViewById(R.id.imageview_notifications_home);
        ImageView ivIsInternet = layoutToolbar.findViewById(R.id.imageview_is_internet);
        ImageView ivBackArrow = layoutToolbar.findViewById(R.id.iv_hamburger);
        ivBackArrow.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_arrow_back_new));
        tvLocation.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        tvLastSyncApp.setVisibility(View.GONE);
        ivNotification.setVisibility(View.GONE);

        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           /*     FragmentManager fm = Objects.requireNonNull(getActivity()).getFragmentManager();
                fm.popBackStack();*/
                Intent intent = new Intent(getActivity(), HomeScreenActivity_New.class);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav_home);
        bottomNav.setVisibility(View.GONE);

        spinnerCountries = view.findViewById(R.id.spinner_countries_profile);
        ivProfileImage = view.findViewById(R.id.iv_profilePic);


        RelativeLayout layoutChangePassword = view.findViewById(R.id.view_change_password);
        layoutChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChangePasswordActivity_New.class);
                startActivity(intent);
            }
        });
        SpinnerAdapter adapter = new SpinnerAdapter(getActivity(), R.layout.spinner_value_layout, textArray, imageArray);
        spinnerCountries.setAdapter(adapter);

        RadioGroup rgGroupGender = view.findViewById(R.id.radioGroup_gender_profile);
        rgGroupGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRadioButton = group.findViewById(checkedId);
                boolean isChecked = checkedRadioButton.isChecked();
                if (isChecked) {
                    selectedGender = checkedRadioButton.getText().toString();
                    Log.d(TAG, "onCheckedChanged: selected  " + checkedRadioButton.getText().toString());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        checkedRadioButton.setButtonDrawable(getActivity().getDrawable(R.drawable.ui2_ic_selected_green));
                    }

                }
            }
        });

        //initialize all input fields

        etUsername = view.findViewById(R.id.et_username_profile);
        etFirstName = view.findViewById(R.id.et_first_name_profile);
        etMiddleName = view.findViewById(R.id.et_middle_name_profile);
        etLastName = view.findViewById(R.id.et_last_name_profile);
        etEmail = view.findViewById(R.id.et_email_profile);
        etMobileNo = view.findViewById(R.id.et_mobile_no_profile);
        tvDob = view.findViewById(R.id.tv_date_of_birth_profile);
        tvAge = view.findViewById(R.id.tv_age_profile);
        Button btnSave = view.findViewById(R.id.btn_save_profile);
        layoutParent = view.findViewById(R.id.layout_parent_profile);
        rbMale = view.findViewById(R.id.rb_male);
        rbFemale = view.findViewById(R.id.rb_female);
        rbOther = view.findViewById(R.id.rb_other);


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProfileDetailsToLocalDb();
            }
        });

        tvDob.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    mDateSetListener1,
                    year, month, day);
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
            tvDob.setText(DateAndTimeUtils.getDisplayDateFromApp(dobToDb));

            String age = DateAndTimeUtils.getAge_FollowUp(dobToDb, getActivity());
            tvAge.setText(age);

        };
        tvChangePhoto = view.findViewById(R.id.tv_change_photo_profile);

        tvChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPerm();
            }
        });

        ProviderProfileDao providerProfileDao = new ProviderProfileDao();
        try {
            ProviderProfileDTO providerProfileDTO = providerProfileDao.getProvidersDetails();
            etUsername.setText(providerProfileDTO.getUsername());
            etFirstName.setText(providerProfileDTO.getFirstName());
            etMiddleName.setText(providerProfileDTO.getMiddleName());
            etLastName.setText(providerProfileDTO.getLastName());
            etEmail.setText(providerProfileDTO.getEmail());
            etMobileNo.setText(providerProfileDTO.getPhoneNumber());
            tvDob.setText(DateAndTimeUtils.getDisplayDateFromApp(providerProfileDTO.getDateOfBirth()));
            tvAge.setText(providerProfileDTO.getAge());

            //spinnerCountries.setse(providerProfileDTO.getUsername());
            // rgGroupGender.setText(providerProfileDTO.getUsername());
            String gender = providerProfileDTO.getGender();
            if (gender != null && !gender.isEmpty()) {

                if (gender.equalsIgnoreCase("male")) {
                    rbMale.setChecked(true);
                } else if (gender.equalsIgnoreCase("female")) {
                    rbFemale.setChecked(true);

                } else if (gender.equalsIgnoreCase("other")) {
                    rbOther.setChecked(true);

                }
            }

            Log.d(TAG, "initUI: path : " + providerProfileDTO.getImagePath());

/*
            if (providerProfileDTO.getImagePath() != null && !providerProfileDTO.getImagePath().isEmpty()) {
                Bitmap bitmap = BitmapFactory.decodeFile(providerProfileDTO.getImagePath());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                byte[] imageArray = stream.toByteArray();
                ivProfileImage.setImageBitmap(bitmap);
            }

*/

        } catch (DAOException e) {
            e.printStackTrace();
        }

    }

    private void selectImage() {
        final CharSequence[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        builder.setTitle(R.string.select_profile_image);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    Intent cameraIntent = new Intent(getActivity(), CameraActivity.class);
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

    private void addProfileDetailsToLocalDb() {
        ProviderProfileDao providerDAO = new ProviderProfileDao();
        SnackbarUtils snackbarUtils = new SnackbarUtils();
        List<ProviderProfileDTO> providersDetails = new ArrayList<>();
        String age = DateAndTimeUtils.getAge_FollowUp(dobToDb, getActivity());
        Log.d(TAG, "addProfileDetailsToLocalDb: age : " + age);


        ProviderProfileDTO providerProfileDTO = new ProviderProfileDTO(sessionManager.getProviderID(), etUsername.getText().toString(),
                etFirstName.getText().toString(),
                etMiddleName.getText().toString(), etLastName.getText().toString(), selectedGender,
                dobToDb, tvAge.getText().toString(), etMobileNo.getText().toString(),
                spinnerCountries.getSelectedItem().toString(), etEmail.getText().toString(), "None");
        providersDetails.add(providerProfileDTO);
        try {
            boolean isInserted = providerDAO.insertProvidersProfile(providersDetails);
            if (isInserted)

                snackbarUtils.showSnackLinearLayoutParentSuccess(getActivity(), layoutParent, getResources().getString(R.string.profile_details_added));

        } catch (DAOException e) {
            e.printStackTrace();
        }

    }

    public class SpinnerAdapter extends ArrayAdapter<String> {

        private Context ctx;
        private String[] contentArray;
        private Integer[] imageArray;

        public SpinnerAdapter(Context context, int resource, String[] objects,
                              Integer[] imageArray) {
            super(context, R.layout.spinner_value_layout, R.id.spinnerTextView, objects);
            this.ctx = context;
            this.contentArray = objects;
            this.imageArray = imageArray;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.spinner_value_layout, parent, false);

            TextView textView = (TextView) row.findViewById(R.id.spinnerTextView);
            textView.setText(contentArray[position]);

            ImageView imageView = (ImageView) row.findViewById(R.id.spinnerImages);
            imageView.setImageResource(imageArray[position]);

            return row;
        }
    }

    void compressImageAndSave(final String filePath) {
        getBackgroundHandler().post(new Runnable() {
            @Override
            public void run() {
                boolean flag = BitmapUtils.fileCompressed(filePath);
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (flag) {
                            saveImage(filePath);
                        } else
                            Toast.makeText(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    private void saveImage(String picturePath) {
        Log.v("AdditionalDocuments", "picturePath = " + picturePath);
        File photo = new File(picturePath);
        if (photo.exists()) {
            try {

                long length = photo.length();
                length = length / 1024;
                Log.e("------->>>>", length + "");
            } catch (Exception e) {
                System.out.println("File not found : " + e.getMessage() + e);
            }

            //   recyclerViewAdapter.add(new DocumentObject(photo.getName(), photo.getAbsolutePath()));
            //   updateProfileImage(StringUtils.getFileNameWithoutExtension(photo));
            updateProfileImage(picturePath);

        }
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
        ProviderProfileDao providerProfileDao = new ProviderProfileDao();

        try {
            providerProfileDao.updateProfilePicture(sessionManager.getProviderID(), imagePath);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CameraActivity.TAKE_IMAGE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                final Bitmap takenImage = BitmapFactory.decodeFile(mCurrentPhotoPath);
                Log.d(TAG, "onActivityResult: mCurrentPhotoPath camera : " + mCurrentPhotoPath);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                takenImage.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                byte[] imageArray = stream.toByteArray();
                ivProfileImage.setImageBitmap(takenImage);
                saveImage(mCurrentPhotoPath);
//                String mCurrentPhotoPath = data.getStringExtra("RESULT");
//                File photo = new File(mCurrentPhotoPath);
//                if (photo.exists()) {
//                    try{
//
//                        long length = photo.length();
//                        length = length/1024;
//                        Log.e("------->>>>",length+"");
//                    }catch(Exception e){
//                        System.out.println("File not found : " + e.getMessage() + e);
//                    }
//
//                    recyclerViewAdapter.add(new DocumentObject(photo.getName(), photo.getAbsolutePath()));
//                    updateImageDatabase(StringUtils.getFileNameWithoutExtension(photo));
//                }
            }
        } else if (requestCode == PICK_IMAGE_FROM_GALLERY) {
            if (data != null) {
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = Objects.requireNonNull(getActivity()).getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                //Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                Log.v("path", picturePath + "");

                // copy & rename the file
                String finalImageName = UUID.randomUUID().toString();
                final String finalFilePath = AppConstants.IMAGE_PATH + finalImageName + ".jpg";
                Log.d(TAG, "onActivityResult: mCurrentPhotoPath gallery : " + finalFilePath);

                Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                byte[] imageArray = stream.toByteArray();
                ivProfileImage.setImageBitmap(bitmap);

                BitmapUtils.copyFile(picturePath, finalFilePath);
                compressImageAndSave(finalFilePath);
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
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(Objects.requireNonNull(getActivity()));

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
                Objects.requireNonNull(getActivity()).finish();
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
        IntelehealthApplication.setAlertDialogCustomTheme(getActivity(), alertDialog);
    }

    private boolean checkAndRequestPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                Manifest.permission.CAMERA);
        int getAccountPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.GET_ACCOUNTS);
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int phoneStatePermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE);

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
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), GROUP_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }


}
