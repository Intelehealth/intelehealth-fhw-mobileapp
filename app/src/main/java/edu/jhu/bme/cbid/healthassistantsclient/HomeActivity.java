package edu.jhu.bme.cbid.healthassistantsclient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import org.acra.ACRA;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setTitle("Home");

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_home);
        recyclerView.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(HomeActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);



        // ImageButton newPtButton = (ImageButton) findViewById(R.id.newPatientButton);
        // assert newPtButton != null;
        // newPtButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        startNewPatient();
        //    }
        //});


        // ImageButton findPatientButton = (ImageButton) findViewById(R.id.findPatientsButton);
        // assert findPatientButton != null;
        // findPatientButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        findPatient();
        //    }
        //});

//        ImageButton diagnosticServicesButton = (ImageButton) findViewById(R.id.diagnosticServicesButton);
//        assert diagnosticServicesButton != null;
//        diagnosticServicesButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //selectDiagnosticService();
//                Snackbar.make(v, R.string.generic_coming_soon, Snackbar.LENGTH_LONG).show();
//            }
//        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
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
            case R.id.logoutOption:
                logout();
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


    public void refreshDatabases() {
        // TODO: write function to sync the patients within a specific location
        // Bandwidth heavy task
    }

    public void settings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void endOfDay() {
        // TODO: sync all the data recorded to EHR, and sync back locally
        // Information to sync includes credentials and patient info
        // Bandwidth heavy task
        try {
            throw new NullPointerException();
        } catch (NullPointerException caughtException) {
            ACRA.getErrorReporter().handleException(caughtException);
        }
    }

    public void logout() {
        AccountManager manager = AccountManager.get(HomeActivity.this);
        Account[] accountList = manager.getAccountsByType("io.intelehealth.openmrs");
        if (accountList.length > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.removeAccount(accountList[0], HomeActivity.this, null, null);
            } else {
                manager.removeAccount(accountList[0], null, null); // Legacy implementation
            }
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

}