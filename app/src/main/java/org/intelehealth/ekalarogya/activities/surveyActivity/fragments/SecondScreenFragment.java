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
import org.intelehealth.ekalarogya.utilities.exception.DAOException;

public class SecondScreenFragment extends Fragment implements View.OnClickListener {

    private FragmentSecondScreenBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSecondScreenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setOnClickListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == binding.nextButton.getId()) {
            try {
                insertData();
            } catch (DAOException exception) {
                exception.printStackTrace();
            }
        }

        if (v.getId() == binding.prevButton.getId()) {
            requireActivity().onBackPressed();
        }
    }

    private void setOnClickListener() {
        binding.prevButton.setOnClickListener(this);
        binding.nextButton.setOnClickListener(this);
    }

    private void insertData() throws DAOException {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.frame_layout_container, new ThirdScreenFragment())
                .addToBackStack(null)
                .commit();
    }
}
