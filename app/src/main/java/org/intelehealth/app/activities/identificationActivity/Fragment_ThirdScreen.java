package org.intelehealth.app.activities.identificationActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Fragment_ThirdScreen extends Fragment {
    private static final String TAG = Fragment_ThirdScreen.class.getSimpleName();
    private PatientDTO patientDTO;
    private View view;
    private EditText relation_edittext;
    private Spinner occupation_spinner, caste_spinner, education_spinner, economicstatus_spinner;
    private ImageView personal_icon, address_icon, other_icon;
    private Button frag3_btn_back, frag3_btn_next;
    private TextView relation_error, occupation_error, caste_error, education_error, economic_error;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_registration_thirdscreen, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null)
            patientDTO = (PatientDTO) getArguments().getSerializable("patientDTO");

        Log.v(TAG, "values_3: " + patientDTO.toString());
        personal_icon = getActivity().findViewById(R.id.addpatient_icon);
        address_icon = getActivity().findViewById(R.id.addresslocation_icon);
        other_icon = getActivity().findViewById(R.id.other_icon);
        frag3_btn_back = getActivity().findViewById(R.id.frag3_btn_back);
        frag3_btn_next = getActivity().findViewById(R.id.frag3_btn_next);

        relation_edittext = view.findViewById(R.id.relation_edittext);
        occupation_spinner = view.findViewById(R.id.occupation_spinner);
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

        // validation - end


        /**
         *  entering value in dataset start
         */
        if (cancel) {
            focusView.requestFocus();
        } else {

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientDTO.getUuid());
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Son/wife/daughter"));
            patientAttributesDTO.setValue(StringUtils.getValue(relation_edittext.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

          //  patientDTO.s(postalcode_edittext.getText().toString());


//            Log.v("fragmemt_2", "values: " + country_spinner.getSelectedItem().toString()
//                    + "\n" + state_spinner.getSelectedItem().toString()
//                    + "\n" + district_spinner.getSelectedItem().toString()
//                    + "\n" + city_spinner.getSelectedItem().toString()
//                    + "\n" + address1_edittext.getText().toString()
//                    + "\n" + address2_edittext.getText().toString());
        }

        // Bundle data
       /* Bundle bundle = new Bundle();
        bundle.putSerializable("patientDTO", (Serializable) patientDTO);
        fragment_thirdScreen.setArguments(bundle); // passing data to Fragment

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_firstscreen, fragment_thirdScreen)
                .commit();*/

        Intent intent = new Intent(getActivity(), PatientDetailActivity2.class);
        intent.putExtra("patientDTO", (Serializable) patientDTO);
        startActivity(intent);

    }


}
