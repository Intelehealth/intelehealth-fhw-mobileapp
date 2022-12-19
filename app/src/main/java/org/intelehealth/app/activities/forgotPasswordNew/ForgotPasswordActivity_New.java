package org.intelehealth.app.activities.forgotPasswordNew;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.activePatientsActivity.ActivePatientActivity;
import org.intelehealth.app.activities.setupActivity.SetupActivityNew;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentListingResponse;
import org.intelehealth.app.models.ForgotPasswordApiResponseModel;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;

import retrofit2.Call;
import retrofit2.Callback;

public class ForgotPasswordActivity_New extends AppCompatActivity {
    private static final String TAG = ActivePatientActivity.class.getSimpleName();
    String[] textArray = {"+91", "+00", "+20", "+22"};
    Integer[] imageArray = {R.drawable.ui2_ic_country_flag_india, R.drawable.ic_flag_black_24dp,
            R.drawable.ic_account_box_black_24dp, R.drawable.ic_done_24dp};
    EditText etUsername, etMobileNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_new_ui2);

        initUI();

    }

    private void initUI() {
        Button buttonUsername = findViewById(R.id.button_username);
        // buttonUsername.setBackground(getResources().getDrawable(R.drawable.ui2_common_button_bg_selected));
        Button buttonContinue = findViewById(R.id.button_continue);
        Button buttonMobileNumber = findViewById(R.id.button_mobile_number);
        etUsername = findViewById(R.id.edittext_username);
        etMobileNo = findViewById(R.id.edittext_mobile_number);


        RelativeLayout layoutMobileNo = findViewById(R.id.layout_parent_mobile_no);
        LinearLayout layoutUsername = findViewById(R.id.layout_parent_username);
        LinearLayout layoutChooseOption = findViewById(R.id.layout_choose_option);
        ImageView imageviewBack = findViewById(R.id.imageview_back_forgot_password);
        imageviewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity_New.this, SetupActivityNew.class);
                startActivity(intent);
            }
        });

        buttonUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutMobileNo.setVisibility(View.GONE);
                layoutUsername.setVisibility(View.VISIBLE);

                buttonUsername.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_ui2));
                buttonMobileNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_disabled_ui2));

            }
        });
        buttonMobileNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutUsername.setVisibility(View.GONE);
                layoutMobileNo.setVisibility(View.VISIBLE);

                buttonMobileNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_ui2));
                buttonUsername.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_disabled_ui2));

            }
        });

        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiCallForRequestOTP(ForgotPasswordActivity_New.this);


            }
        });


        Spinner spinner = findViewById(R.id.mySpinner);

        SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.spinner_value_layout, textArray, imageArray);
        spinner.setAdapter(adapter);
    }

    private void apiCallForRequestOTP(Context context) {
        String username = etUsername.getText().toString();
        String mobileNo = etMobileNo.getText().toString();


        //String baseurl = "https://" + new SessionManager(getActivity()).getServerUrl() + ":3004";
        String baseurl = "https://" + "https://uiux.intelehealth.org:3005";

        ApiClientAppointment.getInstance(baseurl).getApi()
                .forgotPassword(username,
                        mobileNo)

                .enqueue(new Callback<ForgotPasswordApiResponseModel>() {
                    @Override
                    public void onResponse(Call<ForgotPasswordApiResponseModel> call, retrofit2.Response<ForgotPasswordApiResponseModel> response) {
                        if (response.body() == null) return;
                        ForgotPasswordApiResponseModel forgotPasswordApiResponseModel = response.body();

                        if (forgotPasswordApiResponseModel.getSuccess()) {
                            Toast.makeText(context, forgotPasswordApiResponseModel.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ForgotPasswordActivity_New.this, ForgotPasswordOtpVerificationActivity_New.class);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onFailure(Call<ForgotPasswordApiResponseModel> call, Throwable t) {
                        Log.v("onFailure", t.getMessage());
                    }
                });

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