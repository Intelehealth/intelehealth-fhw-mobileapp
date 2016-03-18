package edu.jhu.bme.cbid.healthassistantsclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class IdentificationActivity extends AppCompatActivity {

    //Demographic acquisition screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);

        Button identificationButton = (Button) findViewById(R.id.identificationSubmitButton);
        identificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitIdentifiers();
            }
        });

    }

    public void submitIdentifiers() {
        ViewGroup identifiersLayout = (ViewGroup) findViewById(R.id.identificationTable);
        for (int i = 0; i < identifiersLayout.getChildCount(); i++) {
            View view = identifiersLayout.getChildAt(i);
            if (view instanceof EditText) {
                String buffer = ((EditText) view).getText().toString();
                //TODO: write query to write into DB
                //use view.getTag() to get the DB column/field/idunno what they're called
            }
        }

        CheckBox maleCheckBox = (CheckBox) findViewById(R.id.maleCheckBox);
        CheckBox femaleCheckBox = (CheckBox) findViewById(R.id.femaleCheckBox);

        if (maleCheckBox.isChecked()) {
            //TODO: write male to gender query
            //also stored in maleCheckBox.getTag()
        } else if (femaleCheckBox.isChecked()) {
            //TODO: write female to gender query
            //also stored in femaleCheckBox.getTag()
        }

        //TODO: query to write gender into DB

        //TODO: upload identifiers to OpenMRS using service
    }
}
