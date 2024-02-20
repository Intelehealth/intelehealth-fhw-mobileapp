package org.intelehealth.app.abdm.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.model.AbhaCardResponseBody;
import org.intelehealth.app.abdm.model.MobileLoginOnOTPVerifiedResponse;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.databinding.ActivityAbhaCardBinding;
import org.intelehealth.app.utilities.CameraUtils;
import org.intelehealth.app.utilities.WindowsUtils;

import java.io.File;

public class AbhaCardActivity extends AppCompatActivity {
    private Context context = AbhaCardActivity.this;
    private ActivityAbhaCardBinding binding;
    private String base64CardImage, mCurrentPhotoPath;
    private AbhaCardResponseBody abhaCardResponseBody;
    private MobileLoginOnOTPVerifiedResponse mobileLoginOnOTPVerifiedResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAbhaCardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowsUtils.setStatusBarColor(AbhaCardActivity.this);  // changing status bar color

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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.ivAbhaCard.setImageBitmap(decodedByte);
            }
        });
    }

}