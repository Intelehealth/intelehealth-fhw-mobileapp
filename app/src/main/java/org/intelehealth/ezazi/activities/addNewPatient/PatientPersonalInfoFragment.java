package org.intelehealth.ezazi.activities.addNewPatient;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.intelehealth.ezazi.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public class PatientPersonalInfoFragment extends Fragment {
    private static final String TAG = "PatientPersonalInfoFrag";
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_patient_personal_info, container, false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }


}
