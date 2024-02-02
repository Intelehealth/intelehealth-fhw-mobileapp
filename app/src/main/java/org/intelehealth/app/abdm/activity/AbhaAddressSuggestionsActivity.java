package org.intelehealth.app.abdm.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.chip.Chip;

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
        binding = ActivityAbhaAddressSuggestionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowsUtils.setStatusBarColor(AbhaAddressSuggestionsActivity.this);  // changing status bar color

        Intent intent = getIntent();
        OTPVerificationResponse response = (OTPVerificationResponse) intent.getSerializableExtra("payload");
        response.getABHAProfile().getAddress();     // auto-generated address from abdm end.

        createDynamicChips("prajwalw@sbx");
        createDynamicChips("prajuuu@sbx");
        createDynamicChips("aparna@sbx");
        createDynamicChips("kavita@sbx");
        createDynamicChips("hello@sbx");
    }

    private void createDynamicChips(String chipTitle) {
        Chip chip = new Chip(context);
        chip.setText(chipTitle);
        chip.setCheckable(true);
        chip.setChipBackgroundColorResource(R.color.colorPrimary);
       // chip.setChipStrokeColorResource(R.color.colorPrimaryDark);
        chip.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        chip.setTextAppearance(R.style.TextAppearance_MaterialComponents_Chip);
        chip.isCloseIconVisible();
        binding.chipGrp.addView(chip);
    }
}