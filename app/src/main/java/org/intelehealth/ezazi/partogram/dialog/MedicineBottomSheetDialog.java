package org.intelehealth.ezazi.partogram.dialog;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.setupActivity.LocationArrayAdapter;
import org.intelehealth.ezazi.databinding.MedicinesListBottomSheetDialogBinding;
import org.intelehealth.ezazi.partogram.adapter.MedicineAdapter;
import org.intelehealth.ezazi.partogram.model.Medicine;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;
import org.intelehealth.ezazi.ui.shared.TextChangeListener;
import org.intelehealth.ezazi.utilities.ScreenUtils;
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Vaghela Mithun R. on 06-09-2023 - 17:49.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class MedicineBottomSheetDialog extends BottomSheetDialogFragment
        implements BaseViewHolder.ViewHolderClickListener, View.OnClickListener {
    private List<Medicine> medicines;

    private MedicineListChangeListener medicineListChangeListener;
    private List<Medicine> deletedMedicines;
    private MedicinesListBottomSheetDialogBinding binding;
    private MedicineAdapter adapter;

    public interface MedicineListChangeListener {
        void onMedicineListChanged(List<Medicine> updated, List<Medicine> deleted);
    }

    public void setListener(MedicineListChangeListener listener) {
        this.medicineListChangeListener = listener;
    }

    public static MedicineBottomSheetDialog getInstance(List<Medicine> medicines, MedicineListChangeListener listener) {
        MedicineBottomSheetDialog dialog = new MedicineBottomSheetDialog();
        dialog.setMedicines(new ArrayList<>(medicines));
        dialog.setListener(listener);
        return dialog;
    }

    public MedicineBottomSheetDialog() {
        super(R.layout.medicines_list_bottom_sheet_dialog);
    }

    public void setMedicines(List<Medicine> medicines) {
        this.medicines = medicines;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = MedicinesListBottomSheetDialogBinding.bind(view);
        binding.clMedicineDialogRoot.setMinHeight(ScreenUtils.getInstance(requireContext()).getHeight());
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) binding.clMedicineDialogRoot.getParent());
        behavior.setPeekHeight(ScreenUtils.getInstance(requireContext()).getHeight());
        setMedicineListView();
//        behavior.addBottomSheetCallback(callback);
        setButtonClickListener();
        setToolbarNavClick();
        buildAddNewMedicineDialog();
        validateMedicineFormInput();
    }

