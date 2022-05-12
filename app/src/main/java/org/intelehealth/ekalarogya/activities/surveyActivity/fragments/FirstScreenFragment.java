package org.intelehealth.ekalarogya.activities.surveyActivity.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.databinding.FragmentFirstScreenBinding;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;

public class FirstScreenFragment extends Fragment implements View.OnClickListener {

    private FragmentFirstScreenBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFirstScreenBinding.inflate(inflater, container, false);
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
        if (v.getId() == binding.nextButtonLinearLayout.getId()) {
            try {
                insertData();
            } catch (DAOException exception) {
                exception.printStackTrace();
            }
        }
    }

    private void setOnClickListener() {
        binding.nextButtonLinearLayout.setOnClickListener(this);
    }

    private void insertData() throws DAOException {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.frame_layout_container, new SecondScreenFragment())
                .addToBackStack(null)
                .commit();
    }
}