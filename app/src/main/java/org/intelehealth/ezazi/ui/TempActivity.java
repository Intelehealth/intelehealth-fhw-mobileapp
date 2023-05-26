package org.intelehealth.ezazi.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.ui.dialog.ThemeTimePickerDialog;

/**
 * Created by Vaghela Mithun R. on 23-05-2023 - 16:04.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class TempActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);
        new ThemeTimePickerDialog.Builder(this)
                .title(R.string.current_time)
                .positiveButtonLabel(R.string.ok)
                .build().show(getSupportFragmentManager(), "ThemeTimePickerDialog");
    }
}