//    private final BottomSheetBehavior.BottomSheetCallback callback = new BottomSheetBehavior.BottomSheetCallback() {
//        @Override
//        public void onStateChanged(@NonNull View bottomSheet, int newState) {
//            adapter.updateItems(medicines);
//        }
//
//        @Override
//        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//
//        }
//    };

    private void setToolbarNavClick() {
        binding.bottomSheetAppBar.toolbar.setNavigationOnClickListener(v -> dismiss());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setButtonClickListener() {
        binding.btnAddMoreMedicine.setOnClickListener(this);
        binding.includeAddNewMedicineDialog.btnAddMedicineAdd.setOnClickListener(this);
        binding.includeAddNewMedicineDialog.btnAddMedicineCancel.setOnClickListener(this);
        binding.btnSaveMedicines.setOnClickListener(this);
        binding.clAddNewMedicineRoot.setClickable(false);
        binding.clAddNewMedicineRoot.setOnTouchListener((v, event) -> true);
    }

    private void setMedicineListView() {
        binding.btnSaveMedicines.setEnabled(medicines.size() > 0);
        binding.rvMedicines.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MedicineAdapter(requireContext(), medicines);
        adapter.setClickListener(this);
        binding.rvMedicines.setAdapter(adapter);
    }

    @Override
    public void onViewHolderViewClicked(@Nullable View view, int position) {
        if (view == null) return;
        if (view.getId() == R.id.btnEditMedicine) {
            binding.includeAddNewMedicineDialog.setMedicine(adapter.getItem(position));
            binding.includeAddNewMedicineDialog.setUpdatePosition(position);
            binding.clAddNewMedicineRoot.setVisibility(View.VISIBLE);
            binding.btnAddMoreMedicine.setVisibility(View.GONE);
            binding.includeAddNewMedicineDialog.btnAddMedicineAdd.setText(getString(R.string.update));
        } else if (view.getId() == R.id.btnMedicineDelete) {
            showConfirmationDialog(position);
        }
    }

    private void showConfirmationDialog(int position) {
        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(requireContext())
                .content(getString(R.string.alert_delete_medicine))
                .positiveButtonLabel(R.string.button_delete)
                .negativeButtonLabel(R.string.no)
                .build();

        dialog.setListener(() -> {
            addToDeletedMedicinesList(adapter.getItem(position));
            adapter.remove(position);
        });
        dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void addToDeletedMedicinesList(Medicine item) {
        if (deletedMedicines == null) deletedMedicines = new ArrayList<>();
        if (item.getObsUuid() != null)
            deletedMedicines.add(item);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddMoreMedicine:
                setupMedicines();
                binding.clAddNewMedicineRoot.setVisibility(View.VISIBLE);
                binding.btnAddMoreMedicine.setVisibility(View.GONE);
                binding.includeAddNewMedicineDialog.btnAddMedicineAdd.setText(getString(R.string.lbl_add));
                break;
            case R.id.btnAddMedicineAdd:
                binding.btnAddMoreMedicine.setVisibility(View.VISIBLE);
                binding.clAddNewMedicineRoot.setVisibility(View.GONE);
                Medicine medicine = validateMedicineFormInput();
                if (medicine.isValidMedicine()) {
                    int updated = -1;
                    if (binding.includeAddNewMedicineDialog.getUpdatePosition() != null) {
                        updated = binding.includeAddNewMedicineDialog.getUpdatePosition();
                    }
                    if (updated > -1) adapter.updateItemAt(updated, medicine);
                    else adapter.addItem(medicine);
                    clearAddNewMedicineForm();
                    changeSaveButtonStatus();
                }
                break;
            case R.id.btnAddMedicineCancel:
                binding.btnAddMoreMedicine.setVisibility(View.VISIBLE);
                binding.clAddNewMedicineRoot.setVisibility(View.GONE);
                break;
            case R.id.btnSaveMedicines:
                saveAndUpdateFinalListOfMedicines();
                break;
        }
    }

    private void saveAndUpdateFinalListOfMedicines() {
        medicineListChangeListener.onMedicineListChanged(adapter.getList(), deletedMedicines);
        dismiss();
    }

    private void clearAddNewMedicineForm() {
        binding.includeAddNewMedicineDialog.setUpdatePosition(-1);
        binding.includeAddNewMedicineDialog.autoCompleteOtherMedicine.setText("");
        binding.includeAddNewMedicineDialog.etMedicineStrength.setText("");
        binding.includeAddNewMedicineDialog.etMedicineDosage.setText("");
        binding.includeAddNewMedicineDialog.etMedicineType.setText("");
        binding.includeAddNewMedicineDialog.etMedicineDosageUnit.setText("");
        binding.includeAddNewMedicineDialog.autoCompleteMedicineRoute.setText("");
        binding.includeAddNewMedicineDialog.autoCompleteMedicineFrequency.setText("");
        validateMedicineFormInput();
    }

    private Medicine validateMedicineFormInput() {
        Medicine medicine = new Medicine();
        medicine.setName(binding.includeAddNewMedicineDialog.autoCompleteOtherMedicine.getText().toString());
        medicine.setStrength(binding.includeAddNewMedicineDialog.etMedicineStrength.getText().toString());
        medicine.setDosage(binding.includeAddNewMedicineDialog.etMedicineDosage.getText().toString());
        medicine.setDosageUnit(binding.includeAddNewMedicineDialog.etMedicineDosageUnit.getText().toString());
        medicine.setRoute(binding.includeAddNewMedicineDialog.autoCompleteMedicineRoute.getText().toString());
        medicine.setType(binding.includeAddNewMedicineDialog.etMedicineType.getText().toString());
        medicine.setFrequency(binding.includeAddNewMedicineDialog.autoCompleteMedicineFrequency.getText().toString());
        binding.includeAddNewMedicineDialog.btnAddMedicineAdd.setEnabled(medicine.isValidMedicine());
        return medicine;
    }

    private void buildAddNewMedicineDialog() {
        setupMedicines();
        setupRoutes();
        setupFrequency();
        binding.includeAddNewMedicineDialog.etMedicineDosage.addTextChangedListener(listener);
        binding.includeAddNewMedicineDialog.etMedicineDosageUnit.addTextChangedListener(listener);
        binding.includeAddNewMedicineDialog.etMedicineStrength.addTextChangedListener(listener);
        binding.includeAddNewMedicineDialog.etMedicineType.addTextChangedListener(listener);
    }

    private final TextChangeListener listener = new TextChangeListener() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            validateMedicineFormInput();
        }
    };

    private void setupMedicines() {
        String[] medicineItem = requireContext().getResources().getStringArray(R.array.medicines);
        AutoCompleteTextView medicineDropDown = binding.includeAddNewMedicineDialog.autoCompleteOtherMedicine;
        ArrayList<String> filteredItems = filterMedicines(medicineItem);
        if (filteredItems.size() > 0) {
            String[] array = filteredItems.toArray(new String[0]);
            setupAutoCompleteAdapter(array, medicineDropDown);
        } else setupAutoCompleteAdapter(medicineItem, medicineDropDown);
    }

    private ArrayList<String> filterMedicines(String[] medicineItem) {
        ArrayList<String> filteredItems = new ArrayList<>();
        if (adapter != null && adapter.getList().size() > 0) {
            for (String item : medicineItem) {
                for (Medicine medicine : adapter.getList()) {
                    if (!medicine.getName().equals(item)) {
                        filteredItems.add(item);
                    }
                }
            }
        }
        return filteredItems;
    }

    private void setupRoutes() {
        String[] routes = requireContext().getResources().getStringArray(R.array.medicine_routes);
        AutoCompleteTextView medicineDropDown = binding.includeAddNewMedicineDialog.autoCompleteMedicineRoute;
        setupAutoCompleteAdapter(routes, medicineDropDown);
    }

    private void setupFrequency() {
        String[] frequencies = requireContext().getResources().getStringArray(R.array.medicine_frequencies);
        AutoCompleteTextView medicineDropDown = binding.includeAddNewMedicineDialog.autoCompleteMedicineFrequency;
        setupAutoCompleteAdapter(frequencies, medicineDropDown);
    }

    private void setupAutoCompleteAdapter(String[] items, AutoCompleteTextView autoCompleteTextView) {
        LocationArrayAdapter adapter = new LocationArrayAdapter(requireContext(), Arrays.asList(items));
        autoCompleteTextView.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            validateMedicineFormInput();
        });
        autoCompleteTextView.setAdapter(adapter);
    }

    private void changeSaveButtonStatus() {
        if (adapter != null) binding.btnSaveMedicines.setEnabled(adapter.getItemCount() > 0);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
