package org.intelehealth.ezazi.partogram.dialog;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.database.dao.ObsDAO;
import org.intelehealth.ezazi.databinding.OxytocinListBottomSheetDialogBinding;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.adapter.OxytocinAdministerDataAdapter;
import org.intelehealth.ezazi.partogram.adapter.PrescribedOxytocinAdapter;
import org.intelehealth.ezazi.partogram.model.Medication;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;
import org.intelehealth.ezazi.utilities.DateAndTimeUtils;
import org.intelehealth.ezazi.utilities.ScreenUtils;
import org.intelehealth.klivekit.chat.model.ItemHeader;
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder;
import org.intelehealth.klivekit.utils.DateTimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Kaveri Zaware on 09-02-2024
 * email - kaveri@intelehealth.org
 **/
public class OxytocinBottomSheetDialog extends BottomSheetDialogFragment implements BaseViewHolder.ViewHolderClickListener, View.OnClickListener {
    private static final String TAG = "OxytocinBottomSheetDial";
    private static String mVisitUUID;
    private LinkedList<ItemHeader> oxytocinsList;

    private OxytocinListChangeListener oxytocinListChangeListener;
    private List<Medication> deletedMedicines;
    private OxytocinListBottomSheetDialogBinding binding;
    private OxytocinAdministerDataAdapter adapter;

    private PartogramConstants.AccessMode accessMode;
    private String[] formArray;
    private PrescribedOxytocinAdapter prescribedOxytocinAdapter;
    private LinkedList<ItemHeader> prescribedOxytocinsList;
    private List<Medication> prescribedOxytocins;

    public void setAccessMode(PartogramConstants.AccessMode accessMode) {
        this.accessMode = accessMode;
    }

    public interface OxytocinListChangeListener {
        void onOxytocinListChanged(List<Medication> updated, List<Medication> deleted);
    }

    public void setListener(OxytocinListChangeListener listener) {
        this.oxytocinListChangeListener = listener;
    }

    public static OxytocinBottomSheetDialog getInstance(List<Medication> oxytocins, String visitUuid, OxytocinBottomSheetDialog.OxytocinListChangeListener listener) {
        OxytocinBottomSheetDialog dialog = new OxytocinBottomSheetDialog();
        dialog.setOxytocin(new ArrayList<>(oxytocins));
        mVisitUUID = visitUuid;
        dialog.setListener(listener);
        return dialog;
    }


    public OxytocinBottomSheetDialog() {
        super(R.layout.oxytocin_list_bottom_sheet_dialog);
    }

    public void setOxytocin(List<Medication> oxytocins) {
        this.oxytocinsList = new LinkedList<>();
        this.oxytocinsList.addAll(oxytocins);
    }

    public void setPrescribedMedicines(List<Medication> prescribedMedicinesList) {
        if (prescribedMedicinesList != null && prescribedMedicinesList.size() > 0) {
            this.prescribedOxytocinsList = new LinkedList<>();
            this.prescribedOxytocinsList.addAll(prescribedMedicinesList);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        addMargin(view);
        binding = OxytocinListBottomSheetDialogBinding.bind(view);
        binding.clOxytocinDialogRoot.setMinHeight(ScreenUtils.getInstance(requireContext()).getHeight());
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) binding.clOxytocinDialogRoot.getParent());
        behavior.setPeekHeight(ScreenUtils.getInstance(requireContext()).getHeight());
        behavior.addBottomSheetCallback(getCallback(behavior));

