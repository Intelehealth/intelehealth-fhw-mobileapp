package org.intelehealth.app.abdm.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.model.OTPVerificationResponse;
import org.intelehealth.app.databinding.ActivityAadharMobileVerificationBinding;
import org.intelehealth.app.databinding.ActivityAbhaAddressSuggestionsBinding;
import org.intelehealth.app.utilities.WindowsUtils;

public class AbhaAddressSuggestionsActivity extends AppCompatActivity {
    private Context context = AbhaAddressSuggestionsActivity.this;
    public static final String TAG = AbhaAddressSuggestionsActivity.class.getSimpleName();
    ActivityAbhaAddressSuggestionsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        WindowsUtils.setStatusBarColor(AbhaAddressSuggestionsActivity.this);  // changing status bar color

        Intent intent = getIntent();
        OTPVerificationResponse response = intent.getParcelableExtra("payload");

        response.getABHAProfile().getAddress();     // auto-generated address from abdm end.

    }
}