package org.intelehealth.app.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.app.R;
import org.intelehealth.app.adapter.ImagePickerListAdapter;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.visit.model.ReasonData;
import org.intelehealth.app.ayu.visit.reason.adapter.SelectedChipsPreviewGridAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DialogUtils {
    public interface CustomDialogListener {
        public static final int POSITIVE_CLICK = 0;
        public static final int NEGATIVE_CLICK = 1;
        public static final int CANCELLED = 2;

        public void onDialogActionDone(int action);
    }

    public interface ImagePickerDialogListener {
        public static final int CAMERA = 0;
        public static final int GALLERY = 1;
        public static final int CANCELLED = 2;

        public void onActionDone(int action);
    }

    public void showOkDialog(Context context, String title, String message, String ok) {
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(context);

        //AlertDialog alertDialog = new AlertDialog.Builder(context,R.style.AlertDialogStyle).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!((Activity) context).isFinishing() && !((Activity) context).isDestroyed()) {
                            dialog.dismiss();
                        }
                    }
                });
        AlertDialog dialog = SafeDialogUtil.showDialog(context, alertDialog);

        if(dialog != null){
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
        }
    }

    public void showerrorDialog(Context context, String title, String message, String ok) {
        //AlertDialog alertDialog = new AlertDialog.Builder(context,R.style.AlertDialogStyle).create();
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!((Activity) context).isFinishing() && !((Activity) context).isDestroyed()) {
                            dialog.dismiss();
                        }
                    }
                });
        AlertDialog dialog = SafeDialogUtil.showDialog(context, alertDialog);
        if(dialog != null){
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
        }
    }

    public static void patientRegistrationDialog(Context context, Drawable drawable, String title,
                                                 String subTitle, String positiveBtnTxt, String negativeBtnTxt,
                                                 CustomDialogListener customDialogListener) {

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
        alertDialog.setCancelable(false);

        negative_btn.setOnClickListener(v -> {
            SafeDialogUtil.dismissDialog(context, alertDialog);

            customDialogListener.onDialogActionDone(CustomDialogListener.NEGATIVE_CLICK);
        });

        positive_btn.setOnClickListener(v -> {
            SafeDialogUtil.dismissDialog(context, alertDialog);

            customDialogListener.onDialogActionDone(CustomDialogListener.POSITIVE_CLICK);
        });

        SafeDialogUtil.showDialog(context, alertDialog);
    }

    public void showCommonDialog(Context context, int iconResource, String title, String message,
                                 boolean isSingleButton, String positiveBtnText, String negativeBtnText,
                                 CustomDialogListener customDialogListener) {
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
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        negative_btn.setOnClickListener(v -> {
            SafeDialogUtil.dismissDialog(context, alertDialog);

            customDialogListener.onDialogActionDone(CustomDialogListener.NEGATIVE_CLICK);
        });

        positive_btn.setOnClickListener(v -> {
            SafeDialogUtil.dismissDialog(context, alertDialog);

            customDialogListener.onDialogActionDone(CustomDialogListener.POSITIVE_CLICK);
        });

        SafeDialogUtil.showDialog(context, alertDialog);
    }

    /**
     * added this non cancelable dialog as showCommonDialog is not cancelable
     * also didn't update the showCommonDialog as its being used in multiple places
     * @param context
     * @param iconResource
     * @param title
     * @param message
     * @param isSingleButton
     * @param positiveBtnText
     * @param negativeBtnText
     * @param customDialogListener
     */
    public void showCommonDialogNonCancelable(Context context, int iconResource, String title, String message,
                                              boolean isSingleButton, String positiveBtnText, String negativeBtnText,
                                              CustomDialogListener customDialogListener) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_incomplete_alert_message, null);
        alertdialogBuilder.setView(convertView);
        alertdialogBuilder.setCancelable(false);
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
        if(title==null || title.equalsIgnoreCase(""))
            dialog_title.setVisibility(View.GONE);
        dialog_subtitle.setText(message);
        positive_btn.setText(positiveBtnText);
        negative_btn.setText(negativeBtnText);

        if (isSingleButton) {
            negative_btn.setVisibility(View.GONE);
        }

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        negative_btn.setOnClickListener(v -> {
            SafeDialogUtil.dismissDialog(context, alertDialog);

            //customDialogListener.onDialogActionDone(CustomDialogListener.NEGATIVE_CLICK);
            //alertDialog.dismiss();
            if (customDialogListener != null)
                customDialogListener.onDialogActionDone(CustomDialogListener.NEGATIVE_CLICK);
        });

        positive_btn.setOnClickListener(v -> {
           /* if (!((Activity) context).isFinishing() && !((Activity) context).isDestroyed()) {
                //alertDialog.dismiss();
            }*/
            //customDialogListener.onDialogActionDone(CustomDialogListener.POSITIVE_CLICK);
            //alertDialog.dismiss();
            SafeDialogUtil.dismissDialog(context, alertDialog);

            if (customDialogListener != null)
                customDialogListener.onDialogActionDone(CustomDialogListener.POSITIVE_CLICK);
        });

        SafeDialogUtil.showDialog(context, alertDialog);
    }


    public void showCommonDialogWithChipsGrid(Context context, ArrayList<ReasonData> selectedData, int iconResource, String title, String message, boolean isSingleButton, String positiveBtnText, String negativeBtnText, CustomDialogListener customDialogListener) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_common_message_with_chips_grid, null);
        alertdialogBuilder.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.dialog_icon);
        TextView dialog_title = convertView.findViewById(R.id.dialog_title);
        TextView dialog_subtitle = convertView.findViewById(R.id.dialog_subtitle);
        Button positive_btn = convertView.findViewById(R.id.positive_btn);
        Button negative_btn = convertView.findViewById(R.id.negative_btn);
        RecyclerView recyclerView = convertView.findViewById(R.id.rcv_selected_container);

        icon.setImageResource(iconResource);
        dialog_title.setText(title);
        dialog_subtitle.setText(message);
        positive_btn.setText(positiveBtnText);
        negative_btn.setText(negativeBtnText);

        if (isSingleButton) {
            negative_btn.setVisibility(View.GONE);
        }

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        alertDialog.setCancelable(false);
        negative_btn.setOnClickListener(v -> {
            SafeDialogUtil.dismissDialog(context, alertDialog);

            customDialogListener.onDialogActionDone(CustomDialogListener.NEGATIVE_CLICK);
        });

        positive_btn.setOnClickListener(v -> {
            SafeDialogUtil.dismissDialog(context, alertDialog);

            customDialogListener.onDialogActionDone(CustomDialogListener.POSITIVE_CLICK);
        });

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(context);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);

        recyclerView.setLayoutManager(layoutManager);
        SelectedChipsPreviewGridAdapter selectedChipsPreviewGridAdapter = new SelectedChipsPreviewGridAdapter(recyclerView, context, selectedData, null);
        recyclerView.setAdapter(selectedChipsPreviewGridAdapter);

        SafeDialogUtil.showDialog(context, alertDialog);
    }

    public MaterialAlertDialogBuilder showErrorDialogWithTryAgainButton(Context context, Drawable drawable, String title, String message, String buttonText) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_error_and_button, null);
        builder.setView(dialogView);

        ImageView dialogIcon = dialogView.findViewById(R.id.dialog_icon);
        TextView tvTitle = dialogView.findViewById(R.id.dialog_title);
        TextView tvSubtitle = dialogView.findViewById(R.id.dialog_subtitle);
        Button tryAgainButton = dialogView.findViewById(R.id.positive_btn);

        dialogIcon.setImageDrawable(drawable);
        tvTitle.setText(title);
        tvSubtitle.setText(message);
        tryAgainButton.setText(buttonText);

        return builder;
    }

    public AlertDialog showCommonLoadingDialog(Context context, String title, String message) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_common_loading_message, null);
        alertdialogBuilder.setView(convertView);
        TextView dialog_title = convertView.findViewById(R.id.dialog_title);
        TextView dialog_subtitle = convertView.findViewById(R.id.dialog_subtitle);
        dialog_title.setText(title);
        dialog_subtitle.setText(message);
        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);


        SafeDialogUtil.showDialog(context, alertDialog);
        return alertDialog;
    }

    public static AlertDialog showCommonImagePickerDialog(Context context, String title, ImagePickerDialogListener imagePickerDialogListener) {
        List<String> displaySelection = new ArrayList<>();
        String[] options = {context.getString(R.string.take_photo), context.getString(R.string.choose_from_gallery), context.getString(R.string.cancel)};
        displaySelection = Arrays.asList(options);
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(context);
        View convertView = View.inflate(context, R.layout.image_picker_dialog, null);
        alertDialogBuilder.setView(convertView);

        RecyclerView recyclerView = convertView.findViewById(R.id.rcvDialogImagePicker);
        TextView titleView = convertView.findViewById(R.id.tvTitleDialogImagePicker);
        titleView.setText(title);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        ImagePickerListAdapter dialogListAdapter = new ImagePickerListAdapter(recyclerView, context, displaySelection, new ImagePickerDialogListener() {
            @Override
            public void onActionDone(int action) {
                imagePickerDialogListener.onActionDone(action);
            }
        });
        recyclerView.setAdapter(dialogListAdapter);
        AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.popup_menu_background);
        return alertDialog;
    }

    public void triggerEndAppointmentConfirmationDialog(Context activityContext, CustomDialogListener customDialogListener) {
        showCommonDialog(
                activityContext,
                R.drawable.dialog_close_visit_icon,
                activityContext.getResources().getString(R.string.confirm_cancel_appointment),
                activityContext.getResources().getString(R.string.confirm_cancel_appointment_message),
                false,
                activityContext.getResources().getString(R.string.confirm),
                activityContext.getResources().getString(R.string.cancel),
                customDialogListener
        );
    }

    public AlertDialog showSyncDialog(Context context, Resources resources) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View customLayout = inflater.inflate(R.layout.ui2_layout_dialog_refresh, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setView(customLayout);
        int width = resources.getDimensionPixelSize(R.dimen.internet_dialog_width);

        AlertDialog dialogRefreshInProgress = builder.create();
        if (dialogRefreshInProgress.getWindow() != null) {
            dialogRefreshInProgress.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
            dialogRefreshInProgress.show();
            dialogRefreshInProgress.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        return dialogRefreshInProgress;
    }

    public void displayPrescriptionNotReceivedDialog(Context activityContext) {
        MaterialAlertDialogBuilder builder = new DialogUtils().showErrorDialogWithTryAgainButton(
                activityContext,
                ContextCompat.getDrawable(activityContext, R.drawable.close_patient_svg),
                ContextCompat.getString(activityContext, R.string.prescription_not_received),
                ContextCompat.getString(activityContext, R.string.please_wait_for_the_doctor_to_share_the_prescription_before_closing_the_visit),
                ContextCompat.getString(activityContext, R.string.generic_ok)
        );

        AlertDialog dialog = builder.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            int width = activityContext.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
            dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        Button okButton = dialog.findViewById(R.id.positive_btn);
        if (okButton != null) okButton.setOnClickListener(v ->  SafeDialogUtil.dismissDialog(activityContext, dialog));
    }
}
