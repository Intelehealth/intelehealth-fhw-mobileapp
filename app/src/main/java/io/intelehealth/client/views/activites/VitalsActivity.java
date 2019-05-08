package io.intelehealth.client.views.activites;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.dao.ObsDAO;
import io.intelehealth.client.databinding.ActivityVitalsBinding;
import io.intelehealth.client.dto.ObsDTO;
import io.intelehealth.client.objects.VitalsObject;
import io.intelehealth.client.utilities.ConceptId;
import io.intelehealth.client.utilities.ConfigUtils;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.UuidDictionary;

public class VitalsActivity extends AppCompatActivity {
    private static final String TAG = VitalsActivity.class.getSimpleName();
    ActivityVitalsBinding binding;
    SessionManager sessionManager;
    private String patientName = "";
    private String intentTag;
    private String state;
    private String patientUuid;
    private String visitUuid;
    private String encounterUuid;
    int flag_height = 0, flag_weight = 0;
    String heightvalue;
    String weightvalue;
    ConfigUtils configUtils=new ConfigUtils(VitalsActivity.this);

    VitalsObject results=new VitalsObject();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vitals);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        sessionManager = new SessionManager(this);



        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterUuid=intent.getStringExtra("encounterUuidVitals");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
            Log.v(TAG, "Patient ID: " + patientUuid);
            Log.v(TAG, "Visit ID: " + visitUuid);
            Log.v(TAG, "Patient Name: " + patientName);
            Log.v(TAG, "Intent Tag: " + intentTag);
        }
