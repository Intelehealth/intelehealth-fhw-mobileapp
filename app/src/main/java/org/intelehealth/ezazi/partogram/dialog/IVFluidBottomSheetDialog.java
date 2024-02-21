package org.intelehealth.ezazi.partogram.dialog;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.database.dao.ObsDAO;
import org.intelehealth.ezazi.databinding.IvFluidListBottomSheetDialogBinding;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.adapter.IvFluidAdministerDataAdapter;
import org.intelehealth.ezazi.partogram.adapter.PrescribedIvFluidAdapter;
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
 * Created by Kaveri Zaware on 07-02-2024
 * email - kaveri@intelehealth.org
 **/
public class IVFluidBottomSheetDialog extends BottomSheetDialogFragment implements BaseViewHolder.ViewHolderClickListener, View.OnClickListener {
    private static final String TAG = "IVFluidBottomSheetDialo";
    private static String mVisitUUID;
    private LinkedList<ItemHeader> ivFluidsList;

    private IvFluidListChangeListener ivFluidListChangeListener;
    private List<Medication> deletedMedicines;
    private IvFluidListBottomSheetDialogBinding binding;
    private IvFluidAdministerDataAdapter adapter;

    private PartogramConstants.AccessMode accessMode;
    private String[] formArray;
    private PrescribedIvFluidAdapter prescribedIvFluidAdapter;
    private LinkedList<ItemHeader> prescribedIvFluidList;
    private List<Medication> prescribedIvFluids;

    public void setAccessMode(PartogramConstants.AccessMode accessMode) {
        this.accessMode = accessMode;
    }

    public interface IvFluidListChangeListener {
        void onIvFluidListChanged(List<Medication> updated, List<Medication> deleted);
    }

    public void setListener(IvFluidListChangeListener listener) {
        this.ivFluidListChangeListener = listener;
    }

    public static IVFluidBottomSheetDialog getInstance(List<Medication> ivFluids, String visitUuid, IvFluidListChangeListener listener) {
        IVFluidBottomSheetDialog dialog = new IVFluidBottomSheetDialog();
        dialog.setIvFluids(new ArrayList<>(ivFluids));
        mVisitUUID = visitUuid;
        dialog.setListener(listener);
        return dialog;
    }


    public IVFluidBottomSheetDialog() {
        super(R.layout.iv_fluid_list_bottom_sheet_dialog);
    }

    public void setIvFluids(List<Medication> ivFluids) {
        this.ivFluidsList = new LinkedList<>();
        this.ivFluidsList.addAll(ivFluids);
    }

    public void setPrescribedMedicines(List<Medication> prescribedMedicinesList) {
        if (prescribedMedicinesList != null && prescribedMedicinesList.size() > 0) {
            this.prescribedIvFluidList = new LinkedList<>();
            this.prescribedIvFluidList.addAll(prescribedMedicinesList);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        addMargin(view);
        binding = IvFluidListBottomSheetDialogBinding.bind(view);
        binding.clIvFluidDialogRoot.setMinHeight(ScreenUtils.getInstance(requireContext()).getHeight());
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) binding.clIvFluidDialogRoot.getParent());
        behavior.setPeekHeight(ScreenUtils.getInstance(requireContext()).getHeight());
        behavior.addBottomSheetCallback(getCallback(behavior));