        showPrescribedMedicinesDialog();
        setOxytocinListView();
        setButtonClickListener();
        setToolbarNavClick();
        addBottomMarginIfVersion13();
    }


    private void addBottomMarginIfVersion13() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.btnSaveOxytocins.getLayoutParams();
            params.bottomMargin = params.bottomMargin + getResources().getDimensionPixelOffset(R.dimen.screen_padding) + dpToPx(16);
            binding.btnSaveOxytocins.setLayoutParams(params);

            /*ConstraintLayout.LayoutParams params1 = (ConstraintLayout.LayoutParams) binding.clAddNewMedicineRoot.getLayoutParams();
            params1.bottomMargin = params1.bottomMargin + getResources().getDimensionPixelOffset(R.dimen.screen_padding) + dpToPx(16);
            binding.clAddNewMedicineRoot.setLayoutParams(params1);*/

            ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) binding.includedPrescribedOxytocins.btnHidePrescribedOxytocin.getLayoutParams();
            params2.bottomMargin = params2.bottomMargin + getResources().getDimensionPixelOffset(R.dimen.screen_padding) + dpToPx(16);
            binding.includedPrescribedOxytocins.btnHidePrescribedOxytocin.setLayoutParams(params2);

            ConstraintLayout.LayoutParams params3 = (ConstraintLayout.LayoutParams) binding.clPrescribedOxytocinRoot.getLayoutParams();
            params3.bottomMargin = params3.bottomMargin + getResources().getDimensionPixelOffset(R.dimen.screen_padding) + dpToPx(16);
            binding.clPrescribedOxytocinRoot.setLayoutParams(params3);
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

    private int dpToPx(int dp) {
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return px;
    }

    private void setToolbarNavClick() {
        binding.bottomSheetAppBar.toolbar.setTitle(getString(R.string.lbl_oxytocin_administration));
        binding.bottomSheetAppBar.toolbar.setNavigationOnClickListener(v -> dismiss());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setButtonClickListener() {
        binding.btnSaveOxytocins.setOnClickListener(this);
        binding.btnSaveOxytocins.setEnabled(accessMode != PartogramConstants.AccessMode.READ);
        binding.btnViewPrescriptionOxytocin.setOnClickListener(this);
    }

    private void setOxytocinListView() {
        binding.btnSaveOxytocins.setEnabled(oxytocinsList.size() > 0);
        binding.rvOxytocins.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OxytocinAdministerDataAdapter(requireContext(), oxytocinsList);
        adapter.setAccessMode(accessMode);
        adapter.setClickListener(this);
        binding.rvOxytocins.setAdapter(adapter);
        changeSaveButtonStatus();
    }

    @Override
    public void onViewHolderViewClicked(@Nullable View view, int position) {
        if (view == null) return;
        if (view.getId() == R.id.btnOxytocinAdminister) {
            closePrescribedOxytocinDialog();
            showConfirmationDialog(position);
        }
    }

    private void showConfirmationDialog(int position) {
        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(requireContext()).content(getString(R.string.sure_want_to_administer_oxytocin))
                .positiveButtonLabel(R.string.yes).negativeButtonLabel(R.string.no).build();

        dialog.setListener(() -> {
            addItemInList(position);
        });
        dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSaveOxytocins:
                saveAndUpdateFinalListOfMedicines();
                break;
            case R.id.btnViewPrescriptionOxytocin:
                showPrescribedMedicinesDialog();
                break;
        }
    }

    private void saveAndUpdateFinalListOfMedicines() {
        List<Medication> oxytocinList = new ArrayList<>();
        for (ItemHeader item : adapter.getList()) {
            if (item instanceof Medication medication) oxytocinList.add((Medication) item);
        }
        oxytocinListChangeListener.onOxytocinListChanged(oxytocinList, deletedMedicines);
        dismiss();
    }

