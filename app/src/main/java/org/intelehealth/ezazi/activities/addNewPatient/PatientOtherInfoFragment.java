package org.intelehealth.ezazi.activities.addNewPatient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import org.intelehealth.ezazi.R;

public class PatientOtherInfoFragment extends Fragment {
    private static final String TAG = "PatientPersonalInfoFrag";
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_patient_other_info, container, false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }


}
