package org.intelehealth.ezazi.partogram.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.databinding.PlansListBottomSheetDialogBinding;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.adapter.PlansByHealthWorkerAdapter;
import org.intelehealth.ezazi.partogram.model.Medicine;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;
import org.intelehealth.ezazi.ui.prescription.adapter.PrescriptionAdapter;
import org.intelehealth.ezazi.ui.prescription.data.PrescriptionRepository;
import org.intelehealth.ezazi.ui.shared.TextChangeListener;
import org.intelehealth.ezazi.ui.validation.FirstLetterUpperCaseInputFilter;
import org.intelehealth.ezazi.utilities.ScreenUtils;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.klivekit.chat.model.ItemHeader;
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PlanBottomSheetDialog extends BottomSheetDialogFragment implements
        BaseViewHolder.ViewHolderClickListener, View.OnClickListener {
    private static final String TAG = "PlanBottomSheetDialog";
    private static String mVisitUUID;
    private LinkedList<ItemHeader> plansList;

    private PlanListChangeListener planListChangeListener;
    private List<ObsDTO> deletedPlans;
    private PlansListBottomSheetDialogBinding binding;
    private PlansByHealthWorkerAdapter adapter;

    private PartogramConstants.AccessMode accessMode;

    private PrescriptionAdapter prescriptionAdapter;

    List<ItemHeader> plansPrescriptionList;

    public void setAccessMode(PartogramConstants.AccessMode accessMode) {
        this.accessMode = accessMode;
    }

    @Override
    public void onViewHolderViewClicked(@Nullable View view, int position) {
        if (view == null)
            return;
        if (view.getId() == R.id.btnEditPlan) {
            if (adapter.getItem(position) instanceof ObsDTO obsDTO) {
                binding.includeAddNewPlanDialog.setPlan(obsDTO);
                binding.includeAddNewPlanDialog.setUpdatePosition(position);
                binding.clAddNewPlanRoot.setVisibility(View.VISIBLE);
                binding.btnAddMorePlan.setVisibility(View.GONE);
                binding.includeAddNewPlanDialog.btnAddPlanAdd.setText(getString(R.string.update));
            }
        } else if (view.getId() == R.id.btnDeletePlan) {
            showConfirmationDialog(position);
        } else if (view.getId() == R.id.btnExpandCollapseIndicator) {
            adapter.setExpandedItemPosition(position);
        } else if (view.getId() == R.id.btnPrescriptionPlanViewMore) {
            ObsDTO obs = (ObsDTO) view.getTag();
            obs.updateVisibleContentLine();
            adapter.notifyItemChanged(position);
        } else if (view.getId() == R.id.btnFollowPlan) {
            closePrescribedPlanDialog();
            if (prescriptionAdapter.getItem(position) instanceof ObsDTO obsDTO) {
                Log.d(TAG, "onViewHolderViewClicked: ");
                binding.includeAddNewPlanDialog.setPlan(obsDTO);
                //binding.includeAddNewPlanDialog.setUpdatePosition(position);
                binding.clAddNewPlanRoot.setVisibility(View.VISIBLE);
                binding.btnAddMorePlan.setVisibility(View.GONE);
                binding.includeAddNewPlanDialog.btnAddPlanAdd.setText(getString(R.string.lbl_add));
            }
        }
    }

    public interface PlanListChangeListener {
        void onPlanListChanged(List<ObsDTO> updated, List<ObsDTO> deleted);
    }

    public void setListener(PlanListChangeListener listener) {
        this.planListChangeListener = listener;
    }

    public static PlanBottomSheetDialog getInstance(List<ObsDTO> plansList, String visitUuid, PlanListChangeListener listener) {
        PlanBottomSheetDialog dialog = new PlanBottomSheetDialog();
        dialog.setPlans(new ArrayList<>(plansList));
        mVisitUUID = visitUuid;
        dialog.setListener(listener);
        return dialog;
    }


    public PlanBottomSheetDialog() {
        super(R.layout.plans_list_bottom_sheet_dialog);
    }

    public void setPlans(List<ObsDTO> plansList) {
        this.plansList = new LinkedList<>();
        this.plansList.addAll(plansList);
    }

    /*public void setPrescribedMedicines(List<Plan> prescribedMedicinesList) {
        if (prescribedMedicinesList != null && prescribedMedicinesList.size() > 0) {
            //reset list here
            //prescribedMedicinesList.clear();
            this.prescribedMedicinesList = new LinkedList<>();
            this.prescribedMedicinesList.addAll(prescribedMedicinesList);
        }
    }
*/
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        addMargin(view);
        binding = PlansListBottomSheetDialogBinding.bind(view);
        binding.clPlanDialogRoot.setMinHeight(ScreenUtils.getInstance(requireContext()).getHeight());
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) binding.clPlanDialogRoot.getParent());
        behavior.setPeekHeight(ScreenUtils.getInstance(requireContext()).getHeight());
        behavior.addBottomSheetCallback(getCallback(behavior));

        showPrescribedPlansDialog();
        setPlanListView();
        setButtonClickListener();
        setToolbarNavClick();
        buildAddNewPlanDialog();
        setupInputFilter();

        addBottomMarginIfVersion13();
    }


    private void addBottomMarginIfVersion13() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.btnSavePlan.getLayoutParams();
            params.bottomMargin = params.bottomMargin + getResources().getDimensionPixelOffset(R.dimen.screen_padding) + dpToPx(16);
            binding.btnSavePlan.setLayoutParams(params);

            ConstraintLayout.LayoutParams params1 = (ConstraintLayout.LayoutParams) binding.clAddNewPlanRoot.getLayoutParams();
            params1.bottomMargin = params1.bottomMargin + getResources().getDimensionPixelOffset(R.dimen.screen_padding) + dpToPx(16);
            binding.clAddNewPlanRoot.setLayoutParams(params1);

            ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) binding.includedPrescribedPlan.btnHidePrescribedDialog.getLayoutParams();
            params2.bottomMargin = params2.bottomMargin + getResources().getDimensionPixelOffset(R.dimen.screen_padding) + dpToPx(16);
            binding.includedPrescribedPlan.btnHidePrescribedDialog.setLayoutParams(params2);

            ConstraintLayout.LayoutParams params3 = (ConstraintLayout.LayoutParams) binding.clPrescribedPlanRoot.getLayoutParams();
            params3.bottomMargin = params3.bottomMargin + getResources().getDimensionPixelOffset(R.dimen.screen_padding) + dpToPx(16);
            binding.clPrescribedPlanRoot.setLayoutParams(params3);
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
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        int margin_16dp = dpToPx(16);
        layoutParams.width = ScreenUtils.getInstance(requireContext()).getWidth();
        layoutParams.height = ScreenUtils.getInstance(requireContext()).getHeight();
        view.setLayoutParams(layoutParams);
        view.requestLayout();
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return px;
    }

    private void hideBottomSystemTaskbar(Dialog dialog) {
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(dialog.getWindow(), dialog.getWindow().getDecorView());
        // Configure the behavior of the hidden system bars.
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        // Add a listener to update the behavior of the toggle fullscreen button when
        // the system bars are hidden or revealed.
        dialog.getWindow().getDecorView().setOnApplyWindowInsetsListener((view, windowInsets) -> {
            // You can hide the caption bar even when the other system bars are visible.
            // To account for this, explicitly check the visibility of navigationBars()
            // and statusBars() rather than checking the visibility of systemBars().
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars()) || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())) {
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
        setInputFilter(binding.includeAddNewPlanDialog.etNewPlan);

        //binding.includeAddNewMedicineDialog.autoCompleteMedicineForm.setFilters(new InputFilter[]{new ValueInputFilter(formArray)});

    }

    private void setInputFilter(TextInputEditText editText) {
        editText.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter()});
    }

    private void setToolbarNavClick() {
        binding.bottomSheetAppBar.toolbar.setTitle(getString(R.string.lbl_plan_administration));
        binding.bottomSheetAppBar.toolbar.setNavigationOnClickListener(v -> dismiss());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setButtonClickListener() {
        binding.btnAddMorePlan.setOnClickListener(this);
        binding.includeAddNewPlanDialog.btnAddPlanAdd.setOnClickListener(this);
        binding.includeAddNewPlanDialog.btnAddPlanCancel.setOnClickListener(this);
        binding.btnSavePlan.setOnClickListener(this);
        binding.btnSavePlan.setEnabled(accessMode != PartogramConstants.AccessMode.READ);
        binding.btnSavePlan.setEnabled(accessMode != PartogramConstants.AccessMode.READ);
        binding.clAddNewPlanRoot.setClickable(false);
        binding.clAddNewPlanRoot.setOnTouchListener((v, event) -> true);
        binding.btnViewPrescriptionPlan.setOnClickListener(this);
    }

    private void setPlanListView() {
        binding.btnSavePlan.setEnabled(plansList.size() > 0);
        // if (medicines.size() == 0) openNewMedicineDialog();
        binding.rvPlan.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PlansByHealthWorkerAdapter(requireContext(), plansList);
        adapter.setAccessMode(accessMode);
        adapter.setClickListener(this);
        binding.rvPlan.setAdapter(adapter);
    }

    private void showConfirmationDialog(int position) {
        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(requireContext()).content(getString(R.string.alert_delete_medicine)).positiveButtonLabel(R.string.button_delete).negativeButtonLabel(R.string.no).build();

        dialog.setListener(() -> {
            addToDeletedMedicinesList((ObsDTO) adapter.getItem(position));
            adapter.remove(position);
        });
        dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void addToDeletedMedicinesList(ObsDTO item) {
        if (deletedPlans == null) deletedPlans = new ArrayList<>();
        if (item.getUuid() != null) deletedPlans.add(item);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddMorePlan:
                setupPlan();
                openNewPlanDialog();
                binding.btnAddMorePlan.setVisibility(View.GONE);
                binding.includeAddNewPlanDialog.btnAddPlanAdd.setText(getString(R.string.lbl_add));
                break;
            case R.id.btnAddPlanAdd:
                binding.btnAddMorePlan.setVisibility(View.VISIBLE);
                closeNewPlanDialog();
                ObsDTO plan = validatePlanFormInput();
                if (plan.isValidPlan()) {
                    int updated = -1;
                    if (binding.includeAddNewPlanDialog.getUpdatePosition() != null) {
                        updated = binding.includeAddNewPlanDialog.getUpdatePosition();
                        if (binding.includeAddNewPlanDialog.getPlan() != null)
                            plan.setUuid(binding.includeAddNewPlanDialog.getPlan().getUuid());
                    }
                    if (updated > -1) adapter.updateItemAt(updated, plan);
                    else adapter.addItem(plan);
                    clearAddNewMedicineForm();
                    changeSaveButtonStatus();
                }
                break;
            case R.id.btnAddPlanCancel:
                binding.btnAddMorePlan.setVisibility(View.VISIBLE);
                closeNewPlanDialog();
                break;
            case R.id.btnSavePlan:
                saveAndUpdateFinalListOfMedicines();
                break;
            case R.id.btnViewPrescriptionMedicine:
                showPrescribedPlansDialog();
                break;
        }
    }

    private void saveAndUpdateFinalListOfMedicines() {
        List<ObsDTO> plansList = new ArrayList<>();
        for (ItemHeader item : adapter.getList()) {
            if (item instanceof ObsDTO) plansList.add((ObsDTO) item);
        }
        planListChangeListener.onPlanListChanged(plansList, deletedPlans);
        dismiss();
    }

    private void clearAddNewMedicineForm() {
        binding.includeAddNewPlanDialog.setUpdatePosition(-1);
        binding.includeAddNewPlanDialog.etNewPlan.setText("");

        validatePlanFormInput();
    }

    private ObsDTO validatePlanFormInput() {
        ObsDTO plan = new ObsDTO();
        plan.setValue(binding.includeAddNewPlanDialog.etNewPlan.getText().toString());

        binding.includeAddNewPlanDialog.btnAddPlanAdd.setEnabled(plan.isValidPlan());
        return plan;
    }

    private void buildAddNewPlanDialog() {
        setupPlan();
        binding.includeAddNewPlanDialog.etNewPlan.addTextChangedListener(listener);
    }

    private final TextChangeListener listener = new TextChangeListener() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            validatePlanFormInput();
        }
    };

    private void setupPlan() {
        binding.clPlanListContainer.setVisibility(View.VISIBLE);
        binding.tvLblAdministerPlan.setVisibility(View.VISIBLE);
    }

    private ArrayList<String> filterMedicines(String[] medicineItem) {
        ArrayList<String> filteredItems = new ArrayList<>();
        if (adapter != null && adapter.getList().size() > 0) {
            for (String item : medicineItem) {
                for (ItemHeader medItem : adapter.getList()) {
                    if (medItem instanceof ObsDTO obsDTO && !obsDTO.getValue().equals(item)) {
                        filteredItems.add(item);
                    }
                }
            }
        }
        return filteredItems;
    }

    private void changeSaveButtonStatus() {
        if (adapter != null) binding.btnSavePlan.setEnabled(adapter.getItemCount() > 0);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    private void openNewPlanDialog() {
        binding.clAddNewPlanRoot.setVisibility(View.VISIBLE);
        Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
        binding.clAddNewPlanRoot.startAnimation(bottomUp);
    }

    private void closeNewPlanDialog() {
        Animation bottomDown = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_bottom);
        bottomDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.clAddNewPlanRoot.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        binding.clAddNewPlanRoot.startAnimation(bottomDown);
    }

    private void showPrescribedPlansDialog() {
        //animated dialog open
        binding.clPrescribedPlanRoot.setVisibility(View.VISIBLE);
        Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
        binding.clPrescribedPlanRoot.startAnimation(bottomUp);
        //getPrescribedPlansDetails();
        loadPrescriptions();

        binding.includedPrescribedPlan.btnHidePrescribedDialog.setOnClickListener(view -> {
            closePrescribedPlanDialog();
            setPlanListView();
        });
    }

    private void closePrescribedPlanDialog() {

        Animation bottomDown = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_bottom);
        bottomDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.clPrescribedPlanRoot.setVisibility(View.GONE);
                binding.clPlanListContainer.setVisibility(View.VISIBLE);
                binding.tvLblAdministerPlan.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        binding.clPrescribedPlanRoot.startAnimation(bottomDown);
    }

    private void manageUIVisibilityAsPerData(boolean isPlanPrescribed) {
        if (isPlanPrescribed) {
            binding.clPrescribedPlanRoot.setVisibility(View.VISIBLE);
            binding.clPlanListContainer.setVisibility(View.GONE);
            binding.tvLblAdministerPlan.setVisibility(View.GONE);

        } else {
            binding.clPrescribedPlanRoot.setVisibility(View.GONE);
            binding.clPlanListContainer.setVisibility(View.VISIBLE);
            binding.tvLblAdministerPlan.setVisibility(View.VISIBLE);

            if (adapter != null) binding.btnSavePlan.setEnabled(adapter.getItemCount() > 0);
        }

    }

    private void loadPrescriptions() {
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String creatorId = sessionManager.getCreatorID();

        plansPrescriptionList = new PrescriptionRepository(AppConstants.inteleHealthDatabaseHelper.getReadableDatabase()).fetchPlansPrescription(mVisitUUID, creatorId);
        LinkedList<ItemHeader> linkedListPlansPrescription = new LinkedList<>(plansPrescriptionList);

        if (plansPrescriptionList != null && !plansPrescriptionList.isEmpty()) {
            binding.includedPrescribedPlan.rvPrescribedPlans.setLayoutManager(new LinearLayoutManager(requireContext()));
            prescriptionAdapter = new PrescriptionAdapter(requireContext(), linkedListPlansPrescription);
            prescriptionAdapter.setAccessMode(accessMode);
            //prescriptionAdapter.updateItems(new ArrayList<>(plansPrescriptionList));
            ///adapter.setClickListener(PrescriptionActivity.this);
            //prescriptionAdapter.manageFollowPlanButtonVisibility(true);
            prescriptionAdapter.setClickListener(this);
            prescriptionAdapter.manageFollowUpButtonVisibility(true);
            binding.includedPrescribedPlan.rvPrescribedPlans.setAdapter(prescriptionAdapter);

        } else {
            //viewMode.updateFailResult(getString(R.string.no_prescription));
        }

    }

}