/*
    private ArrayList<String> filterMedicines(String[] medicineItem) {
        ArrayList<String> filteredItems = new ArrayList<>();
        if (adapter != null && adapter.getList().size() > 0) {
            for (String item : medicineItem) {
                for (ItemHeader medItem : adapter.getList()) {
                    if (medItem instanceof Medicine medicine && !medicine.getName().equals(item)) {
                        filteredItems.add(item);
                    }
                }
            }
        }
        return filteredItems;
    }
*/

    private void changeSaveButtonStatus() {
        //if (adapter != null) binding.btnSaveOxytocins.setEnabled(adapter.getItemCount() > 0);
        if (adapter != null) {
            binding.btnSaveOxytocins.setEnabled(adapter.getItemCount() > 0 && accessMode != PartogramConstants.AccessMode.READ);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }


    private void showPrescribedMedicinesDialog() {
        //animated dialog open
        binding.clPrescribedOxytocinRoot.setVisibility(View.VISIBLE);
        Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
        binding.clPrescribedOxytocinRoot.startAnimation(bottomUp);
        getPrescribedMedicinesDetails();

        binding.includedPrescribedOxytocins.btnHidePrescribedOxytocin.setOnClickListener(view -> {
            closePrescribedOxytocinDialog();
            setOxytocinListView();
        });
    }

    private void closePrescribedOxytocinDialog() {
        binding.clOxytocinListContainer.setVisibility(View.VISIBLE);
        binding.tvLblAdministerOxytocin.setVisibility(View.VISIBLE);

        Animation bottomDown = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_bottom);
        bottomDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.clPrescribedOxytocinRoot.setVisibility(View.GONE);
                binding.clOxytocinListContainer.setVisibility(View.VISIBLE);
                binding.tvLblAdministerOxytocin.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        binding.clPrescribedOxytocinRoot.startAnimation(bottomDown);
    }

    private void getPrescribedMedicinesDetails() {
        changeSaveButtonStatus();
        List<ObsDTO> mPrescribedOxytocinList = new ObsDAO().getAllPrescribedOxytocinsByDoctor(mVisitUUID);
        if (mPrescribedOxytocinList != null && mPrescribedOxytocinList.size() > 0) {
            manageUIVisibilityAsPerData(true);

            prescribedOxytocins = new ArrayList<>();
            for (int i = 0; i < mPrescribedOxytocinList.size(); i++) {
                ObsDTO obsDTO = mPrescribedOxytocinList.get(i);
                convertToPrescribedOxytocin(obsDTO.getUuid(), obsDTO.getValue(), obsDTO.getCreatedDate(true));
            }
            setPrescribedMedicines(prescribedOxytocins);
            binding.includedPrescribedOxytocins.rvPrescribedOxytocins.setLayoutManager(new LinearLayoutManager(requireContext()));
            prescribedOxytocinAdapter = new PrescribedOxytocinAdapter(requireContext(), prescribedOxytocinsList);
            prescribedOxytocinAdapter.setAccessMode(accessMode);
            prescribedOxytocinAdapter.setClickListener(this);
            binding.includedPrescribedOxytocins.rvPrescribedOxytocins.setAdapter(prescribedOxytocinAdapter);
        } else {
            //There is no prescribed medicines
            manageUIVisibilityAsPerData(false);

        }
    }

    public void convertToPrescribedOxytocin(String obsUuid, String value, String createdDate) {
        Log.d(TAG, "convertToPrescribedOxytocin: createdDate : " + createdDate);
        Gson gson = new Gson();
        Medication oxytocinData = gson.fromJson(value, Medication.class);
        oxytocinData.setObsUuid(obsUuid);

        oxytocinData.setCreatedAt(createdDate);
        String status = oxytocinData.getInfusionStatus();
        String statusAdminister = "";
        if (status.equalsIgnoreCase("start")) {
            statusAdminister = "Started";
        } else if (status.equalsIgnoreCase("continue")) {
            statusAdminister = "Continued";
        } else if (status.equalsIgnoreCase("stop")) {
            statusAdminister = "Stopped";
        }
        oxytocinData.setInfusionStatus(statusAdminister);
        String strength = oxytocinData.getStrength() + " (U/L)";
        oxytocinData.setStrength(strength);
        prescribedOxytocins.add(oxytocinData);
    }

    private void manageUIVisibilityAsPerData(boolean isMedicinePrescribed) {
        if (isMedicinePrescribed) {
            binding.clPrescribedOxytocinRoot.setVisibility(View.VISIBLE);
            binding.clOxytocinListContainer.setVisibility(View.VISIBLE);
            binding.tvLblAdministerOxytocin.setVisibility(View.VISIBLE);

        } else {
            binding.clPrescribedOxytocinRoot.setVisibility(View.GONE);
            binding.clOxytocinListContainer.setVisibility(View.VISIBLE);
            binding.tvLblAdministerOxytocin.setVisibility(View.VISIBLE);
            binding.btnSaveOxytocins.setEnabled(oxytocinsList.size() > 0);
            if (adapter != null) {
                binding.btnSaveOxytocins.setEnabled(adapter.getItemCount() > 0);
            }
        }

    }

    private void addItemInList(int position) {

        if (prescribedOxytocinAdapter.getItem(position) instanceof Medication medication) {
            if (oxytocinsList != null) {
                //Date currentDate = new Date();
                //SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault());
                // String formattedDate = dateFormat.format(currentDate);
                medication.setCreatedAt(DateTimeUtils.getCurrentDateInUTC(AppConstants.UTC_FORMAT));
                Log.d(TAG, "addItemInList: date in utc : " + DateTimeUtils.getCurrentDateInUTC(AppConstants.UTC_FORMAT));
                Log.d(TAG, "addItemInList:date: " + DateTimeUtils.getCurrentDateInUTC(AppConstants.UTC_FORMAT));
                oxytocinsList.add(0, medication);
                Log.d(TAG, "addItemInList: listoxy : " + new Gson().toJson(oxytocinsList));
                // Notify the adapter that a new item is inserted at position 0
                adapter.notifyItemInserted(0);
                changeSaveButtonStatus();
            }
        }
    }


}

