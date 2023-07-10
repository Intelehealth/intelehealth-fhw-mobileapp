package org.intelehealth.ezazi.ui.password.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.homeActivity.HomeActivity;
import org.intelehealth.ezazi.activities.setupActivity.SetupActivity;
import org.intelehealth.ezazi.databinding.ActivityForgotPasswordBinding;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;

import java.util.Objects;

/**
 * Created by Vaghela Mithun R. on 26-05-2023 - 11:20.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;

    @Override
    public void onBackPressed() {
        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(this)
                .content(getString(R.string.are_you_want_go_back))
                .positiveButtonLabel(R.string.yes)
                .build();

        dialog.setListener(this::finish);

        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater(), null, false);
        setContentView(binding.getRoot());
        setupToolbar();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.actionBarView.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        binding.actionBarView.toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }
}
