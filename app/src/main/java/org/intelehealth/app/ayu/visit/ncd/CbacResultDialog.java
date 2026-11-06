package org.intelehealth.app.ayu.visit.ncd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.app.R;

/** The CBAC result pop-up shown after "View result": score above 4, or 4 and below. */
final class CbacResultDialog {

    private CbacResultDialog() {
    }

    static void show(Context context, boolean highRisk, Runnable onContinue) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_cbac_result, null);
        ImageView icon = view.findViewById(R.id.iv_icon);
        TextView title = view.findViewById(R.id.tv_title);
        TextView message = view.findViewById(R.id.tv_message);
        Button positive = view.findViewById(R.id.btn_positive);
        Button negative = view.findViewById(R.id.btn_negative);

        if (highRisk) {
            icon.setImageResource(R.drawable.ic_cbac_high_risk);
            title.setText(R.string.cbac_high_risk_title);
            message.setText(R.string.cbac_high_risk_message);
            positive.setText(R.string.cbac_proceed_to_screening);
        } else {
            // Figma "<4 popup": same green clipboard as the Eligible pop-up
            icon.setImageResource(R.drawable.ic_ncd_eligible);
            title.setText(R.string.cbac_low_risk_title);
            message.setText(R.string.cbac_low_risk_message);
            positive.setText(R.string.cbac_proceed_to_vitals);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(context).setView(view).create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
        dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        view.findViewById(R.id.iv_close).setOnClickListener(v -> dialog.dismiss());
        negative.setOnClickListener(v -> dialog.dismiss());
        positive.setOnClickListener(v -> {
            dialog.dismiss();
            onContinue.run();
        });
        dialog.show();
    }
}
