package org.intelehealth.app.ui2.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.R;

public class TestDialogsUI2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_dialogs_ui2);

        showCustomDialog();
    }

    private void showCustomDialog() {
        AlertDialog.Builder builder
                = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        final View customLayout = getLayoutInflater().inflate(R.layout.ui2_layout_dialog_internet_warning, null);
        builder.setView(customLayout);

        //EditText etTask = customLayout.findViewById(R.id.et_task_reminder);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}