package org.intelehealth.app.abdm.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

import org.intelehealth.app.R;
import org.intelehealth.app.databinding.ActivityAccountSelectionLoginBinding;
import org.intelehealth.app.utilities.WindowsUtils;

public class AccountSelectionLoginActivity extends AppCompatActivity {
    private ActivityAccountSelectionLoginBinding binding;
    private Context context = AccountSelectionLoginActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountSelectionLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowsUtils.setStatusBarColor(AccountSelectionLoginActivity.this);  // changing status bar color





    }
}