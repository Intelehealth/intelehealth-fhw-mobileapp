package org.intelehealth.app.partogram.adapter;

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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.partogram.PartogramConstants;
import org.intelehealth.app.partogram.model.ParamInfo;
import org.intelehealth.app.partogram.model.PartogramItemData;

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
                .inflate(R.layout.parto_list_item_view, parent, false);
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
                        || paramInfo.getParamDateType().equalsIgnoreCase(PartogramConstants.INPUT_INT_3_DIG_TYPE)
                ) {
                    View tempView = View.inflate(mContext, R.layout.parto_lbl_etv_view, null);
                    showUserInputBox(tempView, position, i, paramInfo.getParamDateType());
                    genericViewHolder.containerLinearLayout.addView(tempView);
                } else if (paramInfo.getParamDateType().equalsIgnoreCase(PartogramConstants.DROPDOWN_SINGLE_SELECT_TYPE)) {
                    View tempView = View.inflate(mContext, R.layout.parto_lbl_dropdown_view, null);
                    showListOptions(tempView, position, i);
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


    private void showUserInputBox(final View tempView, final int position, final int positionChild, final String paramDateType) {
        TextView paramNameTextView = tempView.findViewById(R.id.tvParamName);
        EditText dataEditText = tempView.findViewById(R.id.etvData);
        paramNameTextView.setText(mItemList.get(position).getParamInfoList().get(positionChild).getParamName());
        if(mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue()!=null && !mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue().isEmpty()){
            dataEditText.setText(mItemList.get(position).getParamInfoList().get(positionChild).getOptions()[Arrays.asList(mItemList.get(position).getParamInfoList().get(positionChild).getValues()).indexOf(mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue())]);
        }
        if (paramDateType.equalsIgnoreCase(PartogramConstants.INPUT_TXT_TYPE)) {
            dataEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        } else {

            if (paramDateType.equalsIgnoreCase(PartogramConstants.INPUT_DOUBLE_4_DIG_TYPE)) {
                dataEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
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

            }

            @Override
            public void afterTextChanged(Editable s) {
                mItemList.get(position).getParamInfoList().get(positionChild).setCapturedValue(s.toString().trim());
            }
        });
    }

    private void showListOptions(final View tempView, final int position, final int positionChild) {
        TextView paramNameTextView = tempView.findViewById(R.id.tvParamName);
        TextView dropdownTextView = tempView.findViewById(R.id.tvData);
        paramNameTextView.setText(mItemList.get(position).getParamInfoList().get(positionChild).getParamName());
        if(mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue()!=null && !mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue().isEmpty()){
            dropdownTextView.setText(mItemList.get(position).getParamInfoList().get(positionChild).getOptions()[Arrays.asList(mItemList.get(position).getParamInfoList().get(positionChild).getValues()).indexOf(mItemList.get(position).getParamInfoList().get(positionChild).getCapturedValue())]);
        }
        dropdownTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = mItemList.get(position).getParamInfoList().get(positionChild).getOptions();

                AlertDialog.Builder builder =
                        new AlertDialog.Builder(mContext);

                builder.setTitle("Select for " + mItemList.get(position).getParamInfoList().get(positionChild).getParamName())
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dropdownTextView.setText(mItemList.get(position).getParamInfoList().get(positionChild).getOptions()[which]);
                                mItemList.get(position).getParamInfoList().get(positionChild).setCapturedValue(mItemList.get(position).getParamInfoList().get(positionChild).getValues()[which]);
                                dialog.dismiss();
                            }
                        });


                builder.create().show();
            }
        });

    }
}

