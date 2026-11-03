package org.intelehealth.app.ayu.visit.ncd;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.app.R;

import java.util.List;

/** The Eligible / Not eligible / Urgent attention pop-up shown after "View Eligibility". */
final class NcdEligibilityResultDialog {

    private NcdEligibilityResultDialog() {
    }

    static void show(Context context, NcdEligibilityState state, List<String> symptomNames, Runnable onContinue) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_ncd_eligibility_result, null);
        ImageView icon = view.findViewById(R.id.iv_icon);
        TextView title = view.findViewById(R.id.tv_title);
        TextView message = view.findViewById(R.id.tv_message);
        Button negative = view.findViewById(R.id.btn_negative);
        Button positive = view.findViewById(R.id.btn_positive);

        switch (state.getResult()) {
            case URGENT:
                title.setText(R.string.ncd_urgent_title);
                message.setVisibility(View.GONE);
                view.findViewById(R.id.ll_symptoms).setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.tv_symptoms)).setText(TextUtils.join(", ", symptomNames));
                view.findViewById(R.id.tv_instruction).setVisibility(View.VISIBLE);
                negative.setVisibility(View.GONE);
                positive.setText(R.string.ncd_acknowledge_and_exit);
                break;
            case NOT_ELIGIBLE:
                title.setText(R.string.ncd_not_eligible_title);
                message.setText(R.string.ncd_not_eligible_message);
                view.findViewById(R.id.ll_reason).setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.tv_reason)).setText(reasonText(state));
                // Figma: Continue slightly wider than Cancel (about 130:117)
                ((LinearLayout.LayoutParams) positive.getLayoutParams()).weight = 1.11f;
                break;
            default:
                icon.setImageResource(R.drawable.ic_ncd_eligible);
                title.setText(R.string.ncd_eligible_title);
                message.setText(R.string.ncd_eligible_message);
                positive.setText(R.string.ncd_continue_to_cbac);
                // Figma: narrow Cancel, wide "Continue to CBAC" (about 78:170)
                ((LinearLayout.LayoutParams) positive.getLayoutParams()).weight = 2.2f;
                break;
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

    private static int reasonText(NcdEligibilityState state) {
        boolean pregnant = Boolean.TRUE.equals(state.getPregnant());
        if (pregnant && state.isMultimorbidity()) return R.string.ncd_reason_pregnant_and_multimorbidity;
        return pregnant ? R.string.ncd_reason_pregnant : R.string.ncd_reason_multimorbidity;
    }
}
