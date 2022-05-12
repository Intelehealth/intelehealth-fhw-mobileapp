package org.intelehealth.ekalarogya.activities.surveyActivity.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.homeActivity.HomeActivity;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.databinding.FragmentThirdScreenBinding;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;

public class ThirdScreenFragment extends Fragment {

    private FragmentThirdScreenBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentThirdScreenBinding.inflate(inflater, container, false);
        setOnClickListener();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setOnClickListener() {
        binding.submitButton.setOnClickListener(v -> {
            try {
                insertData();
            } catch (DAOException e) {
                e.printStackTrace();
            }
        });
    }

    private void insertData() throws DAOException {
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(requireActivity());
        alertDialog.setTitle(requireActivity().getResources().getString(R.string.surveyDialogTitle));
        alertDialog.setMessage(requireActivity().getResources().getString(R.string.surveyDialogMessage));
        alertDialog.setPositiveButton(requireActivity().getResources().getString(R.string.ok),
                (dialog, which) -> {
                    Intent intent = new Intent(getActivity(), HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("hasPrescription", "false");
                    startActivity(intent);
                    dialog.dismiss();
                });
        AlertDialog dialog = alertDialog.show();
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(requireActivity().getResources().getColor(R.color.colorPrimaryDark));
        IntelehealthApplication.setAlertDialogCustomTheme(getActivity(), dialog);
    }
}