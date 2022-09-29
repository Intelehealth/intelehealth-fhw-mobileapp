package org.intelehealth.app.activities.householdSurvey;

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
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.householdSurvey.model.AnswerValue;
import org.intelehealth.app.activities.householdSurvey.model.Questions;
import org.intelehealth.app.utilities.LocaleHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SurveyCommonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<Questions> mItemList = new ArrayList<Questions>();

    public interface OnItemSelection {
        public void onSelect(Questions questions);
    }

    private OnItemSelection mOnItemSelection;

    public SurveyCommonAdapter(Context context, RecyclerView recyclerView, List<Questions> itemList, OnItemSelection onItemSelection) {
        mContext = context;
        mItemList = itemList;
        mOnItemSelection = onItemSelection;

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.survey_list_item_view, parent, false);
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
            genericViewHolder.questions = mItemList.get(position);
            genericViewHolder.selectedIndex = position;
            genericViewHolder.tvSectionNameTextView.setText(LocaleHelper.isArabic(mContext) ? genericViewHolder.questions.getQuestionAr() : genericViewHolder.questions.getQuestion());
            genericViewHolder.containerLinearLayout.removeAllViews();
            String dataType = genericViewHolder.questions.getDataType();
            if (dataType.equalsIgnoreCase("text")
                    || dataType.equalsIgnoreCase("currency")
                    || dataType.equalsIgnoreCase("int")
                    || dataType.equalsIgnoreCase("float")) {
                View tempView = View.inflate(mContext, R.layout.survey_lbl_etv_view, null);
                showUserInputBox(tempView, position, dataType);
                genericViewHolder.containerLinearLayout.addView(tempView);
            } else if (dataType.equalsIgnoreCase("single_choice") || dataType.equalsIgnoreCase("multi_choice")) {
                View tempView = View.inflate(mContext, R.layout.survey_lbl_dropdown_view, null);
                showListOptions(tempView, position, dataType);
                genericViewHolder.containerLinearLayout.addView(tempView);
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
        Questions questions;
        int selectedIndex;

        GenericViewHolder(View itemView) {
            super(itemView);
            tvSectionNameTextView = itemView.findViewById(R.id.tvSectionName);
            containerLinearLayout = itemView.findViewById(R.id.llContainer);
        }

    }


    private void showUserInputBox(final View tempView, final int position, final String paramDateType) {
        TextView paramNameTextView = tempView.findViewById(R.id.tvParamName);
        EditText dataEditText = tempView.findViewById(R.id.etvData);
        dataEditText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        paramNameTextView.setText(LocaleHelper.isArabic(mContext) ? mItemList.get(position).getQuestionAr() : mItemList.get(position).getQuestion());
        if (mItemList.get(position).getAnswerValue() != null) {
            dataEditText.setText(String.valueOf(LocaleHelper.isArabic(mContext) ? mItemList.get(position).getAnswerValue().getArValue() : mItemList.get(position).getAnswerValue().getEnValue()));
            dataEditText.setSelection(dataEditText.getText().toString().length());
        } else {
            dataEditText.setText("");
        }


        if (paramDateType.equalsIgnoreCase("text")) {
            dataEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        } else {

            if (paramDateType.equalsIgnoreCase("currency")) {
                dataEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                // dataEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
            } else if (paramDateType.equalsIgnoreCase("float")) {
                //dataEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                dataEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            } else if (paramDateType.equalsIgnoreCase("int")) {
                //dataEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
                dataEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else {
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
                String text = s.toString().trim();
                AnswerValue answerValue = new AnswerValue();
                answerValue.setEnValue(text);
                answerValue.setArValue(text);
                mItemList.get(position).setAnswerValue(answerValue);
            }
        });
    }

    private void showListOptions(final View tempView, final int position, final String paramDateType) {
        TextView paramNameTextView = tempView.findViewById(R.id.tvParamName);
        TextView dropdownTextView = tempView.findViewById(R.id.tvData);
        String query = LocaleHelper.isArabic(mContext) ? mItemList.get(position).getQuestionAr() : mItemList.get(position).getQuestion();
        paramNameTextView.setText(query);
        if (mItemList.get(position).getAnswerValue() != null) {
            dropdownTextView.setText(LocaleHelper.isArabic(mContext) ? mItemList.get(position).getAnswerValue().getArValue() : mItemList.get(position).getAnswerValue().getEnValue());
        }

        dropdownTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = new String[mItemList.get(position).getOptions().size()];
                for (int i = 0; i < mItemList.get(position).getOptions().size(); i++) {
                    items[i] = LocaleHelper.isArabic(mContext) ? mItemList.get(position).getOptions().get(i).getTextAr() : mItemList.get(position).getOptions().get(i).getText();
                }

                AlertDialog.Builder builder =
                        new AlertDialog.Builder(mContext);

                builder.setTitle(mContext.getString(R.string.select_for, query))
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                AnswerValue answerValue = new AnswerValue();
                                answerValue.setArValue(mItemList.get(position).getOptions().get(which).getTextAr());
                                answerValue.setEnValue(mItemList.get(position).getOptions().get(which).getText());
                                mItemList.get(position).setAnswerValue(answerValue);

                                dropdownTextView.setText(LocaleHelper.isArabic(mContext) ? mItemList.get(position).getAnswerValue().getArValue() : mItemList.get(position).getAnswerValue().getEnValue());

                                dialog.dismiss();
                            }
                        });


                builder.create().show();
            }
        });

    }
}