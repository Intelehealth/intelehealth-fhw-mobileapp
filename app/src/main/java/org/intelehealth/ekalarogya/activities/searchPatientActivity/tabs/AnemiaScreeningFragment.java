package org.intelehealth.ekalarogya.activities.searchPatientActivity.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.intelehealth.ekalarogya.databinding.LayoutSearchPatientCategoryBinding;

public class AnemiaScreeningFragment extends Fragment {

    private LayoutSearchPatientCategoryBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutSearchPatientCategoryBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }
}
