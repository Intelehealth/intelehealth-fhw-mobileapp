package org.intelehealth.abdm.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.abdm.R;

import java.util.Objects;

import kotlin.jvm.JvmStatic;

public class DialogUtils {

    public interface CustomDialogListener {
        public static final int POSITIVE_CLICK = 0;
        public static final int NEGATIVE_CLICK = 1;
        public static final int CANCELLED = 2;

        public void onDialogActionDone(int action);
    }

    public interface TextSelectedListener extends CustomDialogListener {
        void onDialogActionDone(int action, String text);
    }

    @JvmStatic
    public static void setAlertDialogCustomTheme(Context context, Dialog builderDialog) {
        TextView textView = builderDialog.getWindow().findViewById(android.R.id.message);
        TextView alertTitle = builderDialog.getWindow().findViewById(androidx.appcompat.R.id.alertTitle);
        Button button1 = builderDialog.getWindow().findViewById(android.R.id.button1);
        Button button2 = builderDialog.getWindow().findViewById(android.R.id.button2);
        textView.setTypeface(ResourcesCompat.getFont(context, R.font.lato_regular));
        alertTitle.setTypeface(ResourcesCompat.getFont(context, R.font.lato_bold));
        button1.setTypeface(ResourcesCompat.getFont(context, R.font.lato_bold));
        button2.setTypeface(ResourcesCompat.getFont(context, R.font.lato_bold));
    }

    public static void showOkDialog(Context context, Drawable drawable, String title, String subTitle, String okBtnTxt, CustomDialogListener customDialogListener) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_error_and_button, null);
        alertdialogBuilder.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.dialog_icon);
        TextView dialog_title = convertView.findViewById(R.id.dialog_title);
        TextView dialog_subtitle = convertView.findViewById(R.id.dialog_subtitle);
        Button neutral_btn = convertView.findViewById(R.id.positive_btn);

        icon.setImageDrawable(drawable);
        dialog_title.setText(title);
        dialog_subtitle.setText(subTitle);
        neutral_btn.setText(okBtnTxt);

        AlertDialog alertDialog = alertdialogBuilder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.bg_common_dialog); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        neutral_btn.setOnClickListener(v -> {
            alertDialog.dismiss();
            if (customDialogListener != null)
                customDialogListener.onDialogActionDone(CustomDialogListener.POSITIVE_CLICK);
        });

        alertDialog.show();
    }

    public void showCommonDialog(Context context, int iconResource, String title, String message, boolean isSingleButton, String positiveBtnText, String negativeBtnText, CustomDialogListener customDialogListener) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_common_message, null);
        alertdialogBuilder.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.dialog_icon);
        TextView dialog_title = convertView.findViewById(R.id.dialog_title);
        TextView dialog_subtitle = convertView.findViewById(R.id.dialog_subtitle);
        Button positive_btn = convertView.findViewById(R.id.positive_btn);
        Button negative_btn = convertView.findViewById(R.id.negative_btn);

        if (iconResource == 0) icon.setVisibility(View.GONE);
        if (message == null || message.equalsIgnoreCase(""))
            dialog_subtitle.setVisibility(View.GONE);
        icon.setImageResource(iconResource);
        dialog_title.setText(title);
        dialog_subtitle.setText(message);
        positive_btn.setText(positiveBtnText);
        negative_btn.setText(negativeBtnText);

        if (isSingleButton) {
            negative_btn.setVisibility(View.GONE);
        }

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog_rounded_corners); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        negative_btn.setOnClickListener(v -> {
            alertDialog.dismiss();
            customDialogListener.onDialogActionDone(CustomDialogListener.NEGATIVE_CLICK);
        });

        positive_btn.setOnClickListener(v -> {
            alertDialog.dismiss();
            customDialogListener.onDialogActionDone(CustomDialogListener.POSITIVE_CLICK);
        });

        alertDialog.show();
    }
}
