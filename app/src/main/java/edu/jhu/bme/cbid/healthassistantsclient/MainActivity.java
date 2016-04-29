package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ImageButton newPtButton = (ImageButton) findViewById(R.id.newPatientButton);
        newPtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewPatient();
            }
        });


        ImageButton findPatientButton = (ImageButton) findViewById(R.id.findPatientsButton);
        findPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findPatient();
            }
        });

        ImageButton diagnosticServicesButton = (ImageButton) findViewById(R.id.diagnosticServicesButton);
        diagnosticServicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDiagnosticService();
            }
        });
//
//        Button activePatientsButton = (Button) findViewById(R.id.activePatientsButton);
//        activePatientsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                searchActivePatients();
//            }
//        });

        // The INSERT and UPDATE/DELETE commands require SQL statements to be passed as parameters
        // TODO: Use SQL statements from schema


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.syncOption:
                refreshDatabases();
                return true;
            case R.id.settingsOption:
                settings();
                return true;
            case R.id.endOfDayOption:
                endOfDay();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startNewPatient() {
        Intent intent = new Intent(this, IdentificationActivity.class);
        startActivity(intent);
    }

    public void findPatient() {
        Intent intent = new Intent(this, SearchPatientActivity.class);
        startActivity(intent);
    }

    public void selectDiagnosticService() {
        Intent intent = new Intent(this, DiagnosticTestsSelectionActivity.class);
        startActivity(intent);
    }

    public void searchActivePatients() {
        Intent intent = new Intent(this, ActivePatientActivity.class);
        startActivity(intent);
    }

    public void refreshDatabases() {
        // TODO: write function to sync the patients within a specific location
        // Bandwidth heavy task
    }

    public void settings() {
        // TODO: WTF are settings?
    }

    public void endOfDay() {
        // TODO: sync all the data recorded to EHR, and sync back locally
        // Information to sync includes credentials and patient info
        // Bandwidth heavy task
    }

}