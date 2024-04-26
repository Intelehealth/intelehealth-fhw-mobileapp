package org.intelehealth.nak.activities.setupActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.intelehealth.nak.R;

public class CustomDialogsForSetupUI2 extends DialogFragment {
    Context context;

    public CustomDialogsForSetupUI2(Context context) {
        this.context = context;
    }

    public void showLoggingInDialog() {
        AlertDialog.Builder builder
                = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        LayoutInflater inflater = LayoutInflater.from(context);
        View customLayout = inflater.inflate(R.layout.ui2_layout_dialog_logging_in, null);
        builder.setView(customLayout);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
        dialog.show();
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);

        dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

    }
}


