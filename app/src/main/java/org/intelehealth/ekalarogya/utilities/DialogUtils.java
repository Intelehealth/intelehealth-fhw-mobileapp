package org.intelehealth.ekalarogya.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;

public class DialogUtils {

    public static void showOkDialog(Context context, String title, String message, String ok) {
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

    public static void showerrorDialog(Context context, String title, String message, String ok) {
        //AlertDialog alertDialog = new AlertDialog.Builder(context,R.style.AlertDialogStyle).create();
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(context.getResources().getString(R.string.button_ok),
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


}