//        Setting the title
        setTitle(R.string.title_activity_vitals);
        setTitle(patientName + ": " + getTitle());

        if (intentTag != null && intentTag.equals("edit")) {
            loadPrevious();
        }

        binding.tableHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    binding.tableBmi.getText().clear();
                    flag_height = 1;
                    heightvalue = binding.tableHeight.getText().toString();
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_HEIGHT)) {
                        binding.tableHeight.setError(getString(R.string.height_error, AppConstants.MAXIMUM_HEIGHT));
                    } else {
                        binding.tableHeight.setError(null);
                    }

                } else {
                    flag_height = 0;
                    binding.tableBmi.getText().clear();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                calculateBMI();
            }
        });

        binding.tableWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    binding.tableBmi.getText().clear();
                    flag_weight = 1;
                    weightvalue = binding.tableWeight.getText().toString();
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_WEIGHT)) {
                        binding.tableWeight.setError(getString(R.string.weight_error, AppConstants.MAXIMUM_WEIGHT));
                    } else {
                        binding.tableWeight.setError(null);
                    }
                } else {
                    flag_weight = 0;
                    binding.tableBmi.getText().clear();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

                calculateBMI();
            }
        });


        binding.tableSpo2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.table_spo2 || id == EditorInfo.IME_NULL) {
                    validateTable();
                    return true;
                }
                return false;
            }
        });

        binding.tableSpo2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_SPO2) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_SPO2)) {
                        binding.tableSpo2.setError(getString(R.string.spo2_error, AppConstants.MINIMUM_SPO2, AppConstants.MAXIMUM_SPO2));
                    } else {
                        binding.tableSpo2.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.tableTemp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (configUtils.celsius()) {
                        if (s.toString().trim().length() > 0) {
                            if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_TEMPERATURE_CELSIUS) ||
                                    Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_TEMPERATURE_CELSIUS)) {
                                binding.tableTemp.setError(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_CELSIUS, AppConstants.MAXIMUM_TEMPERATURE_CELSIUS));
                            } else {
                                binding.tableTemp.setError(null);
                            }

                        }
                    } else if (configUtils.fahrenheit()) {
                        if (s.toString().trim().length() > 0) {
                            if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_TEMPERATURE_FARHENIT) ||
                                    Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_TEMPERATURE_FARHENIT)) {
                                binding.tableTemp.setError(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_FARHENIT, AppConstants.MAXIMUM_TEMPERATURE_FARHENIT));
                            } else {
                                binding.tableTemp.setError(null);
                            }
                        }

                    }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.tableRespiratory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_RESPIRATORY) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_RESPIRATORY)) {
                        binding.tableRespiratory.setError(getString(R.string.temp_error, AppConstants.MINIMUM_RESPIRATORY, AppConstants.MAXIMUM_RESPIRATORY));
                    } else {
                        binding.tableRespiratory.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        binding.tablePulse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_PULSE) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_PULSE)) {
                        binding.tablePulse.setError(getString(R.string.pulse_error, AppConstants.MINIMUM_PULSE, AppConstants.MAXIMUM_PULSE));
                    } else {
                        binding.tablePulse.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.tableBpsys.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_BP_SYS) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_BP_SYS)) {
                        binding.tableBpsys.setError(getString(R.string.bpsys_error, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS));
                    } else {
                        binding.tableBpsys.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.tableBpdia.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_BP_DSYS) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_BP_DSYS)) {
                        binding.tableBpdia.setError(getString(R.string.bpdia_error, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS));
                    } else {
                        binding.tableBpdia.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateTable();
            }
        });
    }
    public void calculateBMI() {
        if (flag_height == 1 && flag_weight == 1) {
            binding.tableBmi.getText().clear();
            double numerator = Double.parseDouble(weightvalue) * 10000;
            double denominator = (Double.parseDouble(heightvalue)) * (Double.parseDouble(heightvalue));
            double bmi_value = numerator / denominator;
            DecimalFormat df = new DecimalFormat("0.00");
            binding.tableBmi.setText(df.format(bmi_value));
            //mBMI.setText(String.format(Locale.ENGLISH, "%.2f", bmi_value));
        } else if (flag_height == 0 || flag_weight == 0) {
            // do nothing
            binding.tableBmi.getText().clear();
        }
    }
    public void loadPrevious() {

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        String[] columns = {"value", " conceptuuid"};
        String visitSelection = "encounteruuid = ? ";
        String[] visitArgs = {encounterUuid};
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
        if (visitCursor.moveToFirst()) {
            do {
                String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                parseData(dbConceptID, dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }

    private void parseData(String concept_id, String value) {
        switch (concept_id) {
            case UuidDictionary.HEIGHT: //Height
                binding.tableHeight.setText(value);
                break;
            case UuidDictionary.WEIGHT: //Weight
                binding.tableWeight.setText(value);
                break;
            case UuidDictionary.PULSE: //Pulse
                binding.tablePulse.setText(value);
                break;
            case UuidDictionary.SYSTOLIC_BP: //Systolic BP
                binding.tableBpsys.setText(value);
                break;
            case UuidDictionary.DIASTOLIC_BP: //Diastolic BP
                binding.tableBpdia.setText(value);
                break;
            case UuidDictionary.TEMPERATURE: //Temperature
                binding.tableTemp.setText(value);
                break;
            //    Respiratory added by mahiti dev team
            case UuidDictionary.RESPIRATORY: //Respiratory
                binding.tableRespiratory.setText(value);
                break;
            case UuidDictionary.SPO2: //SpO2
                binding.tableSpo2.setText(value);
                break;
            default:
                break;

        }
    }
    public void validateTable() {
        boolean cancel = false;
        View focusView = null;

        // Store values at the time of the fab is clicked.
        ArrayList<EditText> values = new ArrayList<EditText>();
        values.add(binding.tableHeight);
        values.add(binding.tableWeight);
        values.add(binding.tablePulse);
        values.add(binding.tableBpsys);
        values.add(binding.tableBpdia);
        values.add(binding.tableTemp);
        values.add(binding.tableRespiratory);
        values.add(binding.tableSpo2);

        // Check to see if values were inputted.
        for (int i = 0; i < values.size(); i++) {
            if (i == 0) {
                EditText et = values.get(i);
                String abc = et.getText().toString().trim();
                if (abc != null && !abc.isEmpty()) {
                    if (Double.parseDouble(abc) > Double.parseDouble(AppConstants.MAXIMUM_HEIGHT)) {
                        et.setError(getString(R.string.height_error, AppConstants.MAXIMUM_HEIGHT));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
//       }
                } else {
                    cancel = false;
                }
            } else if (i == 1) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty()) {
                    if (Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_WEIGHT)) {
                        et.setError(getString(R.string.weight_error, AppConstants.MAXIMUM_WEIGHT));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
//       }
                } else {
                    cancel = false;
                }

            } else if (i == 2) {
                EditText et = values.get(i);
                String abc2 = et.getText().toString().trim();
                if (abc2 != null && !abc2.isEmpty() && (!abc2.equals("0.0"))) {
                    if ((Double.parseDouble(abc2) > Double.parseDouble(AppConstants.MAXIMUM_PULSE)) ||
                            (Double.parseDouble(abc2) < Double.parseDouble(AppConstants.MINIMUM_PULSE))) {
                        et.setError(getString(R.string.pulse_error, AppConstants.MINIMUM_PULSE, AppConstants.MAXIMUM_PULSE));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
//       }
                } else {
                    cancel = false;
                }

            } else if (i == 3) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_BP_SYS)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_BP_SYS))) {
                        et.setError(getString(R.string.bpsys_error, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
//       }
                } else {
                    cancel = false;
                }

            } else if (i == 4) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_BP_DSYS)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_BP_DSYS))) {
                        et.setError(getString(R.string.bpdia_error, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
//       }
                } else {
                    cancel = false;
                }

            } else if (i == 5) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if (configUtils.celsius()) {
                            if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_TEMPERATURE_CELSIUS)) ||
                                    (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_TEMPERATURE_CELSIUS))) {
                                et.setError(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_CELSIUS, AppConstants.MAXIMUM_TEMPERATURE_CELSIUS));
                                focusView = et;
                                cancel = true;
                                break;
                            } else {
                                cancel = false;
                            }
                        } else if (configUtils.fahrenheit()) {
                            if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_TEMPERATURE_FARHENIT)) ||
                                    (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_TEMPERATURE_FARHENIT))) {
                                et.setError(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_FARHENIT, AppConstants.MAXIMUM_TEMPERATURE_FARHENIT));
                                focusView = et;
                                cancel = true;
                                break;
                            } else {
                                cancel = false;
                            }
                        }
                } else {
                    cancel = false;
                }
            } else if (i == 6) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_RESPIRATORY)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_RESPIRATORY))) {
                        et.setError(getString(R.string.resp_error, AppConstants.MINIMUM_RESPIRATORY, AppConstants.MAXIMUM_RESPIRATORY));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
//       }
                } else {
                    cancel = false;
                }
            }else {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_SPO2)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_SPO2))) {
                        et.setError(getString(R.string.spo2_error, AppConstants.MINIMUM_SPO2, AppConstants.MAXIMUM_SPO2));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
//       }
                } else {
                    cancel = false;
                }
            }
        }

        if (cancel) {
            // There was an error - focus the first form field with an error.
            focusView.requestFocus();
            return;
        } else {
            try {
                if (binding.tableHeight.getText() != null) {
                    results.setHeight((binding.tableHeight.getText().toString()));
                }
                if (binding.tableWeight.getText() != null) {
                    results.setWeight((binding.tableWeight.getText().toString()));
                }
                if (binding.tablePulse.getText() != null) {
                    results.setPulse((binding.tablePulse.getText().toString()));
                }
                if (binding.tableBpdia.getText() != null) {
                    results.setBpdia((binding.tableBpdia.getText().toString()));
                }
                if (binding.tableBpsys.getText() != null) {
                    results.setBpsys((binding.tableBpsys.getText().toString()));
                }
                if (binding.tableTemp.getText() != null) {
                    results.setTemperature((binding.tableTemp.getText().toString()));
                }
                if (binding.tableRespiratory.getText() != null) {
                    results.setResp((binding.tableRespiratory.getText().toString()));
                }
                if (binding.tableSpo2.getText() != null) {
                    results.setSpo2((binding.tableSpo2.getText().toString()));
                }


            } catch (NumberFormatException e) {
                Snackbar.make(findViewById(R.id.cl_table), "Error: non-decimal number entered.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }

//
        }
        ObsDAO obsDAO=new ObsDAO();
        ObsDTO obsDTO=new ObsDTO();
        if (intentTag != null && intentTag.equals("edit")) {
            obsDTO=new ObsDTO();
            obsDTO.setConceptuuid(String.valueOf(UuidDictionary.HEIGHT));
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setCreator(1);
            obsDTO.setValue(results.getHeight());
            obsDTO.setUuid(AppConstants.NEW_UUID);

            obsDAO.updateObs(obsDTO);

            obsDTO=new ObsDTO();
            obsDTO.setConceptuuid(String.valueOf(UuidDictionary.WEIGHT));
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setCreator(1);
            obsDTO.setValue(results.getWeight());
            obsDTO.setUuid(AppConstants.NEW_UUID);

            obsDAO.updateObs(obsDTO);

            obsDTO=new ObsDTO();
            obsDTO.setConceptuuid(String.valueOf(UuidDictionary.PULSE));
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setCreator(1);
            obsDTO.setValue(results.getPulse());
            obsDTO.setUuid(AppConstants.NEW_UUID);

            obsDAO.updateObs(obsDTO);

            obsDTO=new ObsDTO();
            obsDTO.setConceptuuid(String.valueOf(UuidDictionary.SYSTOLIC_BP));
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setCreator(1);
            obsDTO.setValue(results.getBpsys());
            obsDTO.setUuid(AppConstants.NEW_UUID);

            obsDAO.updateObs(obsDTO);

            obsDTO=new ObsDTO();
            obsDTO.setConceptuuid(String.valueOf(UuidDictionary.DIASTOLIC_BP));
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setCreator(1);
            obsDTO.setValue(results.getBpdia());
            obsDTO.setUuid(AppConstants.NEW_UUID);

            obsDAO.updateObs(obsDTO);

            obsDTO=new ObsDTO();
            obsDTO.setConceptuuid(String.valueOf(UuidDictionary.TEMPERATURE));
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setCreator(1);
            obsDTO.setValue(results.getTemperature());
            obsDTO.setUuid(AppConstants.NEW_UUID);

            obsDAO.updateObs(obsDTO);

            obsDTO=new ObsDTO();
            obsDTO.setConceptuuid(String.valueOf(UuidDictionary.RESPIRATORY));
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setCreator(1);
            obsDTO.setValue(results.getTemperature());
            obsDTO.setUuid(AppConstants.NEW_UUID);

            obsDAO.updateObs(obsDTO);

            obsDTO=new ObsDTO();
            obsDTO.setConceptuuid(String.valueOf(UuidDictionary.SPO2));
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setCreator(1);
            obsDTO.setValue(results.getSpo2());
            obsDTO.setUuid(AppConstants.NEW_UUID);

            obsDAO.updateObs(obsDTO);

            Intent intent = new Intent(VitalsActivity.this, VisitSummaryActivity.class);
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterUuidVitals",encounterUuid);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            startActivity(intent);
        } else {
            List<ObsDTO> obsDTOS=new ArrayList<>();
            obsDTO=new ObsDTO();
            obsDTO.setConceptuuid(String.valueOf(UuidDictionary.HEIGHT));
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setCreator(1);
            obsDTO.setValue(results.getHeight());
            obsDTO.setUuid(AppConstants.NEW_UUID);

            obsDTOS.add(obsDTO);

            obsDTO=new ObsDTO();
            obsDTO.setConceptuuid(String.valueOf(UuidDictionary.WEIGHT));
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setCreator(1);
            obsDTO.setValue(results.getWeight());

            obsDTOS.add(obsDTO);

            obsDTO=new ObsDTO();
            obsDTO.setConceptuuid(String.valueOf(UuidDictionary.PULSE));
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setCreator(1);
            obsDTO.setValue(results.getPulse());

            obsDTOS.add(obsDTO);

            obsDTO=new ObsDTO();
            obsDTO.setConceptuuid(String.valueOf(UuidDictionary.SYSTOLIC_BP));
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setCreator(1);
            obsDTO.setValue(results.getBpsys());

            obsDTOS.add(obsDTO);

            obsDTO=new ObsDTO();
            obsDTO.setConceptuuid(String.valueOf(UuidDictionary.DIASTOLIC_BP));
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setCreator(1);
            obsDTO.setValue(results.getBpdia());

            obsDTOS.add(obsDTO);

            obsDTO=new ObsDTO();
            obsDTO.setConceptuuid(String.valueOf(UuidDictionary.TEMPERATURE));
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setCreator(1);
            obsDTO.setValue(results.getTemperature());

            obsDTOS.add(obsDTO);

            obsDTO=new ObsDTO();
            obsDTO.setConceptuuid(String.valueOf(UuidDictionary.RESPIRATORY));
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setCreator(1);
            obsDTO.setValue(results.getResp());

            obsDTOS.add(obsDTO);

            obsDTO=new ObsDTO();
            obsDTO.setConceptuuid(String.valueOf(UuidDictionary.SPO2));
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setCreator(1);
            obsDTO.setValue(results.getSpo2());

            obsDAO.insertObs(obsDTOS);
            Intent intent = new Intent(VitalsActivity.this, ComplaintNodeActivity.class);

            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterUuidVitals",encounterUuid);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            //   intent.putStringArrayListExtra("exams", physicalExams);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
    }

}