        showPrescribedMedicinesDialog();
        setIvFluidsListView();
        setButtonClickListener();
        setToolbarNavClick();
        addBottomMarginIfVersion13();
        changeSaveButtonStatus();
    }


    private void addBottomMarginIfVersion13() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.btnSaveIvFluids.getLayoutParams();
            params.bottomMargin = params.bottomMargin + getResources().getDimensionPixelOffset(R.dimen.screen_padding) + dpToPx(16);
            binding.btnSaveIvFluids.setLayoutParams(params);

            /*ConstraintLayout.LayoutParams params1 = (ConstraintLayout.LayoutParams) binding.clAddNewMedicineRoot.getLayoutParams();
            params1.bottomMargin = params1.bottomMargin + getResources().getDimensionPixelOffset(R.dimen.screen_padding) + dpToPx(16);
            binding.clAddNewMedicineRoot.setLayoutParams(params1);*/

            ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) binding.includedPrescribedIvFluids.btnHidePrescribedIvDialog.getLayoutParams();
            params2.bottomMargin = params2.bottomMargin + getResources().getDimensionPixelOffset(R.dimen.screen_padding) + dpToPx(16);
            binding.includedPrescribedIvFluids.btnHidePrescribedIvDialog.setLayoutParams(params2);

            ConstraintLayout.LayoutParams params3 = (ConstraintLayout.LayoutParams) binding.clPrescribedIvFluidRoot.getLayoutParams();
            params3.bottomMargin = params3.bottomMargin + getResources().getDimensionPixelOffset(R.dimen.screen_padding) + dpToPx(16);
            binding.clPrescribedIvFluidRoot.setLayoutParams(params3);
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
        binding.bottomSheetAppBar.toolbar.setTitle(getString(R.string.lbl_iv_fluid_administration));
        binding.bottomSheetAppBar.toolbar.setNavigationOnClickListener(v -> dismiss());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setButtonClickListener() {
        Log.d(TAG, "setButtonClickListener: accessMode : " + accessMode);
        binding.btnSaveIvFluids.setOnClickListener(this);
        binding.btnSaveIvFluids.setEnabled(accessMode != PartogramConstants.AccessMode.READ);
        binding.btnViewPrescriptionIvFluid.setOnClickListener(this);
    }

    private void setIvFluidsListView() {
        binding.btnSaveIvFluids.setEnabled(ivFluidsList.size() > 0);
        binding.rvIvFluids.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new IvFluidAdministerDataAdapter(requireContext(), ivFluidsList);
        adapter.setAccessMode(accessMode);
        adapter.setClickListener(this);
        binding.rvIvFluids.setAdapter(adapter);
        changeSaveButtonStatus();

    }

    @Override
    public void onViewHolderViewClicked(@Nullable View view, int position) {
        if (view == null) return;
        if (view.getId() == R.id.btnIvFluidAdminister) {
            closePrescribedIvFluidDialog();
            showConfirmationDialog(position);
        }
    }

    private void showConfirmationDialog(int position) {
        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(requireContext()).content(getString(R.string.sure_want_to_administer_iv_fluid))
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
            case R.id.btnSaveIvFluids:
                saveAndUpdateFinalListOfMedicines();
                break;
            case R.id.btnViewPrescriptionIvFluid:
                showPrescribedMedicinesDialog();
                break;
        }
    }

    private void saveAndUpdateFinalListOfMedicines() {
        List<Medication> ivFluidList = new ArrayList<>();
        for (ItemHeader item : adapter.getList()) {
            if (item instanceof Medication medication) ivFluidList.add((Medication) item);
        }
        ivFluidListChangeListener.onIvFluidListChanged(ivFluidList, deletedMedicines);
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
        if (adapter != null) {
            binding.btnSaveIvFluids.setEnabled(adapter.getItemCount() > 0 && accessMode != PartogramConstants.AccessMode.READ);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }


    private void showPrescribedMedicinesDialog() {
        //animated dialog open
        binding.clPrescribedIvFluidRoot.setVisibility(View.VISIBLE);
        Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
        binding.clPrescribedIvFluidRoot.startAnimation(bottomUp);
        getPrescribedMedicinesDetails();

        binding.includedPrescribedIvFluids.btnHidePrescribedIvDialog.setOnClickListener(view -> {
            closePrescribedIvFluidDialog();
            setIvFluidsListView();
        });
    }

    private void closePrescribedIvFluidDialog() {
        binding.clIvFluidListContainer.setVisibility(View.VISIBLE);
        binding.tvLblAdministerIvFluid.setVisibility(View.VISIBLE);

        Animation bottomDown = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_bottom);
        bottomDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.clPrescribedIvFluidRoot.setVisibility(View.GONE);
                binding.clIvFluidListContainer.setVisibility(View.VISIBLE);
                binding.tvLblAdministerIvFluid.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        binding.clPrescribedIvFluidRoot.startAnimation(bottomDown);
    }

    private void getPrescribedMedicinesDetails() {
        List<ObsDTO> mPrescribedIvFluidsList = new ObsDAO().getAllPrescribedIvFluidsByDoctor(mVisitUUID);
        if (mPrescribedIvFluidsList != null && mPrescribedIvFluidsList.size() > 0) {
            manageUIVisibilityAsPerData(true);

            prescribedIvFluids = new ArrayList<>();
            for (int i = 0; i < mPrescribedIvFluidsList.size(); i++) {
                ObsDTO obsDTO = mPrescribedIvFluidsList.get(i);
                convertToPrescribedIvFluid(obsDTO.getUuid(), obsDTO.getValue(), obsDTO.getCreatedDate(true));
            }
            setPrescribedMedicines(prescribedIvFluids);
            binding.includedPrescribedIvFluids.rvPrescribedIvFluids.setLayoutManager(new LinearLayoutManager(requireContext()));
            prescribedIvFluidAdapter = new PrescribedIvFluidAdapter(requireContext(), prescribedIvFluidList);
            prescribedIvFluidAdapter.setAccessMode(accessMode);
            prescribedIvFluidAdapter.setClickListener(this);
            binding.includedPrescribedIvFluids.rvPrescribedIvFluids.setAdapter(prescribedIvFluidAdapter);
        } else {
            //There is no prescribed medicines
            manageUIVisibilityAsPerData(false);

        }
    }

    public void convertToPrescribedIvFluid(String obsUuid, String value, String createdDate) {
        Gson gson = new Gson();
        Medication ivFluidData = gson.fromJson(value, Medication.class);
        ivFluidData.setObsUuid(obsUuid);
        //ivFluidData.setCreatedAt(DateAndTimeUtils.formatDateTimeNew(createdDate));
        Log.d(TAG, "convertToPrescribedIvFluid: createdDate : " + createdDate);

        ivFluidData.setCreatedAt(createdDate);

        String status = ivFluidData.getInfusionStatus();
        String statusAdminister = "";
        if (status.equalsIgnoreCase("start")) {
            statusAdminister = "Started";
        } else if (status.equalsIgnoreCase("continue")) {
            statusAdminister = "Continued";
        } else if (status.equalsIgnoreCase("stop")) {
            statusAdminister = "Stopped";
        }
        ivFluidData.setInfusionStatus(statusAdminister);
        prescribedIvFluids.add(ivFluidData);
    }

    private String getFormattedDate(String inputDateString) {
        Log.d(TAG, "getFormattedDate: inputDateString : " + inputDateString);
        String formattedDate = "";
        if (inputDateString != null && !inputDateString.isEmpty()) {
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date;
            try {
                date = inputDateFormat.parse(inputDateString);
            } catch (ParseException e) {
                e.printStackTrace();
                return formattedDate;
            }
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault());
            formattedDate = outputDateFormat.format(date);
        }

        return formattedDate;
    }

    private void manageUIVisibilityAsPerData(boolean isMedicinePrescribed) {
        if (isMedicinePrescribed) {
            binding.clPrescribedIvFluidRoot.setVisibility(View.VISIBLE);
            binding.clIvFluidListContainer.setVisibility(View.VISIBLE);
            binding.tvLblAdministerIvFluid.setVisibility(View.VISIBLE);

        } else {
            binding.clPrescribedIvFluidRoot.setVisibility(View.GONE);
            binding.clIvFluidListContainer.setVisibility(View.VISIBLE);
            binding.tvLblAdministerIvFluid.setVisibility(View.VISIBLE);

           /* if (adapter != null) {
                binding.btnSaveIvFluids.setEnabled(adapter.getItemCount() > 0);
            }
            binding.btnSaveIvFluids.setEnabled(ivFluidsList.size() > 0);*/
            changeSaveButtonStatus();

        }

    }

    private void addItemInList(int position) {
        if (prescribedIvFluidAdapter.getItem(position) instanceof Medication medication) {
            if (ivFluidsList != null) {
                //Date currentDate = new Date();
                //SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault());
                //String formattedDate = dateFormat.format(currentDate);
                medication.setCreatedAt(DateTimeUtils.getCurrentDateInUTC(AppConstants.UTC_FORMAT));
                ivFluidsList.add(0, medication);
                adapter.notifyItemInserted(0);
                changeSaveButtonStatus();
            }
        }
    }

    private void manageHeightOfPrescribedIVFluidUI() {
        ConstraintLayout clIvFluidListContainer = binding.clIvFluidListContainer;

// Calculate 70% of the screen height
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenHeight = displayMetrics.heightPixels;
        int maxContainerHeight = (int) (screenHeight * 0.4);

// Set the maximum height for clIvFluidListContainer
        clIvFluidListContainer.setMaxHeight(maxContainerHeight);

// When you want to show the container:
        clIvFluidListContainer.setVisibility(View.VISIBLE);

// When you want to hide the container:
        clIvFluidListContainer.setVisibility(View.GONE);
    }
}

