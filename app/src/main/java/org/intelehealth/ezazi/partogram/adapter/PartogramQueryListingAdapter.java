package org.intelehealth.ezazi.partogram.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.model.ParamInfo;
import org.intelehealth.ezazi.partogram.model.PartogramItemData;
import org.intelehealth.ezazi.ui.dialog.SingleChoiceDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PartogramQueryListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<PartogramItemData> mItemList = new ArrayList<PartogramItemData>();

    public interface OnItemSelection {
        public void onSelect(PartogramItemData partogramItemData);
    }

    private OnItemSelection mOnItemSelection;

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
                }
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
            /*if (mItemList.get(position).getParamInfoList().get(positionChild).getParamName().equalsIgnoreCase("Initial")) {
                String[] initials = new SessionManager(mContext).getChwname().split(" ");
                String name = "";
                if (initials.length >= 2) {
                    name = initials[0].substring(0, 1) + "" + initials[1].substring(0, 1);
                } else {
                    name = initials[0].substring(0, 2);
                }
                mItemList.get(position).getParamInfoList().get(positionChild).setCapturedValue(name.toUpperCase());
                dataEditText.setText(name.toUpperCase());
            }*/
        }


        if (paramDateType.equalsIgnoreCase(PartogramConstants.INPUT_TXT_TYPE)) {
            dataEditText.setInputType(InputType.TYPE_CLASS_TEXT);
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
            }
        });
    }

    private void showListOptions(final View tempView, final int position, final int positionChild) {
        TextView paramNameTextView = tempView.findViewById(R.id.tvParamName);
        TextView dropdownTextView = tempView.findViewById(R.id.tvData);
        paramNameTextView.setText(mItemList.get(position).getParamInfoList().get(positionChild).getParamName());
        if (mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue() != null &&
                !mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue().isEmpty()) {
            dropdownTextView.setText(mItemList.get(position).getParamInfoList().get(positionChild).getOptions()
                    [Arrays.asList(mItemList.get(position).getParamInfoList().get(positionChild).getValues())
                    .indexOf(mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue())]);
        }

        dropdownTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = mItemList.get(position).getParamInfoList().get(positionChild).getOptions();
                String title = "Select for " + mItemList.get(position).getParamInfoList().get(positionChild).getParamName();
                SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(mContext)
                        .title(title)
                        .content(Arrays.asList(items))
                        .build();

                dialog.setListener((pos, value) -> {
                    dropdownTextView.setText(mItemList.get(position).getParamInfoList().get(positionChild).getOptions()[pos]);
                    mItemList.get(position).getParamInfoList().get(positionChild).setCapturedValue(mItemList.get(position).getParamInfoList().get(positionChild).getValues()[pos]);
                });

                dialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), dialog.getClass().getCanonicalName());

//                AlertDialog.Builder builder =
//                        new AlertDialog.Builder(mContext);
//
//                builder.setTitle("Select for " + mItemList.get(position).getParamInfoList().get(positionChild).getParamName())
//                        .setItems(items, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dropdownTextView.setText(mItemList.get(position).getParamInfoList().get(positionChild).getOptions()[which]);
//                                mItemList.get(position).getParamInfoList().get(positionChild).setCapturedValue(mItemList.get(position).getParamInfoList().get(positionChild).getValues()[which]);
//                                dialog.dismiss();
//                            }
//                        });
//
//
//                builder.create().show();
            }
        });

    }
}