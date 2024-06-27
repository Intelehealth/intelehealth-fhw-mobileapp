package org.intelehealth.app.abdm.activity;

import static androidx.core.content.ContextCompat.startActivity;
import static org.intelehealth.app.utilities.DialogUtils.showOKDialog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.model.AbhaCardResponseBody;
import org.intelehealth.app.abdm.model.MobileLoginOnOTPVerifiedResponse;
import org.intelehealth.app.activities.identificationActivity.model.StateDistMaster;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.databinding.ActivityAbhaCardBinding;
import org.intelehealth.app.utilities.CameraUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.WindowsUtils;

import java.io.File;
import java.util.UUID;

public class AbhaCardActivity extends AppCompatActivity {
    private final Context context = AbhaCardActivity.this;
    private ActivityAbhaCardBinding binding;
    private String base64CardImage;
    private String mCurrentPhotoPath;
    private MobileLoginOnOTPVerifiedResponse mobileLoginOnOTPVerifiedResponse;
    SnackbarUtils snackbarUtils;
    SessionManager sessionManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAbhaCardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowsUtils.setStatusBarColor(AbhaCardActivity.this);  // changing status bar color
        snackbarUtils = new SnackbarUtils();
        sessionManager = new SessionManager(context);

        binding.ivBackArrow.setOnClickListener(v -> {
            finish();
        });


        Intent intent = getIntent();
        mobileLoginOnOTPVerifiedResponse = (MobileLoginOnOTPVerifiedResponse) intent.getSerializableExtra("data");
        AbhaCardResponseBody abhaCardResponseBody = (AbhaCardResponseBody) intent.getSerializableExtra("payload");
        String patientAbhaNumber = intent.getStringExtra("patientAbhaNumber");

        if (abhaCardResponseBody != null && mobileLoginOnOTPVerifiedResponse != null) {
            base64CardImage = abhaCardResponseBody.getImage();
            if (base64CardImage != null && !base64CardImage.isEmpty()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            displayAbhaCardPhotoAndStoreInFile(base64CardImage);
                        }
                    }).start();
            }
        }
        else if (patientAbhaNumber != null && abhaCardResponseBody != null && !patientAbhaNumber.isEmpty()) {
            base64CardImage = abhaCardResponseBody.getImage();
            if (base64CardImage != null && !base64CardImage.isEmpty()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            displayAbhaCardPhotoAndStoreInFile(base64CardImage);
                        }
                    }).start();
            }
        }

        binding.includeLayout.negativeBtn.setText(getString(R.string.download));
        binding.includeLayout.positiveBtn.setText(getString(R.string.share));
        binding.includeLayout.positiveBtn.setVisibility(View.GONE);

/*
        binding.includeLayout.positiveBtn.setOnClickListener(v -> {
            // share intent
        });
*/

        binding.includeLayout.negativeBtn.setOnClickListener(v -> {
            // download intent
            if (mCurrentPhotoPath != null)
                storeAndShowImageInGallery(getString(R.string.abha_card_is_already_downloaded));
//                snackbarUtils.showSnackRelativeLayoutParentSuccess(context, binding.layoutParent,
//                        StringUtils.getMessageTranslated(getString(R.string.abha_card_is_already_downloaded) /*+ " to: " + mCurrentPhotoPath*/,
//                                sessionManager.getAppLanguage()), true);
            else
                displayAbhaCardPhotoAndStoreInFile(base64CardImage);
        });
    }

    private void displayAbhaCardPhotoAndStoreInFile(String profileImage) {
        byte[] decodedString = Base64.decode(profileImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        String filename = "";
        if (mobileLoginOnOTPVerifiedResponse != null) {
            filename = mobileLoginOnOTPVerifiedResponse.getAccounts().get(0).getABHANumber();
        }
        else
            filename = UUID.randomUUID().toString();

        File filePath = new File(AppConstants.IMAGE_PATH + filename);
        if (!filePath.exists())
            filePath.mkdirs();

        CameraUtils cameraUtils = new CameraUtils(AbhaCardActivity.this, filename, filePath.toString());
        mCurrentPhotoPath = cameraUtils.compressImageAndSaveAbhaCard(decodedByte);
        Log.d("TAG", "displayAbhaCardPhotoAndStoreInFile: " + mCurrentPhotoPath);

        storeAndShowImageInGallery(getString(R.string.abha_card_is_downloaded_successfully));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.ivAbhaCard.setImageBitmap(decodedByte);
            }
        });
    }

    private void storeAndShowImageInGallery(String message) {
        MediaScannerConnection.scanFile(context,
                new String[] { mCurrentPhotoPath }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        // add button here to open image in gallery...
                        Snackbar snackbar = snackbarUtils.showSnackRelativeLayoutParentSuccess(context, binding.layoutParent,
                                StringUtils.getMessageTranslated(message, sessionManager.getAppLanguage()), true);
                        snackbarUtils.setImageActionForSnackBar(context, snackbar, uri);
                    }
                });

    }

}