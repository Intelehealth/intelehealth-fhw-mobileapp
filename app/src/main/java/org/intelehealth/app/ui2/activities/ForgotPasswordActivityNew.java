package org.intelehealth.app.ui2.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.activePatientsActivity.ActivePatientActivity;

public class ForgotPasswordActivityNew extends AppCompatActivity {
    private static final String TAG = ActivePatientActivity.class.getSimpleName();
    String[] textArray = {"+91", "+00", "+20", "+22"};
    Integer[] imageArray = {R.drawable.ui2_ic_country_flag_india, R.drawable.ic_flag_black_24dp,
            R.drawable.ic_account_box_black_24dp, R.drawable.ic_done_24dp};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_new);

        initUI();

    }

    private void initUI() {
        Button buttonUsername = findViewById(R.id.button_username);
        // buttonUsername.setBackground(getResources().getDrawable(R.drawable.ui2_common_button_bg_selected));
        Button buttonContinue = findViewById(R.id.button_continue);
        Button buttonMobileNumber = findViewById(R.id.button_mobile_number);
        RelativeLayout layoutMobileNo = findViewById(R.id.layout_parent_mobile_no);
        LinearLayout layoutUsername = findViewById(R.id.layout_parent_username);
        LinearLayout layoutChooseOption = findViewById(R.id.layout_choose_option);
        ImageView imageviewBack = findViewById(R.id.imageview_back_forgot_password);
        imageviewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivityNew.this, LoginActivityNew.class);
                startActivity(intent);
            }
        });

        buttonUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutMobileNo.setVisibility(View.GONE);
                layoutUsername.setVisibility(View.VISIBLE);

                buttonUsername.setBackgroundDrawable(getResources().getDrawable(R.drawable.ui2_common_button_bg_selected));
                buttonMobileNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.ui2_common_primary_bg_disabled));

            }
        });
        buttonMobileNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutUsername.setVisibility(View.GONE);
                layoutMobileNo.setVisibility(View.VISIBLE);

                buttonMobileNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.ui2_common_button_bg_selected));
                buttonUsername.setBackgroundDrawable(getResources().getDrawable(R.drawable.ui2_common_primary_bg_disabled));

            }
        });

        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivityNew.this, ForgotPasswordOtpVerificationActivity.class);
                startActivity(intent);
            }
        });


        Spinner spinner = findViewById(R.id.mySpinner);

        SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.spinner_value_layout, textArray, imageArray);
        spinner.setAdapter(adapter);
    }

    public class SpinnerAdapter extends ArrayAdapter<String> {

        private Context ctx;
        private String[] contentArray;
        private Integer[] imageArray;

        public SpinnerAdapter(Context context, int resource, String[] objects,
                              Integer[] imageArray) {
            super(context, R.layout.spinner_value_layout, R.id.spinnerTextView, objects);
            this.ctx = context;
            this.contentArray = objects;
            this.imageArray = imageArray;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.spinner_value_layout, parent, false);

            TextView textView = (TextView) row.findViewById(R.id.spinnerTextView);
            textView.setText(contentArray[position]);

            ImageView imageView = (ImageView) row.findViewById(R.id.spinnerImages);
            imageView.setImageResource(imageArray[position]);

            return row;
        }
    }
}