package org.intelehealth.ekalarogya.activities.surveyActivity.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.databinding.FragmentSixthScreenBinding;

public class SixthScreenFragment extends Fragment {

    private FragmentSixthScreenBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSixthScreenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
