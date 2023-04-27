package org.intelehealth.app.ayu.visit.common.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.model.ComplainBasicInfo;
import org.intelehealth.app.ayu.visit.reason.adapter.OptionsChipsGridAdapter;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.knowledgeEngine.PhysicalExam;
import org.intelehealth.app.utilities.DialogUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class QuestionsListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = "QuestionsListingAdapter";
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<Node> mItemList = new ArrayList<Node>();
    //private int mTotalQuery = 0;
    RecyclerView mRecyclerView;
    private int mLastImageCaptureSelectedNodeIndex = 0;

    public void addImageInLastNode(String image) {
        mItemList.get(mLastImageCaptureSelectedNodeIndex).getImagePathList().add(image);
        Log.v("ImageCaptured", new Gson().toJson(mItemList.get(mLastImageCaptureSelectedNodeIndex)));
        notifyItemChanged(mLastImageCaptureSelectedNodeIndex);
    }

    public void removeImageInLastNode(int index, String image) {
        mItemList.get(mLastImageCaptureSelectedNodeIndex).getImagePathList().remove(index);
        notifyItemChanged(mLastImageCaptureSelectedNodeIndex);
    }

    public boolean isIsAssociateSymptomsLoaded() {
        return mIsAssociateSymptomsLoaded;
    }

    public void setAssociateSymptomsLoaded(boolean mIsAssociateSymptomsLoaded) {
        Log.v(TAG, "setAssociateSymptomsLoaded()");
        this.mIsAssociateSymptomsLoaded = mIsAssociateSymptomsLoaded;
    }

    public interface OnItemSelection {
        void onSelect(Node node, int index);

        void needTitleChange(String title);

        void onAllAnswered(boolean isAllAnswered);

        void onCameraRequest();

        void onImageRemoved(int index, String image);
    }

    private OnItemSelection mOnItemSelection;
    private boolean mIsForPhysicalExam;
    private PhysicalExam mPhysicalExam;
    private HashMap<Integer, ComplainBasicInfo> mRootComplainBasicInfoHashMap = new HashMap<>();
    private int mRootIndex = 0;
    private boolean mIsAssociateSymptomsLoaded = false;
    private boolean mIsAssociateSymptomsNestedQuery = false;
    private HashMap<Integer, Integer> mIndexMappingHashMap = new HashMap<>();

    public QuestionsListingAdapter(RecyclerView recyclerView, Context context, boolean isPhyExam, PhysicalExam physicalExam, int rootIndex, HashMap<Integer, ComplainBasicInfo> complainBasicInfoHashMap, OnItemSelection onItemSelection) {
        mContext = context;
        mIsForPhysicalExam = isPhyExam;
        mPhysicalExam = physicalExam;
        mRecyclerView = recyclerView;
        mOnItemSelection = onItemSelection;
        //mTotalQuery = totalQuery;
        mRootIndex = rootIndex;
        mRootComplainBasicInfoHashMap = complainBasicInfoHashMap;
        //mAnimator = new RecyclerViewAnimator(recyclerView);
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

    public void addItem(Node node) {
        Log.v(TAG, "addItem()");
        mItemList.add(node);
        mIndexMappingHashMap.put(mItemList.size() - 1, mRootIndex);
        notifyItemInserted(mItemList.size() - 1);
    }

    public void addItemAll(List<Node> nodes) {
        mItemList = nodes;
        notifyDataSetChanged();
    }

    public List<Node> geItems() {
        return mItemList;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ui2_question_main_root, parent, false);
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
            genericViewHolder.node = mItemList.get(genericViewHolder.getAbsoluteAdapterPosition());
            genericViewHolder.index = genericViewHolder.getAbsoluteAdapterPosition();
            genericViewHolder.rootIndex = mIndexMappingHashMap.get(genericViewHolder.index);
            int position = genericViewHolder.getAbsoluteAdapterPosition();

            genericViewHolder.spinKitView.setVisibility(View.VISIBLE);
            genericViewHolder.bodyLayout.setVisibility(View.GONE);

            genericViewHolder.otherContainerLinearLayout.removeAllViews();
            genericViewHolder.singleComponentContainer.removeAllViews();
            genericViewHolder.singleComponentContainer.setVisibility(View.GONE);
            genericViewHolder.recyclerView.setVisibility(View.GONE);
            genericViewHolder.superNestedContainerLinearLayout.setVisibility(View.GONE);
            if (genericViewHolder.node.getPop_up() != null && !genericViewHolder.node.getPop_up().isEmpty()) {
                genericViewHolder.knowMoreTextView.setVisibility(View.VISIBLE);

            } else {
                genericViewHolder.knowMoreTextView.setVisibility(View.GONE);
            }

           /* if (genericViewHolder.node.isDataCaptured() && genericViewHolder.node.isDataCaptured()) {
                genericViewHolder.submitButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24_white, 0, 0, 0);
            } else {
                genericViewHolder.submitButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }*/
            if (mIsForPhysicalExam) {

                Node _mNode = mPhysicalExam.getExamNode(position).getOption(0);
                final String parent_name = mPhysicalExam.getExamParentNodeName(position);
                String nodeText = parent_name + " : " + _mNode.findDisplay();

                genericViewHolder.tvQuestion.setText(nodeText);
                genericViewHolder.tvQuestionCounter.setText((position + 1) + " of " + mPhysicalExam.getTotalNumberOfExams() + " questions"); //"1 of 10 questions"

                if (genericViewHolder.node.getJobAidFile() != null && !genericViewHolder.node.getJobAidFile().isEmpty()) {
                    genericViewHolder.referenceContainerLinearLayout.setVisibility(View.VISIBLE);
                    genericViewHolder.tvReferenceDesc.setVisibility(View.VISIBLE);
                } else {
                    genericViewHolder.referenceContainerLinearLayout.setVisibility(View.GONE);
                    genericViewHolder.tvReferenceDesc.setVisibility(View.GONE);
                }
                genericViewHolder.referenceContainerLinearLayout.removeAllViews();
                String[] imgs = genericViewHolder.node.getJobAidFile().split(",");
                for (int i = 0; i < imgs.length; i++) {
                    View v2 = View.inflate(mContext, R.layout.ui2_ref_image_view, null);
                    ImageView imageView = v2.findViewById(R.id.image);
                    if (genericViewHolder.node.getJobAidFile() != null || !genericViewHolder.node.getJobAidFile().isEmpty()) {
                        String drawableName = "physicalExamAssets/" + genericViewHolder.node.getJobAidFile() + ".jpg";
                        try {
                            // get input stream
                            InputStream ims = mContext.getAssets().open(drawableName);
                            // load image as Drawable
                            Drawable d = Drawable.createFromStream(ims, null);
                            // set image to ImageView
                            imageView.setImageDrawable(d);
                            imageView.setMinimumHeight(150);
                            imageView.setMinimumWidth(300);
                            genericViewHolder.referenceContainerLinearLayout.addView(v2);
                        } catch (IOException ex) {
                            ex.printStackTrace();

                        }
                    }
                }
            } else {
                genericViewHolder.tvQuestion.setText(genericViewHolder.node.findDisplay());
                genericViewHolder.tvQuestionCounter.setText(String.format("%d of %d questions",
                        getCount(genericViewHolder.index, genericViewHolder.rootIndex), mRootComplainBasicInfoHashMap.get(mIndexMappingHashMap.get(genericViewHolder.index)).getOptionSize())); //"1 of 10 questions"

            }
            mOnItemSelection.needTitleChange("2/4 Visit reason : " + mRootComplainBasicInfoHashMap.get(mIndexMappingHashMap.get(genericViewHolder.index)).getComplainName());

            if (genericViewHolder.node.getText().equalsIgnoreCase("Associated symptoms")) {
                //mOnItemSelection.needTitleChange("2/4 Visit reason : Associated symptoms");
                showAssociateSymptoms(genericViewHolder.node, genericViewHolder, position);
                genericViewHolder.tvQuestionCounter.setText("");
            } else {
                //mOnItemSelection.needTitleChange("");


                String type = genericViewHolder.node.getInputType();
                Log.v(TAG, "onBindViewHolder Type - " + type);
                Log.v(TAG, "onBindViewHolder Node - " + new Gson().toJson(genericViewHolder.node));
                if (type == null || type.isEmpty() && (genericViewHolder.node.getOptionsList() != null && !genericViewHolder.node.getOptionsList().isEmpty())) {
                    type = "options";
                }
                switch (type) {
                    case "text":
                        // askText(questionNode, context, adapter);
                        addTextEnterView(mItemList.get(position), genericViewHolder, position);
                        break;
                    case "date":
                        //askDate(questionNode, context, adapter);
                        addDateView(mItemList.get(position), genericViewHolder, position);
                        break;
                    case "location":
                        //askLocation(questionNode, context, adapter);
                        break;
                    case "number":
                        // askNumber(questionNode, context, adapter);
                        addNumberView(mItemList.get(position), genericViewHolder, position);
                        break;
                    case "area":
                        // askArea(questionNode, context, adapter);
                        break;
                    case "duration":
                        // askDuration(questionNode, context, adapter);
                        addDurationView(mItemList.get(position), genericViewHolder, position);
                        break;
                    case "range":
                        // askRange(questionNode, context, adapter);
                        addRangeView(mItemList.get(position), genericViewHolder, position);
                        break;
                    case "frequency":
                        //askFrequency(questionNode, context, adapter);
                        addFrequencyView(mItemList.get(position), genericViewHolder, position);
                        break;
                    case "camera":
                        // openCamera(context, imagePath, imageName);
                        Log.v("showCameraView", "onBindViewHolder 2");
                        showCameraView(mItemList.get(position), genericViewHolder, position);
                        break;

                    case "options":
                        // openCamera(context, imagePath, imageName);
                        //if (mIsForPhysicalExam)
                        //    showOptionsData(genericViewHolder, mPhysicalExam.getExamNode(position).getOption(0).getOptionsList(), position);
                        //else
                        showOptionsData(mItemList.get(position), genericViewHolder, mItemList.get(position).getOptionsList(), position, false);
                        break;
                }
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    genericViewHolder.spinKitView.setVisibility(View.GONE);
                    genericViewHolder.bodyLayout.setVisibility(View.VISIBLE);
                    //mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);

                }
            }, 1000);

            if (!mItemList.get(position).getImagePathList().isEmpty()) {
                Log.v("showCameraView", "onBindViewHolder 1");
                showCameraView(mItemList.get(position), genericViewHolder, position);
            }
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

    private void addRangeView(Node node, GenericViewHolder holder, int index) {
        holder.singleComponentContainer.removeAllViews();
        View view = View.inflate(mContext, R.layout.ui2_visit_number_range, null);
        RangeSlider rangeSlider = view.findViewById(R.id.range_slider);
        //rangeSlider.setLabelBehavior(LABEL_ALWAYS_VISIBLE); //Label always visible" nothing yet ?
        TextView rangeTextView = view.findViewById(R.id.btn_values);
        TextView submitTextView = view.findViewById(R.id.btn_submit);

        Button skipButton = view.findViewById(R.id.btn_skip);
        if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setSelected(false);
                mOnItemSelection.onSelect(node, index);
            }
        });

        if (node.getLanguage() != null && !node.getLanguage().isEmpty() && !node.getLanguage().equalsIgnoreCase("%")
                && node.getLanguage().equalsIgnoreCase(" to ")) {
            String[] vals = node.getLanguage().split(" to ");
            rangeTextView.setText(String.format("%s to %s", vals[0], vals[1]));
            List<Float> list = new ArrayList<>();
            list.add(Float.valueOf(vals[0]));
            list.add(Float.valueOf(vals[1]));
            rangeSlider.setValues(list);
        }
        submitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rangeTextView.getText().toString().equalsIgnoreCase("---")) {
                    Toast.makeText(mContext, "Please select the range!", Toast.LENGTH_SHORT).show();
                } else {
                    List<Float> values = rangeSlider.getValues();
                    int x = values.get(0).intValue();
                    int y = values.get(1).intValue();
                    String durationString = x + " to " + y;
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", durationString));
                    } else {
                        node.addLanguage(" " + durationString);
                        node.setText(durationString);
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    node.setSelected(true);
                    notifyItemChanged(index);
                    mOnItemSelection.onSelect(node, index);
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
                rangeTextView.setText(String.format("%d to %d", x, y));
            }
        });


        holder.singleComponentContainer.addView(view);
    }

    private void addFrequencyView(Node node, GenericViewHolder holder, int index) {
        holder.singleComponentContainer.removeAllViews();
        final View view = View.inflate(mContext, R.layout.ui2_visit_number_slider_with_icon, null);
        Slider rangeSlider = view.findViewById(R.id.number_slider);
        //rangeSlider.setLabelBehavior(LABEL_ALWAYS_VISIBLE); //Label always visible" nothing yet ?
        TextView rangeTextView = view.findViewById(R.id.btn_values);
        TextView submitTextView = view.findViewById(R.id.btn_submit);

        Button skipButton = view.findViewById(R.id.btn_skip);
        if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setSelected(false);
                mOnItemSelection.onSelect(node, index);
            }
        });


        if (node.getLanguage() != null && !node.getLanguage().isEmpty() && !node.getLanguage().equalsIgnoreCase("%") && TextUtils.isDigitsOnly(node.getLanguage())) {
            int i = Integer.parseInt(node.getLanguage());
            rangeTextView.setText(String.format("Level %d ", i));
            rangeSlider.setValue(i);
        }
        submitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rangeTextView.getText().toString().equalsIgnoreCase("---")) {
                    Toast.makeText(mContext, "Please drag to select!", Toast.LENGTH_SHORT).show();
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
                    notifyItemChanged(index);
                    mOnItemSelection.onSelect(node, index);
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
                rangeTextView.setText(String.format("Level %d ", x));
                updateCustomEmojiSliderUI(view, x);
            }
        });

        updateCustomEmojiSliderUI(view, 0);
        holder.singleComponentContainer.addView(view);
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

    private void showAssociateSymptoms(Node node, GenericViewHolder holder, int position) {
        Log.v(TAG, "showAssociateSymptoms()");
        holder.singleComponentContainer.removeAllViews();
        holder.singleComponentContainer.setVisibility(View.VISIBLE);
        holder.tvQuestionDesc.setVisibility(View.VISIBLE);
        holder.recyclerView.setVisibility(View.GONE);
        holder.submitButton.setVisibility(View.GONE);
        holder.skipButton.setVisibility(View.GONE);
        holder.tvQuestionDesc.setText("Select yes or no");

        View view = View.inflate(mContext, R.layout.associate_symptoms_questionar_main_view, null);
        Button submitButton = view.findViewById(R.id.btn_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemSelection.onAllAnswered(true);

            }
        });
        RecyclerView recyclerView = view.findViewById(R.id.rcv_container);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        AssociateSymptomsQueryAdapter associateSymptomsQueryAdapter = new AssociateSymptomsQueryAdapter(recyclerView, mContext, node.getOptionsList(), new AssociateSymptomsQueryAdapter.OnItemSelection() {
            @Override
            public void onSelect(Node data) {
                Log.v(TAG, new Gson().toJson(data));
                mItemList.get(position).setSelected(false);
                for (int i = 0; i < mItemList.get(position).getOptionsList().size(); i++) {
                    if (mItemList.get(position).getOptionsList().get(i).isSelected() || node.getOptionsList().get(i).isNoSelected()) {
                        mItemList.get(position).setSelected(true);
                        Log.v(TAG, "updated associate symptoms selected status");
                    }
                }
            }
        });
        recyclerView.setAdapter(associateSymptomsQueryAdapter);
