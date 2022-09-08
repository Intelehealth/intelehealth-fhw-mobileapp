package org.intelehealth.app.ui2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.textfield.TextInputEditText;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.followuppatients.FollowUpPatientActivity;
import org.intelehealth.app.activities.followuppatients.FollowUpPatientActivity_New;
import org.intelehealth.app.activities.searchPatientActivity.SearchPatientActivity_New;

public class LoginActivityNew extends AppCompatActivity {
    TextInputEditText edittextUsername, editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_new);


        TextView textviewPassword = findViewById(R.id.tv_forgot_password1);
        TextView buttonLogin = findViewById(R.id.button_login);

        edittextUsername = findViewById(R.id.edittext_username);
        editTextPassword = findViewById(R.id.edittext_password);

        textviewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivityNew.this, ForgotPasswordActivityNew.class);
                startActivity(intent);
            }
        });
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivityNew.this, HomeScreenActivityNew.class);
                startActivity(intent);
            }
        });


        manageCollapsingLayout();

    }

    private void manageCollapsingLayout() {
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.layout_collapsing);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle("Login");
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(" ");//careful there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });

        edittextUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, boolean hasFocus) {
                if (hasFocus) {
                    appBarLayout.setExpanded(false, true);
                }
            }
        });

        editTextPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, boolean hasFocus) {
                if (hasFocus) {
                    appBarLayout.setExpanded(false, true);
                }
            }
        });
    }
}