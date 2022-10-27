package org.intelehealth.app.activities.visitSummaryActivity;

import static org.intelehealth.app.utilities.DialogUtils.patientRegistrationDialog;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;

public class VisitSummaryActivity_New extends AppCompatActivity {
    Button btn_vs_sendvisit;
    private Context context;
    private ImageButton btn_up_header, btn_up_vitals_header, btn_up_visitreason_header,
            btn_up_phyexam_header, btn_up_medhist_header, openall_btn;
    private RelativeLayout vs_header_expandview, vs_vitals_header_expandview,
            vs_visitreason_header_expandview, vs_phyexam_header_expandview, vs_medhist_header_expandview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_summary_new);
        context = VisitSummaryActivity_New.this;

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        initUI();
        expandableCardVisibilityHandling();

        btn_vs_sendvisit.setOnClickListener(v -> {
            visitSendDialog(context, getResources().getDrawable(R.drawable.dialog_close_visit_icon), "Send visit?",
                    "Are you sure you want to send the visit to the doctor?",
                    "Yes", "No");
        });

    }

    private void expandableCardVisibilityHandling() {
        openall_btn.setOnClickListener(v -> {

            Drawable drawable = openall_btn.getDrawable();
            if (drawable.getConstantState().equals(getResources().getDrawable(R.drawable.open_all_btn).getConstantState())) {
                openall_btn.setImageDrawable(getResources().getDrawable(R.drawable.close_all_btn));
                vs_vitals_header_expandview.setVisibility(View.VISIBLE);
                vs_visitreason_header_expandview.setVisibility(View.VISIBLE);
                vs_phyexam_header_expandview.setVisibility(View.VISIBLE);
                vs_medhist_header_expandview.setVisibility(View.VISIBLE);
            }
            else {
                openall_btn.setImageDrawable(getResources().getDrawable(R.drawable.open_all_btn));
                vs_vitals_header_expandview.setVisibility(View.GONE);
                vs_visitreason_header_expandview.setVisibility(View.GONE);
                vs_phyexam_header_expandview.setVisibility(View.GONE);
                vs_medhist_header_expandview.setVisibility(View.GONE);
            }

        });

        btn_up_header.setOnClickListener(v -> {
            if (vs_header_expandview.getVisibility() == View.VISIBLE)
                vs_header_expandview.setVisibility(View.GONE);
            else
                vs_header_expandview.setVisibility(View.VISIBLE);
        });

        btn_up_vitals_header.setOnClickListener(v -> {
            if (vs_vitals_header_expandview.getVisibility() == View.VISIBLE)
                vs_vitals_header_expandview.setVisibility(View.GONE);
            else
                vs_vitals_header_expandview.setVisibility(View.VISIBLE);
        });

        btn_up_visitreason_header.setOnClickListener(v -> {
            if (vs_visitreason_header_expandview.getVisibility() == View.VISIBLE)
                vs_visitreason_header_expandview.setVisibility(View.GONE);
            else
                vs_visitreason_header_expandview.setVisibility(View.VISIBLE);
        });

        btn_up_phyexam_header.setOnClickListener(v -> {
            if (vs_phyexam_header_expandview.getVisibility() == View.VISIBLE)
                vs_phyexam_header_expandview.setVisibility(View.GONE);
            else
                vs_phyexam_header_expandview.setVisibility(View.VISIBLE);
        });

        btn_up_medhist_header.setOnClickListener(v -> {
            if (vs_medhist_header_expandview.getVisibility() == View.VISIBLE)
                vs_medhist_header_expandview.setVisibility(View.GONE);
            else
                vs_medhist_header_expandview.setVisibility(View.VISIBLE);
        });
    }

    private void initUI() {
        btn_up_header = findViewById(R.id.btn_up_header);
        openall_btn = findViewById(R.id.openall_btn);
        btn_up_vitals_header = findViewById(R.id.btn_up_vitals_header);
        btn_up_visitreason_header = findViewById(R.id.btn_up_visitreason_header);
        btn_up_phyexam_header = findViewById(R.id.btn_up_phyexam_header);
        btn_up_medhist_header = findViewById(R.id.btn_up_medhist_header);

        vs_header_expandview = findViewById(R.id.vs_header_expandview);
        vs_vitals_header_expandview = findViewById(R.id.vs_vitals_header_expandview);
        vs_visitreason_header_expandview = findViewById(R.id.vs_visitreason_header_expandview);
        vs_phyexam_header_expandview = findViewById(R.id.vs_phyexam_header_expandview);
        vs_medhist_header_expandview = findViewById(R.id.vs_medhist_header_expandview);

        btn_vs_sendvisit = findViewById(R.id.btn_vs_sendvisit);
    }

    private void visitSendDialog(Context context, Drawable drawable, String title, String subTitle,
                    String positiveBtnTxt, String negativeBtnTxt) {

            MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
            final LayoutInflater inflater = LayoutInflater.from(context);
            View convertView = inflater.inflate(R.layout.dialog_patient_registration, null);
            alertdialogBuilder.setView(convertView);
            ImageView icon = convertView.findViewById(R.id.dialog_icon);
            TextView dialog_title = convertView.findViewById(R.id.dialog_title);
            TextView dialog_subtitle = convertView.findViewById(R.id.dialog_subtitle);
            Button positive_btn = convertView.findViewById(R.id.positive_btn);
            Button negative_btn = convertView.findViewById(R.id.negative_btn);

            icon.setImageDrawable(drawable);
            dialog_title.setText(title);
            dialog_subtitle.setText(subTitle);
            positive_btn.setText(positiveBtnTxt);
            negative_btn.setText(negativeBtnTxt);

            AlertDialog alertDialog = alertdialogBuilder.create();
            alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
            int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
            alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

            negative_btn.setOnClickListener(v -> {
                alertDialog.dismiss();
            });

            positive_btn.setOnClickListener(v -> {
                alertDialog.dismiss();
                visitSentSuccessDialog(context, getResources().getDrawable(R.drawable.dialog_visit_sent_success_icon),
                        "Visit successfully sent!",
                        "Patient's visit has been successfully sent to the doctor.",
                        "Okay");
            });

            alertDialog.show();
    }

    private void visitSentSuccessDialog(Context context, Drawable drawable, String title, String subTitle,
                                        String neutral) {

        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_patient_registration, null);
        alertdialogBuilder.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.dialog_icon);
        TextView dialog_title = convertView.findViewById(R.id.dialog_title);
        TextView dialog_subtitle = convertView.findViewById(R.id.dialog_subtitle);
        Button positive_btn = convertView.findViewById(R.id.positive_btn);
        Button negative_btn = convertView.findViewById(R.id.negative_btn);
        negative_btn.setVisibility(View.GONE);  // as this view requires only one button so other button has hidden.

        icon.setImageDrawable(drawable);
        dialog_title.setText(title);
        dialog_subtitle.setText(subTitle);
        positive_btn.setText(neutral);

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);


        positive_btn.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        alertDialog.show();
    }
}