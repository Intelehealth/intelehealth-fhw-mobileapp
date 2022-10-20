package org.intelehealth.app.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.app.IntelehealthApplication;

public class DialogUtils {

    public void showOkDialog(Context context, String title, String message, String ok) {
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(context);

        //AlertDialog alertDialog = new AlertDialog.Builder(context,R.style.AlertDialogStyle).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = alertDialog.show();
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    public void showerrorDialog(Context context, String title, String message, String ok) {
        //AlertDialog alertDialog = new AlertDialog.Builder(context,R.style.AlertDialogStyle).create();
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = alertDialog.show();
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    public static void patientRegistrationDialog(Context context, Drawable drawable, String title, String subTitle, String positiveBtnTxt, String negativeBtnTxt) {
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
       /* dialog_title.setText("Close patient registration?");
        dialog_subtitle.setText("Are you sure you want to close the patient registration?");
        positive_btn.setText("No");
        negative_btn.setText("Yes");*/

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
            Intent i_back = new Intent(context.getApplicationContext(), HomeScreenActivity_New.class);
            context.startActivity(i_back);
        });

        positive_btn.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        alertDialog.show();
    }


}
