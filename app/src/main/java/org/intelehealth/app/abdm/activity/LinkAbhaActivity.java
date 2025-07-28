package org.intelehealth.app.abdm.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.intelehealth.app.R;
import org.intelehealth.app.databinding.ActivityLinkAbhaBinding;

public class LinkAbhaActivity extends AppCompatActivity {

    private ActivityLinkAbhaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLinkAbhaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setToolbar();
    }

    private void setToolbar() {
        setTitle(R.string.title_activity_link_abha);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}