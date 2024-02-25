package org.intelehealth.ezazi.partogram.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.databinding.DialogIvfluidOptionsBinding;
import org.intelehealth.ezazi.databinding.PartoLablRadioViewAssessmentBinding;
import org.intelehealth.ezazi.databinding.PartoLablRadioViewMedicineBinding;
import org.intelehealth.ezazi.databinding.PartoLablRadioViewPlanBinding;
import org.intelehealth.ezazi.databinding.PartoLblRadioViewEzaziBinding;
import org.intelehealth.ezazi.databinding.PartoLblRadioViewOxytocinBinding;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.models.uploadSurvey.Ob;
import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.dialog.AssessmentBottomSheetDialog;
import org.intelehealth.ezazi.partogram.dialog.IVFluidBottomSheetDialog;
import org.intelehealth.ezazi.partogram.dialog.OxytocinBottomSheetDialog;
import org.intelehealth.ezazi.partogram.dialog.PlanBottomSheetDialog;
import org.intelehealth.ezazi.partogram.model.Medication;
import org.intelehealth.ezazi.partogram.model.Medicine;
import org.intelehealth.ezazi.partogram.model.ParamInfo;
import org.intelehealth.ezazi.partogram.model.PartogramItemData;
import org.intelehealth.ezazi.ui.dialog.CustomViewDialogFragment;
import org.intelehealth.ezazi.ui.dialog.SingleChoiceDialogFragment;
import org.intelehealth.ezazi.ui.dialog.model.SingChoiceItem;
import org.intelehealth.ezazi.ui.prescription.activity.AdministeredActivity;
import org.intelehealth.ezazi.ui.prescription.listener.MedicineChangeListener;
import org.intelehealth.ezazi.ui.prescription.data.MedicineSingleton;
import org.intelehealth.ezazi.ui.prescription.fragment.PrescriptionFragment;
import org.intelehealth.ezazi.ui.prescription.model.PrescriptionArg;
import org.intelehealth.ezazi.ui.shared.TextChangeListener;
import org.intelehealth.ezazi.utilities.UuidDictionary;
import org.intelehealth.klivekit.chat.model.ItemHeader;
import org.intelehealth.klivekit.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class PartogramQueryListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "PartogramQueryListingAd";
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<PartogramItemData> mItemList = new ArrayList<PartogramItemData>();
    private TextView selectedTextview;
    boolean isToastShownTemperature, isToastShownPulse, isToastShownContraction, isToastShownFHR, isToastShownSysBP, isToastShownDysBP;
    private String mVisitUuid;

    //    private static JSONObject ivFluidsJsonObject = new JSONObject();
