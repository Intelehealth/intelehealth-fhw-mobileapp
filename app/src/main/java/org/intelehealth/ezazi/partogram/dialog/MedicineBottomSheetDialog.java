package org.intelehealth.ezazi.partogram.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.setupActivity.LocationArrayAdapter;
import org.intelehealth.ezazi.databinding.MedicinesListBottomSheetDialogBinding;
import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.adapter.MedicineAdapter;
import org.intelehealth.ezazi.partogram.behavior.LockableBottomSheetBehavior;
import org.intelehealth.ezazi.partogram.model.GetMedicineData;
import org.intelehealth.ezazi.partogram.model.Medicine;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;
import org.intelehealth.ezazi.ui.shared.TextChangeListener;
import org.intelehealth.ezazi.ui.validation.FirstLetterUpperCaseInputFilter;
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
    private static final String TAG = "MedicineBottomSheetDial";
    private List<Medicine> medicines;

    private MedicineListChangeListener medicineListChangeListener;
    private List<Medicine> deletedMedicines;
    private MedicinesListBottomSheetDialogBinding binding;
    private MedicineAdapter adapter;

    private PartogramConstants.AccessMode accessMode;
    private List<Medicine> medicineDetailsList;
    private String[] formArray;

    public void setAccessMode(PartogramConstants.AccessMode accessMode) {
        this.accessMode = accessMode;
    }

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
//        addMargin(view);
        binding = MedicinesListBottomSheetDialogBinding.bind(view);
        binding.clMedicineDialogRoot.setMinHeight(ScreenUtils.getInstance(requireContext()).getHeight());
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) binding.clMedicineDialogRoot.getParent());
        behavior.setPeekHeight(ScreenUtils.getInstance(requireContext()).getHeight());
        behavior.addBottomSheetCallback(getCallback(behavior));

        setMedicinesAndItsDetails();
        setMedicineListView();
        setButtonClickListener();
        setToolbarNavClick();
        buildAddNewMedicineDialog();
        validateMedicineFormInput();
        setupInputFilter();

        addBottomMarginIfVersion13();
    }

    private void addBottomMarginIfVersion13() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.btnSaveMedicines.getLayoutParams();
            params.bottomMargin = params.bottomMargin + getResources().getDimensionPixelOffset(R.dimen.screen_padding) + dpToPx(16);
            binding.btnSaveMedicines.setLayoutParams(params);

            ConstraintLayout.LayoutParams params1 = (ConstraintLayout.LayoutParams) binding.clAddNewMedicineRoot.getLayoutParams();
            params1.bottomMargin = params1.bottomMargin + getResources().getDimensionPixelOffset(R.dimen.screen_padding) + dpToPx(16);
            binding.clAddNewMedicineRoot.setLayoutParams(params1);
        }
    }

    private BottomSheetBehavior.BottomSheetCallback getCallback(BottomSheetBehavior<View> behavior) {
        return new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        };
    }

    private void setMedicinesAndItsDetails() {

    }

//    @NonNull
//    @Override
//    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        Dialog dialog = super.onCreateDialog(savedInstanceState);
////        dialog.setOnShowListener(dialogInterface -> {
////            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
////            setupFullWidth(bottomSheetDialog);
////        });
//
//        hideBottomSystemTaskbar(dialog);
//        return dialog;
//    }

    private void addMargin(View view) {
        FrameLayout.LayoutParams layoutParams =
                (FrameLayout.LayoutParams) view.getLayoutParams();
        int margin_16dp = dpToPx(16);
        layoutParams.width = ScreenUtils.getInstance(requireContext()).getWidth();
        layoutParams.height = ScreenUtils.getInstance(requireContext()).getHeight();
        view.setLayoutParams(layoutParams);
        view.requestLayout();
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
        return px;
    }

    private void hideBottomSystemTaskbar(Dialog dialog) {
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(dialog.getWindow(), dialog.getWindow().getDecorView());
        // Configure the behavior of the hidden system bars.
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );

        // Add a listener to update the behavior of the toggle fullscreen button when
        // the system bars are hidden or revealed.
        dialog.getWindow().getDecorView().setOnApplyWindowInsetsListener((view, windowInsets) -> {
            // You can hide the caption bar even when the other system bars are visible.
            // To account for this, explicitly check the visibility of navigationBars()
            // and statusBars() rather than checking the visibility of systemBars().
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                        || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())) {
                    // Hide both the status bar and the navigation bar.
                    windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());
                    windowInsetsController.hide(WindowInsetsCompat.Type.displayCutout());
                    windowInsetsController.hide(WindowInsetsCompat.Type.captionBar());
                }
            }
            return view.onApplyWindowInsets(windowInsets);
        });
    }

