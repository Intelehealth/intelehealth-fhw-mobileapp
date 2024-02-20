package org.intelehealth.app.abdm.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.model.AbhaCardResponseBody;
import org.intelehealth.app.abdm.model.MobileLoginOnOTPVerifiedResponse;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.databinding.ActivityAbhaCardBinding;
import org.intelehealth.app.utilities.CameraUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.WindowsUtils;

import java.io.File;

public class AbhaCardActivity extends AppCompatActivity {
    private Context context = AbhaCardActivity.this;
    private ActivityAbhaCardBinding binding;
    private String base64CardImage, mCurrentPhotoPath;
    private AbhaCardResponseBody abhaCardResponseBody;
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

        abhaCardResponseBody = (AbhaCardResponseBody) intent.getSerializableExtra("payload");
        if (abhaCardResponseBody != null && mobileLoginOnOTPVerifiedResponse != null) {
            base64CardImage = abhaCardResponseBody.getImage();
            if (base64CardImage != null || !base64CardImage.isEmpty()) {
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
                snackbarUtils.showSnackRelativeLayoutParentSuccess(context, binding.layoutParent,
                        StringUtils.getMessageTranslated(getString(R.string.abha_card_is_already_downloaded) /*+ " to: " + mCurrentPhotoPath*/,
                                sessionManager.getAppLanguage()), true);
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

        File filePath = new File(AppConstants.IMAGE_PATH + filename);
        if (!filePath.exists())
            filePath.mkdir();

        CameraUtils cameraUtils = new CameraUtils(AbhaCardActivity.this, filename, filePath.toString());
        mCurrentPhotoPath = cameraUtils.compressImageAndSave(decodedByte);
        Log.d("TAG", "displayAbhaCardPhotoAndStoreInFile: " + mCurrentPhotoPath);   // /storage/emulated/0/Android/data/org.intelehealth.app/files/Pictures/91-7533-7132-6608.jpg

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.ivAbhaCard.setImageBitmap(decodedByte);
            }
        });
    }

}