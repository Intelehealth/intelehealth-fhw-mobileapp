package org.intelehealth.app.activities.identificationActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2;

public class Fragment_ThirdScreen extends Fragment {
    private View view;
    private ImageView personal_icon, address_icon, other_icon;
    private Button frag3_btn_back, frag3_btn_next;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_registration_thirdscreen, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        personal_icon = getActivity().findViewById(R.id.addpatient_icon);
        address_icon = getActivity().findViewById(R.id.addresslocation_icon);
        other_icon = getActivity().findViewById(R.id.other_icon);
        frag3_btn_back = getActivity().findViewById(R.id.frag3_btn_back);
        frag3_btn_next = getActivity().findViewById(R.id.frag3_btn_next);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        personal_icon.setImageDrawable(getResources().getDrawable(R.drawable.addpatient_icon_done));
        address_icon.setImageDrawable(getResources().getDrawable(R.drawable.addresslocation_icon_done));
        other_icon.setImageDrawable(getResources().getDrawable(R.drawable.other_icon));

        frag3_btn_back.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            //   bundle.putString("");
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_firstscreen, new Fragment_SecondScreen())
                    .commit();
        });

        frag3_btn_next.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), PatientDetailActivity2.class);
                startActivity(intent);
            });

    }
}
