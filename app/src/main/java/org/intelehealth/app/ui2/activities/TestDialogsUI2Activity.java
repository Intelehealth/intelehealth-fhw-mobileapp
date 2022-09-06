package org.intelehealth.app.ui2.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.R;

public class TestDialogsUI2Activity extends AppCompatActivity {
    Dialog dialogLoggingIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_dialogs_ui2);

        showLoggingInDialog();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialogLoggingIn.dismiss();
            }
        }, 2000);
    }



    public void showLoggingInDialog() {
        AlertDialog.Builder builder
                = new AlertDialog.Builder(TestDialogsUI2Activity.this);
        builder.setCancelable(false);
        LayoutInflater inflater = LayoutInflater.from(TestDialogsUI2Activity.this);
        View customLayout = inflater.inflate(R.layout.ui2_layout_dialog_logging_in, null);
        builder.setView(customLayout);

         dialogLoggingIn = builder.create();
        dialogLoggingIn.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
        dialogLoggingIn.show();
        int width = getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);

        dialogLoggingIn.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

    }


}