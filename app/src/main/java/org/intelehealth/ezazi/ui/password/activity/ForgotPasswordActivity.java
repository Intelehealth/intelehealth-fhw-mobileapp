package org.intelehealth.ezazi.ui.password.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.ActivityForgotPasswordBinding;

/**
 * Created by Vaghela Mithun R. on 26-05-2023 - 11:20.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater(), null, false);
        setContentView(binding.getRoot());
        setupToolbar();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.actionBarView.toolbar);
        binding.actionBarView.toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }
}
