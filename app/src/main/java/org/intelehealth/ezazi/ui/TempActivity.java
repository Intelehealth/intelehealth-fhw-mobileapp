package org.intelehealth.ezazi.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.ajalt.timberkt.Timber;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.ui.dialog.MultiChoiceDialogFragment;
import org.intelehealth.ezazi.ui.dialog.ThemeTimePickerDialog;
import org.intelehealth.ezazi.ui.dialog.adapter.RiskFactorMultiChoiceAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        TextView textView = findViewById(R.id.txtContent);
        textView.post(() -> Timber.tag(TempActivity.class.getName()).d("Line count::%s", textView.getLineCount()));

//        ThemeTimePickerDialog dialog = new ThemeTimePickerDialog.Builder(this)
//                .title(R.string.current_time)
//                .positiveButtonLabel(R.string.ok)
//                .build();
//        dialog.setListener((hours, minutes, amPm, value) -> {
//            Log.d("ThemeTimePickerDialog", "value : " + value);
//        });
//        dialog.show(getSupportFragmentManager(), "ThemeTimePickerDialog");


//        MultiChoiceDialogFragment<String> dialog = new MultiChoiceDialogFragment.Builder<String>(this)
//                .title(R.string.select_risk_factors)
//                .positiveButtonLabel(R.string.save_button)
//                .build();
//
//        List<String> items = Arrays.asList(getResources().getStringArray(R.array.risk_factors));
//
//        dialog.setAdapter(new RiskFactorMultiChoiceAdapter(this, new ArrayList<>(items)));
//        dialog.setListener(selectedItems -> {
//            // Todo get all selected item here
//        });
//
//        dialog.show(getSupportFragmentManager(), MultiChoiceDialogFragment.class.getCanonicalName());
    }
}
