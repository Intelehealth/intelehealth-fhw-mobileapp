package org.intelehealth.app.activities.identificationActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;
import org.intelehealth.app.models.dto.PatientDTO;

import java.util.List;

/**
 * Created by Prajwal Waingankar on 13/10/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class Fragment_SecondScreen extends Fragment {
    private View view;
    private ImageView personal_icon, address_icon, other_icon;
    private Button frag2_btn_back, frag2_btn_next;
    private EditText postalcode_edittext;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_registration_secondscreen, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        personal_icon = getActivity().findViewById(R.id.addpatient_icon);
        address_icon = getActivity().findViewById(R.id.addresslocation_icon);
        other_icon = getActivity().findViewById(R.id.other_icon);
        frag2_btn_back = getActivity().findViewById(R.id.frag2_btn_back);
        frag2_btn_next = getActivity().findViewById(R.id.frag2_btn_next);
        postalcode_edittext = view.findViewById(R.id.postalcode_edittext);

        PatientDTO patientDTO = (PatientDTO) getArguments().getSerializable("patientDTO");
        Log.v("ff", "ddd");
        postalcode_edittext.setText(patientDTO.getFirstname());

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        personal_icon.setImageDrawable(getResources().getDrawable(R.drawable.addpatient_icon_done));
        address_icon.setImageDrawable(getResources().getDrawable(R.drawable.addresslocation_icon));
        other_icon.setImageDrawable(getResources().getDrawable(R.drawable.other_icon_unselected));

        frag2_btn_back.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            //   bundle.putString("");
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_firstscreen, new Fragment_FirstScreen())
                    .commit();
        });

        frag2_btn_next.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            //   bundle.putString("");
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_firstscreen, new Fragment_ThirdScreen())
                    .commit();
        });

    }
}
