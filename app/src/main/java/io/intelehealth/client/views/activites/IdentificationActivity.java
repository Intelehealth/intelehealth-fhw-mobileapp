package io.intelehealth.client.views.activites;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import io.intelehealth.client.R;
import io.intelehealth.client.database.InteleHealthDatabaseHelper;
import io.intelehealth.client.databinding.ActivityIdentificationBinding;
import io.intelehealth.client.utilities.EditTextUtils;
import io.intelehealth.client.utilities.FileUtils;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.viewModels.IdentificationViewModel;
import io.intelehealth.client.viewModels.requestModels.Patient;

public class IdentificationActivity extends AppCompatActivity {
    ActivityIdentificationBinding binding;
    IdentificationViewModel identificationViewModel;
    SessionManager sessionManager = null;
    InteleHealthDatabaseHelper mDbHelper = null;
    private boolean hasLicense = false;
    private String mFileName = "config.json";
    private ArrayAdapter<CharSequence> educationAdapter;
    private ArrayAdapter<CharSequence> economicStatusAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_identification);
        identificationViewModel = ViewModelProviders.of(this).get(IdentificationViewModel.class);
        binding.setIdentificationViewModel(identificationViewModel);
        binding.setLifecycleOwner(this);

        setTitle(R.string.title_activity_identification);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        identificationViewModel.getPatient().observe(this, new Observer<Patient>() {
            @Override
            public void onChanged(@Nullable Patient patient) {
                identificationViewModel.onPatientCreateClicked();
            }
        });
//Initialize the local database to store patient information
        mDbHelper = new InteleHealthDatabaseHelper(this);

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            if (intent.hasExtra("pid")) {
                this.setTitle("Update Patient");

            }
        }
        if (sessionManager.valueContains("licensekey"))
            hasLicense = true;

        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(FileUtils.readFileRoot(mFileName, this)); //Load the config file

            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, mFileName)));

            }

            //Display the fields on the Add Patient screen as per the config file
            if (obj.getBoolean("mFirstName")) {
                binding.identificationFirstName.setVisibility(View.VISIBLE);
            } else {
                binding.identificationFirstName.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mMiddleName")) {
                binding.identificationMiddleName.setVisibility(View.VISIBLE);
            } else {
                binding.identificationMiddleName.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mLastName")) {
                binding.identificationLastName.setVisibility(View.VISIBLE);
            } else {
                binding.identificationLastName.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mDOB")) {
                binding.identificationBirthDateTextView.setVisibility(View.VISIBLE);
            } else {
                binding.identificationBirthDateTextView.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mPhoneNum")) {
                binding.identificationPhoneNumber.setVisibility(View.VISIBLE);
            } else {
                binding.identificationPhoneNumber.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mAge")) {
                binding.identificationAge.setVisibility(View.VISIBLE);
            } else {
                binding.identificationAge.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mAddress1")) {
                binding.identificationAddress1.setVisibility(View.VISIBLE);
            } else {
                binding.identificationAddress1.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mAddress2")) {
                binding.identificationAddress2.setVisibility(View.VISIBLE);
            } else {
                binding.identificationAddress2.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mCity")) {
                binding.identificationCity.setVisibility(View.VISIBLE);
            } else {
                binding.identificationCity.setVisibility(View.GONE);
            }

            if (obj.getBoolean("countryStateLayout")) {
                binding.identificationLlcountryState.setVisibility(View.VISIBLE);
            } else {
                binding.identificationLlcountryState.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mPostal")) {
                binding.identificationPostalCode.setVisibility(View.VISIBLE);
            } else {
                binding.identificationPostalCode.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mGenderM")) {
                binding.identificationGenderMale.setVisibility(View.VISIBLE);
            } else {
                binding.identificationGenderMale.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mGenderF")) {
                binding.identificationGenderFemale.setVisibility(View.VISIBLE);
            } else {
                binding.identificationGenderFemale.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mRelationship")) {
                binding.identificationRelationship.setVisibility(View.VISIBLE);
            } else {
                binding.identificationRelationship.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mOccupation")) {
                binding.identificationOccupation.setVisibility(View.VISIBLE);
            } else {
                binding.identificationOccupation.setVisibility(View.GONE);
            }
            if (obj.getBoolean("casteLayout")) {
                binding.identificationCaste.setVisibility(View.VISIBLE);
            } else {
                binding.identificationCaste.setVisibility(View.GONE);
            }
            if (obj.getBoolean("educationLayout")) {
                binding.identificationEducation.setVisibility(View.VISIBLE);
            } else {
                binding.identificationEducation.setVisibility(View.GONE);
            }
            if (obj.getBoolean("economicLayout")) {
                binding.identificationEconiomicStatus.setVisibility(View.VISIBLE);
            } else {
                binding.identificationEconiomicStatus.setVisibility(View.GONE);
            }
            String country1;
            country1 = obj.getString("mCountry");

            if (country1.equalsIgnoreCase("India")) {
                EditTextUtils.setEditTextMaxLength(10, binding.identificationPhoneNumber);
            } else if (country1.equalsIgnoreCase("Philippines")) {
                EditTextUtils.setEditTextMaxLength(11, binding.identificationPhoneNumber);
            }

        } catch (JSONException e) {
            e.printStackTrace();
//            Issue #627
//            added the catch exception to check the config and throwing back to setup activity
            Toast.makeText(getApplicationContext(), "JsonException" + e, Toast.LENGTH_LONG).show();
            showAlertDialogButtonClicked(e.toString());
        }
        Resources res = getResources();
        ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(this,
                R.array.countries, android.R.layout.simple_spinner_item);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCountry.setAdapter(countryAdapter);

        ArrayAdapter<CharSequence> casteAdapter = ArrayAdapter.createFromResource(this,
                R.array.caste, android.R.layout.simple_spinner_item);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCaste.setAdapter(casteAdapter);
        try {
            String economicLanguage = "economic_" + Locale.getDefault().getLanguage();
            int economics = res.getIdentifier(economicLanguage, "array", getApplicationContext().getPackageName());
            if (economics != 0) {
                economicStatusAdapter = ArrayAdapter.createFromResource(this,
                        economics, android.R.layout.simple_spinner_item);
            }
            countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerEconomicStatus.setAdapter(economicStatusAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Economic values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }
        try {
            String educationLanguage = "education_" + Locale.getDefault().getLanguage();
            int educations = res.getIdentifier(educationLanguage, "array", getApplicationContext().getPackageName());
            if (educations != 0) {
                educationAdapter = ArrayAdapter.createFromResource(this,
                        educations, android.R.layout.simple_spinner_item);

            }
            countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerEducation.setAdapter(educationAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Education values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }


    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to go back ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).setNegativeButton("No", null).show();

    }

    public void showAlertDialogButtonClicked(String errorMessage) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Config Error");
        alertDialogBuilder.setMessage(errorMessage);
        alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                Intent i = new Intent(IdentificationActivity.this, SetupActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// This flag ensures all activities on top of the CloseAllViewsDemo are cleared.
                startActivity(i);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}
