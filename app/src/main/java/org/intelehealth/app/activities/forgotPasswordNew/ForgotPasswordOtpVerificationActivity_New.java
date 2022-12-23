package org.intelehealth.app.activities.forgotPasswordNew;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.setupActivity.SetupActivityNew;
import org.intelehealth.app.utilities.SnackbarUtils;


public class ForgotPasswordOtpVerificationActivity_New extends AppCompatActivity {
    private static final String TAG = "OtpVerificationActivity";
    //temporary OTP is hardcode
    String OTP = "111111";
    String userUuid = "";
    TextView tvOtpError, tvResendOtp;
    EditText etPin1, etPin2, etPin3, etPin4, etPin5, etPin6;
    LinearLayout layoutParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification_ui2);

        SnackbarUtils snackbarUtils = new SnackbarUtils();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userUuid = extras.getString("userUuid");
        }
        Button buttonVerifyOtp = findViewById(R.id.button_verify_otp);
        LinearLayout layoutPinView = findViewById(R.id.pinview_otp);

        etPin1 = layoutPinView.findViewById(R.id.et_pin_1);
        etPin2 = layoutPinView.findViewById(R.id.et_pin_2);
        etPin3 = layoutPinView.findViewById(R.id.et_pin_3);
        etPin4 = layoutPinView.findViewById(R.id.et_pin_4);
        etPin5 = layoutPinView.findViewById(R.id.et_pin_5);
        etPin6 = layoutPinView.findViewById(R.id.et_pin_6);
        tvOtpError = findViewById(R.id.tv_otp_error);
        tvResendOtp = findViewById(R.id.textview_no_otp);
        layoutParent = findViewById(R.id.layout_parent_otp);


        tvResendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //api call for resend otp request
                // snackbarUtils.showSnacksWithRelativeLayoutSuccess(ForgotPasswordOtpVerificationActivity_New.this, getResources().getString(R.string.otp_sent_success), layoutParent);


            }
        });

        ImageView ivBack = findViewById(R.id.imageview_back_otp_verify);
        ivBack.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordOtpVerificationActivity_New.this, SetupActivityNew.class);
            startActivity(intent);
        });


        buttonVerifyOtp.setOnClickListener(v -> {
            String pin1 = etPin1.getText().toString();
            String pin2 = etPin2.getText().toString();
            String pin3 = etPin3.getText().toString();
            String pin4 = etPin4.getText().toString();
            String pin5 = etPin5.getText().toString();
            String pin6 = etPin6.getText().toString();

           /* Intent intent = new Intent(ForgotPasswordOtpVerificationActivity_New.this, ResetPasswordActivity_New.class);
            intent.putExtra("otp", "111111");
            intent.putExtra("userUuid", userUuid);
            startActivity(intent);*/
            if (!pin1.isEmpty() && !pin2.isEmpty() && !pin3.isEmpty() && !pin4.isEmpty() && !pin5.isEmpty() && !pin6.isEmpty()) {
                tvOtpError.setVisibility(View.GONE);

                String otp = pin1 + pin2 + pin3 + pin4 + pin5 + pin6;

                if (otp.equals("111111")) {
                    Intent intent = new Intent(ForgotPasswordOtpVerificationActivity_New.this, ResetPasswordActivity_New.class);
                    intent.putExtra("otp", otp);
                    intent.putExtra("userUuid", userUuid);
                    startActivity(intent);
                    finish();

                } else {
                    tvOtpError.setVisibility(View.VISIBLE);
                    tvOtpError.setText("OTP is incorrect!");

                }

            } else {
                tvOtpError.setVisibility(View.VISIBLE);
            }
        });

        handleEditextFocus();
        resendOtp();

    }


    private void handleEditextFocus() {

        //for focus to the next edittext
        etPin1.addTextChangedListener(new GenericTextWatcher(etPin1, etPin2));
        etPin2.addTextChangedListener(new GenericTextWatcher(etPin2, etPin3));
        etPin3.addTextChangedListener(new GenericTextWatcher(etPin3, etPin4));
        etPin4.addTextChangedListener(new GenericTextWatcher(etPin4, etPin5));
        etPin5.addTextChangedListener(new GenericTextWatcher(etPin5, etPin6));
        etPin6.addTextChangedListener(new GenericTextWatcher(etPin6, etPin6));

        //for focus to the previous edittext
        etPin1.setOnKeyListener(new GenericKeyEvent(etPin1, etPin1));
        etPin2.setOnKeyListener(new GenericKeyEvent(etPin2, etPin1));
        etPin3.setOnKeyListener(new GenericKeyEvent(etPin3, etPin2));
        etPin4.setOnKeyListener(new GenericKeyEvent(etPin4, etPin3));
        etPin5.setOnKeyListener(new GenericKeyEvent(etPin5, etPin4));
        etPin6.setOnKeyListener(new GenericKeyEvent(etPin6, etPin5));

    }

    class GenericKeyEvent implements View.OnKeyListener {
        EditText currentView;
        EditText previousView;

        public GenericKeyEvent(EditText currentView, EditText previousView) {
            this.currentView = currentView;
            this.previousView = previousView;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL &&
                    currentView.getText().toString().isEmpty()) {
                previousView.setText("");
                previousView.requestFocus();
                return true;
            }
            return false;
        }
    }

    public class GenericTextWatcher implements TextWatcher {
        private EditText etPrev;
        private EditText etNext;
        StringBuilder sb = new StringBuilder();

        public GenericTextWatcher(EditText etPrev, EditText etNext) {
            this.etPrev = etPrev;
            this.etNext = etNext;

        }

        @Override
        public void afterTextChanged(Editable editable) {

            if (sb.length() == 0) {

                etPrev.requestFocus();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            if (sb.length() == 1) {

                sb.deleteCharAt(0);

            }
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            if (sb.length() == 0 & etPrev.length() == 1) {
                sb.append(arg0);
                etPrev.clearFocus();

                if (etNext != null) {
                    etNext.requestFocus();
                    etNext.setCursorVisible(true);
                }

            }

        }
    }

    private void resendOtp() {
        tvResendOtp.setEnabled(false);
        String resendTime = getResources().getString(R.string.resend_otp_in);
        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                String time = resendTime + " " + millisUntilFinished / 1000 + " seconds";
                tvResendOtp.setText(time);
            }

            public void onFinish() {
                tvResendOtp.setEnabled(true);

                etPin1.setText("");
                etPin2.setText("");
                etPin3.setText("");
                etPin4.setText("");
                etPin5.setText("");
                etPin6.setText("");
                tvResendOtp.setText(getResources().getString(R.string.resend_otp));

            }

        }.start();
    }


}