//    private static JSONObject oxytocinDataObject = new JSONObject();
//
//        , ivInfusionRate, ivInfusionStatus;
//    private TextView strengthOxytocin, ivInfusionRateOxytocin, ivInfusionStatusOxytocin;

    public interface OnItemSelection {
        public void onSelect(PartogramItemData partogramItemData);
    }

    private interface OnRadioCheckedListener {
        void onCheckedYes();

        void onCheckedNo();
    }

    private OnItemSelection mOnItemSelection;
    private int currentChildFocusedIndex = -1;
    private PartogramConstants.AccessMode accessMode;

    public PartogramQueryListingAdapter(RecyclerView recyclerView, String visitUuid, Context context, List<PartogramItemData> itemList, OnItemSelection onItemSelection) {
        mContext = context;
        mVisitUuid = visitUuid;
        mItemList = itemList;
        mOnItemSelection = onItemSelection;
    }

    public void setAccessMode(PartogramConstants.AccessMode accessMode) {
        Log.d(TAG, "setAccessMode: accessMode :: kk :: " + accessMode);
        this.accessMode = accessMode;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.parto_list_item_view_ezazi, parent, false);
        return new GenericViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (holder instanceof GenericViewHolder) {
            GenericViewHolder genericViewHolder = (GenericViewHolder) holder;
            genericViewHolder.partogramItemData = mItemList.get(position);
            genericViewHolder.selectedIndex = position;
            genericViewHolder.tvSectionNameTextView.setText(genericViewHolder.partogramItemData.getParamSectionName());
            genericViewHolder.containerLinearLayout.removeAllViews();
            for (int i = 0; i < genericViewHolder.partogramItemData.getParamInfoList().size(); i++) {
                ParamInfo paramInfo = genericViewHolder.partogramItemData.getParamInfoList().get(i);
                if (paramInfo.getParamDateType().equalsIgnoreCase(PartogramConstants.INPUT_TXT_TYPE) || paramInfo.getParamDateType().equalsIgnoreCase(PartogramConstants.INPUT_DOUBLE_4_DIG_TYPE) || paramInfo.getParamDateType().equalsIgnoreCase(PartogramConstants.INPUT_INT_1_DIG_TYPE) || paramInfo.getParamDateType().equalsIgnoreCase(PartogramConstants.INPUT_INT_2_DIG_TYPE) || paramInfo.getParamDateType().equalsIgnoreCase(PartogramConstants.INPUT_INT_3_DIG_TYPE)) {
                    View tempView = View.inflate(mContext, R.layout.parto_lbl_etv_view_ezazi, null);
                    tempView.setTag(genericViewHolder.containerLinearLayout);
                    showUserInputBox(tempView, position, i, paramInfo.getParamDateType());
                    genericViewHolder.containerLinearLayout.addView(tempView);
                } else if (paramInfo.getParamDateType().equalsIgnoreCase(PartogramConstants.DROPDOWN_SINGLE_SELECT_TYPE)) {
                    View tempView = View.inflate(mContext, R.layout.parto_lbl_dropdown_view_ezazi, null);
                    showListOptions(tempView, position, i);
                    genericViewHolder.containerLinearLayout.addView(tempView);
                } else if (paramInfo.getParamDateType().equalsIgnoreCase(PartogramConstants.AUTOCOMPLETE_SUGGESTION_EDITTEXT)) {
                    View tempView = View.inflate(mContext, R.layout.parto_lbl_autocomplete_edittext_ezazi, null);
                    showAutoComplete_EditText(tempView, position, i, paramInfo.getParamDateType());
                    genericViewHolder.containerLinearLayout.addView(tempView);
                } else if (paramInfo.getParamDateType().equalsIgnoreCase(PartogramConstants.RADIO_SELECT_TYPE)) {
                    View tempView = null;
                    if (!paramInfo.getConceptUUID().isEmpty() && paramInfo.getConceptUUID().equals(UuidDictionary.IV_FLUIDS)) {
                        tempView = View.inflate(mContext, R.layout.parto_lbl_radio_view_ezazi, null);
                    } else if (!paramInfo.getConceptUUID().isEmpty() && paramInfo.getConceptUUID().equals(UuidDictionary.OXYTOCIN_UL_DROPS_MIN)) {
                        tempView = View.inflate(mContext, R.layout.parto_lbl_radio_view_oxytocin, null);
                    } else if (!paramInfo.getConceptUUID().isEmpty() && paramInfo.getConceptUUID().equals(UuidDictionary.MEDICINE)) {
                        tempView = View.inflate(mContext, R.layout.parto_labl_radio_view_medicine, null);
                    } else if (!paramInfo.getConceptUUID().isEmpty() && paramInfo.getConceptUUID().equals(UuidDictionary.PLAN)) {
                        tempView = View.inflate(mContext, R.layout.parto_labl_radio_view_plan, null);
                    } else if (!paramInfo.getConceptUUID().isEmpty() && paramInfo.getConceptUUID().equals(UuidDictionary.ASSESSMENT)) {
                        tempView = View.inflate(mContext, R.layout.parto_labl_radio_view_assessment, null);
                    }
                    if (tempView != null) {
                        showRadioOptionBox(tempView, position, i);
                        genericViewHolder.containerLinearLayout.addView(tempView);
                    }

                } /*else if (paramInfo.getParamDateType().equalsIgnoreCase(PartogramConstants.RADIO_SELECT_TYPE_OXYTOCIN)) {
                    View tempView = View.inflate(mContext, R.layout.parto_lbl_radio_view_oxytocin, null);
                    showRadioOptionBoxForOxytocin(tempView, position, i, paramInfo.getParamDateType());
                    genericViewHolder.containerLinearLayout.addView(tempView);
                }*/
            }

        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView tvSectionNameTextView;
        LinearLayout containerLinearLayout;
        PartogramItemData partogramItemData;
        int selectedIndex;

        GenericViewHolder(View itemView) {
            super(itemView);
            tvSectionNameTextView = itemView.findViewById(R.id.tvSectionName);
            containerLinearLayout = itemView.findViewById(R.id.llContainer);
        }

    }

    private void showAutoComplete_EditText(final View tempView, final int position, final int positionChild, final String paramDateType) {
        TextView paramNameTextView = tempView.findViewById(R.id.tvParamName);
        AutoCompleteTextView dataEditText = tempView.findViewById(R.id.etvData);
        dataEditText.setEnabled(accessMode != PartogramConstants.AccessMode.READ);
        dataEditText.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        dataEditText.setAdapter(new ArrayAdapter(mContext, R.layout.spinner_textview, mContext.getResources().getStringArray(R.array.medications)));
        dataEditText.setThreshold(1);

        paramNameTextView.setText(mItemList.get(position).getParamInfoList().get(positionChild).getParamName());

        if (mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue() != null && !mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue().isEmpty()) {
            dataEditText.setText(String.valueOf(mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue()));
        }

        if (paramDateType.equalsIgnoreCase(PartogramConstants.AUTOCOMPLETE_SUGGESTION_EDITTEXT))
            dataEditText.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE); // input type to AutoComplete

        dataEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mItemList.get(position).getParamInfoList().get(positionChild).setCapturedValue(s.toString().trim());
            }
        });
    }


    private void showUserInputBox(final View tempView, final int position, final int positionChild, final String paramDateType) {
        TextView paramNameTextView = tempView.findViewById(R.id.tvParamName);
        EditText dataEditText = tempView.findViewById(R.id.etvData);
        dataEditText.setEnabled(accessMode != PartogramConstants.AccessMode.READ);
        paramNameTextView.setText(mItemList.get(position).getParamInfoList().get(positionChild).getParamName());
        if (mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue() != null && !mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue().isEmpty()) {
            dataEditText.setText(String.valueOf(mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue()));
        } else {
            dataEditText.setText("");
        }

        dataEditText.setTag(R.id.etvData, mItemList.get(position).getParamInfoList().get(positionChild));

      /*  dataEditText.setOnFocusChangeListener((v, hasFocus) -> {
            ParamInfo info = (ParamInfo) v.getTag(R.id.etvData);
            if (!hasFocus && info.getParamName().equalsIgnoreCase(PartogramConstants.Params.TEMPERATURE.value)) {
                EditText editText = (EditText) v;
                if (editText.getText().length() < 2) {
                    editText.setText("");
                    Toast.makeText(mContext, mContext.getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_CELSIUS, AppConstants.MAXIMUM_TEMPERATURE_CELSIUS), Toast.LENGTH_LONG).show();
                }
            }
        });*/

        if (positionChild == currentChildFocusedIndex) {
            dataEditText.requestFocus();
            String lastAdded = mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue();
            if (lastAdded != null && lastAdded.length() > 0)
                dataEditText.setSelection(lastAdded.length());
        }

        if (paramDateType.equalsIgnoreCase(PartogramConstants.INPUT_TXT_TYPE)) {
            dataEditText.setInputType(InputType.TYPE_CLASS_TEXT);
            String conceptId = mItemList.get(position).getParamInfoList().get(positionChild).getConceptUUID();
            // Supervisor Doctor concept id matching to apply only for it
            if (conceptId.equals("7a9cb7bc-9ab9-4ff0-ae82-7a1bd2cca93e")) {
                dataEditText.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
                    if (source.toString().matches("[a-zA-Z 0-9]+")) {
                        return source;
                    } else return "";
                }});
            } else if (conceptId.equals("9d316d82-538f-11e6-9cfe-86f436325720")) {
                dataEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
            }
        } else {

            if (paramDateType.equalsIgnoreCase(PartogramConstants.INPUT_DOUBLE_4_DIG_TYPE)) {
                //added tempareture 3 digits
                dataEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
            } else if (paramDateType.equalsIgnoreCase(PartogramConstants.INPUT_INT_1_DIG_TYPE)) {
                dataEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                dataEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else if (paramDateType.equalsIgnoreCase(PartogramConstants.INPUT_INT_2_DIG_TYPE)) {
                dataEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
                dataEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else {
                dataEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
                dataEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        }

        showDataRangeMessage(dataEditText, position, positionChild);
    }

    private void showDataRangeMessage(EditText dataEditText, int position, int positionChild) {
        isToastShownContraction = false;
        isToastShownTemperature = false;
        isToastShownFHR = false;
        isToastShownPulse = false;
        isToastShownSysBP = false;
        isToastShownDysBP = false;

        dataEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ParamInfo info = (ParamInfo) dataEditText.getTag(R.id.etvData);
                if (!s.toString().isEmpty()) {
                    //int enteredValue = Integer.parseInt(s.toString());
                    if (info.getParamName().equalsIgnoreCase(PartogramConstants.Params.TEMPERATURE.value)) {
                        validatedTemperature(s.toString(), mItemList.get(position).getParamInfoList().get(positionChild), dataEditText);
                    } else if (info.getParamName().equalsIgnoreCase(PartogramConstants.Params.BASELINE_FHR.value)) {
                        validateBaseLineFHR(dataEditText);
                    } else if (info.getParamName().equalsIgnoreCase(PartogramConstants.Params.PULSE.value)) {
                        validatePulse(dataEditText);
                    } else if (info.getParamName().equalsIgnoreCase(PartogramConstants.Params.DURATION_OF_CONTRACTION.value)) {
                        validateDurationOfContraction(dataEditText);
                    } else if (info.getParamName().equalsIgnoreCase(PartogramConstants.Params.SYSTOLIC_BP.value)) {
                        validateSysBp(dataEditText);
                    } else if (info.getParamName().equalsIgnoreCase(PartogramConstants.Params.DIASTOLIC_BP.value)) {
                        validateDysBp(dataEditText);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (dataEditText.getText().toString().endsWith(".")) {
                    dataEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
                } else {
                }
                mItemList.get(position).getParamInfoList().get(positionChild).setCapturedValue(s.toString().trim());
                if (s.length() > 0) {
                    clearDiastolic(s.toString(), positionChild, mItemList.get(position).getParamInfoList(), position);
                    validDiastolicBP(s.toString(), positionChild, mItemList.get(position).getParamInfoList(), dataEditText);
                }
            }
        });

    }

    private void validatedTemperature(String value, ParamInfo info, EditText dataEditText) {
        //if (!value.trim().isEmpty() && value.length() == 2 && !value.startsWith(".") && info.getParamName().equalsIgnoreCase(PartogramConstants.Params.TEMPERATURE.value)) {

        //if (Double.parseDouble(value) > Double.parseDouble(AppConstants.MAXIMUM_TEMPERATURE_CELSIUS) || Double.parseDouble(value) < Double.parseDouble(AppConstants.MINIMUM_TEMPERATURE_CELSIUS)) {
        //dataEditText.setError(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_CELSIUS, AppConstants.MAXIMUM_TEMPERATURE_CELSIUS));
        if (!isToastShownTemperature) {
            Toast.makeText(mContext, mContext.getString(R.string.temp_error_new, AppConstants.MINIMUM_TEMPERATURE_CELSIUS, AppConstants.MAXIMUM_TEMPERATURE_CELSIUS), Toast.LENGTH_LONG).show();
            //dataEditText.setText("");
            dataEditText.requestFocus();
            isToastShownTemperature = true;
        }
        //}
        // }
    }

    private void showRadioOptionBox(final View tempView, final int position, final int positionChild) {
        ParamInfo info = mItemList.get(position).getParamInfoList().get(positionChild);
        TextView paramNameTextView = tempView.findViewById(R.id.tvParamName);
        String title = info.getParamName();
        paramNameTextView.setText(title);
        TextView selected = tempView.findViewById(R.id.tvSelectedValue);
        selected.setTag(info.getCapturedValue());
        selected.setText(info.getCapturedValue());

        switch (info.getConceptUUID()) {
            case UuidDictionary.IV_FLUIDS:
                showRadioOptionBoxForIVFluid(tempView, info, selected, title);
                break;
            case UuidDictionary.OXYTOCIN_UL_DROPS_MIN:
                showRadioOptionBoxForOxytocin(tempView, info, selected, title);
                break;
            case UuidDictionary.MEDICINE:
                showRadioOptionBoxForMedicine(tempView, info, selected, title);
                break;
            case UuidDictionary.PLAN:
                showRadioOptionBoxForPlan(tempView, info, selected, title);
                break;
            case UuidDictionary.ASSESSMENT:
                showRadioOptionBoxForAssessment(tempView, info, selected, title);
                break;
        }
    }

    private void showRadioOptionBoxForMedicine(View tempView, ParamInfo info, TextView selected, String title) {
        PartoLablRadioViewMedicineBinding binding = PartoLablRadioViewMedicineBinding.bind(tempView);

        binding.clMedicineCountView.setOnClickListener(v -> {
            PrescriptionArg arg = getMedicationArg(PrescriptionFragment.PrescriptionType.MEDICINE);
            AdministeredActivity.startAdministeredActivity(mContext, arg);
            MedicineSingleton.INSTANCE.setMedicineListener(new MedicineChangeListener() {
                @Override
                public void onMedicineListChanged(@NonNull List<? extends ItemHeader> updated) {
                    List<Medicine> medicineList = new ArrayList<>();
                    for (ItemHeader item : updated) {
                        if (item instanceof Medicine) medicineList.add((Medicine) item);
                    }
                    info.setMedicines(medicineList);
                    setupMedicineCountView(binding.tvMedicineCount, medicineList);
                }

                @NonNull
                @Override
                public LinkedList<ItemHeader> getExistingList() {
                    return new LinkedList<>(info.getMedicines());
                }
            });
        });
        handleRadioCheckListener(tempView, info, new OnRadioCheckedListener() {
            @Override
            public void onCheckedYes() {
                binding.clMedicineCountView.setVisibility(View.VISIBLE);
                setupMedicineCountView(binding.tvMedicineCount, info.getMedicines());
            }

            @Override
            public void onCheckedNo() {
                binding.clMedicineCountView.setVisibility(View.GONE);
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void setupMedicineCountView(TextView textView, List<Medicine> updated) {
        if (updated.size() == 0) textView.setText(mContext.getString(R.string.lbl_add));
        else textView.setText("" + updated.size());
    }

    private PrescriptionArg getMedicationArg(PrescriptionFragment.PrescriptionType type) {
        return new PrescriptionArg(
                mVisitUuid,
                type,
                true,
                PartogramConstants.AccessMode.WRITE);
    }

    private void showRadioOptionBoxForIVFluid(View tempView, ParamInfo info, TextView selected, String title) {
        PartoLblRadioViewEzaziBinding binding = PartoLblRadioViewEzaziBinding.bind(tempView);
        View ivFluidDetails = binding.ivFluidOptions.getRoot();
        Log.d(TAG, "showRadioOptionBoxForIVFluid: iv data: " + new Gson().toJson(info.getMedicationsForFluid()));

        binding.clIvFluidCountView.setOnClickListener(v -> {
            PrescriptionArg arg = getMedicationArg(PrescriptionFragment.PrescriptionType.IV_FLUID);
            AdministeredActivity.startAdministeredActivity(mContext, arg);
            MedicineSingleton.INSTANCE.setIvFluidListener(new MedicineChangeListener() {
                @Override
                public void onMedicineListChanged(@NonNull List<? extends ItemHeader> updated) {
                    List<Medication> ivFluids = new ArrayList<>();
                    for (ItemHeader item : updated) {
                        if (item instanceof Medication) ivFluids.add((Medication) item);
                    }
                    info.setMedicationsForFluid(ivFluids);
                    setivFluidDataNew(binding, info);
                }

                @NonNull
                @Override
                public LinkedList<ItemHeader> getExistingList() {
                    return new LinkedList<>(info.getMedicationsForFluid());
                }
            });
//            @SuppressLint("SetTextI18n")
//            IVFluidBottomSheetDialog dialog = IVFluidBottomSheetDialog.getInstance(info.getMedicationsForFluid(), mVisitUuid, (updated, deleted) -> {
//                info.setMedicationsForFluid(updated);
//                Log.d(TAG, "showRadioOptionBoxForIVFluid: updated ivs : " + new Gson().toJson(updated));
//                if (updated.size() > 0) {
//                    //Medication medicationLatest = updated.get(updated.size() - 1);
//
//                   /* Medication ivFluidDataForDb = new Medication();
//                   ivFluidDataForDb.setType(medicationLatest.getType());
//                    ivFluidDataForDb.setInfusionStatus(medicationLatest.getInfusionStatus());
//                    ivFluidDataForDb.setStrength(medicationLatest.getStrength());
//                    info.setMedication(ivFluidDataForDb);
//                    info.setCapturedValue(ivFluidDataForDb.toJson());
//                     binding.ivFluidOptions.viewTypeOfIvFluid.tvData.setText(medicationLatest.getType());
//                    binding.ivFluidOptions.viewInfusionRate.etvData.setText(medicationLatest.getInfusionRate());
//                    binding.ivFluidOptions.viewInfusionStatus.tvData.setText(medicationLatest.getInfusionStatus());
//                       Log.d(TAG, "showRadioOptionBoxForIVFluid: db data  : " + new Gson().toJson(ivFluidDataForDb));*/
//
//
//                }
//            });
//            dialog.setAccessMode(accessMode);
//            dialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), dialog.getClass().getCanonicalName());
        });
        handleRadioCheckListener(tempView, info, new OnRadioCheckedListener() {
            @Override
            public void onCheckedYes() {
                binding.clIvFluidCountView.setVisibility(View.VISIBLE);
                ivFluidDetails.setVisibility(View.VISIBLE);
                // setIvFluidDetails(info, binding);
                setivFluidDataNew(binding, info);
                showIVFluidOptionsDetails(title, info, tempView, binding);
            }

            @Override
            public void onCheckedNo() {
                binding.clIvFluidCountView.setVisibility(View.GONE);
                ivFluidDetails.setVisibility(View.GONE);
            }
        });
    }

    private void setivFluidDataNew(PartoLblRadioViewEzaziBinding binding, ParamInfo info) {
        List<Medication> allAdministerIvFluidsList = info.getMedicationsForFluid();
        if (allAdministerIvFluidsList.size() > 0) {
            Medication ivFluidData = allAdministerIvFluidsList.get(0);
            //Medication ivFluidData = getMedication(info);//old
            //Medication ivFluidData = getMedicationNew(medicationLatest1.toJson());
            Medication ivFluidDataForDb = new Medication();

            ivFluidDataForDb.setInfusionStatus(ivFluidData.getInfusionStatus());
            ivFluidDataForDb.setInfusionRate(ivFluidData.getInfusionRate());

            String ivFluidType = ivFluidData.getType();
            Log.d(TAG, "setivFluidDataNew: ivFluidType :" + ivFluidType);
            if (ivFluidType.equals("Ringer Lactate") || ivFluidType.equals("Normal Saline") || ivFluidType.equals("Dextrose 5% (D5)")) {
                ivFluidDataForDb.setType(ivFluidType);
                binding.ivFluidOptions.viewTypeOfIvFluid.tvData.setText(ivFluidData.getType());
            } else {
                ivFluidDataForDb.setType("Other");
                ivFluidDataForDb.setOtherType(ivFluidData.getOtherType());
                binding.ivFluidOptions.viewTypeOfIvFluid.tvData.setText(ivFluidData.getOtherType());
            }
            binding.ivFluidOptions.viewInfusionRate.etvData.setText(ivFluidData.getInfusionRate());
            binding.ivFluidOptions.viewInfusionStatus.tvData.setText(ivFluidData.getInfusionStatus());
            info.setMedication(ivFluidDataForDb);
            info.setCapturedValue(ivFluidDataForDb.toJson());
        }
    }

    private void uncheckAllOptions(DialogIvfluidOptionsBinding binding) {
        Log.d(TAG, "uncheckAllOptions: ");
        binding.tvDextrose.setSelected(false);
        binding.tvNormalSaline.setSelected(false);
        binding.tvRingerLactate.setSelected(false);
    }

    private void showIVFluidDialog(String title, ParamInfo info, View view) {
        DialogIvfluidOptionsBinding binding = DialogIvfluidOptionsBinding.inflate(LayoutInflater.from(mContext));
        binding.setItems(info.getOptions());
        TextView selected = view.findViewById(R.id.tvSelectedValue);
        TextView ivTypeValue = view.findViewById(R.id.tvData);
        binding.etOtherFluid.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        binding.etOtherFluid.setInputType(InputType.TYPE_CLASS_TEXT);

        binding.etOtherFluid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) uncheckAllOptions(binding);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.setClickListener(v -> {
            uncheckAllOptions(binding);
            v.setSelected(true);
            info.setCapturedValue(((TextView) v).getText().toString());
            binding.etOtherFluid.setText("");
            selected.setText(info.getCapturedValue());
            //selected.setVisibility(View.VISIBLE);
            ivTypeValue.setText(((TextView) v).getText().toString());
            Log.d(TAG, "showIVFluidDialog: cchheck " + ((TextView) v).getText().toString());
            info.getMedication().setType(((TextView) v).getText().toString());
            info.saveJson();
//            saveIvFluidDataInJson(info, ((TextView) v).getText().toString(), IvFluidTypes.type.name());

        });

        CustomViewDialogFragment dialog = new CustomViewDialogFragment.Builder(mContext).title(title).positiveButtonLabel(R.string.save_button).negativeButtonLabel(R.string.cancel).view(binding.getRoot()).build();

        dialog.requireValidationBeforeDismiss(true);
        dialog.setListener(new CustomViewDialogFragment.OnConfirmationActionListener() {
            @Override
            public void onAccept() {
                if (TextUtils.isEmpty(info.getCapturedValue()) && TextUtils.isEmpty(binding.etOtherFluid.getText())) {
                    Toast.makeText(mContext, "Please choose the any one option", Toast.LENGTH_LONG).show();
                } else if (!TextUtils.isEmpty(binding.etOtherFluid.getText())) {
                    String otherFluidValue = binding.etOtherFluid.getText().toString();
                    info.setCapturedValue(otherFluidValue);
                    selected.setText(otherFluidValue);
                    ivTypeValue.setText(otherFluidValue);
                    Log.d(TAG, "custom dialog: otherval :  " + otherFluidValue);
                    //info.getMedication().setType(otherFluidValue);
                    info.getMedication().setOtherType(otherFluidValue);
                    info.getMedication().setType("Other");
                    info.saveJson();
//                    saveIvFluidDataInJson(info, otherFluidValue, IvFluidTypes.type.name());

                    if (binding.etOtherFluid.getText().toString().length() > 0)
                        // selected.setVisibility(View.VISIBLE);
                        dialog.dismiss();

                    /*if (info.getCapturedValue() != null && info.getCapturedValue().length() > 0)
                        selected.setVisibility(View.VISIBLE);
                    dialog.dismiss();*/
                } else dialog.dismiss();
            }

            @Override
            public void onDecline() {
                RadioGroup radioGroup = view.findViewById(R.id.radioYesNoGroup);
                if (selected.getTag() != null) {
                    String oldValue = (String) selected.getTag();
                    info.setCapturedValue(oldValue);
                    String value = (String) selected.getTag();
                    if (!value.equalsIgnoreCase("NO")) {
                        // selected.setVisibility(View.VISIBLE);
                        radioGroup.check(R.id.radioYes);
                    }
                } else {
                    selected.setVisibility(View.GONE);
                    radioGroup.check(R.id.radioNo);
                }
                selected.setText(info.getCapturedValue());
            }
        });

        dialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), dialog.getClass().getCanonicalName());

        binding.etOtherFluid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) uncheckAllOptions(binding);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void clearDiastolic(String input, int position, List<ParamInfo> paramInfos, int adapterPosition) {
        ParamInfo info = paramInfos.get(position);
        if (!TextUtils.isEmpty(input) && info.getParamName().equals(PartogramConstants.Params.SYSTOLIC_BP.value)) {
            String dBp = paramInfos.get(position + 1).getCapturedValue();
            if (!TextUtils.isEmpty(dBp)) {
                int systolic = Integer.parseInt(input);
                int diastolic = Integer.parseInt(dBp);
                if (systolic < diastolic) {
                    currentChildFocusedIndex = position;
                    ParamInfo paramInfo = mItemList.get(adapterPosition).getParamInfoList().get(position + 1);
                    Log.e("Partogram", "clearDiastolic: " + paramInfo.getParamName());
                    mItemList.get(adapterPosition).getParamInfoList().get(position + 1).setCapturedValue("");
                    notifyItemChanged(adapterPosition);
//                    EditText dataEditText = tempView.findViewById(R.id.etvData);
                }
            }
        }
//        else if (TextUtils.isEmpty(input) && info.getParamName().equals(PartogramConstants.Params.SYSTOLIC_BP.value)) {
//            paramInfos.get(position + 1).setCapturedValue("");
//            notifyDataSetChanged();
//        }
    }

    private void validDiastolicBP(String input, int position, List<ParamInfo> paramInfos, EditText editText) {
        ParamInfo info = paramInfos.get(position);
        if (!TextUtils.isEmpty(input) && info.getParamName().equals(PartogramConstants.Params.DIASTOLIC_BP.value)) {
            if (!TextUtils.isEmpty(paramInfos.get(position - 1).getCapturedValue())) {
                int systolic = Integer.parseInt(paramInfos.get(position - 1).getCapturedValue());
                int diastolic = Integer.parseInt(input);
                if (systolic <= diastolic) {
                    Toast.makeText(mContext, "Diastolic BP must be less than Systolic BP", Toast.LENGTH_LONG).show();
                    editText.setText("");
                    editText.requestFocus();
                    info.setCapturedValue("");
                }
            } else {
                editText.setText("");
                editText.requestFocus();
                info.setCapturedValue("");
                Toast.makeText(mContext, "Enter Systolic BP before Diastolic BP", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showListOptions(final View tempView, final int position, final int positionChild) {
        TextView paramNameTextView = tempView.findViewById(R.id.tvParamName);
        TextView dropdownTextView = tempView.findViewById(R.id.tvData);
        ParamInfo info = mItemList.get(position).getParamInfoList().get(positionChild);
        dropdownTextView.setTag(info);
        dropdownTextView.setEnabled(accessMode != PartogramConstants.AccessMode.READ);
        paramNameTextView.setText(mItemList.get(position).getParamInfoList().get(positionChild).getParamName());
        if (mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue() != null && !mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue().isEmpty()) {
            dropdownTextView.setText(mItemList.get(position).getParamInfoList().get(positionChild).getOptions()[Arrays.asList(mItemList.get(position).getParamInfoList().get(positionChild).getValues()).indexOf(mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue())]);
        }

        dropdownTextView.setOnClickListener(v -> {
            if (v.getTag() instanceof ParamInfo) {
                ParamInfo ivFluidInfo = (ParamInfo) v.getTag();
                if (ivFluidInfo.getParamName().equalsIgnoreCase(PartogramConstants.Params.IV_FLUID.value)) {
                    showIVFluidDialog(ivFluidInfo.getParamName(), ivFluidInfo, tempView);
                } else {
                    final String[] items = mItemList.get(position).getParamInfoList().get(positionChild).getOptions();

                    ArrayList<SingChoiceItem> choiceItems = new ArrayList<>();
                    for (int i = 0; i < items.length; i++) {
                        SingChoiceItem item = new SingChoiceItem();
                        item.setItemIndex(i);
                        item.setItem(items[i]);
                        choiceItems.add(item);
                    }

                    String title = "Select for " + mItemList.get(position).getParamInfoList().get(positionChild).getParamName();
                    SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(mContext).title(title).content(choiceItems).build();

                    dialog.setListener(item -> {
                        ParamInfo paramInfo = mItemList.get(position).getParamInfoList().get(positionChild);
                        dropdownTextView.setText(paramInfo.getOptions()[item.getItemIndex()]);
                        paramInfo.setCapturedValue(paramInfo.getValues()[item.getItemIndex()]);
                    });

                    dialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), dialog.getClass().getCanonicalName());
                }
            }
        });

    }

    private void showIVFluidOptionsDetails(String title, ParamInfo info, View view, PartoLblRadioViewEzaziBinding binding) {
        binding.ivFluidOptions.viewTypeOfIvFluid.tvParamName.setText(R.string.type_of_iv_fluid);
        binding.ivFluidOptions.viewInfusionRate.tvParamName.setText(R.string.iv_infusion_rate);
        binding.ivFluidOptions.viewInfusionStatus.tvParamName.setText(R.string.iv_infusion_status);
        TextView ivTypeValue = binding.ivFluidOptions.viewInfusionStatus.tvData;
        EditText ivInfusionRate = binding.ivFluidOptions.viewInfusionRate.etvData;
//        ivInfusionStatus = binding.ivFluidOptions.viewInfusionStatus.tvData;

        TextView selected = view.findViewById(R.id.tvSelectedValue);


        binding.ivFluidOptions.viewTypeOfIvFluid.getRoot().setOnClickListener(v -> {
            //show iv fluid options
            showIVFluidDialog(title, info, view);
        });
        binding.ivFluidOptions.viewInfusionStatus.getRoot().setOnClickListener(v -> {
            //show infusion status
            // showIVFluidInfusionStatusDialog(title, info, view);
            String heading = "Select " + v.getContext().getString(R.string.title_iv_infusion_status);
            showSingleSelectionDialog(heading, info, ivTypeValue);
        });

        setInfusionRateTextChangeListener(ivInfusionRate, info);
        boolean enable = accessMode != PartogramConstants.AccessMode.READ;
        binding.ivFluidOptions.viewTypeOfIvFluid.tvData.setEnabled(enable);
        binding.ivFluidOptions.viewInfusionRate.etvData.setEnabled(enable);
        binding.ivFluidOptions.viewInfusionStatus.tvData.setEnabled(enable);
    }

    private void setInfusionRateTextChangeListener(EditText editText, ParamInfo info) {
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
//        , new RangeInputFilter(5, 60, (min, max) ->
//                Toast.makeText(editText.getContext(), "Infusion Rate must be in range of " + min + " to " + max, Toast.LENGTH_LONG).show())}

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && editText.getText().length() > 0) {
                int value = Integer.parseInt(editText.getText().toString());
                if (value < 5) {
                    saveInfusionRateValue(info, null);
                    editText.setText("");
                    showInfusionRateError(editText.getContext());
                }
            }
        });

        editText.addTextChangedListener(new TextChangeListener() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    int value = Integer.parseInt(s.toString());
                    if (value < 5) {
                        showInfusionRateError(editText.getContext());
                    } else if (value <= 60) {
                        saveInfusionRateValue(info, s.toString());
                    } else {
                        editText.setText("");
                        showInfusionRateError(editText.getContext());
                    }
                } else {
                    saveInfusionRateValue(info, null);
                }
            }
        });
    }

    private void showInfusionRateError(Context context) {
        Toast.makeText(context, "Infusion Rate must be in range of 5 to 60", Toast.LENGTH_LONG).show();
    }

    private void saveInfusionRateValue(ParamInfo info, String value) {
        info.getMedication().setInfusionRate(value);
        info.saveJson();
    }

    private void setIvFluidDetails(ParamInfo info, PartoLblRadioViewEzaziBinding binding) {
        Medication ivFluidData = getMedication(info);
        Log.e(TAG, "setIvFluidDetails: " + ivFluidData.toJson());
        binding.ivFluidOptions.viewTypeOfIvFluid.tvData.setText(ivFluidData.getType());
        binding.ivFluidOptions.viewInfusionRate.etvData.setText(ivFluidData.getInfusionRate());
        binding.ivFluidOptions.viewInfusionStatus.tvData.setText(ivFluidData.getInfusionStatus());
        info.setMedication(ivFluidData);
        info.setCapturedValue(ivFluidData.toJson());
    }

    private void showSingleSelectionDialog(String title, ParamInfo info, TextView selected) {
        final String[] items = info.getStatus();
        ArrayList<SingChoiceItem> choiceItems = new ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            SingChoiceItem item = new SingChoiceItem();
            item.setItemIndex(i);
            item.setItem(items[i]);
            choiceItems.add(item);
        }

        SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(mContext).title(title).content(choiceItems).build();

        dialog.setListener(item -> {
            manageSelectionSingleChoiceSelection(info, item, selected);
        });
        dialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), dialog.getClass().getCanonicalName());

    }

    private void handleRadioCheckListener(final View tempView, final ParamInfo info, OnRadioCheckedListener listener) {
        RadioGroup radioGroup = tempView.findViewById(R.id.radioYesNoGroup);
        if (info.getCapturedValue() != null && !TextUtils.isEmpty(info.getCapturedValue()) && !info.getCapturedValue().equalsIgnoreCase("NO")) {
            radioGroup.check(R.id.radioYes);
            info.setCheckedRadioOption(ParamInfo.RadioOptions.YES);
            listener.onCheckedYes();
        } else {
            radioGroup.check(R.id.radioNo);
            info.setCapturedValue(ParamInfo.RadioOptions.NO.name());
            info.setCheckedRadioOption(ParamInfo.RadioOptions.NO);
            listener.onCheckedNo();
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton radioButton = tempView.findViewById(checkedId);
            if (checkedId == R.id.radioYes && radioButton.isChecked()) {
                info.setCheckedRadioOption(ParamInfo.RadioOptions.YES);
                listener.onCheckedYes();
            } else if (checkedId == R.id.radioNo && radioButton.isChecked()) {
                listener.onCheckedNo();
                if (!TextUtils.isEmpty(info.getCapturedValue())) {
                    info.setCapturedValue(ParamInfo.RadioOptions.NO.name());
                    info.setCheckedRadioOption(ParamInfo.RadioOptions.NO);
                }
            }
        });

        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(accessMode != PartogramConstants.AccessMode.READ);
        }
    }

    private void showRadioOptionBoxForOxytocin(final View tempView, final ParamInfo info, TextView selected, String title) {
        PartoLblRadioViewOxytocinBinding binding = PartoLblRadioViewOxytocinBinding.bind(tempView);
        View oxytocinDetails = binding.includeLayoutPartoOxytocin.getRoot();

        Log.d(TAG, "showRadioOptionBoxForOxytocin: iv data: " + new Gson().toJson(info.getMedicationsForOxytocin()));

        binding.clOxytocinCountView.setOnClickListener(v -> {
            PrescriptionArg arg = getMedicationArg(PrescriptionFragment.PrescriptionType.OXYTOCIN);
            AdministeredActivity.startAdministeredActivity(mContext, arg);
            MedicineSingleton.INSTANCE.setOxytocinListener(new MedicineChangeListener() {
                @Override
                public void onMedicineListChanged(@NonNull List<? extends ItemHeader> updated) {
                    List<Medication> oxytocins = new ArrayList<>();
                    for (ItemHeader item : updated) {
                        if (item instanceof Medication) oxytocins.add((Medication) item);
                    }
                    info.setMedicationsForOxytocin(oxytocins);
                    setOxytocinDataNew(binding, info);
                }

                @NonNull
                @Override
                public LinkedList<ItemHeader> getExistingList() {
                    return new LinkedList<>(info.getMedicationsForOxytocin());
                }
            });
//            @SuppressLint("SetTextI18n")
//            OxytocinBottomSheetDialog dialog = OxytocinBottomSheetDialog.getInstance(info.getMedicationsForOxytocin(), mVisitUuid, (updated, deleted) -> {
//                info.setMedicationsForOxytocin(updated);
//                Log.d(TAG, "showRadioOptionBoxForOxytocin: updated ivs : " + new Gson().toJson(updated));
//                if (updated.size() > 0) {
//                    //Medication medicationLatest = updated.get(updated.size() - 1);
//                    info.setMedicationsForOxytocin(updated);
//                    setOxytocinDataNew(binding, info);
//                   /* Medication ivFluidDataForDb = new Medication();
//                   ivFluidDataForDb.setType(medicationLatest.getType());
//                    ivFluidDataForDb.setInfusionStatus(medicationLatest.getInfusionStatus());
//                    ivFluidDataForDb.setStrength(medicationLatest.getStrength());
//                    info.setMedication(ivFluidDataForDb);
//                    info.setCapturedValue(ivFluidDataForDb.toJson());
//                     binding.ivFluidOptions.viewTypeOfIvFluid.tvData.setText(medicationLatest.getType());
//                    binding.ivFluidOptions.viewInfusionRate.etvData.setText(medicationLatest.getInfusionRate());
//                    binding.ivFluidOptions.viewInfusionStatus.tvData.setText(medicationLatest.getInfusionStatus());
//                       Log.d(TAG, "showRadioOptionBoxForIVFluid: db data  : " + new Gson().toJson(ivFluidDataForDb));*/
//
//
//                }
//            });
//            dialog.setAccessMode(accessMode);
//            dialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), dialog.getClass().getCanonicalName());
        });
        handleRadioCheckListener(tempView, info, new OnRadioCheckedListener() {
            @Override
            public void onCheckedYes() {
                binding.clOxytocinCountView.setVisibility(View.VISIBLE);
                oxytocinDetails.setVisibility(View.VISIBLE);
                //setOxytocinDetails(info, binding);
                setOxytocinDataNew(binding, info);
                showOxytocinOptionsDetails(title, info, tempView, binding);
            }

            @Override
            public void onCheckedNo() {
                binding.clOxytocinCountView.setVisibility(View.GONE);
                oxytocinDetails.setVisibility(View.GONE);
            }
        });
    }

    private void setOxytocinDetails(ParamInfo info, PartoLblRadioViewOxytocinBinding binding) {
        Medication oxytocinData = getMedication(info);
        Log.e(TAG, "setOxytocinDetails: " + oxytocinData.toJson());
        binding.includeLayoutPartoOxytocin.viewStrength.etvData.setText(oxytocinData.getStrength());
        binding.includeLayoutPartoOxytocin.viewInfusionRate.etvData.setText(oxytocinData.getInfusionRate());
        binding.includeLayoutPartoOxytocin.viewInfusionStatus.tvData.setText(oxytocinData.getInfusionStatus());
        info.setMedication(oxytocinData);
        info.setCapturedValue(oxytocinData.toJson());
    }


    private Medication getMedication(ParamInfo info) {
        try {
            Gson gson = new Gson();
            Medication oxytocinData = gson.fromJson(info.getCapturedValue(), Medication.class);
            if (oxytocinData == null) oxytocinData = info.getMedication();
            return oxytocinData;
        } catch (JsonSyntaxException e) {
            return info.getMedication();
        }
    }

    private void showOxytocinOptionsDetails(String title, ParamInfo info, View view, PartoLblRadioViewOxytocinBinding binding) {
        binding.includeLayoutPartoOxytocin.viewStrength.tvParamName.setText(R.string.strength_unit);
        binding.includeLayoutPartoOxytocin.viewInfusionRate.tvParamName.setText(R.string.iv_infusion_rate);
        binding.includeLayoutPartoOxytocin.viewInfusionStatus.tvParamName.setText(R.string.iv_infusion_status);

        binding.includeLayoutPartoOxytocin.viewStrength.etvData.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        binding.includeLayoutPartoOxytocin.viewStrength.etvData.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.includeLayoutPartoOxytocin.viewInfusionRate.etvData.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        binding.includeLayoutPartoOxytocin.viewInfusionRate.etvData.setInputType(InputType.TYPE_CLASS_NUMBER);

        binding.includeLayoutPartoOxytocin.viewInfusionStatus.getRoot().setOnClickListener(v -> {
            //show infusion status
            // showIVFluidInfusionStatusDialog(title, info, view);
            String heading = "Select " + v.getContext().getString(R.string.title_iv_infusion_status);
            showSingleSelectionDialog(heading, info, binding.includeLayoutPartoOxytocin.viewInfusionStatus.tvData);
        });

        binding.includeLayoutPartoOxytocin.viewStrength.etvData.addTextChangedListener(new TextChangeListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    //if (s.toString().contains("(U/L)")) {
                    String cleanedString = s.toString().replace("(U/L)", "").trim();

                    int value = Integer.parseInt(cleanedString);
                    if (value <= 10 && value > 0) {
                        info.getMedication().setStrength(cleanedString);
                        info.saveJson();
                    } else {
                        binding.includeLayoutPartoOxytocin.viewStrength.etvData.setText("");
                        Context context = binding.includeLayoutPartoOxytocin.viewStrength.etvData.getContext();
                        Toast.makeText(context, "Strength must be in range of 1 to 10", Toast.LENGTH_LONG).show();
                    }
                    // }

                } else {
                    info.getMedication().setStrength(null);
                    info.saveJson();
                }
            }
        });

        setInfusionRateTextChangeListener(binding.includeLayoutPartoOxytocin.viewInfusionRate.etvData, info);
        boolean enable = accessMode != PartogramConstants.AccessMode.READ;
        binding.includeLayoutPartoOxytocin.viewStrength.etvData.setEnabled(enable);
        binding.includeLayoutPartoOxytocin.viewInfusionRate.etvData.setEnabled(enable);
        binding.includeLayoutPartoOxytocin.viewInfusionStatus.tvData.setEnabled(enable);
    }

    private void manageSelectionSingleChoiceSelection(ParamInfo info, SingChoiceItem item, TextView view) {
        if (info.getConceptUUID().equals(UuidDictionary.IV_FLUIDS) || info.getConceptUUID().equals(UuidDictionary.OXYTOCIN_UL_DROPS_MIN)) { //for infusion status -iv fluid
            info.getMedication().setInfusionStatus(item.getItem());
            info.saveJson();
            TextView selected = view.findViewById(R.id.tvData);
            selected.setText(item.getItem());
        }
    }

    private void validateBaseLineFHR(EditText dataEditText) {
        if (!isToastShownFHR) {
            Toast.makeText(mContext, mContext.getString(R.string.baseline_fhr_err, AppConstants.MINIMUM_BASELINE_FHR, AppConstants.MAXIMUM_BASELINE_FHR), Toast.LENGTH_LONG).show();
            dataEditText.requestFocus();
            isToastShownFHR = true;
        }
    }

    private void validatePulse(EditText dataEditText) {
        if (!isToastShownPulse) {
            Toast.makeText(mContext, mContext.getString(R.string.pulse_err, AppConstants.MINIMUM_PULSE, AppConstants.MAXIMUM_PULSE), Toast.LENGTH_LONG).show();
            dataEditText.requestFocus();
            isToastShownPulse = true;
        }
    }

    private void validateDurationOfContraction(EditText dataEditText) {
        if (!isToastShownContraction) {
            Toast.makeText(mContext, mContext.getString(R.string.contraction_duration_err, AppConstants.MINIMUM_CONTRACTION_DURATION, AppConstants.MAXIMUM_CONTRACTION_DURATION), Toast.LENGTH_LONG).show();
            dataEditText.requestFocus();
            isToastShownContraction = true;
        }
    }

    private void validateSysBp(EditText dataEditText) {
        if (!isToastShownSysBP) {
            Toast.makeText(mContext, mContext.getString(R.string.systolic_bp_range_toast_err, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS), Toast.LENGTH_LONG).show();
            dataEditText.requestFocus();
            isToastShownSysBP = true;
        }
    }

    private void validateDysBp(EditText dataEditText) {
        if (!isToastShownDysBP) {
            Toast.makeText(mContext, mContext.getString(R.string.diastolic_bp_range_toast_err, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS), Toast.LENGTH_LONG).show();
            dataEditText.requestFocus();
            isToastShownDysBP = true;
        }
    }

    private void setOxytocinDataNew(PartoLblRadioViewOxytocinBinding binding, ParamInfo info) {
        List<Medication> allAdministerOxytocinsList = info.getMedicationsForOxytocin();
        Log.d(TAG, "setOxytocinDataNew: allAdministerOxytocinsList : " + allAdministerOxytocinsList.size());
        if (allAdministerOxytocinsList.size() > 0) {
            Medication oxytocinData = allAdministerOxytocinsList.get(0);
            Medication oxytocinDataForDb = new Medication();
            String strengthValue = oxytocinData.getStrength();
            if (oxytocinData.getStrength().contains("(U/L)")) {
                strengthValue = oxytocinData.getStrength().replace("(U/L)", "").trim();
            }

            binding.includeLayoutPartoOxytocin.viewStrength.etvData.setText(strengthValue);
            binding.includeLayoutPartoOxytocin.viewInfusionRate.etvData.setText(oxytocinData.getInfusionRate());
            binding.includeLayoutPartoOxytocin.viewInfusionStatus.tvData.setText(oxytocinData.getInfusionStatus());
            oxytocinDataForDb.setStrength(strengthValue);
            oxytocinDataForDb.setInfusionStatus(oxytocinData.getInfusionStatus());
            oxytocinDataForDb.setInfusionRate(oxytocinData.getInfusionRate());
            info.setMedication(oxytocinDataForDb);
            info.setCapturedValue(oxytocinDataForDb.toJson());
            Log.d(TAG, "setOxytocinDataNew: date :" + DateTimeUtils.getCurrentDateInUTC(AppConstants.UTC_FORMAT));
            info.setCreatedDate(DateTimeUtils.getCurrentDateInUTC(AppConstants.UTC_FORMAT));
            Log.d(TAG, "setOxytocinDataNew: infodate : " + info.getCreatedDate());
        }
    }

    private void showRadioOptionBoxForPlan(View tempView, ParamInfo info, TextView selected, String title) {
        Log.d(TAG, "showRadioOptionBoxForPlan: list: " + new Gson().toJson(info.getPlans()));
        PartoLablRadioViewPlanBinding binding = PartoLablRadioViewPlanBinding.bind(tempView);

        binding.clPlanCountView.setOnClickListener(v -> {
            PrescriptionArg arg = getMedicationArg(PrescriptionFragment.PrescriptionType.PLAN);
            AdministeredActivity.startAdministeredActivity(mContext, arg);
            MedicineSingleton.INSTANCE.setPlanListener(new MedicineChangeListener() {
                @Override
                public void onMedicineListChanged(@NonNull List<? extends ItemHeader> updated) {
                    List<ObsDTO> plans = new ArrayList<>();
                    for (ItemHeader item : updated) {
                        if (item instanceof ObsDTO) plans.add((ObsDTO) item);
                    }
                    info.setPlans(plans);
                    setupPlansCountView(binding.tvPlanCount, plans);
                }

                @NonNull
                @Override
                public LinkedList<ItemHeader> getExistingList() {
                    return new LinkedList<>(info.getPlans());
                }
            });
//            @SuppressLint("SetTextI18n")
//            PlanBottomSheetDialog dialog = PlanBottomSheetDialog.getInstance(info.getPlans(), mVisitUuid, (updated, deleted) -> {
//                info.setPlans(updated);
//                info.setDeletedPlans(deleted);
//                //info.setCapturedValue(updated);
//
//            });
//            dialog.setAccessMode(accessMode);
//            dialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), dialog.getClass().getCanonicalName());
        });
        setupPlansCountView(binding.tvPlanCount, info.getPlans());


/*
        handleRadioCheckListener(tempView, info, new OnRadioCheckedListener() {
            @Override
            public void onCheckedYes() {
                binding.clPlanCountView.setVisibility(View.VISIBLE);
                setupPlansCountView(binding.tvPlanCount, info.getPlans());
            }

            @Override
            public void onCheckedNo() {
                binding.clPlanCountView.setVisibility(View.GONE);
            }
        });
*/


    }

    private void showRadioOptionBoxForAssessment(View tempView, ParamInfo info, TextView selected, String title) {
        PartoLablRadioViewAssessmentBinding binding = PartoLablRadioViewAssessmentBinding.bind(tempView);

        binding.clAssessmentCountView.setOnClickListener(v -> {
            PrescriptionArg arg = getMedicationArg(PrescriptionFragment.PrescriptionType.ASSESSMENT);
            AdministeredActivity.startAdministeredActivity(mContext, arg);
            MedicineSingleton.INSTANCE.setAssessmentListener(new MedicineChangeListener() {
                @Override
                public void onMedicineListChanged(@NonNull List<? extends ItemHeader> updated) {
                    List<ObsDTO> assessments = new ArrayList<>();
                    for (ItemHeader item : updated) {
                        if (item instanceof ObsDTO) assessments.add((ObsDTO) item);
                    }
                    info.setAssessments(assessments);
                    //info.setDeletedMedicines(deleted);
                    setupAssessmentCountView(binding.tvAssessmentCount, assessments);
                }

                @NonNull
                @Override
                public LinkedList<ItemHeader> getExistingList() {
                    return new LinkedList<>(info.getAssessments());
                }
            });
//            @SuppressLint("SetTextI18n")
//            AssessmentBottomSheetDialog dialog = AssessmentBottomSheetDialog.getInstance(info.getAssessments(), mVisitUuid, (updated, deleted) -> {
//                info.setAssessments(updated);
//                //info.setDeletedMedicines(deleted);
//                setupAssessmentCountView(binding.tvAssessmentCount, updated);
//            });
//            dialog.setAccessMode(accessMode);
//            dialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), dialog.getClass().getCanonicalName());
        });
        setupAssessmentCountView(binding.tvAssessmentCount, info.getAssessments());

    }

    @SuppressLint("SetTextI18n")
    private void setupPlansCountView(TextView textView, List<ObsDTO> updated) {
        if (updated.size() == 0) textView.setText(mContext.getString(R.string.lbl_add));
        else textView.setText("" + updated.size());
    }

    @SuppressLint("SetTextI18n")
    private void setupAssessmentCountView(TextView textView, List<ObsDTO> updated) {
        if (updated.size() == 0) textView.setText(mContext.getString(R.string.lbl_add));
        else textView.setText("" + updated.size());
    }
}