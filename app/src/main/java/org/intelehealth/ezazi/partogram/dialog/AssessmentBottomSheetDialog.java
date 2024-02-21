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
import android.widget.Toast;

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
import com.google.gson.Gson;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.database.dao.ProviderDAO;
import org.intelehealth.ezazi.databinding.AssessmentListBottomSheetDialogBinding;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.adapter.PlansByHealthWorkerAdapter;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;
import org.intelehealth.ezazi.ui.prescription.adapter.PrescriptionAdapter;
import org.intelehealth.ezazi.ui.prescription.data.PrescriptionRepository;
import org.intelehealth.ezazi.ui.shared.TextChangeListener;
import org.intelehealth.ezazi.ui.validation.FirstLetterUpperCaseInputFilter;
import org.intelehealth.ezazi.utilities.ScreenUtils;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.intelehealth.klivekit.chat.model.ItemHeader;
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder;
import org.intelehealth.klivekit.utils.DateTimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class AssessmentBottomSheetDialog extends BottomSheetDialogFragment implements
        BaseViewHolder.ViewHolderClickListener, View.OnClickListener {
    private static final String TAG = "PlanBottomSheetDialog";
    private static String mVisitUUID;
    private LinkedList<ItemHeader> assessmentList;

    private AssessmentListChangeListener assessmentListChangeListener;
    private List<ObsDTO> deletedAssessments;
    private AssessmentListBottomSheetDialogBinding binding;
    private PlansByHealthWorkerAdapter adapter;

    private PartogramConstants.AccessMode accessMode;

    private PrescriptionAdapter prescriptionAdapter;

    private List<ItemHeader> assessmentsPrescriptionList;

    public void setAccessMode(PartogramConstants.AccessMode accessMode) {
        this.accessMode = accessMode;
    }

    @Override
    public void onViewHolderViewClicked(@Nullable View view, int position) {
        if (view == null)
            return;
        if (view.getId() == R.id.btnEditPlan) {
            if (adapter.getItem(position) instanceof ObsDTO obsDTO) {
                binding.includeAddNewAssessmentDialog.setAssessment(obsDTO);
                binding.includeAddNewAssessmentDialog.setUpdatePosition(position);
                binding.clAddNewAssessmentRoot.setVisibility(View.VISIBLE);
                binding.btnAddMoreAssessment.setVisibility(View.GONE);
                binding.includeAddNewAssessmentDialog.btnAddAssessmentAdd.setText(getString(R.string.update));
            }
        } else if (view.getId() == R.id.btnDeletePlan) {
            showConfirmationDialog(position);
        } else if (view.getId() == R.id.btnExpandCollapseIndicator) {
            adapter.setExpandedItemPosition(position);
        } else if (view.getId() == R.id.btnPrescriptionPlanViewMore) {
            ObsDTO obs = (ObsDTO) view.getTag();
            obs.updateVisibleContentLine();
            adapter.notifyItemChanged(position);
        }
    }

    public interface AssessmentListChangeListener {
        void onAssessmentListChanged(List<ObsDTO> updated, List<ObsDTO> deleted);
    }

    public void setListener(AssessmentListChangeListener listener) {
        this.assessmentListChangeListener = listener;
    }

    public static AssessmentBottomSheetDialog getInstance(List<ObsDTO> assessmentList, String visitUuid, AssessmentListChangeListener listener) {
        AssessmentBottomSheetDialog dialog = new AssessmentBottomSheetDialog();
        dialog.setAssessments(new ArrayList<>(assessmentList));
        mVisitUUID = visitUuid;
        dialog.setListener(listener);
        return dialog;
    }


    public AssessmentBottomSheetDialog() {
        super(R.layout.assessment_list_bottom_sheet_dialog);
    }

    public void setAssessments(List<ObsDTO> assessmentList) {
        this.assessmentList = new LinkedList<>();
        this.assessmentList.addAll(assessmentList);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        addMargin(view);
        binding = AssessmentListBottomSheetDialogBinding.bind(view);
        binding.clAssessmentDialogRoot.setMinHeight(ScreenUtils.getInstance(requireContext()).getHeight());
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) binding.clAssessmentDialogRoot.getParent());
        behavior.setPeekHeight(ScreenUtils.getInstance(requireContext()).getHeight());
        behavior.addBottomSheetCallback(getCallback(behavior));

        showPrescribedAssessmentsDialog();
        setAssessmentListView();
        setButtonClickListener();
        setToolbarNavClick();
        buildAddNewAssessmentDialog();
        setupInputFilter();

        addBottomMarginIfVersion13();
    }


    private void addBottomMarginIfVersion13() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.btnSaveAssessment.getLayoutParams();
            params.bottomMargin = params.bottomMargin + getResources().getDimensionPixelOffset(R.dimen.screen_padding) + dpToPx(16);
            binding.btnSaveAssessment.setLayoutParams(params);

            ConstraintLayout.LayoutParams params1 = (ConstraintLayout.LayoutParams) binding.clAddNewAssessmentRoot.getLayoutParams();
            params1.bottomMargin = params1.bottomMargin + getResources().getDimensionPixelOffset(R.dimen.screen_padding) + dpToPx(16);
            binding.clAddNewAssessmentRoot.setLayoutParams(params1);

            ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) binding.includedPrescribedAssessment.btnHidePrescribedDialog.getLayoutParams();
            params2.bottomMargin = params2.bottomMargin + getResources().getDimensionPixelOffset(R.dimen.screen_padding) + dpToPx(16);
            binding.includedPrescribedAssessment.btnHidePrescribedDialog.setLayoutParams(params2);

            ConstraintLayout.LayoutParams params3 = (ConstraintLayout.LayoutParams) binding.clPrescribedAssessmentRoot.getLayoutParams();
            params3.bottomMargin = params3.bottomMargin + getResources().getDimensionPixelOffset(R.dimen.screen_padding) + dpToPx(16);
            binding.clPrescribedAssessmentRoot.setLayoutParams(params3);
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
        setInputFilter(binding.includeAddNewAssessmentDialog.etNewAssessment);

        //binding.includeAddNewMedicineDialog.autoCompleteMedicineForm.setFilters(new InputFilter[]{new ValueInputFilter(formArray)});

    }

    private void setInputFilter(TextInputEditText editText) {
        editText.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter()});
    }

    private void setToolbarNavClick() {
        binding.bottomSheetAppBar.toolbar.setTitle(getString(R.string.lbl_assessment_administration));
        binding.bottomSheetAppBar.toolbar.setNavigationOnClickListener(v -> dismiss());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setButtonClickListener() {
        binding.btnAddMoreAssessment.setOnClickListener(this);
        binding.includeAddNewAssessmentDialog.btnAddAssessmentAdd.setOnClickListener(this);
        binding.includeAddNewAssessmentDialog.btnAddAssessmentCancel.setOnClickListener(this);
        binding.btnSaveAssessment.setOnClickListener(this);
        binding.btnSaveAssessment.setEnabled(accessMode != PartogramConstants.AccessMode.READ);
        binding.btnSaveAssessment.setEnabled(accessMode != PartogramConstants.AccessMode.READ);
        binding.clAddNewAssessmentRoot.setClickable(false);
        binding.clAddNewAssessmentRoot.setOnTouchListener((v, event) -> true);
        binding.btnViewPrescriptionAssessment.setOnClickListener(this);
    }

    private void setPlanListView() {
        binding.btnSaveAssessment.setEnabled(assessmentList.size() > 0);
        // if (medicines.size() == 0) openNewMedicineDialog();
        binding.rvAssessment.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PlansByHealthWorkerAdapter(requireContext(), assessmentList);
        adapter.setAccessMode(accessMode);
        adapter.setClickListener(this);
        binding.rvAssessment.setAdapter(adapter);
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
        if (deletedAssessments == null) deletedAssessments = new ArrayList<>();
        if (item.getUuid() != null) deletedAssessments.add(item);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddMoreAssessment:
                closePrescribedPlanDialog();
                setupAssessment();
                openNewAssessmentDialog();
                binding.btnAddMoreAssessment.setVisibility(View.GONE);
                binding.includeAddNewAssessmentDialog.btnAddAssessmentAdd.setText(getString(R.string.lbl_add));
                break;
            case R.id.btnAddAssessmentAdd:
                binding.btnAddMoreAssessment.setVisibility(View.VISIBLE);
                closeNewAssessmentDialog();
                ObsDTO assessment = validateAssessmentFormInput();
                try {
                    assessment.setName(new ProviderDAO().getCreatorGivenName(new SessionManager(IntelehealthApplication.getAppContext()).getProviderID()));
                    assessment.dateWithDrName();
                    //Date currentDate = new Date();
                    //SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault());
                    //String formattedDate = dateFormat.format(currentDate);
                    assessment.setCreatedDate(DateTimeUtils.getCurrentDateInUTC(AppConstants.UTC_FORMAT));
                } catch (DAOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                Log.d(TAG, "onClick: plan kk : " + new Gson().toJson(assessment));
                Log.d(TAG, "onClick: position : " + binding.includeAddNewAssessmentDialog.getUpdatePosition());
                if (assessment.isValidPlan()) {
                    int updated = -1;
                    if (binding.includeAddNewAssessmentDialog.getUpdatePosition() != null) {
                        updated = binding.includeAddNewAssessmentDialog.getUpdatePosition();
                        if (binding.includeAddNewAssessmentDialog.getAssessment() != null)
                            assessment.setUuid(binding.includeAddNewAssessmentDialog.getAssessment().getUuid());
                    }
                    if (updated > -1) adapter.updateItemAt(updated, assessment);
                    else {
                        //adapter.addItem(plan);
                        adapter.addItemAt(0, assessment);
                    }
                    clearAddNewMedicineForm();
                    changeSaveButtonStatus();
                }
                break;
            case R.id.btnAddAssessmentCancel:
                binding.btnAddMoreAssessment.setVisibility(View.VISIBLE);
                closeNewAssessmentDialog();
                break;
            case R.id.btnSaveAssessment:
                saveAndUpdateFinalListOfMedicines();
                break;
            case R.id.btnViewPrescriptionAssessment:
                showPrescribedAssessmentsDialog();
                break;
        }
    }

    private void saveAndUpdateFinalListOfMedicines() {
        List<ObsDTO> assessmentsList = new ArrayList<>();
        for (ItemHeader item : adapter.getList()) {
            if (item instanceof ObsDTO) assessmentsList.add((ObsDTO) item);
        }
        assessmentListChangeListener.onAssessmentListChanged(assessmentsList, deletedAssessments);
        dismiss();
    }

    private void clearAddNewMedicineForm() {
        binding.includeAddNewAssessmentDialog.setUpdatePosition(-1);
        binding.includeAddNewAssessmentDialog.etNewAssessment.setText("");

        validateAssessmentFormInput();
    }

    private ObsDTO validateAssessmentFormInput() {
        ObsDTO assessment = new ObsDTO();
        assessment.setValue(binding.includeAddNewAssessmentDialog.etNewAssessment.getText().toString());

        binding.includeAddNewAssessmentDialog.btnAddAssessmentAdd.setEnabled(assessment.isValidPlan());
        return assessment;
    }

    private void buildAddNewAssessmentDialog() {
        setupAssessment();
        binding.includeAddNewAssessmentDialog.etNewAssessment.addTextChangedListener(listener);
    }

    private final TextChangeListener listener = new TextChangeListener() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            validateAssessmentFormInput();
        }
    };

    private void setupAssessment() {
        binding.clAssessmentListContainer.setVisibility(View.VISIBLE);
        binding.tvLblAdministerAssessment.setVisibility(View.VISIBLE);
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
        if (adapter != null) binding.btnSaveAssessment.setEnabled(adapter.getItemCount() > 0);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    private void openNewAssessmentDialog() {
        binding.clAddNewAssessmentRoot.setVisibility(View.VISIBLE);
        Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
        binding.clAddNewAssessmentRoot.startAnimation(bottomUp);
    }

    private void closeNewAssessmentDialog() {
        Animation bottomDown = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_bottom);
        bottomDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.clAddNewAssessmentRoot.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        binding.clAddNewAssessmentRoot.startAnimation(bottomDown);
    }

    private void showPrescribedAssessmentsDialog() {
        //animated dialog open
        binding.clPrescribedAssessmentRoot.setVisibility(View.VISIBLE);
        Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
        binding.clPrescribedAssessmentRoot.startAnimation(bottomUp);
        //getPrescribedPlansDetails();
        loadPrescriptions();

        binding.includedPrescribedAssessment.btnHidePrescribedDialog.setOnClickListener(view -> {
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
                binding.clPrescribedAssessmentRoot.setVisibility(View.GONE);
                binding.clAssessmentListContainer.setVisibility(View.VISIBLE);
                binding.tvLblAdministerAssessment.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        binding.clPrescribedAssessmentRoot.startAnimation(bottomDown);
    }

    private void manageUIVisibilityAsPerData(boolean isAssessmentPrescribed) {
        if (isAssessmentPrescribed) {
            binding.clPrescribedAssessmentRoot.setVisibility(View.VISIBLE);
            binding.clAssessmentListContainer.setVisibility(View.VISIBLE);
            binding.tvLblAdministerAssessment.setVisibility(View.VISIBLE);

        } else {
            binding.clPrescribedAssessmentRoot.setVisibility(View.GONE);
            binding.clAssessmentListContainer.setVisibility(View.VISIBLE);
            binding.tvLblAdministerAssessment.setVisibility(View.VISIBLE);

            if (adapter != null) binding.btnSaveAssessment.setEnabled(adapter.getItemCount() > 0);
        }

    }

    private void loadPrescriptions() {
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String creatorId = sessionManager.getCreatorID();

        assessmentsPrescriptionList = new PrescriptionRepository(AppConstants.inteleHealthDatabaseHelper.getReadableDatabase()).fetchAssessmentPrescription(mVisitUUID, creatorId);
        LinkedList<ItemHeader> linkedListAssessmentsPrescription = new LinkedList<>(assessmentsPrescriptionList);
        Log.d(TAG, "loadPrescriptions: AssessmentsPrescriptionList size: " + assessmentsPrescriptionList);
        if (assessmentsPrescriptionList != null && assessmentsPrescriptionList.size() > 0) {
            manageUIVisibilityAsPerData(true);

            binding.includedPrescribedAssessment.rvPrescribedAssessments.setLayoutManager(new LinearLayoutManager(requireContext()));
            prescriptionAdapter = new PrescriptionAdapter(requireContext(), linkedListAssessmentsPrescription);
            prescriptionAdapter.setAccessMode(accessMode);
            //prescriptionAdapter.updateItems(new ArrayList<>(plansPrescriptionList));
            ///adapter.setClickListener(PrescriptionActivity.this);
            //prescriptionAdapter.manageFollowPlanButtonVisibility(true);
            prescriptionAdapter.setClickListener(this);
            prescriptionAdapter.manageFollowUpButtonVisibility(true);
            binding.includedPrescribedAssessment.rvPrescribedAssessments.setAdapter(prescriptionAdapter);

        } else {
            manageUIVisibilityAsPerData(false);

            //viewMode.updateFailResult(getString(R.string.no_prescription));
        }

    }

    private void setAssessmentListView() {
        binding.btnSaveAssessment.setEnabled(assessmentList.size() > 0);
        // if (medicines.size() == 0) openNewMedicineDialog();
        binding.rvAssessment.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PlansByHealthWorkerAdapter(requireContext(), assessmentList);
        adapter.setAccessMode(accessMode);
        adapter.setClickListener(this);
        binding.rvAssessment.setAdapter(adapter);
    }

}

