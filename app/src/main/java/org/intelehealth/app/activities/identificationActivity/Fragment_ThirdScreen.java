package org.intelehealth.app.activities.identificationActivity;

import android.content.Intent;
import android.content.res.Resources;
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

        if (getArguments() != null)
            patientDTO = (PatientDTO) getArguments().getSerializable("patientDTO");

        Log.v(TAG, "values_3: " + patientDTO.toString());
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
        personal_icon.setImageDrawable(getResources().getDrawable(R.drawable.addpatient_icon_done));
        address_icon.setImageDrawable(getResources().getDrawable(R.drawable.addresslocation_icon_done));
        other_icon.setImageDrawable(getResources().getDrawable(R.drawable.other_icon));

        frag3_btn_back.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_firstscreen, new Fragment_SecondScreen())
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
            boolean isPatientInserted = patientsDAO.insertPatientToDB(patientDTO, patientDTO.getUuid());
            boolean isPatientImageInserted = imagesDAO.insertPatientProfileImages(patientDTO.getPatientPhoto(), patientDTO.getUuid());

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
                getActivity().startActivity(intent);
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), "Error of adding the data", Toast.LENGTH_SHORT).show();
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


}
