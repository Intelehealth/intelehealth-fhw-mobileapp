package org.intelehealth.app.ayu.visit.common.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.common.OnItemSelection;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.ayu.visit.model.ComplainBasicInfo;
import org.intelehealth.app.ayu.visit.reason.adapter.OptionsChipsGridAdapter;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.knowledgeEngine.PhysicalExam;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.WindowsUtils;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class NestedQuestionsListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = "NestedQuestionsListingAdapter";
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<Node> mItemList = new ArrayList<Node>();
    private List<Node> mSuperItemList = new ArrayList<Node>();
    //private int mTotalQuery = 0;
    RecyclerView mRecyclerView, mRootRecyclerView;
    private int mLastImageCaptureSelectedNodeIndex = 0;
    private boolean mIsEditMode;
    private String engineVersion;

    public String getEngineVersion() {
        Log.v(TAG, "engineVersion - " + engineVersion);
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        Log.v(TAG, "setEngineVersion - " + engineVersion);
        this.engineVersion = engineVersion;
    }


    public void addImageInLastNode(String image) {
        mItemList.get(mLastImageCaptureSelectedNodeIndex).getImagePathList().add(image);
        Log.v("showCameraView", "ImageCaptured mLastImageCaptureSelectedNodeIndex - " + mLastImageCaptureSelectedNodeIndex);
        Log.v("showCameraView", "ImageCaptured - " + new Gson().toJson(mItemList.get(mLastImageCaptureSelectedNodeIndex)));
        notifyItemChanged(mLastImageCaptureSelectedNodeIndex);
    }

    public void removeImageInLastNode(int index, String image) {
        mItemList.get(mLastImageCaptureSelectedNodeIndex).getImagePathList().remove(index);
        notifyItemChanged(mLastImageCaptureSelectedNodeIndex);
    }

    public void setSuperNodeList(List<Node> nodes) {
        mSuperItemList = nodes;
    }

    public boolean isIsAssociateSymptomsLoaded() {
        return mIsAssociateSymptomsLoaded;
    }

    public void setAssociateSymptomsLoaded(boolean mIsAssociateSymptomsLoaded) {
        Log.v(TAG, "setAssociateSymptomsLoaded()");
        this.mIsAssociateSymptomsLoaded = mIsAssociateSymptomsLoaded;
    }


    private OnItemSelection mOnItemSelection;
    private boolean mIsForPhysicalExam;
    private PhysicalExam mPhysicalExam;
    private HashMap<Integer, ComplainBasicInfo> mRootComplainBasicInfoHashMap = new HashMap<>();
    private int mRootIndex = 0;
    private boolean mIsAssociateSymptomsLoaded = false;
    private boolean mIsAssociateSymptomsNestedQuery = false;
    private HashMap<Integer, Integer> mIndexMappingHashMap = new HashMap<>();

    private int mNestedLevel = 0;
    private int mNodeLevel = 0;
    private Node mParentNode;


    public NestedQuestionsListingAdapter(Context context, RecyclerView rootRecyclerView, RecyclerView recyclerView, Node parentNode, int nestedLevel, int rootIndex, boolean editMode, OnItemSelection onItemSelection) {
        mContext = context;
//        mIsForPhysicalExam = isPhyExam;
//        mPhysicalExam = physicalExam;
        mRootRecyclerView = rootRecyclerView;
        mRecyclerView = recyclerView;
        mOnItemSelection = onItemSelection;
        mNestedLevel = nestedLevel;
        //mTotalQuery = totalQuery;
        mRootIndex = rootIndex;
        mParentNode = parentNode;
        mIsEditMode = editMode;
        //mRootComplainBasicInfoHashMap = complainBasicInfoHashMap;
        //mAnimator = new RecyclerViewAnimator(recyclerView);
        Log.v(TAG, "new NestedQuestionsListingAdapter created!");
    }

    public void setRootNodeIndex(int rootIndex) {
        Log.v(TAG, "setRootNodeIndex()");
        mRootIndex = rootIndex;
    }

    public void setAssociateSymptomNestedQueryFlag(boolean isAssociateSymptom) {
        Log.v(TAG, "setAssociateSymptomNestedQueryFlag()");
        mIsAssociateSymptomsNestedQuery = isAssociateSymptom;
    }

    private JSONObject mThisScreenLanguageJsonObject = new JSONObject();

    public void clearItems() {
        mItemList.clear();
    }

    public void addItem(Node node) {
        Log.v(TAG, "addItem()");
        for (int i = 0; i < mItemList.size(); i++) {
            if (mItemList.get(i).getId().equalsIgnoreCase(node.getId())) {
                return;
            }
        }
        mItemList.add(node);
        mIndexMappingHashMap.put(mItemList.size() - 1, mRootIndex);
        notifyItemInserted(mItemList.size() - 1);
    }

    public void addItemAll(List<Node> nodes) {
        Log.v(TAG, "addItemAll nodes - " + nodes.size());
        mItemList = nodes;
        notifyDataSetChanged();
    }

    public List<Node> geItems() {
        return mItemList;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ui2_nested_question_item_view, parent, false);
        /**
         * First item's entrance animations.
         */
        //mAnimator.onCreateViewHolder(itemView);

        return new GenericViewHolder(itemView);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int rawPosition) {
        if (holder instanceof GenericViewHolder) {
            GenericViewHolder genericViewHolder = (GenericViewHolder) holder;
            int position = genericViewHolder.getAbsoluteAdapterPosition();
            genericViewHolder.node = mItemList.get(position);
            genericViewHolder.index = position;

            genericViewHolder.singleComponentContainer.removeAllViews();
            genericViewHolder.singleComponentContainer.setVisibility(View.GONE);
            genericViewHolder.optionRecyclerView.setVisibility(View.GONE);
            genericViewHolder.superNestedRecyclerView.setVisibility(View.GONE);
            genericViewHolder.submitButton.setVisibility(View.GONE);
            genericViewHolder.skipButton.setVisibility(View.GONE);

            genericViewHolder.submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, genericViewHolder.node.isDataCaptured() ? R.drawable.ic_baseline_check_18_white : 0, 0);
            genericViewHolder.submitButton.setBackgroundResource(genericViewHolder.node.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);

            // show know more link if its available
            if (genericViewHolder.node.getPop_up() != null && !genericViewHolder.node.getPop_up().isEmpty()) {
                genericViewHolder.knowMoreTextView.setVisibility(View.VISIBLE);

            } else {
                genericViewHolder.knowMoreTextView.setVisibility(View.GONE);
            }

            genericViewHolder.tvQuestion.setText(genericViewHolder.node.findDisplay());

            routeByType(genericViewHolder, mParentNode, genericViewHolder.node, position, true, false);
            setTextViewDrawableColor(genericViewHolder.tvQuestion, mColors[0]);

        }
    }

    private int[] mColors = new int[]{R.color.colorPrimary1,
            R.color.ui2_bmi3,
            R.color.ui2_bmi4,
            R.color.ui2_bmi5,
            R.color.ui2_bmi6,
            R.color.colorPrimaryDark2,
            R.color.colorPrimaryDark2,
            R.color.colorPrimaryDark2,
            R.color.colorPrimaryDark2};

    private void setTextViewDrawableColor(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawablesRelative()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(textView.getContext(), color), PorterDuff.Mode.SRC_IN));
            }
        }
    }

    private void routeByType(GenericViewHolder genericViewHolder, Node parentNode, Node currentNode, int position, boolean isSuperNested, boolean isGotFromChipSelected) {
        Log.v(TAG, position + ". routeByType currentNode - " + new Gson().toJson(currentNode));
        String type = currentNode.getInputType();

        genericViewHolder.singleComponentContainer.setVisibility(View.VISIBLE);
        genericViewHolder.superNestedRecyclerView.removeAllViews();
        genericViewHolder.submitButton.setVisibility(View.GONE);
        genericViewHolder.skipButton.setVisibility(View.GONE);

        if (!isGotFromChipSelected) {
            genericViewHolder.tvQuestionDesc.setVisibility(View.GONE);
//            genericViewHolder.submitButton.setVisibility(View.GONE);
//            genericViewHolder.skipButton.setVisibility(View.GONE);
        }
        if (type == null || type.isEmpty() && (currentNode.getOptionsList() != null && !currentNode.getOptionsList().isEmpty())) {
            type = "options";
            genericViewHolder.singleComponentContainer.setVisibility(View.GONE);
            genericViewHolder.tvQuestionDesc.setVisibility(View.VISIBLE);
            if (!isGotFromChipSelected) {
                if (currentNode.isMultiChoice()) {
                    genericViewHolder.tvQuestionDesc.setText(mContext.getString(R.string.select_one_or_more));
                    genericViewHolder.submitButton.setVisibility(View.VISIBLE);
                    genericViewHolder.submitButton.setBackgroundResource(currentNode.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);
                    if (currentNode.isDataCaptured()) {
                        AdapterUtils.setToDisable(genericViewHolder.skipButton);
                    } else {
                        AdapterUtils.setToDefault(genericViewHolder.skipButton);
                    }
                } else {
                    genericViewHolder.tvQuestionDesc.setText(mContext.getString(R.string.select_any_one));
                    genericViewHolder.submitButton.setVisibility(View.GONE);
                    if (currentNode.isDataCaptured()) {
                        AdapterUtils.setToDisable(genericViewHolder.skipButton);
                    } else {
                        AdapterUtils.setToDefault(genericViewHolder.skipButton);
                    }
                }
                if (currentNode.isRequired()) {
                    genericViewHolder.skipButton.setVisibility(View.GONE);
                } else {
                    genericViewHolder.skipButton.setVisibility(View.VISIBLE);
                }
            }
        }

        Log.v(TAG, "onBindViewHolder Type - " + type);
        Log.v(TAG, "onBindViewHolder Node - " + new Gson().toJson(currentNode));

        switch (type) {
            case "text":
                genericViewHolder.singleComponentContainer.setVisibility(View.VISIBLE);
                addTextEnterView(parentNode, currentNode, genericViewHolder.singleComponentContainer, position);
                break;
            case "date":
                addDateView(genericViewHolder, parentNode, currentNode, genericViewHolder.singleComponentContainer, position);
                break;
            case "location":
                //askLocation(questionNode, context, adapter);
                genericViewHolder.singleComponentContainer.setVisibility(View.VISIBLE);
                addTextEnterView(parentNode, currentNode, genericViewHolder.singleComponentContainer, position);
                break;
            case "number":
                addNumberView(parentNode, currentNode, genericViewHolder.singleComponentContainer, position);
                break;
            case "area":
                // askArea(questionNode, context, adapter);
                genericViewHolder.singleComponentContainer.setVisibility(View.VISIBLE);
                addTextEnterView(parentNode, currentNode, genericViewHolder.singleComponentContainer, position);
                break;
            case "duration":
                addDurationView(parentNode, currentNode, genericViewHolder.singleComponentContainer, position);
                break;
            case "range":
                addRangeView(parentNode, currentNode, genericViewHolder.singleComponentContainer, position);
                break;
            case "frequency":
                addFrequencyView(parentNode, currentNode, genericViewHolder.singleComponentContainer, position);
                break;
            case "camera":
                showCameraView(parentNode, currentNode, genericViewHolder.singleComponentContainer, position);
                break;
            case "options":

                // check for end node or custom input node
                if (currentNode.getOptionsList().size() == 1 &&
                        (currentNode.getOptionsList().get(0).getOptionsList() == null || currentNode.getOptionsList().get(0).getOptionsList().isEmpty())) {
                    routeByType(genericViewHolder, currentNode, currentNode.getOptionsList().get(0), position, isSuperNested, false);
                } else {
                    showOptionsData(currentNode, genericViewHolder, currentNode.getOptionsList(), position, isSuperNested, isGotFromChipSelected);
                }
                break;
        }
    }

    private int getCount(int adapterPosition, int rootIndex) {
        int count = 0;
        //> mRootComplainBasicInfoHashMap.get(mRootIndex).getOptionSize() ? position - mRootComplainBasicInfoHashMap.get(mRootIndex).getOptionSize() - 1 : position + 1)
        if (rootIndex == 0) {
            count = adapterPosition + 1;
        } else {
            int completedCount = 0;
            for (int i = rootIndex - 1; i >= 0; i--) {
                completedCount += mRootComplainBasicInfoHashMap.get(i).getOptionSize();
            }
            count = adapterPosition + 1 - completedCount;
        }
        return count;
    }

    private void addRangeView(Node parentNode, Node node, LinearLayout containerLayout, int index) {
        containerLayout.removeAllViews();
        View view = View.inflate(mContext, R.layout.ui2_visit_number_range, null);
        RangeSlider rangeSlider = view.findViewById(R.id.range_slider);
        //rangeSlider.setLabelBehavior(LABEL_ALWAYS_VISIBLE); //Label always visible" nothing yet ?
        TextView rangeTextView = view.findViewById(R.id.btn_values);
        TextView submitTextView = view.findViewById(R.id.btn_submit);

        Button skipButton = view.findViewById(R.id.btn_skip);
        /*if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);*/
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setSelected(false);
                parentNode.setSelected(false);
                parentNode.setDataCaptured(false);
                mOnItemSelection.onSelect(node, mRootIndex, true, parentNode);
                notifyItemChanged(index);
            }
        });

        if (node.getLanguage() != null && !node.getLanguage().isEmpty() && !node.getLanguage().equalsIgnoreCase("%")
                && node.getLanguage().equalsIgnoreCase(" to ")) {
            String[] vals = node.getLanguage().split(" to ");
            rangeTextView.setText(vals[0] + " " + mContext.getString(R.string.to) + " " + vals[1]);
            List<Float> list = new ArrayList<>();
            list.add(Float.valueOf(vals[0]));
            list.add(Float.valueOf(vals[1]));
            rangeSlider.setValues(list);
        }
        submitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rangeTextView.getText().toString().equalsIgnoreCase("---")) {
                    Toast.makeText(mContext, mContext.getString(R.string.please_select_range), Toast.LENGTH_SHORT).show();
                } else {
                    List<Float> values = rangeSlider.getValues();
                    int x = values.get(0).intValue();
                    int y = values.get(1).intValue();
                    String durationString = x + " " + mContext.getString(R.string.to) + " " + y;
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", durationString));
                    } else {
                        node.addLanguage(" " + durationString);
                        node.setText(durationString);
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    node.setSelected(true);
                    parentNode.setSelected(true);
                    parentNode.setDataCaptured(true);
                    notifyItemChanged(index);
                    mOnItemSelection.onSelect(node, mRootIndex, false, parentNode);
                }
            }
        });
        rangeSlider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
        rangeSlider.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {

            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {
                List<Float> values = rangeSlider.getValues();
                int x = values.get(0).intValue();
                int y = values.get(1).intValue();
                rangeTextView.setText(String.format(x + " " + mContext.getString(R.string.to) + " " + y));
            }
        });
        containerLayout.addView(view);
    }

    private void addFrequencyView(Node parentNode, Node node, LinearLayout containerLayout, int index) {
        containerLayout.removeAllViews();
        final View view = View.inflate(mContext, R.layout.ui2_visit_number_slider_with_icon, null);
        Slider rangeSlider = view.findViewById(R.id.number_slider);
        //rangeSlider.setLabelBehavior(LABEL_ALWAYS_VISIBLE); //Label always visible" nothing yet ?
        TextView rangeTextView = view.findViewById(R.id.btn_values);
        TextView submitTextView = view.findViewById(R.id.btn_submit);

        Button skipButton = view.findViewById(R.id.btn_skip);
       /* if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);*/
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setSelected(false);
                parentNode.setSelected(false);
                parentNode.setDataCaptured(false);
                mOnItemSelection.onSelect(node, mRootIndex, true, parentNode);
                notifyItemChanged(index);
            }
        });


        if (node.getLanguage() != null && !node.getLanguage().isEmpty() && !node.getLanguage().equalsIgnoreCase("%") && TextUtils.isDigitsOnly(node.getLanguage())) {
            int i = Integer.parseInt(node.getLanguage());
            rangeTextView.setText(mContext.getString(R.string.level) + " " + i);
            rangeSlider.setValue(i);
        }
        submitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rangeTextView.getText().toString().equalsIgnoreCase("---")) {
                    Toast.makeText(mContext, mContext.getString(R.string.drag_to_select), Toast.LENGTH_SHORT).show();
                } else {
                    int x = (int) rangeSlider.getValue();
                    String durationString = String.valueOf(x);
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", durationString));
                    } else {
                        node.addLanguage(" " + durationString);
                        node.setText(durationString);
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    node.setSelected(true);
                    parentNode.setSelected(true);
                    parentNode.setDataCaptured(true);
                    notifyItemChanged(index);
                    mOnItemSelection.onSelect(node, mRootIndex, false, parentNode);
                }
            }
        });
        rangeSlider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
        rangeSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                int x = (int) rangeSlider.getValue();
                rangeTextView.setText(mContext.getString(R.string.level) + " " + x);
                updateCustomEmojiSliderUI(view, x);
            }
        });

        updateCustomEmojiSliderUI(view, 0);
        containerLayout.addView(view);
    }

    private void updateCustomEmojiSliderUI(View view, int range) {
        TextView tv0 = view.findViewById(R.id.n0_tv);
        TextView tv1 = view.findViewById(R.id.n1_tv);
        TextView tv2 = view.findViewById(R.id.n2_tv);
        TextView tv3 = view.findViewById(R.id.n3_tv);
        TextView tv4 = view.findViewById(R.id.n4_tv);
        TextView tv5 = view.findViewById(R.id.n5_tv);
        TextView tv6 = view.findViewById(R.id.n6_tv);
        TextView tv7 = view.findViewById(R.id.n7_tv);
        TextView tv8 = view.findViewById(R.id.n8_tv);
        TextView tv9 = view.findViewById(R.id.n9_tv);
        TextView tv10 = view.findViewById(R.id.n10_tv);

        ImageView i0 = view.findViewById(R.id.n0_imv);
        ImageView i1 = view.findViewById(R.id.n1_imv);
        ImageView i2 = view.findViewById(R.id.n2_imv);
        ImageView i3 = view.findViewById(R.id.n3_imv);
        ImageView i4 = view.findViewById(R.id.n4_imv);
        ImageView i5 = view.findViewById(R.id.n5_imv);
        ImageView i6 = view.findViewById(R.id.n6_imv);
        ImageView i7 = view.findViewById(R.id.n7_imv);
        ImageView i8 = view.findViewById(R.id.n8_imv);
        ImageView i9 = view.findViewById(R.id.n9_imv);
        ImageView i10 = view.findViewById(R.id.n10_imv);

        // set default values
        tv0.setTextColor(mContext.getResources().getColor(R.color.gray_3));
        tv0.setTextSize(14);
        tv0.setTypeface(tv0.getTypeface(), Typeface.NORMAL);

        tv1.setTextColor(mContext.getResources().getColor(R.color.gray_3));
        tv1.setTextSize(14);
        tv1.setTypeface(tv1.getTypeface(), Typeface.NORMAL);

        tv2.setTextColor(mContext.getResources().getColor(R.color.gray_3));
        tv2.setTextSize(14);
        tv2.setTypeface(tv2.getTypeface(), Typeface.NORMAL);

        tv3.setTextColor(mContext.getResources().getColor(R.color.gray_3));
        tv3.setTextSize(14);
        tv3.setTypeface(tv3.getTypeface(), Typeface.NORMAL);

        tv4.setTextColor(mContext.getResources().getColor(R.color.gray_3));
        tv4.setTextSize(14);
        tv4.setTypeface(tv4.getTypeface(), Typeface.NORMAL);

        tv5.setTextColor(mContext.getResources().getColor(R.color.gray_3));
        tv5.setTextSize(14);
        tv5.setTypeface(tv5.getTypeface(), Typeface.NORMAL);

        tv6.setTextColor(mContext.getResources().getColor(R.color.gray_3));
        tv6.setTextSize(14);
        tv6.setTypeface(tv6.getTypeface(), Typeface.NORMAL);

        tv7.setTextColor(mContext.getResources().getColor(R.color.gray_3));
        tv7.setTextSize(14);
        tv7.setTypeface(tv7.getTypeface(), Typeface.NORMAL);

        tv8.setTextColor(mContext.getResources().getColor(R.color.gray_3));
        tv8.setTextSize(14);
        tv8.setTypeface(tv8.getTypeface(), Typeface.NORMAL);

        tv9.setTextColor(mContext.getResources().getColor(R.color.gray_3));
        tv9.setTextSize(14);
        tv9.setTypeface(tv9.getTypeface(), Typeface.NORMAL);

        tv10.setTextColor(mContext.getResources().getColor(R.color.gray_3));
        tv10.setTextSize(14);
        tv10.setTypeface(tv10.getTypeface(), Typeface.NORMAL);

        i0.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i1.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i2.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i3.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i4.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i5.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i6.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i7.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i8.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i9.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i10.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));

        if (range == 0) {
            tv0.setTextColor(mContext.getResources().getColor(R.color.colorPrimary1));
            tv0.setTextSize(16);
            tv0.setTypeface(tv0.getTypeface(), Typeface.BOLD);

            i0.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary1));

        } else if (range == 1) {
            tv1.setTextColor(mContext.getResources().getColor(R.color.colorPrimary1));
            tv1.setTextSize(16);
            tv1.setTypeface(tv1.getTypeface(), Typeface.BOLD);

            i1.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary1));
        } else if (range == 2) {
            tv2.setTextColor(mContext.getResources().getColor(R.color.colorPrimary1));
            tv2.setTextSize(16);
            tv2.setTypeface(tv2.getTypeface(), Typeface.BOLD);

            i2.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary1));
        } else if (range == 3) {
            tv3.setTextColor(mContext.getResources().getColor(R.color.colorPrimary1));
            tv3.setTextSize(16);
            tv3.setTypeface(tv3.getTypeface(), Typeface.BOLD);

            i3.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary1));
        } else if (range == 4) {
            tv4.setTextColor(mContext.getResources().getColor(R.color.colorPrimary1));
            tv4.setTextSize(16);
            tv4.setTypeface(tv4.getTypeface(), Typeface.BOLD);

            i4.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary1));
        } else if (range == 5) {
            tv5.setTextColor(mContext.getResources().getColor(R.color.colorPrimary1));
            tv5.setTextSize(16);
            tv5.setTypeface(tv5.getTypeface(), Typeface.BOLD);

            i5.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary1));
        } else if (range == 6) {
            tv6.setTextColor(mContext.getResources().getColor(R.color.colorPrimary1));
            tv6.setTextSize(16);
            tv6.setTypeface(tv6.getTypeface(), Typeface.BOLD);

            i6.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary1));
        } else if (range == 7) {
            tv7.setTextColor(mContext.getResources().getColor(R.color.colorPrimary1));
            tv7.setTextSize(16);
            tv7.setTypeface(tv7.getTypeface(), Typeface.BOLD);

            i7.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary1));
        } else if (range == 8) {
            tv8.setTextColor(mContext.getResources().getColor(R.color.colorPrimary1));
            tv8.setTextSize(16);
            tv8.setTypeface(tv8.getTypeface(), Typeface.BOLD);

            i8.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary1));
        } else if (range == 9) {
            tv9.setTextColor(mContext.getResources().getColor(R.color.colorPrimary1));
            tv9.setTextSize(16);
            tv9.setTypeface(tv9.getTypeface(), Typeface.BOLD);

            i9.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary1));
        } else if (range == 10) {
            tv10.setTextColor(mContext.getResources().getColor(R.color.colorPrimary1));
            tv10.setTextSize(16);
            tv10.setTypeface(tv10.getTypeface(), Typeface.BOLD);

            i10.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary1));
        }
    }


    /**
     * @param selectedNode
     * @param holder
     * @param options
     * @param index
     * @param isSuperNested
     */
    private void showOptionsDataV2(final Node selectedNode, final GenericViewHolder holder, List<Node> options, int index, boolean isSuperNested, boolean isGotFromChipSelected) {
        Log.v(TAG, "showOptionsDataV2 - " + getEngineVersion());
        holder.singleComponentContainer.removeAllViews();
        holder.optionRecyclerView.setVisibility(View.VISIBLE);
        Log.v(TAG, "showOptionsDataV2 isMultiChoice - " + selectedNode.isMultiChoice());
        //mNestedLevel= mNestedLevel + 1;
        Log.v(TAG, "NestedQuestionsListingAdapter node - " + new Gson().toJson(selectedNode));
        Log.v(TAG, "NestedQuestionsListingAdapter mNestedLevel - " + mNestedLevel);
        if (!isGotFromChipSelected && (selectedNode.isHavingNestedQuestion() || selectedNode.isHavingMoreNestedQuestion())) {
            //if (isSuperNested) {
            //if(mNestedLevel%2==0){
            showNestedItemsV2(selectedNode, holder, options, index, isSuperNested, isGotFromChipSelected);
        } else {
            holder.superNestedRecyclerView.setVisibility(View.GONE);
            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(mContext);
            layoutManager.setFlexDirection(FlexDirection.ROW);
            layoutManager.setJustifyContent(JustifyContent.FLEX_START);

            holder.optionRecyclerView.setLayoutManager(layoutManager);
            //**********
            // Avoid the duplicate options asking to user in connected questions
            //**************
            String duplicateCheckNodeNames = mItemList.get(index).getCompareDuplicateNode();
            Log.v(TAG, "duplicateCheckNodeNames - " + duplicateCheckNodeNames);
            if (duplicateCheckNodeNames != null && !duplicateCheckNodeNames.isEmpty()) {
                int sourceIndex = 0;
                Node toCompareWithNode = null;
                for (int i = 0; i < mSuperItemList.size(); i++) {
                    Log.v(TAG, "toCompareWithNode - " + mSuperItemList.get(i).getText());
                    if (mSuperItemList.get(i).getText().equalsIgnoreCase(duplicateCheckNodeNames)) {
                        toCompareWithNode = mSuperItemList.get(i);
                        Log.v(TAG, "toCompareWithNode - " + new Gson().toJson(toCompareWithNode));
                        break;
                    }
                }
                NodeAdapterUtils.updateForHideShowFlag(mContext, mItemList.get(index), toCompareWithNode);
            }
            // *****************
            for (int i = 0; i < options.size(); i++) {
                options.get(i).setNestedLeve(options.get(i).getNestedLeve() + 1);
            }
            OptionsChipsGridAdapter optionsChipsGridAdapter = new OptionsChipsGridAdapter(holder.optionRecyclerView, mContext, mItemList.get(index), options, new OptionsChipsGridAdapter.OnItemSelection() {
                @Override
                public void onSelect(Node node, boolean isLoadingForNestedEditData) {
                    if (!isLoadingForNestedEditData)
                        VisitUtils.scrollNow(mRootRecyclerView, 1000, 0, 300);
                    if (!isLoadingForNestedEditData) {
                        mItemList.get(index).setSelected(false);
                        mItemList.get(index).setDataCaptured(false);
                    }
                    for (int i = 0; i < options.size(); i++) {
                        if (options.get(i).isSelected()) {
                            mItemList.get(index).setSelected(true);
                            mItemList.get(index).setDataCaptured(true);
                            break;
                        }
                    }
                    if (isLoadingForNestedEditData) {
                        if (selectedNode.isDataCaptured()) {
                            AdapterUtils.setToDisable(holder.skipButton);
                            AdapterUtils.setToDisable(holder.submitButton);
                        } else {
                            AdapterUtils.setToDefault(holder.skipButton);
                            AdapterUtils.setToDefault(holder.submitButton);
                        }
                    } else {
                        AdapterUtils.setToDefault(holder.submitButton);
                        AdapterUtils.setToDefault(holder.skipButton);

                    }
                     /*holder.submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,  0, 0);
                    holder.submitButton.setBackgroundResource(R.drawable.ui2_common_button_bg_submit);*/

                    String type = node.getInputType();

                    if (type == null || type.isEmpty() && (node.getOptionsList() != null && !node.getOptionsList().isEmpty())) {
                        type = "options";
                    }
                    Log.v(TAG, "node - " + node.getText());
                    Log.v(TAG, "node - " + node.findDisplay());
                    Log.v(TAG, "Type - " + type);
                    Log.v(TAG, "isLoadingForNestedEditData - " + isLoadingForNestedEditData);
                    if (!node.isSelected()) {
                        node.unselectAllNestedNode();
                        if (type.equalsIgnoreCase("camera"))
                            mItemList.get(index).removeImagesAllNestedNode();

                        if (!mItemList.get(index).isMultiChoice()) {

                            if (mItemList.size() > 1) {
                                mItemList.remove(1);
                                notifyItemRemoved(1);
                            } else {
                                notifyItemChanged(index);
                            }
                        } else {

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!isLoadingForNestedEditData) {
                                        // found the correct node
                                        boolean found = false;
                                        for (int i = 0; i < mItemList.size(); i++) {
                                            Node n = mItemList.get(i);
                                            Log.v(TAG, "n - " + n.getText());
                                            if (node.getText().equalsIgnoreCase(n.getText())) {
                                                found = true;
                                                mItemList.remove(i);
                                                notifyItemRemoved(i);
                                                break;
                                            }
                                        }
                                        if (!found)
                                            notifyItemChanged(index);
                                    }
                                }
                            }, 100);
                        }

                        return;
                    } else if (!type.isEmpty() && node.isSelected()) {
                        if (node.isExcludedFromMultiChoice() || !mItemList.get(index).isMultiChoice()) {
                            for (int i = 0; i < options.size(); i++) {
                                if (!options.get(i).getText().equals(node.getText())) {
                                    options.get(i).unselectAllNestedNode();
                                }
                            }
                            if (mItemList.size() > 1) {
                                mItemList.remove(1);
                                notifyItemRemoved(1);
                            }
                        }
                        //holder.singleComponentContainer.removeAllViews();
                        //holder.singleComponentContainer.setVisibility(View.VISIBLE);

                    } else {
                        holder.singleComponentContainer.removeAllViews();
                        //holder.superNestedContainerLinearLayout.removeAllViews();
                        if (mItemList.get(index).isMultiChoice()) {
                            //holder.tvQuestionDesc.setText(mContext.getString(R.string.select_one_or_more));
                            holder.submitButton.setVisibility(View.VISIBLE);
                        } else {
                            //holder.tvQuestionDesc.setText(mContext.getString(R.string.select_any_one));
                            holder.submitButton.setVisibility(View.GONE);
                            mOnItemSelection.onSelect(node, mRootIndex, false, mItemList.get(index));
                            AdapterUtils.setToDisable(holder.skipButton);
                        }

                        if (mItemList.get(index).isRequired()) {
                            holder.skipButton.setVisibility(View.GONE);
                        } else {
                            holder.skipButton.setVisibility(View.VISIBLE);
                        }
                        return;
                    }

                    if (type.equals("options")) {
                        addItem(node);
                        //showNestedItemsV2(node, holder, node.getOptionsList(), index, false, false);
                    } else {
                        routeByType(holder, selectedNode, node, index, true, true);
                    }
                }
            });
            holder.optionRecyclerView.setAdapter(optionsChipsGridAdapter);
        }

            /*for (int i = 0; i < options.size(); i++) {
                String type = options.get(i).getInputType();
                if (type.equalsIgnoreCase("camera") && options.get(i).isSelected()) {
                    // openCamera(context, imagePath, imageName);
                    Log.v("showCameraView", "showOptionsData - " + new Gson().toJson(options.get(i).getImagePathList()));
                    showCameraView(options.get(i), holder, index);
                }
            }*/


    }

    private void showNestedItemsV2(final Node selectedNode, final GenericViewHolder holder, List<Node> options, int index, boolean isSuperNested, boolean isGotFromChipSelected) {
        Log.v(TAG, index + " - " + new Gson().toJson(selectedNode));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(false);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        holder.superNestedRecyclerView.setLayoutManager(linearLayoutManager);
        int nestedLevel = mNestedLevel + 1;
        holder.nestedQuestionsListingAdapter = new NestedQuestionsListingAdapter(mContext, mRootRecyclerView, holder.superNestedRecyclerView, selectedNode, nestedLevel, mRootIndex, mIsEditMode, new OnItemSelection() {
            @Override
            public void onSelect(Node node, int indexSelected, boolean isSkipped, Node parentNode) {
                Log.v(TAG, "NestedQuestionsListingAdapter showOptionsDataV2 onSelect index- " + indexSelected);
                Log.v(TAG, "NestedQuestionsListingAdapter showOptionsDataV2 onSelect selectedNode- " + selectedNode.findDisplay());
                Log.v(TAG, "NestedQuestionsListingAdapter showOptionsDataV2 onSelect nestedLevel- " + nestedLevel);
                Log.v(TAG, "NestedQuestionsListingAdapter showOptionsDataV2 onSelect nestedLevel- " + selectedNode.isHavingNestedQuestion());
                Log.v(TAG, "NestedQuestionsListingAdapter showOptionsDataV2 onSelect nestedLevel- " + selectedNode.getOptionsList());


                if (isSkipped) {
                    if (options.size() == 1) {
                        mItemList.get(index).setSelected(false);
                        mItemList.get(index).setDataCaptured(false);
                        selectedNode.setSelected(false);
                        selectedNode.setDataCaptured(false);
                        selectedNode.unselectAllNestedNode();
                        notifyItemChanged(index);
                    }/* else {
                            return;
                        }*/
                }
                VisitUtils.scrollNow(mRootRecyclerView, 1000, 0, 400);
                Log.v(TAG, "NestedQuestionsListingAdapter onSelect selectedNestedOptionIndex- " + holder.selectedNestedOptionIndex);

                boolean isLastNodeSubmit = holder.selectedNestedOptionIndex >= options.size() - 1;

                if (!selectedNode.isHavingNestedQuestion() && !selectedNode.isContainsTheQuestionBeforeOptions()) {
                    mOnItemSelection.onSelect(node, index, isSkipped, selectedNode);
                } else {
                    if (isLastNodeSubmit)
                        mOnItemSelection.onSelect(node, indexSelected, isSkipped, selectedNode);

                    else {
                        holder.selectedNestedOptionIndex += 1;
                        holder.nestedQuestionsListingAdapter.addItem(options.get(holder.selectedNestedOptionIndex));
                    }
                }

            }

            @Override
            public void needTitleChange(String title) {

            }

            @Override
            public void onAllAnswered(boolean isAllAnswered) {

            }

            @Override
            public void onCameraRequest() {

            }

            @Override
            public void onImageRemoved(int nodeIndex, int imageIndex, String image) {

            }
        });
        holder.nestedQuestionsListingAdapter.setEngineVersion(getEngineVersion());
        holder.superNestedRecyclerView.setAdapter(holder.nestedQuestionsListingAdapter);
        if (mIsEditMode) {
            holder.nestedQuestionsListingAdapter.addItemAll(options);
        } else {
            holder.nestedQuestionsListingAdapter.addItem(options.get(holder.selectedNestedOptionIndex));
        }
        holder.nestedQuestionsListingAdapter.setSuperNodeList(mSuperItemList);
        holder.superNestedRecyclerView.setVisibility(View.VISIBLE);
        holder.submitButton.setVisibility(View.GONE);
        holder.skipButton.setVisibility(View.GONE);
    }

    /**
     * @param selectedNode
     * @param holder
     * @param options
     * @param index
     * @param isSuperNested
     */
    private void showOptionsData(final Node selectedNode, final GenericViewHolder holder, List<Node> options, int index, boolean isSuperNested, boolean isGotFromChipSelected) {
        if (getEngineVersion().equalsIgnoreCase("3.0")) {
            showOptionsDataV2(selectedNode, holder, options, index, isSuperNested, isGotFromChipSelected);
            return;
        }
        Log.v(TAG, "showOptionsData");
        holder.singleComponentContainer.removeAllViews();
        holder.optionRecyclerView.setVisibility(View.VISIBLE);

        boolean havingNestedQuestion = selectedNode.isHavingNestedQuestion();


                /*if (mItemList.get(index).isRequired()) {
                    skipButton.setVisibility(View.GONE);
                } else {
                    skipButton.setVisibility(View.VISIBLE);
                }*/
        //mNestedLevel= mNestedLevel + 1;
        Log.v(TAG, "NestedQuestionsListingAdapter mNestedLevel - " + mNestedLevel);
        if (havingNestedQuestion) {
            //if (isSuperNested) {
            //if(mNestedLevel%2==0){
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
            linearLayoutManager.setStackFromEnd(false);
            linearLayoutManager.setSmoothScrollbarEnabled(true);
            holder.superNestedRecyclerView.setLayoutManager(linearLayoutManager);
            int nestedLevel = mNestedLevel + 1;
            holder.nestedQuestionsListingAdapter = new NestedQuestionsListingAdapter(mContext, mRootRecyclerView, holder.superNestedRecyclerView, selectedNode, nestedLevel, mRootIndex, mIsEditMode, new OnItemSelection() {
                @Override
                public void onSelect(Node node, int indexSelected, boolean isSkipped, Node parentNode) {
                    Log.v(TAG, "NestedQuestionsListingAdapter onSelect index- " + indexSelected);
                    Log.v(TAG, "NestedQuestionsListingAdapter onSelect selectedNode- " + selectedNode.findDisplay());
                    Log.v(TAG, "NestedQuestionsListingAdapter onSelect nestedLevel- " + nestedLevel);
                    Log.v(TAG, "NestedQuestionsListingAdapter onSelect nestedLevel- " + selectedNode.isHavingNestedQuestion());
                    Log.v(TAG, "NestedQuestionsListingAdapter onSelect nestedLevel- " + selectedNode.getOptionsList());


                    if (isSkipped) {
                        if (options.size() == 1) {
                            mItemList.get(index).setSelected(false);
                            mItemList.get(index).setDataCaptured(false);
                            selectedNode.setSelected(false);
                            selectedNode.setDataCaptured(false);
                            selectedNode.unselectAllNestedNode();
                            notifyItemChanged(index);
                        }/* else {
                            return;
                        }*/
                    }
                    VisitUtils.scrollNow(mRootRecyclerView, 1000, 0, 400);
                    Log.v(TAG, "NestedQuestionsListingAdapter onSelect selectedNestedOptionIndex- " + holder.selectedNestedOptionIndex);

                    boolean isLastNodeSubmit = holder.selectedNestedOptionIndex >= options.size() - 1;

                    if (isLastNodeSubmit)
                        mOnItemSelection.onSelect(node, indexSelected, isSkipped, selectedNode);

                    else {
                        holder.selectedNestedOptionIndex += 1;
                        holder.nestedQuestionsListingAdapter.addItem(options.get(holder.selectedNestedOptionIndex));
                    }
                }

                @Override
                public void needTitleChange(String title) {

                }

                @Override
                public void onAllAnswered(boolean isAllAnswered) {

                }

                @Override
                public void onCameraRequest() {

                }

                @Override
                public void onImageRemoved(int nodeIndex, int imageIndex, String image) {

                }
            });
            holder.superNestedRecyclerView.setAdapter(holder.nestedQuestionsListingAdapter);
            if (mIsEditMode) {
                holder.nestedQuestionsListingAdapter.addItemAll(options);
            } else {
                holder.nestedQuestionsListingAdapter.addItem(options.get(holder.selectedNestedOptionIndex));
            }
            holder.nestedQuestionsListingAdapter.setSuperNodeList(mSuperItemList);
            holder.superNestedRecyclerView.setVisibility(View.VISIBLE);
            holder.submitButton.setVisibility(View.GONE);
            holder.skipButton.setVisibility(View.GONE);
        } else {

            holder.superNestedRecyclerView.setVisibility(View.GONE);
            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(mContext);
            layoutManager.setFlexDirection(FlexDirection.ROW);
            layoutManager.setJustifyContent(JustifyContent.FLEX_START);

            holder.optionRecyclerView.setLayoutManager(layoutManager);
            //**********
            // Avoid the duplicate options asking to user in connected questions
            //**************
            String duplicateCheckNodeNames = mItemList.get(index).getCompareDuplicateNode();
            Log.v(TAG, "duplicateCheckNodeNames - " + duplicateCheckNodeNames);
            if (duplicateCheckNodeNames != null && !duplicateCheckNodeNames.isEmpty()) {
                int sourceIndex = 0;
                Node toCompareWithNode = null;
                for (int i = 0; i < mSuperItemList.size(); i++) {
                    Log.v(TAG, "toCompareWithNode - " + mSuperItemList.get(i).getText());
                    if (mSuperItemList.get(i).getText().equalsIgnoreCase(duplicateCheckNodeNames)) {
                        toCompareWithNode = mSuperItemList.get(i);
                        Log.v(TAG, "toCompareWithNode - " + new Gson().toJson(toCompareWithNode));
                        break;
                    }
                }
                NodeAdapterUtils.updateForHideShowFlag(mContext, mItemList.get(index), toCompareWithNode);
            }
            // *****************
            OptionsChipsGridAdapter optionsChipsGridAdapter = new OptionsChipsGridAdapter(holder.optionRecyclerView, mContext, mItemList.get(index), options, new OptionsChipsGridAdapter.OnItemSelection() {
                @Override
                public void onSelect(Node node, boolean isLoadingForNestedEditData) {
                    if (!isLoadingForNestedEditData)
                        VisitUtils.scrollNow(mRootRecyclerView, 1000, 0, 300);
                    if (!isLoadingForNestedEditData) {
                        mItemList.get(index).setSelected(false);
                        mItemList.get(index).setDataCaptured(false);
                    }
                    for (int i = 0; i < options.size(); i++) {
                        if (options.get(i).isSelected()) {
                            mItemList.get(index).setSelected(true);
                            mItemList.get(index).setDataCaptured(true);
                            break;
                        }
                    }
                    if (isLoadingForNestedEditData) {
                        if (selectedNode.isDataCaptured()) {
                            AdapterUtils.setToDisable(holder.skipButton);
                            AdapterUtils.setToDisable(holder.submitButton);
                        } else {
                            AdapterUtils.setToDefault(holder.skipButton);
                            AdapterUtils.setToDefault(holder.submitButton);
                        }
                    } else {
                        AdapterUtils.setToDefault(holder.submitButton);
                        AdapterUtils.setToDefault(holder.skipButton);

                    }
                     /*holder.submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,  0, 0);
                    holder.submitButton.setBackgroundResource(R.drawable.ui2_common_button_bg_submit);*/

                    String type = node.getInputType();

                    if (type == null || type.isEmpty() && (node.getOptionsList() != null && !node.getOptionsList().isEmpty())) {
                        type = "options";
                    }
                    Log.v(TAG, "Type - " + type);
                    if (!type.isEmpty() && node.isSelected()) {
                        //holder.singleComponentContainer.removeAllViews();
                        //holder.singleComponentContainer.setVisibility(View.VISIBLE);
                    } else {
                        holder.singleComponentContainer.removeAllViews();
                        //holder.superNestedContainerLinearLayout.removeAllViews();
                        if (mItemList.get(index).isMultiChoice()) {
                            //holder.tvQuestionDesc.setText(mContext.getString(R.string.select_one_or_more));
                            holder.submitButton.setVisibility(View.VISIBLE);
                        } else {
                            //holder.tvQuestionDesc.setText(mContext.getString(R.string.select_any_one));
                            holder.submitButton.setVisibility(View.GONE);
                            mOnItemSelection.onSelect(node, mRootIndex, false, mItemList.get(index));
                            AdapterUtils.setToDisable(holder.skipButton);
                        }

                        if (mItemList.get(index).isRequired()) {
                            holder.skipButton.setVisibility(View.GONE);
                        } else {
                            holder.skipButton.setVisibility(View.VISIBLE);
                        }
                        return;
                    }

                    routeByType(holder, selectedNode, node, index, true, true);
                }
            });
            holder.optionRecyclerView.setAdapter(optionsChipsGridAdapter);
        }

            /*for (int i = 0; i < options.size(); i++) {
                String type = options.get(i).getInputType();
                if (type.equalsIgnoreCase("camera") && options.get(i).isSelected()) {
                    // openCamera(context, imagePath, imageName);
                    Log.v("showCameraView", "showOptionsData - " + new Gson().toJson(options.get(i).getImagePathList()));
                    showCameraView(options.get(i), holder, index);
                }
            }*/


    }

    private void showCameraView(Node parentNode, Node node, LinearLayout containerLayout, int index) {
        Log.v("showCameraView", "Start method - " + new Gson().toJson(node));
        Log.v("showCameraView", "ImagePathList - " + new Gson().toJson(node.getImagePathList()));
        containerLayout.removeAllViews();
        View view = View.inflate(mContext, R.layout.ui2_visit_image_capture_view, null);
        Button submitButton = view.findViewById(R.id.btn_submit);
        submitButton.setText(mContext.getString(R.string.visit_summary_button_upload));
        submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, node.isDataCaptured() ? R.drawable.ic_baseline_check_18_white : 0, 0);
        submitButton.setBackgroundResource(node.isDataCaptured() && node.isImageUploaded() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);
        LinearLayout newImageCaptureLinearLayout = view.findViewById(R.id.ll_emptyView);
        //newImageCaptureLinearLayout.setVisibility(View.VISIBLE);
        newImageCaptureLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //openCamera(getImagePath(), "");
                mLastImageCaptureSelectedNodeIndex = index;
                mOnItemSelection.onCameraRequest();
            }
        });
        view.findViewById(R.id.btn_1st_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setImageUploaded(false);
                //openCamera(getImagePath(), "");
                node.setImageUploaded(false);
                node.setDataCaptured(false);
                mLastImageCaptureSelectedNodeIndex = index;
                mOnItemSelection.onCameraRequest();
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setSkipped(false);
                //parentNode.setSkipped(false);
                AdapterUtils.buttonProgressAnimation(mContext, submitButton, true, new AdapterUtils.OnFinishActionListener() {
                    @Override
                    public void onFinish() {
                        node.setImageUploaded(true);
                        mOnItemSelection.onSelect(node, mRootIndex, false, parentNode);

                    }
                });
            }
        });

        RecyclerView imagesRcv = view.findViewById(R.id.rcv_added_image);
        imagesRcv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

        if (!node.getImagePathList().isEmpty()) {
            ImageGridAdapter imageGridAdapter = new ImageGridAdapter(imagesRcv, mContext, node.getImagePathList(), new ImageGridAdapter.OnImageAction() {
                @Override
                public void onImageRemoved(int imageIndex, String image) {
                    node.setImageUploaded(false);
                    node.setDataCaptured(false);
                    mOnItemSelection.onImageRemoved(index, imageIndex, image);
                }

                @Override
                public void onNewImageRequest() {
                    node.setImageUploaded(false);
                    node.setDataCaptured(false);
                    mLastImageCaptureSelectedNodeIndex = index;
                    mOnItemSelection.onCameraRequest();
                }
            });
            imagesRcv.setAdapter(imageGridAdapter);
            imageGridAdapter.addNull();
            Log.v("showCameraView", "ImagePathList recyclerView - " + imagesRcv.getAdapter().getItemCount());
        }


        if (node.getImagePathList().isEmpty()) {
            newImageCaptureLinearLayout.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.GONE);
            imagesRcv.setVisibility(View.GONE);
        } else {
            newImageCaptureLinearLayout.setVisibility(View.GONE);
            submitButton.setVisibility(View.VISIBLE);
            imagesRcv.setVisibility(View.VISIBLE);
        }

        containerLayout.addView(view);
        containerLayout.setVisibility(View.VISIBLE);

    }


    /**
     * Time duration
     *
     * @param node
     * @param containerLayout
     * @param index
     */
    private void addDurationView(Node parentNode, Node node, LinearLayout containerLayout, int index) {
        Log.v("addDurationView", new Gson().toJson(node));
        containerLayout.removeAllViews();
        View view = View.inflate(mContext, R.layout.ui2_visit_reason_time_range, null);
        final Spinner numberRangeSpinner = view.findViewById(R.id.sp_number_range);
        final Spinner durationTypeSpinner = view.findViewById(R.id.sp_duration_type);
        Button submitButton = view.findViewById(R.id.btn_submit);
        submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, node.isDataCaptured() ? R.drawable.ic_baseline_check_18_white : 0, 0);
        submitButton.setBackgroundResource(node.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);
        Button skipButton = view.findViewById(R.id.btn_skip);
        if (node.isSkipped()) {
            skipButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_18_white, 0);
            skipButton.setBackgroundResource(R.drawable.ui2_common_primary_bg);
            AdapterUtils.setToDisable(submitButton);
        }
        String oldDataNumber = "", oldDataType = "";
        String finalOldDataNumber = oldDataNumber;
        String finalOldDataType = oldDataType;

        /*if (node.isDataCaptured()) {
            AdapterUtils.setToDisable(skipButton);
        } else {
            AdapterUtils.setToDefault(skipButton);
        }*/

        /*if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);*/
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                node.setSelected(false);
                parentNode.setSelected(false);
                parentNode.setDataCaptured(false);

                node.setSkipped(true);
                parentNode.setSkipped(true);
                AdapterUtils.setToDisable(submitButton);
                AdapterUtils.buttonProgressAnimation(mContext, skipButton, false, new AdapterUtils.OnFinishActionListener() {
                    @Override
                    public void onFinish() {
                        mOnItemSelection.onSelect(node, mRootIndex, true, parentNode);
                        notifyItemChanged(index);
                    }
                });

            }
        });

        // add a list
        int i = 0;
        int max = 100;
        final String[] data = new String[max + 1];
        data[0] = mContext.getString(R.string.number_label);
        for (i = 1; i <= max; i++) {
            data[i] = String.valueOf(i);
        }

        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(mContext,
                R.layout.simple_spinner_item_1, data);
        adaptador.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);

        numberRangeSpinner.setAdapter(adaptador);
        numberRangeSpinner.setPopupBackgroundDrawable(mContext.getDrawable(R.drawable.popup_menu_background));

        numberRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int which, long l) {
                String newNumber = numberRangeSpinner.getSelectedItem().toString();
                if (!newNumber.equals(finalOldDataNumber)) {
                    AdapterUtils.setToDefault(submitButton);
                    AdapterUtils.setToDefault(skipButton);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // add a list
        final String[] data1 = new String[]{mContext.getString(R.string.duration_type),
                mContext.getString(R.string.Hours), mContext.getString(R.string.Days),
                mContext.getString(R.string.Weeks), mContext.getString(R.string.Months),
                mContext.getString(R.string.Years)};

        ArrayAdapter<String> adaptador1 = new ArrayAdapter<String>(mContext,
                R.layout.simple_spinner_item_1, data1);
        adaptador1.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);

        durationTypeSpinner.setAdapter(adaptador1);
        durationTypeSpinner.setPopupBackgroundDrawable(mContext.getDrawable(R.drawable.popup_menu_background));

        durationTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int which, long l) {
                String newType = durationTypeSpinner.getSelectedItem().toString();
                if (!newType.equals(finalOldDataType)) {
                    AdapterUtils.setToDefault(submitButton);
                    AdapterUtils.setToDefault(skipButton);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (!node.getLanguage().isEmpty()) {
            String[] val = node.getLanguage().trim().split(" ");
            if (val.length == 2) {
                oldDataNumber = val[0];
                oldDataType = val[1];
                numberRangeSpinner.setSelection(Arrays.asList(data).indexOf(oldDataNumber));
                durationTypeSpinner.setSelection(Arrays.asList(data1).indexOf(oldDataType));
            }
        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (numberRangeSpinner.getSelectedItemPosition() == 0 || numberRangeSpinner.getSelectedItem().toString().isEmpty()) {
                    Toast.makeText(mContext, mContext.getString(R.string.duration_validation_txt), Toast.LENGTH_SHORT).show();
                    return;
                } else if (durationTypeSpinner.getSelectedItemPosition() == 0 || durationTypeSpinner.getSelectedItem().toString().isEmpty()) {
                    Toast.makeText(mContext, mContext.getString(R.string.duration_type_validation_txt), Toast.LENGTH_SHORT).show();
                    return;
                }
                String durationString = numberRangeSpinner.getSelectedItem().toString() + " " + durationTypeSpinner.getSelectedItem().toString();

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }

                node.setSelected(true);
                //holder.node.setSelected(true);

                node.setDataCaptured(true);
                //holder.node.setDataCaptured(true);

                parentNode.setSelected(true);
                parentNode.setDataCaptured(true);

                //notifyDataSetChanged();
                AdapterUtils.setToDisable(skipButton);
                node.setSkipped(false);
                parentNode.setSkipped(false);
                AdapterUtils.buttonProgressAnimation(mContext, submitButton, true, new AdapterUtils.OnFinishActionListener() {
                    @Override
                    public void onFinish() {
                        mOnItemSelection.onSelect(node, mRootIndex, false, parentNode);
                    }
                });

            }
        });
        /*if (node.isDataCaptured() && node.isDataCaptured()) {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24_white, 0, 0, 0);
        } else {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }*/

        containerLayout.addView(view);
    }

    private void showNumberListing(final TextView textView, String title, int i, int max) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(title);

        // add a list
        final String[] data = new String[max];
        for (; i < max; i++) {
            data[i] = String.valueOf(i);
        }
        builder.setItems(data, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textView.setText(data[which]);

            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDurationTypes(final TextView textView) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.select_duration_type_title));

        // add a list
        final String[] data = new String[]{
                mContext.getString(R.string.Hours), mContext.getString(R.string.Days),
                mContext.getString(R.string.Weeks), mContext.getString(R.string.Months),
                mContext.getString(R.string.Years)};

        builder.setItems(data, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textView.setText(data[which]);

            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void addNumberView(Node parentNode, Node node, LinearLayout containerLayout, int index) {
        containerLayout.removeAllViews();
        View view = View.inflate(mContext, R.layout.visit_reason_input_text, null);
        Button submitButton = view.findViewById(R.id.btn_submit);
        submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, node.isDataCaptured() ? R.drawable.ic_baseline_check_18_white : 0, 0);
        submitButton.setBackgroundResource(node.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);
        final EditText editText = view.findViewById(R.id.actv_reasons);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        Button skipButton = view.findViewById(R.id.btn_skip);
        if (node.isSkipped()) {
            skipButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_18_white, 0);
            skipButton.setBackgroundResource(R.drawable.ui2_common_primary_bg);
            AdapterUtils.setToDisable(submitButton);
        }

        if (node.isSelected() && node.getLanguage() != null && node.isDataCaptured()) {
            if (node.getLanguage().contains(" : "))
                editText.setText(node.getLanguage().split(" : ")[1]);
        }
        String oldValue = editText.getText().toString().trim();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().trim().equals(oldValue)) {
                    AdapterUtils.setToDefault(submitButton);
                    AdapterUtils.setToDefault(skipButton);
                }
            }
        });
       /* if (node.isDataCaptured()) {
            AdapterUtils.setToDisable(skipButton);
        } else {
            AdapterUtils.setToDefault(skipButton);
        }*/
        /*if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);*/
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                node.setSelected(false);
                node.setDataCaptured(false);
                //holder.node.setDataCaptured(true);
                parentNode.setSelected(false);
                parentNode.setDataCaptured(false);

                node.setSkipped(true);
                parentNode.setSkipped(true);
                AdapterUtils.setToDisable(submitButton);
                AdapterUtils.buttonProgressAnimation(mContext, skipButton, false, new AdapterUtils.OnFinishActionListener() {
                    @Override
                    public void onFinish() {
                        mOnItemSelection.onSelect(node, mRootIndex, true, parentNode);
                        notifyItemChanged(index);
                    }
                });

                WindowsUtils.hideSoftKeyboard((AppCompatActivity) mContext);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(mContext, mContext.getString(R.string.please_enter_the_value), Toast.LENGTH_SHORT).show();
                } else {

                    if (!editText.getText().toString().equalsIgnoreCase("")) {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", editText.getText().toString()));
                        } else {
                            node.addLanguage(editText.getText().toString());
                        }
                        node.setSelected(true);
                        //holder.node.setSelected(true);

                        node.setDataCaptured(true);
                        parentNode.setSelected(true);
                        parentNode.setDataCaptured(true);
                        //holder.node.setDataCaptured(true);
                    } else {
                        node.setDataCaptured(false);
                        //holder.node.setDataCaptured(false);


                        //if (node.isRequired()) {
                        node.setSelected(false);

                        parentNode.setSelected(false);
                        parentNode.setDataCaptured(false);
                        //holder.node.setSelected(false);
                        //} else {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                        } else {
                            node.addLanguage("Question not answered");
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }
                        //   node.setSelected(true);
                        //}
                    }
                    //notifyDataSetChanged();
                    AdapterUtils.setToDisable(skipButton);
                    node.setSkipped(false);
                    parentNode.setSkipped(false);
                    AdapterUtils.buttonProgressAnimation(mContext, submitButton, true, new AdapterUtils.OnFinishActionListener() {
                        @Override
                        public void onFinish() {
                            mOnItemSelection.onSelect(node, mRootIndex, false, parentNode);

                        }
                    });
                    WindowsUtils.hideSoftKeyboard((AppCompatActivity) mContext);
                }
            }
        });

        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setHint(mContext.getString(R.string.describe_hint_txt));
        /*if (node.isDataCaptured() && node.isDataCaptured()) {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24_white, 0, 0, 0);
        } else {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }*/

        containerLayout.addView(view);
    }

    private void addTextEnterView(Node parentNode, Node node, LinearLayout containerLayout, int index) {
        Log.v(TAG, "addTextEnterView");
        containerLayout.removeAllViews();
        View view = View.inflate(mContext, R.layout.visit_reason_input_text, null);
        Button submitButton = view.findViewById(R.id.btn_submit);
        submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, node.isDataCaptured() ? R.drawable.ic_baseline_check_18_white : 0, 0);
        submitButton.setBackgroundResource(node.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);
        final EditText editText = view.findViewById(R.id.actv_reasons);
        Button skipButton = view.findViewById(R.id.btn_skip);
        if (node.isSkipped()) {
            skipButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_18_white, 0);
            skipButton.setBackgroundResource(R.drawable.ui2_common_primary_bg);
            AdapterUtils.setToDisable(submitButton);
        }
        if (node.isSelected() && node.getLanguage() != null && node.isDataCaptured()) {
            if (node.getLanguage().contains(" : "))
                editText.setText(node.getLanguage().split(" : ")[1]);
            else
                editText.setText(node.getLanguage());

        }
        String oldValue = editText.getText().toString().trim();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().trim().equals(oldValue)) {
                    AdapterUtils.setToDefault(submitButton);
                    AdapterUtils.setToDefault(skipButton);
                }
            }
        });

        /*if (node.isDataCaptured()) {
            AdapterUtils.setToDisable(skipButton);
        } else {
            AdapterUtils.setToDefault(skipButton);
        }*/
        /*if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);*/
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                node.setSelected(false);
                node.setDataCaptured(false);

                parentNode.setSelected(false);
                parentNode.setDataCaptured(false);

                node.setSkipped(true);
                parentNode.setSkipped(true);
                AdapterUtils.setToDisable(submitButton);
                AdapterUtils.buttonProgressAnimation(mContext, skipButton, false, new AdapterUtils.OnFinishActionListener() {
                    @Override
                    public void onFinish() {
                        mOnItemSelection.onSelect(node, mRootIndex, true, parentNode);
                        notifyItemChanged(index);
                    }
                });

                WindowsUtils.hideSoftKeyboard((AppCompatActivity) mContext);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(mContext, mContext.getString(R.string.please_enter_the_value), Toast.LENGTH_SHORT).show();
                } else {

                    if (!editText.getText().toString().equalsIgnoreCase("")) {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", editText.getText().toString()));
                        } else {
                            node.addLanguage(editText.getText().toString());
                        }
                        node.setSelected(true);
                        //holder.node.setSelected(true);

                        node.setDataCaptured(true);
                        //holder.node.setDataCaptured(true);

                        parentNode.setSelected(true);
                        parentNode.setDataCaptured(true);

                    } else {
                        node.setDataCaptured(false);
                        //holder.node.setDataCaptured(false);
                        //if (node.isRequired()) {
                        node.setSelected(false);
                        parentNode.setSelected(false);
                        parentNode.setDataCaptured(false);
                        //holder.node.setSelected(false);
                        //} else {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                        } else {
                            node.addLanguage("Question not answered");
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }
                        //   node.setSelected(true);
                        //}
                    }
                    //notifyDataSetChanged();
                    AdapterUtils.setToDisable(skipButton);
                    node.setSkipped(false);
                    parentNode.setSkipped(false);
                    AdapterUtils.buttonProgressAnimation(mContext, submitButton, true, new AdapterUtils.OnFinishActionListener() {
                        @Override
                        public void onFinish() {
                            mOnItemSelection.onSelect(node, mRootIndex, false, parentNode);

                        }
                    });
                    WindowsUtils.hideSoftKeyboard((AppCompatActivity) mContext);
                }

            }

        });

        editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setMinLines(5);
        editText.setLines(5);
        editText.setHorizontallyScrolling(false);
        editText.setHint(mContext.getString(R.string.describe_hint_txt));
        editText.setMinHeight(320);

        /*if (node.isDataCaptured() && node.isDataCaptured()) {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24_white, 0, 0, 0);
        } else {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
*/
        containerLayout.addView(view);
    }

    private void addDateView(GenericViewHolder genericViewHolder, Node parentNode, Node node, LinearLayout containerLayout, int index) {
        containerLayout.removeAllViews();
        View view = View.inflate(mContext, R.layout.visit_reason_date, null);
        final Button submitButton = view.findViewById(R.id.btn_submit);
        submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, node.isDataCaptured() ? R.drawable.ic_baseline_check_18_white : 0, 0);
        submitButton.setBackgroundResource(node.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);
        final TextView displayDateButton = view.findViewById(R.id.btn_view_date);
        final CalendarView calendarView = view.findViewById(R.id.cav_date);
        calendarView.setMaxDate(System.currentTimeMillis() + 1000);
        Log.v(TAG, "addDateView - " + node.getLanguage());
        String langVal = node.getLanguage();
        Button skipButton = view.findViewById(R.id.btn_skip);
        if (node.isSkipped()) {
            skipButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_18_white, 0);
            skipButton.setBackgroundResource(R.drawable.ui2_common_primary_bg);
            AdapterUtils.setToDisable(submitButton);
        }
        if (node.isDataCaptured()) {
            String dateString = node.getLanguage();
            displayDateButton.setText(dateString);
            displayDateButton.setTag(dateString);
        }
        boolean isToDate = genericViewHolder.node.getText().equalsIgnoreCase("To");
        String fromDate = "";
        if (isToDate) {
            if (mParentNode.getOptionsList() != null && mParentNode.getOptionsList().size() >= 1) {
                fromDate = mParentNode.getOption(1).getOption(0).getLanguage();
            }
        }
        Log.v("DataSubmit", "fromDate - " + fromDate);

        Date fromDateFormat = null;
        if (!fromDate.isEmpty() && !fromDate.equalsIgnoreCase("%")) {
            //22/Aug/2023
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
            try {
                fromDateFormat = simpleDateFormat.parse(fromDate);
                calendarView.setMinDate(fromDateFormat.getTime() + 1000);
                calendarView.setMaxDate(System.currentTimeMillis() + 1000);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }


        if (langVal != null && !langVal.isEmpty() && !langVal.equals("%") && node.isDataCaptured()) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
            Date date = null;
            try {
                date = simpleDateFormat.parse(langVal.trim());
                SimpleDateFormat simpleDateFormatLocal = new SimpleDateFormat("dd/MMM/yyyy", new Locale(new SessionManager(mContext).getAppLanguage()));
                String dateString = simpleDateFormat.format(date);
                displayDateButton.setText(simpleDateFormatLocal.format(date));
                displayDateButton.setTag(dateString);

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // display the selected date by using a toast
                int m = month + 1;
                //String d = (dayOfMonth < 10 ? "0" + dayOfMonth : String.valueOf(dayOfMonth))
                //       + "-" + (m < 10 ? "0" + m : String.valueOf(m)) + "-" + String.valueOf(year);


                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(0);
                //cal.set(Integer.parseInt(d.split("-")[2]), Integer.parseInt(d.split("-")[1]) - 1, Integer.parseInt(d.split("-")[0]));
                cal.set(year, month, dayOfMonth);
                Date date = cal.getTime();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
                SimpleDateFormat simpleDateFormatLocal = new SimpleDateFormat("dd/MMM/yyyy", new Locale(new SessionManager(mContext).getAppLanguage()));
                String dateString = simpleDateFormat.format(date);
                displayDateButton.setText(simpleDateFormatLocal.format(date));
                displayDateButton.setTag(dateString);
                VisitUtils.scrollNow(mRootRecyclerView, 400, 0, 400);
                AdapterUtils.setToDefault(submitButton);
                AdapterUtils.setToDefault(skipButton);
            }
        });
        /*if (node.isDataCaptured()) {
            AdapterUtils.setToDisable(skipButton);
        } else {
            AdapterUtils.setToDefault(skipButton);
        }*/
        //holder.skipButton.setVisibility(View.GONE);
        /*if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);*/
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                node.setSelected(false);
                parentNode.setSelected(false);
                parentNode.setDataCaptured(false);

                node.setSkipped(true);
                parentNode.setSkipped(true);
                AdapterUtils.setToDisable(submitButton);
                AdapterUtils.buttonProgressAnimation(mContext, skipButton, false, new AdapterUtils.OnFinishActionListener() {
                    @Override
                    public void onFinish() {
                        mOnItemSelection.onSelect(node, mRootIndex, true, parentNode);
                        notifyItemChanged(index);
                    }
                });

            }
        });

        final Date finalFromDateFormat = fromDateFormat;
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get from data

                String fromDate = "";
                if (isToDate) {
                    if (mParentNode.getOptionsList() != null && mParentNode.getOptionsList().size() >= 1) {
                        fromDate = mParentNode.getOption(1).getOption(0).getLanguage();
                    }
                }
                Log.v("DataSubmit", "fromDate - " + fromDate);


                String d = (String) displayDateButton.getTag();
                if (d == null || d.equalsIgnoreCase("null") || d.isEmpty() || !d.contains("/")) {
                    Toast.makeText(mContext, mContext.getString(R.string.please_select_date), Toast.LENGTH_SHORT).show();
                } else {
                    Date fromDateFormat = null;
                    Date toDateFormat = null;
                    Date currentDateFormat = new Date();

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
                    try {
                        Log.v("DataSubmit", "d - " + d);
                        if (!fromDate.isEmpty() && !fromDate.equalsIgnoreCase("%"))
                            fromDateFormat = simpleDateFormat.parse(fromDate);
                        toDateFormat = simpleDateFormat.parse(d);

                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    Log.v("DataSubmit", "finalFromDateFormat - " + finalFromDateFormat);
                    Log.v("DataSubmit", "toDateFormat - " + toDateFormat);
                    if (fromDateFormat != null && fromDateFormat.after(toDateFormat)) {
                        Toast.makeText(mContext, mContext.getString(R.string.to_date_validation), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    /*Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(0);
                    cal.set(Integer.parseInt(d.split("-")[2]), Integer.parseInt(d.split("-")[1]) - 1, Integer.parseInt(d.split("-")[0]));
                    Date date = cal.getTime();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);*/

                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", d));
                    } else {
                        node.addLanguage(d);
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    node.setSelected(true);
                    //holder.node.setSelected(true);

                    node.setDataCaptured(true);
                    //holder.node.setDataCaptured(true);
                    parentNode.setSelected(true);
                    parentNode.setDataCaptured(true);

                    //notifyDataSetChanged();
                    AdapterUtils.setToDisable(skipButton);
                    node.setSkipped(false);
                    parentNode.setSkipped(false);
                    AdapterUtils.buttonProgressAnimation(mContext, submitButton, true, new AdapterUtils.OnFinishActionListener() {
                        @Override
                        public void onFinish() {
                            mOnItemSelection.onSelect(node, mRootIndex, false, parentNode);
                        }
                    });

                }
            }
        });
        /*if (node.isDataCaptured() && node.isDataCaptured()) {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24_white, 0, 0, 0);
        } else {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }*/
        containerLayout.addView(view);
    }


    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvQuestionDesc, knowMoreTextView;
        Node node, parentNode;
        int index, rootIndex;
        RecyclerView optionRecyclerView, superNestedRecyclerView;
        // this will contain independent view like, edittext, date, time, range, etc
        LinearLayout singleComponentContainer;
        //SpinKitView spinKitView;
        //LinearLayout bodyLayout;
        Button submitButton, skipButton;
        NestedQuestionsListingAdapter nestedQuestionsListingAdapter;
        int selectedNestedOptionIndex = 0;

        GenericViewHolder(View itemView) {
            super(itemView);
            knowMoreTextView = itemView.findViewById(R.id.tv_know_more);
            knowMoreTextView.setPaintFlags(knowMoreTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            knowMoreTextView.setVisibility(View.GONE);

            skipButton = itemView.findViewById(R.id.btn_skip);
            submitButton = itemView.findViewById(R.id.btn_submit);
            optionRecyclerView = itemView.findViewById(R.id.rcv_nested_container);
            superNestedRecyclerView = itemView.findViewById(R.id.rcv_super_nested_container);
            singleComponentContainer = itemView.findViewById(R.id.ll_single_component_container);

            tvQuestion = itemView.findViewById(R.id.tv_question);
            tvQuestionDesc = itemView.findViewById(R.id.tv_question_desc);

            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemList.get(index).isSelected()) {
                        mItemList.get(index).setSkipped(false);
                        AdapterUtils.setToDisable(skipButton);
                        AdapterUtils.buttonProgressAnimation(mContext, submitButton, true, new AdapterUtils.OnFinishActionListener() {
                            @Override
                            public void onFinish() {
                                mOnItemSelection.onSelect(node, mRootIndex, false, null);
                            }
                        });

                    } else
                        Toast.makeText(mContext, mContext.getString(R.string.select_at_least_one_option), Toast.LENGTH_SHORT).show();
                }
            });

            skipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    mItemList.get(index).setSelected(false);
                    mItemList.get(index).setDataCaptured(false);

                    mItemList.get(index).setSkipped(true);
                    AdapterUtils.setToDisable(submitButton);
                    AdapterUtils.buttonProgressAnimation(mContext, skipButton, false, new AdapterUtils.OnFinishActionListener() {
                        @Override
                        public void onFinish() {
                            mOnItemSelection.onSelect(node, mRootIndex, true, null);
                            notifyItemChanged(index);
                        }
                    });


                }
            });

            knowMoreTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NodeAdapterUtils.showKnowMoreDialog(mContext, node.getDisplay(), node.getPop_up());
                }
            });
        }


    }


}

