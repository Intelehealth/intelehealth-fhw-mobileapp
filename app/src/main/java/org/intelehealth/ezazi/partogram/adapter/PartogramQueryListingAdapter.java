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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.databinding.DialogIvfluidOptionsBinding;
import org.intelehealth.ezazi.databinding.MedicineDetailsBinding;
import org.intelehealth.ezazi.databinding.PartoLablRadioViewMedicineBinding;
import org.intelehealth.ezazi.databinding.PartoLblRadioViewEzaziBinding;
import org.intelehealth.ezazi.databinding.PartoLblRadioViewOxytocinBinding;
import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.model.ParamInfo;
import org.intelehealth.ezazi.partogram.model.PartogramItemData;
import org.intelehealth.ezazi.ui.dialog.CustomViewDialogFragment;
import org.intelehealth.ezazi.ui.dialog.SingleChoiceDialogFragment;
import org.intelehealth.ezazi.ui.dialog.model.SingChoiceItem;
import org.intelehealth.ezazi.utilities.UuidDictionary;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PartogramQueryListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "PartogramQueryListingAd";
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<PartogramItemData> mItemList = new ArrayList<PartogramItemData>();
    private TextView selectedTextview;
    private static JSONObject ivFluidsJsonObject = new JSONObject();
    private static JSONObject oxytocinDataObject = new JSONObject();

    private TextView ivTypeValue, ivInfusionRate, ivInfusionStatus;
    private TextView strengthOxytocin, ivInfusionRateOxytocin, ivInfusionStatusOxytocin;

    public interface OnItemSelection {
        public void onSelect(PartogramItemData partogramItemData);
    }

    private OnItemSelection mOnItemSelection;
    private int currentChildFocusedIndex = -1;

    public PartogramQueryListingAdapter(RecyclerView recyclerView, Context context, List<PartogramItemData> itemList, OnItemSelection onItemSelection) {
        mContext = context;
        mItemList = itemList;
        mOnItemSelection = onItemSelection;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.parto_list_item_view_ezazi, parent, false);
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
                if (paramInfo.getParamDateType().equalsIgnoreCase(PartogramConstants.INPUT_TXT_TYPE)
                        || paramInfo.getParamDateType().equalsIgnoreCase(PartogramConstants.INPUT_DOUBLE_4_DIG_TYPE)
                        || paramInfo.getParamDateType().equalsIgnoreCase(PartogramConstants.INPUT_INT_1_DIG_TYPE)
                        || paramInfo.getParamDateType().equalsIgnoreCase(PartogramConstants.INPUT_INT_2_DIG_TYPE)
                        || paramInfo.getParamDateType().equalsIgnoreCase(PartogramConstants.INPUT_INT_3_DIG_TYPE)) {
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
        dataEditText.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        dataEditText.setAdapter(new ArrayAdapter(mContext, R.layout.spinner_textview, mContext.getResources().getStringArray(R.array.medications)));
        dataEditText.setThreshold(1);

        paramNameTextView.setText(mItemList.get(position).getParamInfoList().get(positionChild).getParamName());

        if (mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue() != null &&
                !mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue().isEmpty()) {
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
        paramNameTextView.setText(mItemList.get(position).getParamInfoList().get(positionChild).getParamName());
        if (mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue() != null && !mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue().isEmpty()) {
            dataEditText.setText(String.valueOf(mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue()));
        } else {
            dataEditText.setText("");
        }

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
                }
                });
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
        dataEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty() && !s.toString().startsWith(".") && mItemList.get(position).getParamInfoList()
                        .get(positionChild).getParamName().equalsIgnoreCase("Temperature(C)")) {

                    if (Double.parseDouble(s.toString()) > Double.parseDouble(AppConstants.MAXIMUM_TEMPERATURE_CELSIUS) ||
                            Double.parseDouble(s.toString()) < Double.parseDouble(AppConstants.MINIMUM_TEMPERATURE_CELSIUS)) {
                        //dataEditText.setError(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_CELSIUS, AppConstants.MAXIMUM_TEMPERATURE_CELSIUS));
                        Toast.makeText(mContext, mContext.getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_CELSIUS, AppConstants.MAXIMUM_TEMPERATURE_CELSIUS), Toast.LENGTH_LONG).show();
                        dataEditText.requestFocus();
                        return;
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
                clearDiastolic(s.toString(), positionChild, mItemList.get(position).getParamInfoList(), position);
                validDiastolicBP(s.toString(), positionChild, mItemList.get(position).getParamInfoList(), dataEditText);
            }
        });
    }

    private void showRadioOptionBox(final View tempView, final int position, final int positionChild) {
        ParamInfo info = mItemList.get(position).getParamInfoList().get(positionChild);
        TextView paramNameTextView = tempView.findViewById(R.id.tvParamName);
        String title = info.getParamName();
        paramNameTextView.setText(title);
        TextView selected = tempView.findViewById(R.id.tvSelectedValue);
        selected.setTag(info.getCapturedValue());
        selected.setText(info.getCapturedValue());

        if (info.getConceptUUID().equals(UuidDictionary.IV_FLUIDS)) {
            showRadioOptionBoxForIVFluid(tempView, info, selected, title);
        } else if (info.getConceptUUID().equals(UuidDictionary.OXYTOCIN_UL_DROPS_MIN)) {
            showRadioOptionBoxForOxytocin(tempView, info, selected, title);
        } else if (info.getConceptUUID().equals(UuidDictionary.MEDICINE)) {
            showRadioOptionBoxForMedicine(tempView, info, selected, title);
        }
    }

    private void showRadioOptionBoxForMedicine(View tempView, ParamInfo info, TextView selected, String title) {
        RadioGroup radioGroup = tempView.findViewById(R.id.radioYesNoGroup);

        PartoLablRadioViewMedicineBinding binding = PartoLablRadioViewMedicineBinding.bind(tempView);
        View medicineDetails = binding.includeLayoutPartoMedicine.getRoot();

        if (info.getCapturedValue() != null &&
                !TextUtils.isEmpty(info.getCapturedValue())
                && !info.getCapturedValue().equalsIgnoreCase("NO")) {
            // selected.setVisibility(View.VISIBLE);
            radioGroup.check(R.id.radioYes);

            medicineDetails.setVisibility(View.VISIBLE);
            // setOxytocinDetails(info.getCapturedValue(), binding);
            //showOxytocinOptionsDetails(title, info, tempView, binding);

        } else {
            radioGroup.check(R.id.radioNo);
            info.setCapturedValue("No");
            selected.setVisibility(View.GONE);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton radioButton = tempView.findViewById(checkedId);
            if (checkedId == R.id.radioYes && radioButton.isChecked()) {
                // showIVFluidDialog(title, info, tempView);
                medicineDetails.setVisibility(View.VISIBLE);
                showMedicineDialog(title, info, tempView);
                //showMedicineOptionsDialog(title, info, tempView, binding);
            } else if (checkedId == R.id.radioNo && radioButton.isChecked()) {
                medicineDetails.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(info.getCapturedValue())) {
                    info.setCapturedValue("NO");
                    selected.setVisibility(View.GONE);
                    // binding.includeLayoutPartoOxytocin.viewStrength.etvData.setText("");
                    // binding.includeLayoutPartoOxytocin.viewInfusionRate.etvData.setText("");
                    //binding.includeLayoutPartoOxytocin.viewInfusionStatus.tvData.setText("");

                }
            }
        });

    }

    private void showMedicineOptionsDialog(String title, ParamInfo info, View tempView, PartoLablRadioViewMedicineBinding binding) {
        MedicineDetailsBinding medicineBinding = binding.includeLayoutPartoMedicine;
        medicineBinding.viewListOfMedicines.tvParamName.setText(R.string.list_of_medicines);
        medicineBinding.viewListOfMedicines.tvParamName.setText(R.string.strength);
        medicineBinding.viewListOfMedicines.tvParamName.setText(R.string.route);
        medicineBinding.viewListOfMedicines.tvParamName.setText(R.string.dosage);
        medicineBinding.viewListOfMedicines.tvParamName.setText(R.string.frequency);

        TextView medicineName = medicineBinding.viewListOfMedicines.tvData;
        TextView strength = medicineBinding.viewStrength.etvData;
        TextView route = medicineBinding.viewRoute.tvData;
        TextView dosage = medicineBinding.viewDosage.etvData;
        TextView frequency = medicineBinding.viewFrequency.tvData;


        TextView selected = tempView.findViewById(R.id.tvSelectedValue);

        medicineBinding.viewRoute.getRoot().setOnClickListener(v -> {
            //show dialog for route

            Toast.makeText(mContext, "clickedddd", Toast.LENGTH_SHORT).show();
            final String[] items = {"Oral", "Sublingual (SL)", "Intravenous (IV)", "IV Infusion", "Intramuscular (IM)", "Subcutaneous (SC)", "Intradermal (ID)"};
            ArrayList<SingChoiceItem> choiceItems = new ArrayList<>();
            for (int i = 0; i < items.length; i++) {
                SingChoiceItem item = new SingChoiceItem();
                item.setItemIndex(i);
                item.setItem(items[i]);
                choiceItems.add(item);
            }

            String titleDialog = "Select for " + title;
            SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(mContext)
                    .title(titleDialog)
                    .content(choiceItems)
                    .build();

            dialog.setListener(item -> {
                route.setText(item.getItem());
                //manageSelectionSingleChoiceSelection(info, item);
            });
            dialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), dialog.getClass().getCanonicalName());
        });
    /*    binding.viewFrequency.getRoot().setOnClickListener(v -> {
            //show dialog for frequency
        });*/


       /* binding.includeLayoutPartoMedicine.viewStrength.etvData.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        binding.includeLayoutPartoMedicine.viewStrength.etvData.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.includeLayoutPartoMedicine.viewInfusionRate.etvData.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        binding.includeLayoutPartoMedicine.viewInfusionRate.etvData.setInputType(InputType.TYPE_CLASS_NUMBER);
        */


      /*  binding.includeLayoutPartoMedicine.viewStrength.etvData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    saveOxytocinDataInJson(info, s.toString(), OxytocinDetails.strength.name());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.includeLayoutPartoMedicine.viewInfusionRate.etvData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    saveOxytocinDataInJson(info, s.toString(), OxytocinDetails.infusionRate.name());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });*/

    }

    private void showRadioOptionBoxForIVFluid(View tempView, ParamInfo info, TextView selected, String title) {
        PartoLblRadioViewEzaziBinding binding = PartoLblRadioViewEzaziBinding.bind(tempView);
        View ivFluidDetails = binding.ivFluidOptions.getRoot();
        RadioGroup radioGroup = tempView.findViewById(R.id.radioYesNoGroup);

        if (info.getCapturedValue() != null &&
                !TextUtils.isEmpty(info.getCapturedValue())
                && !info.getCapturedValue().equalsIgnoreCase("NO")) {
            // selected.setVisibility(View.VISIBLE);
            radioGroup.check(R.id.radioYes);

            binding.ivFluidOptions.getRoot().setVisibility(View.VISIBLE);
            setIvFluidDetails(info.getCapturedValue(), binding);

            showIVFluidOptionsDetails(title, info, tempView, binding);

        } else {
            radioGroup.check(R.id.radioNo);
            info.setCapturedValue("No");
            selected.setVisibility(View.GONE);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton radioButton = tempView.findViewById(checkedId);
            if (checkedId == R.id.radioYes && radioButton.isChecked()) {
                // showIVFluidDialog(title, info, tempView);
                ivFluidDetails.setVisibility(View.VISIBLE);
                showIVFluidOptionsDetails(title, info, tempView, binding);

            } else if (checkedId == R.id.radioNo && radioButton.isChecked()) {
                ivFluidDetails.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(info.getCapturedValue())) {
                    info.setCapturedValue("NO");
                    selected.setVisibility(View.GONE);
                    binding.ivFluidOptions.viewTypeOfIvFluid.tvData.setText("");
                    binding.ivFluidOptions.viewInfusionRate.etvData.setText("");
                    binding.ivFluidOptions.viewInfusionStatus.tvData.setText("");

                }
            }
        });
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
//        TextView selected = view.findViewById(R.id.tvData);
        binding.etOtherFluid.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        binding.etOtherFluid.setInputType(InputType.TYPE_CLASS_TEXT);

        binding.etOtherFluid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0)
                    uncheckAllOptions(binding);
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
            saveIvFluidDataInJson(info, ((TextView) v).getText().toString(), IvFluidTypes.type.name());

        });

        CustomViewDialogFragment dialog = new CustomViewDialogFragment.Builder(mContext)
                .title(title)
                .positiveButtonLabel(R.string.save_button)
                .negativeButtonLabel(R.string.cancel)
                .view(binding.getRoot())
                .build();

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
                    saveIvFluidDataInJson(info, otherFluidValue, IvFluidTypes.type.name());

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
                if (s.length() > 0)
                    uncheckAllOptions(binding);
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
        paramNameTextView.setText(mItemList.get(position).getParamInfoList().get(positionChild).getParamName());
        if (mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue() != null &&
                !mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue().isEmpty()) {
            dropdownTextView.setText(mItemList.get(position).getParamInfoList().get(positionChild).getOptions()
                    [Arrays.asList(mItemList.get(position).getParamInfoList().get(positionChild).getValues())
                    .indexOf(mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue())]);
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
                    SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(mContext)
                            .title(title)
                            .content(choiceItems)
                            .build();

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
        ivTypeValue = binding.ivFluidOptions.viewTypeOfIvFluid.tvData;
        ivInfusionRate = binding.ivFluidOptions.viewInfusionRate.etvData;
        ivInfusionStatus = binding.ivFluidOptions.viewInfusionStatus.tvData;
        ivInfusionRate.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        ivInfusionRate.setInputType(InputType.TYPE_CLASS_NUMBER);


        TextView selected = view.findViewById(R.id.tvSelectedValue);


        binding.ivFluidOptions.viewTypeOfIvFluid.getRoot().setOnClickListener(v -> {
            //show iv fluid options
            showIVFluidDialog(title, info, view);
        });
        binding.ivFluidOptions.viewInfusionStatus.getRoot().setOnClickListener(v -> {
            //show infusion status
            // showIVFluidInfusionStatusDialog(title, info, view);
            showSingleSelectionDialog(title, info, view);

        });
        binding.ivFluidOptions.viewInfusionRate.etvData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    saveIvFluidDataInJson(info, s.toString(), IvFluidTypes.infusionRate.name());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void saveIvFluidDataInJson(ParamInfo info, String value, String type) {
        try {
            ivFluidsJsonObject.put(type, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        info.setCapturedValue(ivFluidsJsonObject.toString());
    }

    private void saveOxytocinDataInJson(ParamInfo info, String value, String type) {
        try {
            oxytocinDataObject.put(type, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        info.setCapturedValue(oxytocinDataObject.toString());
        String oxytocinString = oxytocinDataObject.toString();
        Log.d(TAG, "oxytocinDataObject: oxytocinString : " + oxytocinString);
    }

    private enum IvFluidTypes {
        type,
        infusionRate,
        infusionStatus
    }

    private enum OxytocinDetails {
        strength,
        infusionRate,
        infusionStatus
    }

    private void setIvFluidDetails(String capturedValue, PartoLblRadioViewEzaziBinding binding) {
        try {
            Gson gson = new Gson();
            IvFluidData ivFluidData = gson.fromJson(capturedValue, IvFluidData.class);
            binding.ivFluidOptions.viewTypeOfIvFluid.tvData.setText(ivFluidData.type);
            binding.ivFluidOptions.viewInfusionRate.etvData.setText(ivFluidData.infusionRate);
            binding.ivFluidOptions.viewInfusionStatus.tvData.setText(ivFluidData.infusionStatus);

        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    private void showSingleSelectionDialog(String title, ParamInfo info, View view) {
        final String[] items = info.getStatus();
        ArrayList<SingChoiceItem> choiceItems = new ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            SingChoiceItem item = new SingChoiceItem();
            item.setItemIndex(i);
            item.setItem(items[i]);
            choiceItems.add(item);
        }

        String titleDialog = "Select for " + title;
        SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(mContext)
                .title(titleDialog)
                .content(choiceItems)
                .build();

        dialog.setListener(item -> {
            manageSelectionSingleChoiceSelection(info, item);
        });
        dialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), dialog.getClass().getCanonicalName());

    }

    private void showRadioOptionBoxForOxytocin(final View tempView, final ParamInfo info, TextView selected, String title) {
        RadioGroup radioGroup = tempView.findViewById(R.id.radioYesNoGroup);

        PartoLblRadioViewOxytocinBinding binding = PartoLblRadioViewOxytocinBinding.bind(tempView);
        View oxytocinDetails = binding.includeLayoutPartoOxytocin.getRoot();

        if (info.getCapturedValue() != null &&
                !TextUtils.isEmpty(info.getCapturedValue())
                && !info.getCapturedValue().equalsIgnoreCase("NO")) {
            // selected.setVisibility(View.VISIBLE);
            radioGroup.check(R.id.radioYes);

            oxytocinDetails.setVisibility(View.VISIBLE);
            setOxytocinDetails(info.getCapturedValue(), binding);
            showOxytocinOptionsDetails(title, info, tempView, binding);

        } else {
            radioGroup.check(R.id.radioNo);
            info.setCapturedValue("No");
            selected.setVisibility(View.GONE);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton radioButton = tempView.findViewById(checkedId);
            if (checkedId == R.id.radioYes && radioButton.isChecked()) {
                // showIVFluidDialog(title, info, tempView);
                oxytocinDetails.setVisibility(View.VISIBLE);
                showOxytocinOptionsDetails(title, info, tempView, binding);

            } else if (checkedId == R.id.radioNo && radioButton.isChecked()) {
                oxytocinDetails.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(info.getCapturedValue())) {
                    info.setCapturedValue("NO");
                    selected.setVisibility(View.GONE);
                    binding.includeLayoutPartoOxytocin.viewStrength.etvData.setText("");
                    binding.includeLayoutPartoOxytocin.viewInfusionRate.etvData.setText("");
                    binding.includeLayoutPartoOxytocin.viewInfusionStatus.tvData.setText("");

                }
            }
        });
    }

    private void setOxytocinDetails(String capturedValue, PartoLblRadioViewOxytocinBinding binding) {
        try {
            Gson gson = new Gson();
            OxytocinData oxytocinData = gson.fromJson(capturedValue, OxytocinData.class);
            binding.includeLayoutPartoOxytocin.viewStrength.etvData.setText(oxytocinData.strength);
            binding.includeLayoutPartoOxytocin.viewInfusionRate.etvData.setText(oxytocinData.infusionRate);
            binding.includeLayoutPartoOxytocin.viewInfusionStatus.tvData.setText(oxytocinData.infusionStatus);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    private void showOxytocinOptionsDetails(String title, ParamInfo info, View view, PartoLblRadioViewOxytocinBinding binding) {
        binding.includeLayoutPartoOxytocin.viewStrength.tvParamName.setText(R.string.strength_unit);
        binding.includeLayoutPartoOxytocin.viewInfusionRate.tvParamName.setText(R.string.iv_infusion_rate);
        binding.includeLayoutPartoOxytocin.viewInfusionStatus.tvParamName.setText(R.string.iv_infusion_status);
        strengthOxytocin = binding.includeLayoutPartoOxytocin.viewStrength.etvData;
        ivInfusionRateOxytocin = binding.includeLayoutPartoOxytocin.viewInfusionRate.etvData;
        ivInfusionStatusOxytocin = binding.includeLayoutPartoOxytocin.viewInfusionStatus.tvData;

        TextView selected = view.findViewById(R.id.tvSelectedValue);
        binding.includeLayoutPartoOxytocin.viewStrength.etvData.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        binding.includeLayoutPartoOxytocin.viewStrength.etvData.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.includeLayoutPartoOxytocin.viewInfusionRate.etvData.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        binding.includeLayoutPartoOxytocin.viewInfusionRate.etvData.setInputType(InputType.TYPE_CLASS_NUMBER);

        binding.includeLayoutPartoOxytocin.viewInfusionStatus.getRoot().setOnClickListener(v -> {
            //show infusion status
            // showIVFluidInfusionStatusDialog(title, info, view);
            showSingleSelectionDialog(title, info, view);

        });

        binding.includeLayoutPartoOxytocin.viewStrength.etvData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    saveOxytocinDataInJson(info, s.toString(), OxytocinDetails.strength.name());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.includeLayoutPartoOxytocin.viewInfusionRate.etvData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    saveOxytocinDataInJson(info, s.toString(), OxytocinDetails.infusionRate.name());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void manageSelectionSingleChoiceSelection(ParamInfo info, SingChoiceItem item) {
        if (info.getConceptUUID().equals(UuidDictionary.IV_FLUIDS)) { //for infusion status -iv fluid
            ivInfusionStatus.setText(item.getItem());
            saveIvFluidDataInJson(info, item.getItem(), IvFluidTypes.infusionStatus.name());
        } else if (info.getConceptUUID().equals(UuidDictionary.OXYTOCIN_UL_DROPS_MIN)) { //for oxytocin
            ivInfusionStatusOxytocin.setText(item.getItem());
            saveOxytocinDataInJson(info, item.getItem(), IvFluidTypes.infusionStatus.name());
        }
    }

    private void showMedicineDialog(String title, ParamInfo info, View tempView) {
        //PartoLablRadioViewMedicineBinding binding = PartoLablRadioViewMedicineBinding.inflate(LayoutInflater.from(mContext));
        MedicineDetailsBinding binding = MedicineDetailsBinding.inflate(LayoutInflater.from(mContext));
        binding.viewListOfMedicines.tvParamName.setText(R.string.list_of_medicines);
        binding.viewStrength.tvParamName.setText(R.string.strength);
        binding.viewDosage.tvParamName.setText(R.string.dosage);
        binding.viewRoute.tvParamName.setText(R.string.route);
        binding.viewFrequency.tvParamName.setText(R.string.frequency);

        TextView medicineName = binding.viewListOfMedicines.tvData;
        TextView medicineStrength = binding.viewStrength.etvData;
        TextView medicineDosage = binding.viewDosage.etvData;
        TextView medicineRoute = binding.viewRoute.tvData;
        TextView medicineFrequency = binding.viewFrequency.tvData;

        // medicineRoute.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        //ivInfusionRate.setInputType(InputType.TYPE_CLASS_NUMBER);


        // binding.setItems(info.getOptions());
        TextView selected = tempView.findViewById(R.id.tvSelectedValue);
//        TextView selected = view.findViewById(R.id.tvData);
        //binding.etOtherFluid.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        //binding.etOtherFluid.setInputType(InputType.TYPE_CLASS_TEXT);
        binding.viewListOfMedicines.getRoot().setOnClickListener(v -> {
            //show list of medicines

        });
        binding.viewRoute.getRoot().setOnClickListener(v -> {
            Toast.makeText(mContext, "clickedddd", Toast.LENGTH_SHORT).show();
            //show dialog for route
            final String[] items = {"Oral", "Sublingual (SL)", "Intravenous (IV)", "IV Infusion", "Intramuscular (IM)", "Subcutaneous (SC)", "Intradermal (ID)"};
            ArrayList<SingChoiceItem> choiceItems = new ArrayList<>();
            for (int i = 0; i < items.length; i++) {
                SingChoiceItem item = new SingChoiceItem();
                item.setItemIndex(i);
                item.setItem(items[i]);
                choiceItems.add(item);
            }

            String titleDialog = "Select for " + title;
            SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(mContext)
                    .title(titleDialog)
                    .content(choiceItems)
                    .build();

            dialog.setListener(item -> {
                medicineRoute.setText(item.getItem());
                //manageSelectionSingleChoiceSelection(info, item);
            });
            dialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), dialog.getClass().getCanonicalName());
        });
        binding.viewFrequency.getRoot().setOnClickListener(v -> {
            //show dialog for frequency
        });
       /* binding.includeLayoutPartoMedicine.getRoot().setClickListener(v -> {
            info.setCapturedValue(((TextView) v).getText().toString());
            String text = ((TextView) v).getText().toString();
            Log.d(TAG, "showMedicineDialog:text ::  "+text);
            binding.etDosage.setText("");
            binding.etStrength.setText("");
            selected.setText(info.getCapturedValue());
            //selected.setVisibility(View.VISIBLE);
            //ivTypeValue.setText(((TextView) v).getText().toString());
            Log.d(TAG, "showIVFluidDialog: cchheck " + ((TextView) v).getText().toString());
           // saveIvFluidDataInJson(info, ((TextView) v).getText().toString(), IvFluidTypes.type.name());

        });*/

        CustomViewDialogFragment dialog = new CustomViewDialogFragment.Builder(mContext)
                .title(title)
                .positiveButtonLabel(R.string.save_button)
                .negativeButtonLabel(R.string.cancel)
                .view(binding.getRoot())
                .build();

        dialog.requireValidationBeforeDismiss(true);
        dialog.setListener(new CustomViewDialogFragment.OnConfirmationActionListener() {
            @Override
            public void onAccept() {
                /*if (TextUtils.isEmpty(info.getCapturedValue()) && TextUtils.isEmpty(binding.etStrength.getText()) && TextUtils.isEmpty(binding.etDosage.getText())) {
                    Toast.makeText(mContext, "Please choose the any one option", Toast.LENGTH_LONG).show();
                } else if (!TextUtils.isEmpty(binding.etDosage.getText())) {
                    String otherFluidValue = binding.etDosage.getText().toString();
                    info.setCapturedValue(otherFluidValue);
                    selected.setText(otherFluidValue);
                    ivTypeValue.setText(otherFluidValue);
                    Log.d(TAG, "custom dialog: otherval :  " + otherFluidValue);
                    saveIvFluidDataInJson(info, otherFluidValue, IvFluidTypes.type.name());

                    if (binding.etDosage.getText().toString().length() > 0)
                        // selected.setVisibility(View.VISIBLE);
                        dialog.dismiss();

                    *//*if (info.getCapturedValue() != null && info.getCapturedValue().length() > 0)
                        selected.setVisibility(View.VISIBLE);
                    dialog.dismiss();*//*
                } else dialog.dismiss();*/
            }

            @Override
            public void onDecline() {
                RadioGroup radioGroup = tempView.findViewById(R.id.radioYesNoGroup);
                 /*if(selected.getTag() != null) {
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
                selected.setText(info.getCapturedValue());*/
            }
        });

        dialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), dialog.getClass().getCanonicalName());

/*
        binding.etOtherFluid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0)
                    uncheckAllOptions(binding);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
*/
    }

    private class IvFluidData {
        @SerializedName("type")
        private String type;

        @SerializedName("infusionRate")
        private String infusionRate;

        @SerializedName("infusionStatus")
        private String infusionStatus;
    }

    private class OxytocinData {
        @SerializedName("strength")
        private String strength;

        @SerializedName("infusionRate")
        private String infusionRate;

        @SerializedName("infusionStatus")
        private String infusionStatus;
    }

}