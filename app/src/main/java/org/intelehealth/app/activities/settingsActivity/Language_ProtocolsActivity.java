package org.intelehealth.app.activities.settingsActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.chooseLanguageActivity.SplashScreenActivity;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.utilities.SessionManager;

import java.util.Locale;

public class Language_ProtocolsActivity extends AppCompatActivity {
    private Spinner lang_spinner;
    private ImageButton reset_btn, update_protocols_btn;
    private String selected_language = "English";
    private Context context;
    private ArrayAdapter<String> langAdapter;
    private SessionManager sessionManager = null;
    private CardView snackbar_cv;
    private TextView snackbar_text;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_protocols);

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        initUI();
        clickListeners();
    }

    private void initUI() {
        context = Language_ProtocolsActivity.this;
        sessionManager = new SessionManager(context);
        lang_spinner = findViewById(R.id.lang_spinner);
        reset_btn = findViewById(R.id.reset_btn);
        snackbar_cv = findViewById(R.id.snackbar_cv);
        snackbar_text = findViewById(R.id.snackbar_text);
        update_protocols_btn = findViewById(R.id.update_protocols_btn);
    }

    private void clickListeners() {
        // language spinner - start
        langAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.language_names));
        lang_spinner.setAdapter(langAdapter); // setting up language spinners.

        String l = sessionManager.getAppLanguage();
        int i = langAdapter.getPosition(sessionManager.getAppLanguage());
        if (!sessionManager.getAppLanguage().equalsIgnoreCase(""))
            lang_spinner.setSelection(langAdapter.getPosition(sessionManager.getAppLanguage()));
        else
            lang_spinner.setSelection(langAdapter.getPosition("English"));

        lang_spinner.setSelection(i,false);
        lang_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                if (index != 0) {
                    selected_language = adapterView.getItemAtPosition(index).toString();
                    Log.v("Langauge", "selection: " + selected_language);
                    String message = "Are you sure you want to change language to " + selected_language + "?";
                    dialog(context, getResources().getDrawable(R.drawable.close_patient_svg), "Change language?",
                            message, "Yes", "No");

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Reset button.
        reset_btn.setOnClickListener(v -> {
            lang_spinner.setSelection(langAdapter.getPosition("English"));
            showSnackBarAndRemoveLater("English");
        });

        // language spinner - end
    }


    // Dialog - start
    public void dialog(Context context, Drawable drawable, String title, String subTitle,
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
            lang_spinner.setSelection(langAdapter.getPosition("English"));
            alertDialog.dismiss();
        });

        positive_btn.setOnClickListener(v -> {
            // setting app language here...
            if (!selected_language.equalsIgnoreCase("")) {
                setLocale(selected_language);
            }
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    // Dialog - end

    /**
     * Setting the language selected by user as app language.
     * @param appLanguage
     */
    public void setLocale(String appLanguage) {
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        sessionManager.setAppLanguage(appLanguage);

        // show snackbar view
        showSnackBarAndRemoveLater(appLanguage);
    }

    // show snackbar
    private void showSnackBarAndRemoveLater(String appLanguage) {
        snackbar_cv.setVisibility(View.VISIBLE);
        snackbar_text.setText("Language successfully changed to " + appLanguage + "!");
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                snackbar_cv.setVisibility(View.GONE);
            }
        }, 4000);
    }


}