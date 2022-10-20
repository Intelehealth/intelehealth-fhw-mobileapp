package org.intelehealth.app.activities.identificationActivity;

import static org.intelehealth.app.utilities.StringUtils.switch_as_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_as_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_as_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_bn_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_bn_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_bn_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_gu_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_gu_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_gu_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_hi_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_hi_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_hi_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_kn_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_kn_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_kn_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ml_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ml_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ml_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_mr_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_mr_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_mr_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_or_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_or_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_or_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ru_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ru_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ru_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ta_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ta_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ta_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_te_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_te_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_te_education_edit;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ImagesPushDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Fragment_ThirdScreen extends Fragment {
    private static final String TAG = Fragment_ThirdScreen.class.getSimpleName();
    private PatientDTO patientDTO;
    private View view;
    SessionManager sessionManager = null;
    private ArrayAdapter<CharSequence> educationAdapter;
    private ArrayAdapter<CharSequence> casteAdapter;
    private ArrayAdapter<CharSequence> economicStatusAdapter;
    private EditText relation_edittext, occupation_editText;
    private Spinner caste_spinner, education_spinner, economicstatus_spinner;
    private ImageView personal_icon, address_icon, other_icon;
    private Button frag3_btn_back, frag3_btn_next;
    private TextView relation_error, occupation_error, caste_error, education_error, economic_error;
    ImagesDAO imagesDAO = new ImagesDAO();
    private Fragment_SecondScreen secondScreen;
    boolean fromThirdScreen = false;
    Patient patient1 = new Patient();
    PatientsDAO patientsDAO = new PatientsDAO();
    String patientID_edit;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_registration_thirdscreen, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(getActivity());

        personal_icon = getActivity().findViewById(R.id.addpatient_icon);
        address_icon = getActivity().findViewById(R.id.addresslocation_icon);
        other_icon = getActivity().findViewById(R.id.other_icon);
        frag3_btn_back = getActivity().findViewById(R.id.frag3_btn_back);
        frag3_btn_next = getActivity().findViewById(R.id.frag3_btn_next);

        relation_edittext = view.findViewById(R.id.relation_edittext);
        occupation_editText = view.findViewById(R.id.occupation_editText);
        caste_spinner = view.findViewById(R.id.caste_spinner);
        education_spinner = view.findViewById(R.id.education_spinner);
        economicstatus_spinner = view.findViewById(R.id.economicstatus_spinner);

        relation_error = view.findViewById(R.id.relation_error);
        occupation_error = view.findViewById(R.id.occupation_error);
        caste_error = view.findViewById(R.id.caste_error);
        education_error = view.findViewById(R.id.education_error);
        economic_error = view.findViewById(R.id.economic_error);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        secondScreen = new Fragment_SecondScreen();
        personal_icon.setImageDrawable(getResources().getDrawable(R.drawable.addpatient_icon_done));
        address_icon.setImageDrawable(getResources().getDrawable(R.drawable.addresslocation_icon_done));
        other_icon.setImageDrawable(getResources().getDrawable(R.drawable.other_icon));

        if (getArguments() != null) {
            patientDTO = (PatientDTO) getArguments().getSerializable("patientDTO");
            fromThirdScreen = getArguments().getBoolean("fromSecondScreen");
            patientID_edit = getArguments().getString("patientUuid");

            if (patientID_edit != null) {
                patient1.setUuid(patientID_edit);
                setscreen(patientID_edit);
            }
            else {
                patient1.setUuid(patientDTO.getUuid());
                setscreen(patientDTO.getUuid());
            }
        }


        frag3_btn_back.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("patientDTO", (Serializable) patientDTO);
            bundle.putBoolean("fromSecondScreen", true);
            secondScreen.setArguments(bundle); // passing data to Fragment

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_firstscreen, secondScreen)
                    .commit();
        });

        frag3_btn_next.setOnClickListener(v -> {
//                Intent intent = new Intent(getActivity(), PatientDetailActivity2.class);
//                startActivity(intent);
            onPatientCreateClicked();
        });

        // caste spinner
        Resources res = getResources();
        try {
            String casteLanguage = "caste_" + sessionManager.getAppLanguage();
            int castes = res.getIdentifier(casteLanguage, "array", getActivity().getApplicationContext().getPackageName());
            if (castes != 0) {
                casteAdapter = ArrayAdapter.createFromResource(getActivity(),
                        castes, android.R.layout.simple_spinner_dropdown_item);
            }
            caste_spinner.setAdapter(casteAdapter);
        } catch (Exception e) {
//            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        // education spinner
        try {
            String educationLanguage = "education_" + sessionManager.getAppLanguage();
            int educations = res.getIdentifier(educationLanguage, "array", getActivity().getApplicationContext().getPackageName());
            if (educations != 0) {
                educationAdapter = ArrayAdapter.createFromResource(getActivity(),
                        educations, android.R.layout.simple_spinner_dropdown_item);
            }
            education_spinner.setAdapter(educationAdapter);
        } catch (Exception e) {
//            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        // economic spinner
        try {
            String economicLanguage = "economic_" + sessionManager.getAppLanguage();
            int economics = res.getIdentifier(economicLanguage, "array", getActivity().getApplicationContext().getPackageName());
            if (economics != 0) {
                economicStatusAdapter = ArrayAdapter.createFromResource(getActivity(),
                        economics, android.R.layout.simple_spinner_dropdown_item);
            }
            economicstatus_spinner.setAdapter(economicStatusAdapter);
        } catch (Exception e) {
//            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        relation_edittext.setText(patient1.getSdw());
        Log.v(TAG, "relation: " + patient1.getSdw());
        occupation_editText.setText(patient1.getOccupation());
        
        // setting screen in edit for spinners...
        if (fromThirdScreen) {
            //caste
            if (patient1.getCaste() != null) {
            if (patient1.getCaste().equals(getResources().getString(R.string.not_provided)))
                caste_spinner.setSelection(0);
//            else
//                caste_spinner.setSelection(casteAdapter.getPosition(patient1.getCaste()));

            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String caste = switch_hi_caste_edit(patient1.getCaste());
                    caste_spinner.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String caste = switch_or_caste_edit(patient1.getCaste());
                    caste_spinner.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                    String caste = switch_te_caste_edit(patient1.getCaste());
                    caste_spinner.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                    String caste = switch_mr_caste_edit(patient1.getCaste());
                    caste_spinner.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    String caste = switch_as_caste_edit(patient1.getCaste());
                    caste_spinner.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                    String caste = switch_ml_caste_edit(patient1.getCaste());
                    caste_spinner.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                    String caste = switch_kn_caste_edit(patient1.getCaste());
                    caste_spinner.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                    String caste = switch_ru_caste_edit(patient1.getCaste());
                    caste_spinner.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    String caste = switch_gu_caste_edit(patient1.getCaste());
                    caste_spinner.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                    String caste = switch_bn_caste_edit(patient1.getCaste());
                    caste_spinner.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                    String caste = switch_ta_caste_edit(patient1.getCaste());
                    caste_spinner.setSelection(casteAdapter.getPosition(caste));
                } else {
                    caste_spinner.setSelection(casteAdapter.getPosition(patient1.getCaste()));
                }
            }
        }
            
            //education status
            if (patient1.getEducation_level() != null) {
                if (patient1.getEducation_level().equals(getResources().getString(R.string.not_provided)))
                    education_spinner.setSelection(0);
//            else
//                education_spinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(patient1.getEducation_level()) : 0);

                else {
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        String education = switch_hi_education_edit(patient1.getEducation_level());
                        education_spinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        String education = switch_or_education_edit(patient1.getEducation_level());
                        education_spinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                        String education = switch_te_education_edit(patient1.getEducation_level());
                        education_spinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                        String education = switch_mr_education_edit(patient1.getEducation_level());
                        education_spinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                        String education = switch_as_education_edit(patient1.getEducation_level());
                        education_spinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                        String education = switch_gu_education_edit(patient1.getEducation_level());
                        education_spinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                        String education = switch_ta_education_edit(patient1.getEducation_level());
                        education_spinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                        String education = switch_bn_education_edit(patient1.getEducation_level());
                        education_spinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                        String education = switch_ml_education_edit(patient1.getEducation_level());
                        education_spinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                        String education = switch_kn_education_edit(patient1.getEducation_level());
                        education_spinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                        String education = switch_ru_education_edit(patient1.getEducation_level());
                        education_spinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                    } else {
                        education_spinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(patient1.getEducation_level()) : 0);
                    }
                }
            }

            
            
            // economic statius
            if (patient1.getEconomic_status() != null) {
                if (patient1.getEconomic_status().equals(getResources().getString(R.string.not_provided)))
                    economicstatus_spinner.setSelection(0);
//            else
//                economicstatus_spinner.setSelection(economicStatusAdapter.getPosition(patient1.getEconomic_status()));

                else {
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        String economic = switch_hi_economic_edit(patient1.getEconomic_status());
                        economicstatus_spinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        String economic = switch_or_economic_edit(patient1.getEconomic_status());
                        economicstatus_spinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                        String economic = switch_te_economic_edit(patient1.getEconomic_status());
                        economicstatus_spinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                        String economic = switch_mr_economic_edit(patient1.getEconomic_status());
                        economicstatus_spinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                        String economic = switch_as_economic_edit(patient1.getEconomic_status());
                        economicstatus_spinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                        String economic = switch_ml_economic_edit(patient1.getEconomic_status());
                        economicstatus_spinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                        String economic = switch_kn_economic_edit(patient1.getEconomic_status());
                        economicstatus_spinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                        String economic = switch_ru_economic_edit(patient1.getEconomic_status());
                        economicstatus_spinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                        String economic = switch_gu_economic_edit(patient1.getEconomic_status());
                        economicstatus_spinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                        String economic = switch_bn_economic_edit(patient1.getEconomic_status());
                        economicstatus_spinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                        String economic = switch_ta_economic_edit(patient1.getEconomic_status());
                        economicstatus_spinner.setSelection(economicStatusAdapter.getPosition(economic));
                    } else {
                        economicstatus_spinner.setSelection(economicStatusAdapter.getPosition(patient1.getEconomic_status()));
                    }
                }
            }
            



        }

    }

    private void onPatientCreateClicked() {
        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();

        Gson gson = new Gson();
        boolean cancel = false;
        View focusView = null;

        // validation - start
        if (relation_edittext.getText().toString().equals("")) {
            relation_error.setVisibility(View.VISIBLE);
            return;
        }
        else {
            relation_error.setVisibility(View.GONE);
        }

        if (occupation_editText.getText().toString().equals("")) {
            occupation_error.setVisibility(View.VISIBLE);
            return;
        }
        else {
            occupation_error.setVisibility(View.GONE);
        }

        if (caste_spinner.getSelectedItemPosition() == 0) {
            caste_error.setVisibility(View.VISIBLE);
            return;
        }
        else {
            caste_error.setVisibility(View.GONE);
        }

        if (education_spinner.getSelectedItemPosition() == 0) {
            education_error.setVisibility(View.VISIBLE);
            return;
        }
        else {
            education_error.setVisibility(View.GONE);
        }

        if (economicstatus_spinner.getSelectedItemPosition() == 0) {
            economic_error.setVisibility(View.VISIBLE);
            return;
        }
        else {
            economic_error.setVisibility(View.GONE);
        }
        // validation - end


        /**
         *  entering value in dataset start
         */
        if (cancel) {
            focusView.requestFocus();
        } else {
            String uuid = patientDTO.getUuid();

            // mobile no adding in patient attributes.
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Telephone Number"));
            patientAttributesDTO.setValue(StringUtils.getValue(patientDTO.getPhonenumber()));
            patientAttributesDTOList.add(patientAttributesDTO);

            // son/daughter/wife of
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Son/wife/daughter"));
            patientAttributesDTO.setValue(StringUtils.getValue(relation_edittext.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

            // occupation
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("occupation"));
            patientAttributesDTO.setValue(StringUtils.getValue(occupation_editText.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

            // caste
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("caste"));
            patientAttributesDTO.setValue(StringUtils.getProvided(caste_spinner));
            patientAttributesDTOList.add(patientAttributesDTO);
            Log.v(TAG, "values_caste: " + patientAttributesDTO.toString());

            // education
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Education Level"));
            patientAttributesDTO.setValue(StringUtils.getProvided(education_spinner));
            patientAttributesDTOList.add(patientAttributesDTO);

            // economic status
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Economic Status"));
            patientAttributesDTO.setValue(StringUtils.getProvided(economicstatus_spinner));
            patientAttributesDTOList.add(patientAttributesDTO);

            patientDTO.setPatientAttributesDTOList(patientAttributesDTOList);
            patientDTO.setSyncd(false); // todo:uncomment later
          //  patientDTO.setSyncd(true); // todo: remove ...just for testing.
            Logger.logD("patient json : ", "Json : " + gson.toJson(patientDTO, PatientDTO.class));
        }
        
        // inserting data in db and uploading to server...
        try {
            Logger.logD(TAG, "insertpatinet ");
            boolean isPatientInserted = false;
            boolean isPatientImageInserted = false;

            if (patientID_edit != null) {
                /*isPatientInserted = patientsDAO.insertPatientToDB(patientDTO, patientID_edit);
                isPatientImageInserted = imagesDAO.insertPatientProfileImages(patientDTO.getPatientPhoto(), patientID_edit);*/

                isPatientInserted = patientsDAO.updatePatientToDB_PatientDTO(patientDTO, patientID_edit, patientAttributesDTOList);
                isPatientImageInserted = imagesDAO.updatePatientProfileImages(patientDTO.getPatientPhoto(), patientID_edit);
            }
            else {
                isPatientInserted = patientsDAO.insertPatientToDB(patientDTO, patientDTO.getUuid());
                isPatientImageInserted = imagesDAO.insertPatientProfileImages(patientDTO.getPatientPhoto(), patientDTO.getUuid());
            }

            if (NetworkConnection.isOnline(getActivity().getApplication())) { // todo: uncomment later jsut for testing added.
                SyncDAO syncDAO = new SyncDAO();
                ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
                boolean push = syncDAO.pushDataApi();
                boolean pushImage = imagesPushDAO.patientProfileImagesPush();
            }

            if (isPatientInserted && isPatientImageInserted) {
                Logger.logD(TAG, "inserted");
                Intent intent = new Intent(getActivity().getApplication(), PatientDetailActivity2.class);
                intent.putExtra("patientUuid", patientDTO.getUuid());
                intent.putExtra("patientName", patientDTO.getFirstname() + " " + patientDTO.getLastname());
                intent.putExtra("tag", "newPatient");
                intent.putExtra("hasPrescription", "false");
                //   i.putExtra("privacy", privacy_value); // todo: uncomment later.
             //   Log.d(TAG, "Privacy Value on (Identification): " + privacy_value); //privacy value transferred to PatientDetail activity.
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                Bundle args = new Bundle();
                args.putSerializable("patientDTO", (Serializable) patientDTO);
                intent.putExtra("BUNDLE",args);
                intent.putExtra("patientUuid", patientID_edit);
                getActivity().startActivity(intent);
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), "Error of adding the data", Toast.LENGTH_SHORT).show();
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


    private void setscreen(String str) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Log.v(TAG, "relation: " + str);

        String patientSelection = "uuid=?";
        String[] patientArgs = {str};
        String[] patientColumns = {"uuid", "first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province",
                "postal_code", "country", "phone_number", "gender", "sdw", "occupation", "patient_photo",
                "economic_status", "education_status", "caste"};
        Cursor idCursor = db.query("tbl_patient", patientColumns, patientSelection, patientArgs, null, null, null);
        if (idCursor.moveToFirst()) {
            do {
                patient1.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                patient1.setFirst_name(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                patient1.setMiddle_name(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                patient1.setLast_name(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));
                patient1.setDate_of_birth(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                patient1.setAddress1(idCursor.getString(idCursor.getColumnIndexOrThrow("address1")));
                patient1.setAddress2(idCursor.getString(idCursor.getColumnIndexOrThrow("address2")));
                patient1.setCity_village(idCursor.getString(idCursor.getColumnIndexOrThrow("city_village")));
                patient1.setState_province(idCursor.getString(idCursor.getColumnIndexOrThrow("state_province")));
                patient1.setPostal_code(idCursor.getString(idCursor.getColumnIndexOrThrow("postal_code")));
                patient1.setCountry(idCursor.getString(idCursor.getColumnIndexOrThrow("country")));
                patient1.setPhone_number(idCursor.getString(idCursor.getColumnIndexOrThrow("phone_number")));
                patient1.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                patient1.setSdw(idCursor.getString(idCursor.getColumnIndexOrThrow("sdw")));
                patient1.setOccupation(idCursor.getString(idCursor.getColumnIndexOrThrow("occupation")));
                patient1.setPatient_photo(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_photo")));

            } while (idCursor.moveToNext());
            idCursor.close();
        }
        String patientSelection1 = "patientuuid = ?";
        String[] patientArgs1 = {str};
        String[] patientColumns1 = {"value", "person_attribute_type_uuid"};
        final Cursor idCursor1 = db.query("tbl_patient_attribute", patientColumns1, patientSelection1, patientArgs1, null, null, null);
        String name = "";
        if (idCursor1.moveToFirst()) {
            do {
                try {
                    name = patientsDAO.getAttributesName(idCursor1.getString(idCursor1.getColumnIndexOrThrow("person_attribute_type_uuid")));
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                if (name.equalsIgnoreCase("caste")) {
                    patient1.setCaste(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Telephone Number")) {
                    patient1.setPhone_number(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Education Level")) {
                    patient1.setEducation_level(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Economic Status")) {
                    patient1.setEconomic_status(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("occupation")) {
                    patient1.setOccupation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Son/wife/daughter")) {
                    patient1.setSdw(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

            } while (idCursor1.moveToNext());
        }
        idCursor1.close();
    }


}
