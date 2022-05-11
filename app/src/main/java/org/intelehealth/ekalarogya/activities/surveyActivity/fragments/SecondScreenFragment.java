package org.intelehealth.ekalarogya.activities.surveyActivity.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.databinding.FragmentSecondScreenBinding;

public class SecondScreenFragment extends Fragment {

    private FragmentSecondScreenBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSecondScreenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}