//    private void setupFullWidth(BottomSheetDialog bottomSheetDialog) {
//        FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
//        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
//        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) binding.clMedicineDialogRoot.getParent());
//        if (layoutParams != null) {
//            layoutParams.width = ScreenUtils.getInstance(requireContext()).getWidth();
//            layoutParams.height = ScreenUtils.getInstance(requireContext()).getHeight();
//        } else {
//            Log.e("MedicineBottomSheet", "setupFullWidth: null");
//        }
//        bottomSheet.setLayoutParams(layoutParams);
//        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//    }

    private void setupInputFilter() {
        setInputFilter(binding.includeAddNewMedicineDialog.autoCompleteMedicineForm);
        setInputFilter(binding.includeAddNewMedicineDialog.autoCompleteMedicineStrength);
        setInputFilter(binding.includeAddNewMedicineDialog.etMedicineDosage);
        setInputFilter(binding.includeAddNewMedicineDialog.autoCompleteMedicineDosageUnit);
        setInputFilter(binding.includeAddNewMedicineDialog.autoCompleteMedicineFrequency);
        setInputFilter(binding.includeAddNewMedicineDialog.autoCompleteOtherMedicine);
        setInputFilter(binding.includeAddNewMedicineDialog.autoCompleteMedicineRoute);
        setInputFilter(binding.includeAddNewMedicineDialog.autoCompleteMedicineDurationUnit);
        setInputFilter(binding.includeAddNewMedicineDialog.etMedicineDuration);
        setInputFilter(binding.includeAddNewMedicineDialog.etRemark);

        //binding.includeAddNewMedicineDialog.autoCompleteMedicineForm.setFilters(new InputFilter[]{new ValueInputFilter(formArray)});

    }

    private void setInputFilter(TextInputEditText editText) {
        editText.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter()});
    }

    private void setInputFilter(AutoCompleteTextView autoCompleteTextView) {
        autoCompleteTextView.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter()});
    }

    private void setToolbarNavClick() {
        binding.bottomSheetAppBar.toolbar.setTitle(getString(R.string.lbl_medicines));
        binding.bottomSheetAppBar.toolbar.setNavigationOnClickListener(v -> dismiss());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setButtonClickListener() {
        binding.btnAddMoreMedicine.setOnClickListener(this);
        binding.includeAddNewMedicineDialog.btnAddMedicineAdd.setOnClickListener(this);
        binding.includeAddNewMedicineDialog.btnAddMedicineCancel.setOnClickListener(this);
        binding.btnSaveMedicines.setOnClickListener(this);
        binding.btnAddMoreMedicine.setEnabled(accessMode != PartogramConstants.AccessMode.READ);
        binding.btnSaveMedicines.setEnabled(accessMode != PartogramConstants.AccessMode.READ);
        binding.clAddNewMedicineRoot.setClickable(false);
        binding.clAddNewMedicineRoot.setOnTouchListener((v, event) -> true);
    }

    private void setMedicineListView() {
        binding.btnSaveMedicines.setEnabled(medicines.size() > 0);
        if (medicines.size() == 0) openNewMedicineDialog();
        binding.rvMedicines.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MedicineAdapter(requireContext(), medicines);
        adapter.setAccessMode(accessMode);
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
        } else if (view.getId() == R.id.clMedicineRowItemRoot) {
            adapter.setExpandedItemPosition(position);
        } else if (view.getId() == R.id.btnExpandCollapseIndicator) {
            adapter.setExpandedItemPosition(position);
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
                openNewMedicineDialog();
                binding.btnAddMoreMedicine.setVisibility(View.GONE);
                binding.includeAddNewMedicineDialog.btnAddMedicineAdd.setText(getString(R.string.lbl_add));
                break;
            case R.id.btnAddMedicineAdd:
                binding.btnAddMoreMedicine.setVisibility(View.VISIBLE);
                closeNewMedicineDialog();
                Medicine medicine = validateMedicineFormInput();
                if (medicine.isValidMedicine()) {
                    int updated = -1;
                    if (binding.includeAddNewMedicineDialog.getUpdatePosition() != null) {
                        updated = binding.includeAddNewMedicineDialog.getUpdatePosition();
                        if (binding.includeAddNewMedicineDialog.getMedicine() != null)
                            medicine.setObsUuid(binding.includeAddNewMedicineDialog.getMedicine().getObsUuid());
                    }
                    if (updated > -1) adapter.updateItemAt(updated, medicine);
                    else adapter.addItem(medicine);
                    clearAddNewMedicineForm();
                    changeSaveButtonStatus();
                }
                break;
            case R.id.btnAddMedicineCancel:
                binding.btnAddMoreMedicine.setVisibility(View.VISIBLE);
                closeNewMedicineDialog();
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
        binding.includeAddNewMedicineDialog.autoCompleteMedicineStrength.setText("");
        binding.includeAddNewMedicineDialog.etMedicineDosage.setText("");
        binding.includeAddNewMedicineDialog.autoCompleteMedicineForm.setText("");
        binding.includeAddNewMedicineDialog.autoCompleteMedicineDosageUnit.setText("");
        binding.includeAddNewMedicineDialog.autoCompleteMedicineRoute.setText("");
        binding.includeAddNewMedicineDialog.autoCompleteMedicineFrequency.setText("");
        binding.includeAddNewMedicineDialog.autoCompleteMedicineDurationUnit.setText("");
        binding.includeAddNewMedicineDialog.etMedicineDuration.setText("");
        binding.includeAddNewMedicineDialog.etRemark.setText("");

        validateMedicineFormInput();
    }

    private Medicine validateMedicineFormInput() {
        Medicine medicine = new Medicine();
        medicine.setName(binding.includeAddNewMedicineDialog.autoCompleteOtherMedicine.getText().toString());
        medicine.setStrength(binding.includeAddNewMedicineDialog.autoCompleteMedicineStrength.getText().toString());
        medicine.setDosage(binding.includeAddNewMedicineDialog.etMedicineDosage.getText().toString());
        medicine.setDosageUnit(binding.includeAddNewMedicineDialog.autoCompleteMedicineDosageUnit.getText().toString());
        medicine.setRoute(binding.includeAddNewMedicineDialog.autoCompleteMedicineRoute.getText().toString());
        medicine.setForm(binding.includeAddNewMedicineDialog.autoCompleteMedicineForm.getText().toString());
        medicine.setFrequency(binding.includeAddNewMedicineDialog.autoCompleteMedicineFrequency.getText().toString());
        medicine.setDurationUnit(binding.includeAddNewMedicineDialog.autoCompleteMedicineDurationUnit.getText().toString());
        medicine.setDuration(binding.includeAddNewMedicineDialog.etMedicineDuration.getText().toString());
        medicine.setRemark(binding.includeAddNewMedicineDialog.etRemark.getText().toString());

        binding.includeAddNewMedicineDialog.btnAddMedicineAdd.setEnabled(medicine.isValidMedicine());
        return medicine;
    }

    private void buildAddNewMedicineDialog() {
        setupMedicines();
        setupRoutes();
        setupDoseUnits();
        setupFrequency();
        setupDurationUnits();
        setFormArray();
        setStrength();
        binding.includeAddNewMedicineDialog.etMedicineDosage.addTextChangedListener(listener);
        binding.includeAddNewMedicineDialog.etMedicineDuration.addTextChangedListener(listener);
        binding.includeAddNewMedicineDialog.etRemark.addTextChangedListener(listener);
    }

    private final TextChangeListener listener = new TextChangeListener() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            validateMedicineFormInput();
        }
    };

    private void setupMedicines() {
        medicineDetailsList = new GetMedicineData().getMedicineDetails(getActivity());

        String[] medicineItem = new String[medicineDetailsList.size()];
        for (int i = 0; i < medicineDetailsList.size(); i++) {
            Medicine medicine = medicineDetailsList.get(i);
            medicine.setMedicineFullName(medicine.getForm() + " " + medicine.getName() + " " + medicine.getStrength());
            medicineItem[i] = medicine.getMedicineFullName();
        }

        //String[] medicineItem = requireContext().getResources().getStringArray(R.array.medicines);
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

    private void setupDoseUnits() {
        String[] routes = requireContext().getResources().getStringArray(R.array.dose_units);
        AutoCompleteTextView doseDropDown = binding.includeAddNewMedicineDialog.autoCompleteMedicineDosageUnit;
        setupAutoCompleteAdapter(routes, doseDropDown);
    }

    private void setupRoutes() {
        String[] routes = requireContext().getResources().getStringArray(R.array.medicine_routes);
        AutoCompleteTextView routeDropDown = binding.includeAddNewMedicineDialog.autoCompleteMedicineRoute;
        setupAutoCompleteAdapter(routes, routeDropDown);
    }

    private void setupFrequency() {
        String[] frequencies = requireContext().getResources().getStringArray(R.array.medicine_frequencies);
        AutoCompleteTextView frequenciesDropDown = binding.includeAddNewMedicineDialog.autoCompleteMedicineFrequency;
        setupAutoCompleteAdapter(frequencies, frequenciesDropDown);
    }

    private void setupAutoCompleteAdapter(String[] items, AutoCompleteTextView autoCompleteTextView) {
        LocationArrayAdapter adapter = new LocationArrayAdapter(requireContext(), Arrays.asList(items));
        autoCompleteTextView.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);

        autoCompleteTextView.setThreshold(0);
        autoCompleteTextView.setOnClickListener(v -> {
            autoCompleteTextView.requestFocus(); // Request focus to ensure the dropdown appears
            autoCompleteTextView.showDropDown();
        });
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            if (autoCompleteTextView.getId() == binding.includeAddNewMedicineDialog.autoCompleteOtherMedicine.getId()) {
                //autofill other fields
                String selectedItem = (String) parent.getItemAtPosition(position);
                autoCompleteTextView.setSelection(selectedItem.length());

                for (Medicine medicine : medicineDetailsList) {
                    if (selectedItem.equalsIgnoreCase(medicine.getMedicineFullName())) {
                        binding.includeAddNewMedicineDialog.setMedicine(medicine);
                    }
                }
            }
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

    private void openNewMedicineDialog() {
        binding.clAddNewMedicineRoot.setVisibility(View.VISIBLE);
        Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
        binding.clAddNewMedicineRoot.startAnimation(bottomUp);
    }

    private void closeNewMedicineDialog() {
        Animation bottomDown = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_bottom);
        bottomDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.clAddNewMedicineRoot.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        binding.clAddNewMedicineRoot.startAnimation(bottomDown);
    }

    private void setupDurationUnits() {
        String[] durationUnitArray = requireContext().getResources().getStringArray(R.array.medicine_duration_units);
        AutoCompleteTextView durationUnitDropdown = binding.includeAddNewMedicineDialog.autoCompleteMedicineDurationUnit;
        setupAutoCompleteAdapter(durationUnitArray, durationUnitDropdown);
    }

    private void setFormArray() {
        formArray = requireContext().getResources().getStringArray(R.array.medicine_form);
        AutoCompleteTextView formDropdown = binding.includeAddNewMedicineDialog.autoCompleteMedicineForm;
        setupAutoCompleteAdapter(formArray, formDropdown);
    }

    private void setStrength() {
        String[] strengthArray = requireContext().getResources().getStringArray(R.array.medicine_strength);
        AutoCompleteTextView strengthDropdown = binding.includeAddNewMedicineDialog.autoCompleteMedicineStrength;
        setupAutoCompleteAdapter(strengthArray, strengthDropdown);
    }
}