/*

        if (node.isDataCaptured() && node.isDataCaptured()) {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24_white, 0, 0, 0);
        } else {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
*/


        holder.singleComponentContainer.addView(view);
        //recyclerView.scrollToPosition(0);
    }


    private void showOptionsData(Node selectedNode, final GenericViewHolder holder, List<Node> options, int index, boolean isSuperNested) {
        holder.singleComponentContainer.removeAllViews();
        Log.v(TAG, "showOptionsData isSuperNested - " + isSuperNested);
        if (options.size() == 1 && (options.get(0).getOptionsList() == null || options.get(0).getOptionsList().isEmpty())) {
            Log.v(TAG, "showOptionsData single option");
            if (isSuperNested)
                holder.superNestedContainerLinearLayout.setVisibility(View.VISIBLE);
            else
                holder.superNestedContainerLinearLayout.setVisibility(View.GONE);
            holder.submitButton.setVisibility(View.GONE);
            holder.skipButton.setVisibility(View.GONE);
            // it seems that inside the options only one view and its simple component like text,date, number, area, duration, range, frequency, camera, etc
            // we we have add same in linear layout dynamically instead of adding in to recyclerView
            holder.singleComponentContainer.setVisibility(View.VISIBLE);
            holder.tvQuestionDesc.setVisibility(View.GONE);
            Node node = options.get(0);
            String type = node.getInputType() == null ? "" : node.getInputType();

            if (node.getOptionsList() != null && !node.getOptionsList().isEmpty()) {
                type = "options";
            }
            Log.v(TAG, "Type - " + type);
            switch (type) {
                case "text":
                    // askText(questionNode, context, adapter);
                    addTextEnterView(options.get(0), holder, index);
                    break;
                case "date":
                    //askDate(questionNode, context, adapter);
                    addDateView(options.get(0), holder, index);
                    break;
                case "location":
                    //askLocation(questionNode, context, adapter);
                    break;
                case "number":
                    // askNumber(questionNode, context, adapter);
                    addNumberView(options.get(0), holder, index);
                    break;
                case "area":
                    // askArea(questionNode, context, adapter);
                    break;
                case "duration":
                    // askDuration(questionNode, context, adapter);
                    addDurationView(options.get(0), holder, index);
                    break;
                case "range":
                    // askRange(questionNode, context, adapter);
                    addRangeView(options.get(0), holder, index);
                    break;
                case "frequency":
                    //askFrequency(questionNode, context, adapter);
                    addFrequencyView(options.get(0), holder, index);
                    break;
                case "camera":
                    // openCamera(context, imagePath, imageName);
                    Log.v("showCameraView", "showOptionsData 1");
                    showCameraView(options.get(0), holder, index);
                    break;

                case "options":
                    // openCamera(context, imagePath, imageName);
                    //showOptionsData(genericViewHolder, genericViewHolder.node.getOptionsList());
                    break;
                default:
                    holder.submitButton.setVisibility(View.VISIBLE);
                    break;
            }

        } else {

            holder.tvQuestionDesc.setVisibility(View.VISIBLE);
            holder.recyclerView.setVisibility(View.VISIBLE);

            if (mItemList.get(index).isMultiChoice()) {
                holder.tvQuestionDesc.setText(mContext.getString(R.string.select_one_or_more));
                holder.submitButton.setVisibility(View.VISIBLE);
            } else {
                holder.tvQuestionDesc.setText(mContext.getString(R.string.select_any_one));
                holder.submitButton.setVisibility(View.GONE);

            }

            if (mItemList.get(index).isRequired()) {
                holder.skipButton.setVisibility(View.GONE);
            } else {
                holder.skipButton.setVisibility(View.VISIBLE);
            }
            if (isSuperNested) {
                //holder.superNestedContainerLinearLayout.removeAllViews();
                View v1 = View.inflate(mContext, R.layout.nested_recycle_view, null);
                RecyclerView recyclerView = v1.findViewById(R.id.rcv_nested_container);
                FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(mContext);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                layoutManager.setJustifyContent(JustifyContent.FLEX_START);
                recyclerView.setLayoutManager(layoutManager);
                TextView tvQuestionDesc = v1.findViewById(R.id.tv_question_desc);

                if (selectedNode.isMultiChoice()) {
                    tvQuestionDesc.setText(mContext.getString(R.string.select_one_or_more));
                    holder.submitButton.setVisibility(View.VISIBLE);
                } else {
                    tvQuestionDesc.setText(mContext.getString(R.string.select_any_one));
                    holder.submitButton.setVisibility(View.GONE);

                }

                /*if (mItemList.get(index).isRequired()) {
                    skipButton.setVisibility(View.GONE);
                } else {
                    skipButton.setVisibility(View.VISIBLE);
                }*/

                OptionsChipsGridAdapter optionsChipsGridAdapter = new OptionsChipsGridAdapter(recyclerView, mContext, selectedNode, options, new OptionsChipsGridAdapter.OnItemSelection() {
                    @Override
                    public void onSelect(Node node) {
                        selectedNode.setSelected(false);
                        for (int i = 0; i < options.size(); i++) {
                            if (options.get(i).isSelected()) {
                                selectedNode.setSelected(true);
                            }
                        }
                        //Toast.makeText(mContext, "Selected : " + data, Toast.LENGTH_SHORT).show();
                        String type = node.getInputType();

                        if (type == null || type.isEmpty() && (node.getOptionsList() != null && !node.getOptionsList().isEmpty())) {
                            type = "options";
                        }
                        if (!type.isEmpty()) {
                            holder.singleComponentContainer.setVisibility(View.VISIBLE);
                            //holder.singleComponentContainer.removeAllViews();
                        } else {
                            holder.singleComponentContainer.removeAllViews();
                            if (!selectedNode.isMultiChoice()) {
                                mOnItemSelection.onSelect(node, index);

                            }
                        }
                        Log.v(TAG, "Type - " + type);
                        switch (type) {
                            case "text":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askText(questionNode, context, adapter);
                                addTextEnterView(node, holder, index);
                                break;
                            case "date":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                //askDate(questionNode, context, adapter);
                                addDateView(node, holder, index);
                                break;
                            case "location":
                                //askLocation(questionNode, context, adapter);
                                break;
                            case "number":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askNumber(questionNode, context, adapter);
                                addNumberView(node, holder, index);
                                break;
                            case "area":
                                // askArea(questionNode, context, adapter);
                                break;
                            case "duration":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askDuration(questionNode, context, adapter);
                                addDurationView(node, holder, index);
                                break;
                            case "range":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askRange(questionNode, context, adapter);
                                addRangeView(node, holder, index);
                                break;
                            case "frequency":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                //askFrequency(questionNode, context, adapter);
                                addFrequencyView(node, holder, index);
                                break;
                            case "camera":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // openCamera(context, imagePath, imageName);
                                Log.v("showCameraView", "showOptionsData 2");
                                showCameraView(node, holder, index);
                                break;

                            case "options":
                                // openCamera(context, imagePath, imageName);
                                showOptionsData(node, holder, node.getOptionsList(), index, true);
                                break;
                        }
                        //notifyDataSetChanged();
                    }
                });
                recyclerView.setAdapter(optionsChipsGridAdapter);
                holder.superNestedContainerLinearLayout.addView(v1);
                holder.superNestedContainerLinearLayout.setVisibility(View.VISIBLE);
            } else {
                Log.v(TAG, "showOptionsData multiple option");
                holder.tvQuestionDesc.setVisibility(View.VISIBLE);
                holder.recyclerView.setVisibility(View.VISIBLE);
                holder.superNestedContainerLinearLayout.setVisibility(View.GONE);
                if (mItemList.get(index).isMultiChoice()) {
                    holder.tvQuestionDesc.setText(mContext.getString(R.string.select_one_or_more));
                    holder.submitButton.setVisibility(View.VISIBLE);
                } else {
                    holder.tvQuestionDesc.setText(mContext.getString(R.string.select_any_one));
                    holder.submitButton.setVisibility(View.GONE);

                }

                if (mItemList.get(index).isRequired()) {
                    holder.skipButton.setVisibility(View.GONE);
                } else {
                    holder.skipButton.setVisibility(View.VISIBLE);
                }
                //holder.recyclerView.setLayoutManager(new GridLayoutManager(mContext, options.size() == 1 ? 1 : 2));
                FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(mContext);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                layoutManager.setJustifyContent(JustifyContent.FLEX_START);
                holder.recyclerView.setLayoutManager(layoutManager);
                OptionsChipsGridAdapter optionsChipsGridAdapter = new OptionsChipsGridAdapter(holder.recyclerView, mContext, mItemList.get(index), options, new OptionsChipsGridAdapter.OnItemSelection() {
                    @Override
                    public void onSelect(Node node) {

                        mItemList.get(index).setSelected(false);
                        for (int i = 0; i < options.size(); i++) {
                            if (options.get(i).isSelected()) {
                                mItemList.get(index).setSelected(true);
                            }
                        }
                        //Toast.makeText(mContext, "Selected : " + data, Toast.LENGTH_SHORT).show();
                        String type = node.getInputType();

                        if (type == null || type.isEmpty() && (node.getOptionsList() != null && !node.getOptionsList().isEmpty())) {
                            type = "options";
                        }
                        Log.v(TAG, "Type - " + type);
                        if (!type.isEmpty() && node.isSelected()) {
                            holder.singleComponentContainer.removeAllViews();
                            holder.singleComponentContainer.setVisibility(View.VISIBLE);
                        } else {
                            holder.singleComponentContainer.removeAllViews();
                            holder.superNestedContainerLinearLayout.removeAllViews();
                            if (mItemList.get(index).isMultiChoice()) {
                                holder.tvQuestionDesc.setText(mContext.getString(R.string.select_one_or_more));
                                holder.submitButton.setVisibility(View.VISIBLE);
                            } else {
                                holder.tvQuestionDesc.setText(mContext.getString(R.string.select_any_one));
                                holder.submitButton.setVisibility(View.GONE);
                                mOnItemSelection.onSelect(node, index);
                            }

                            if (mItemList.get(index).isRequired()) {
                                holder.skipButton.setVisibility(View.GONE);
                            } else {
                                holder.skipButton.setVisibility(View.VISIBLE);
                            }
                            return;
                        }

                        switch (type) {
                            case "text":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askText(questionNode, context, adapter);
                                addTextEnterView(node, holder, index);
                                break;
                            case "date":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                //askDate(questionNode, context, adapter);
                                addDateView(node, holder, index);
                                break;
                            case "location":
                                //askLocation(questionNode, context, adapter);
                                break;
                            case "number":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askNumber(questionNode, context, adapter);
                                addNumberView(node, holder, index);
                                break;
                            case "area":
                                // askArea(questionNode, context, adapter);
                                break;
                            case "duration":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askDuration(questionNode, context, adapter);
                                addDurationView(node, holder, index);
                                break;
                            case "range":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askRange(questionNode, context, adapter);
                                addRangeView(node, holder, index);
                                break;
                            case "frequency":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                //askFrequency(questionNode, context, adapter);
                                addFrequencyView(node, holder, index);
                                break;
                            case "camera":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // openCamera(context, imagePath, imageName);
                                Log.v("showCameraView", "showOptionsData 2");
                                showCameraView(node, holder, index);
                                break;

                            case "options":
                                // openCamera(context, imagePath, imageName);
                                holder.superNestedContainerLinearLayout.removeAllViews();
                                showOptionsData(node, holder, node.getOptionsList(), index, true);
                                break;
                        }
                        //notifyDataSetChanged();
                    }
                });
                holder.recyclerView.setAdapter(optionsChipsGridAdapter);

            }
            for (int i = 0; i < options.size(); i++) {
                String type = options.get(i).getInputType();
                if (type.equalsIgnoreCase("camera") && options.get(i).isSelected()) {
                    // openCamera(context, imagePath, imageName);
                    showCameraView(options.get(i), holder, index);
                }
            }
        }

    }

    private void showCameraView(Node node, GenericViewHolder holder, int index) {
        Log.v("showCameraView", new Gson().toJson(node));
        Log.v("showCameraView", "ImagePathList - " + new Gson().toJson(node.getImagePathList()));
        holder.otherContainerLinearLayout.removeAllViews();
        View view = View.inflate(mContext, R.layout.ui2_visit_image_capture_view, null);
        Button submitButton = view.findViewById(R.id.btn_submit);
        submitButton.setText(mContext.getString(R.string.visit_summary_button_upload));
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
                //openCamera(getImagePath(), "");
                mLastImageCaptureSelectedNodeIndex = index;
                mOnItemSelection.onCameraRequest();
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mOnItemSelection.onSelect(node, index);
            }
        });

        RecyclerView imagesRcv = view.findViewById(R.id.rcv_added_image);
        imagesRcv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

        ImageGridAdapter imageGridAdapter = new ImageGridAdapter(imagesRcv, mContext, node.getImagePathList(), new ImageGridAdapter.OnImageAction() {
            @Override
            public void onImageRemoved(int index, String image) {
                mOnItemSelection.onImageRemoved(index, image);
            }

            @Override
            public void onNewImageRequest() {
                mLastImageCaptureSelectedNodeIndex = index;
                mOnItemSelection.onCameraRequest();
            }
        });
        imagesRcv.setAdapter(imageGridAdapter);
        Log.v("showCameraView", "ImagePathList recyclerView - " + imagesRcv.getAdapter().getItemCount());


        if (node.getImagePathList().isEmpty()) {
            newImageCaptureLinearLayout.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.GONE);
            imagesRcv.setVisibility(View.GONE);
        } else {
            newImageCaptureLinearLayout.setVisibility(View.GONE);
            submitButton.setVisibility(View.VISIBLE);
            imagesRcv.setVisibility(View.VISIBLE);
        }

        holder.otherContainerLinearLayout.addView(view);

    }


    /**
     * Time duration
     *
     * @param node
     * @param holder
     * @param index
     */
    private void addDurationView(Node node, GenericViewHolder holder, int index) {
        Log.v("addDurationView", new Gson().toJson(node));
        holder.singleComponentContainer.removeAllViews();
        View view = View.inflate(mContext, R.layout.ui2_visit_reason_time_range, null);
        final Spinner numberRangeSpinner = view.findViewById(R.id.sp_number_range);
        final Spinner durationTypeSpinner = view.findViewById(R.id.sp_duration_type);
        Button submitButton = view.findViewById(R.id.btn_submit);

        Button skipButton = view.findViewById(R.id.btn_skip);
        if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setSelected(false);
                mOnItemSelection.onSelect(node, index);
            }
        });

        // add a list
        int i = 0;
        int max = 100;
        final String[] data = new String[max + 1];
        data[0] = "Number";
        for (i = 1; i <=max; i++) {
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

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // add a list
        final String[] data1 = new String[]{"Duration Type",
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

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (!node.getLanguage().isEmpty()) {
            String[] val = node.getLanguage().trim().split(" ");
            if (val.length == 2) {
                numberRangeSpinner.setSelection(Arrays.asList(data).indexOf(val[0]));
                durationTypeSpinner.setSelection(Arrays.asList(data1).indexOf(val[1]));
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
                holder.node.setSelected(true);

                node.setDataCaptured(true);
                holder.node.setDataCaptured(true);

                //notifyDataSetChanged();
                mOnItemSelection.onSelect(node, index);
            }
        });
        /*if (node.isDataCaptured() && node.isDataCaptured()) {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24_white, 0, 0, 0);
        } else {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }*/

        holder.singleComponentContainer.addView(view);
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
        builder.setTitle("Select Duration Type");

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


    private void addNumberView(Node node, GenericViewHolder holder, int index) {
        holder.singleComponentContainer.removeAllViews();
        View view = View.inflate(mContext, R.layout.visit_reason_input_text, null);
        Button submitButton = view.findViewById(R.id.btn_submit);
        final EditText editText = view.findViewById(R.id.actv_reasons);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        if (node.isSelected() && node.getLanguage() != null && node.isDataCaptured()) {
            editText.setText(node.getLanguage());
        }

        Button skipButton = view.findViewById(R.id.btn_skip);
        if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setSelected(false);
                node.setDataCaptured(false);
                //holder.node.setDataCaptured(true);
                mOnItemSelection.onSelect(node, index);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(mContext, "Please enter the value", Toast.LENGTH_SHORT).show();
                } else {
                    if (!editText.getText().toString().equalsIgnoreCase("")) {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", editText.getText().toString()));
                        } else if (node.getLanguage().contains("%")) {
                            node.addLanguage(editText.getText().toString());
                        } else {
                            node.addLanguage(node.getLanguage() + " : " + editText.getText().toString());
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }
                        node.setSelected(true);
                        holder.node.setSelected(true);

                        node.setDataCaptured(true);
                        holder.node.setDataCaptured(true);
                    } else {
                        node.setDataCaptured(false);
                        holder.node.setDataCaptured(false);


                        //if (node.isRequired()) {
                        node.setSelected(false);
                        holder.node.setSelected(false);
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
                    mOnItemSelection.onSelect(node, index);
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

        holder.singleComponentContainer.addView(view);
    }

    private void addTextEnterView(Node node, GenericViewHolder holder, int index) {
        Log.v(TAG, "addTextEnterView");
        holder.singleComponentContainer.removeAllViews();
        View view = View.inflate(mContext, R.layout.visit_reason_input_text, null);
        Button submitButton = view.findViewById(R.id.btn_submit);

        final EditText editText = view.findViewById(R.id.actv_reasons);
        if (node.isSelected() && node.getLanguage() != null && node.isDataCaptured()) {
            editText.setText(node.getLanguage());
        }
        Button skipButton = view.findViewById(R.id.btn_skip);
        if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setSelected(false);
                node.setDataCaptured(false);
                mOnItemSelection.onSelect(node, index);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(mContext, "Please enter the value", Toast.LENGTH_SHORT).show();
                } else {
                    if (!editText.getText().toString().equalsIgnoreCase("")) {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", editText.getText().toString()));
                        } else if (node.getLanguage().contains("%")) {
                            node.addLanguage(editText.getText().toString());
                        } else {
                            node.addLanguage(node.getLanguage() + " : " + editText.getText().toString());
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }
                        node.setSelected(true);
                        holder.node.setSelected(true);

                        node.setDataCaptured(true);
                        holder.node.setDataCaptured(true);

                    } else {
                        node.setDataCaptured(false);
                        holder.node.setDataCaptured(false);
                        //if (node.isRequired()) {
                        node.setSelected(false);
                        holder.node.setSelected(false);
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
                    mOnItemSelection.onSelect(node, index);
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
        holder.singleComponentContainer.addView(view);
    }

    private void addDateView(Node node, GenericViewHolder holder, int index) {
        holder.singleComponentContainer.removeAllViews();
        View view = View.inflate(mContext, R.layout.visit_reason_date, null);
        final Button submitButton = view.findViewById(R.id.btn_submit);
        final Button displayDateButton = view.findViewById(R.id.btn_view_date);
        final CalendarView calendarView = view.findViewById(R.id.cav_date);
        calendarView.setMaxDate(System.currentTimeMillis() + 1000);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // display the selected date by using a toast
                int m = month + 1;
                String date = (dayOfMonth < 10 ? "0" + dayOfMonth : String.valueOf(dayOfMonth))
                        + "-" + (m < 10 ? "0" + m : String.valueOf(m)) + "-" + String.valueOf(year);
                displayDateButton.setText(date);
            }
        });
        holder.skipButton.setVisibility(View.GONE);
        Button skipButton = view.findViewById(R.id.btn_skip);
        if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setSelected(false);
                mOnItemSelection.onSelect(node, index);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String d = displayDateButton.getText().toString().trim();
                if (!d.contains("-")) {
                    Toast.makeText(mContext, "Please select the date", Toast.LENGTH_SHORT).show();
                } else {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(0);
                    cal.set(Integer.parseInt(d.split("-")[2]), Integer.parseInt(d.split("-")[1]) - 1, Integer.parseInt(d.split("-")[0]));
                    Date date = cal.getTime();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
                    String dateString = simpleDateFormat.format(date);
                    if (!dateString.equalsIgnoreCase("")) {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", dateString));
                        } else {
                            node.addLanguage(dateString);
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }
                        node.setSelected(true);
                        holder.node.setSelected(true);

                        node.setDataCaptured(true);
                        holder.node.setDataCaptured(true);
                    } else {
                        if (node.isRequired()) {
                            node.setSelected(false);
                        } else {
                            node.setSelected(true);
                            if (node.getLanguage().contains("_")) {
                                node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                            } else {
                                node.addLanguage("Question not answered");
                                //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                            }
                        }
                    }
                    //notifyDataSetChanged();
                    mOnItemSelection.onSelect(node, index);
                }
            }
        });
        /*if (node.isDataCaptured() && node.isDataCaptured()) {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24_white, 0, 0, 0);
        } else {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }*/
        holder.singleComponentContainer.addView(view);
    }


    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvQuestionDesc, tvQuestionCounter, tvReferenceDesc;
        Node node;
        int index, rootIndex;
        RecyclerView recyclerView;
        // this will contain independent view like, edittext, date, time, range, etc
        LinearLayout singleComponentContainer, referenceContainerLinearLayout, otherContainerLinearLayout, superNestedContainerLinearLayout;
        SpinKitView spinKitView;
        LinearLayout bodyLayout;
        Button submitButton, skipButton;
        TextView knowMoreTextView;


        GenericViewHolder(View itemView) {
            super(itemView);
            knowMoreTextView = itemView.findViewById(R.id.tv_know_more);
            knowMoreTextView.setPaintFlags(knowMoreTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            knowMoreTextView.setVisibility(View.GONE);

            skipButton = itemView.findViewById(R.id.btn_skip);
            submitButton = itemView.findViewById(R.id.btn_submit);
            recyclerView = itemView.findViewById(R.id.rcv_container);
            singleComponentContainer = itemView.findViewById(R.id.ll_single_component_container);
            referenceContainerLinearLayout = itemView.findViewById(R.id.ll_reference_container);
            otherContainerLinearLayout = itemView.findViewById(R.id.ll_others_container);

            superNestedContainerLinearLayout = itemView.findViewById(R.id.ll_super_nested_container);
            superNestedContainerLinearLayout.setVisibility(View.GONE);

            tvReferenceDesc = itemView.findViewById(R.id.tv_reference_desc);
            spinKitView = itemView.findViewById(R.id.spin_kit);
            bodyLayout = itemView.findViewById(R.id.rl_body);
            spinKitView.setVisibility(View.VISIBLE);
            bodyLayout.setVisibility(View.GONE);

            tvQuestion = itemView.findViewById(R.id.tv_question);
            tvQuestionDesc = itemView.findViewById(R.id.tv_question_desc);
            tvQuestionCounter = itemView.findViewById(R.id.tv_question_counter);

            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemList.get(index).isSelected())
                        mOnItemSelection.onSelect(node, index);
                    else
                        Toast.makeText(mContext, "Please select at least one option!", Toast.LENGTH_SHORT).show();
                }
            });

            skipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemList.get(index).setSelected(false);
                    mOnItemSelection.onSelect(node, index);

                }
            });

            knowMoreTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showKnowMoreDialog(node.getDisplay(), node.getPop_up());
                }
            });
        }


    }

    private void showKnowMoreDialog(String title, String message) {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialog(mContext, 0, title, message, true, mContext.getResources().getString(R.string.okay), mContext.getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
            @Override
            public void onDialogActionDone(int action) {

            }
        });
    }

}

