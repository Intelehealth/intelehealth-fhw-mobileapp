package io.intelehealth.client.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;

import io.intelehealth.client.R;

public class DialogUtils {

    public void showOkDialog(Context context, String title, String message, String ok) {
        AlertDialog alertDialog = new AlertDialog.Builder(context,R.style.AlertDialogStyle).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    }

    public void showerrorDialog(Context context, String title, String message, String ok) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
    }



}
