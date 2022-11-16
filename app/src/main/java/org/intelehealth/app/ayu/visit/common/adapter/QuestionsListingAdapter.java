package org.intelehealth.app.ayu.visit.common.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.common.adapter.AssociateSymptomsQueryAdapter;
import org.intelehealth.app.ayu.visit.reason.adapter.OptionsChipsGridAdapter;
import org.intelehealth.app.knowledgeEngine.Node;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class QuestionsListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<Node> mItemList = new ArrayList<Node>();
    private int mTotalQuery = 0;
    RecyclerView mRecyclerView;

    public interface OnItemSelection {
        void onSelect(Node node);

        void needTitleChange(String title);

        void onAllAnswered(boolean isAllAnswered);
    }

    private OnItemSelection mOnItemSelection;

    public QuestionsListingAdapter(RecyclerView recyclerView, Context context, int totalQuery, OnItemSelection onItemSelection) {
        mContext = context;
        mRecyclerView = recyclerView;
        mOnItemSelection = onItemSelection;
        mTotalQuery = totalQuery;
        //mAnimator = new RecyclerViewAnimator(recyclerView);
    }

    private JSONObject mThisScreenLanguageJsonObject = new JSONObject();

    public void addItem(Node node) {
        mItemList.add(node);
        notifyDataSetChanged();
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

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GenericViewHolder) {
            GenericViewHolder genericViewHolder = (GenericViewHolder) holder;
            genericViewHolder.node = mItemList.get(position);
            genericViewHolder.tvQuestion.setText(genericViewHolder.node.findDisplay());
            genericViewHolder.tvQuestionCounter.setText((position + 1) + " of " + mTotalQuery + " questions"); //"1 of 10 questions"

            genericViewHolder.singleComponentContainer.removeAllViews();
            genericViewHolder.singleComponentContainer.setVisibility(View.GONE);
            genericViewHolder.recyclerView.setVisibility(View.GONE);

            if (genericViewHolder.node.getText().equalsIgnoreCase("Associated symptoms")) {
                mOnItemSelection.needTitleChange("2/4 Visit reason : Associated symptoms");
                showAssociateSymptoms(genericViewHolder.node, genericViewHolder, position);
            } else {
                mOnItemSelection.needTitleChange("");


                String type = genericViewHolder.node.getInputType();
                Log.v("Node", "Type - " + type);
                if (type == null || type.isEmpty() && !genericViewHolder.node.getOptionsList().isEmpty()) {
                    type = "options";
                }
                switch (type) {
                    case "text":
                        // askText(questionNode, context, adapter);
                        addTextEnterView(genericViewHolder.node, genericViewHolder, position);
                        break;
                    case "date":
                        //askDate(questionNode, context, adapter);
                        addDateView(genericViewHolder.node, genericViewHolder, position);
                        break;
                    case "location":
                        //askLocation(questionNode, context, adapter);
                        break;
                    case "number":
                        // askNumber(questionNode, context, adapter);
                        addNumberView(genericViewHolder.node, genericViewHolder, position);
                        break;
                    case "area":
                        // askArea(questionNode, context, adapter);
                        break;
                    case "duration":
                        // askDuration(questionNode, context, adapter);
                        addDurationView(genericViewHolder.node, genericViewHolder, position);
                        break;
                    case "range":
                        // askRange(questionNode, context, adapter);
                        addNumberView(genericViewHolder.node, genericViewHolder, position);
                        break;
                    case "frequency":
                        //askFrequency(questionNode, context, adapter);
                        addNumberView(genericViewHolder.node, genericViewHolder, position);
                        break;
                    case "camera":
                        // openCamera(context, imagePath, imageName);
                        addCameraiew(genericViewHolder.node, genericViewHolder, position);
                        break;

                    case "options":
                        // openCamera(context, imagePath, imageName);
                        showOptionsData(genericViewHolder, genericViewHolder.node.getOptionsList(), position);
                        break;
                }
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    genericViewHolder.spinKitView.setVisibility(View.GONE);
                    genericViewHolder.bodyRelativeLayout.setVisibility(View.VISIBLE);
                    mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
                }
            }, 2000);
        }
    }

    private void showAssociateSymptoms(Node node, GenericViewHolder holder, int position) {
        holder.singleComponentContainer.setVisibility(View.VISIBLE);
        holder.tvQuestionDesc.setVisibility(View.VISIBLE);
        holder.recyclerView.setVisibility(View.GONE);
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

            }
        });
        recyclerView.setAdapter(associateSymptomsQueryAdapter);
        holder.singleComponentContainer.addView(view);
    }


    private void showOptionsData(GenericViewHolder holder, List<Node> options, int index) {
        if (options.size() == 1 && (options.get(0).getOptionsList() == null || options.get(0).getOptionsList().isEmpty())) {
            // it seems that inside the options only one view and its simple component like text,date, number, area, duration, range, frequency, camera, etc
            // we we have add same in linear layout dynamically instead of adding in to recyclerView
            holder.singleComponentContainer.setVisibility(View.VISIBLE);
            holder.tvQuestionDesc.setVisibility(View.GONE);
            Node node = options.get(0);
            String type = node.getInputType();

            if (type == null || type.isEmpty() && !node.getOptionsList().isEmpty()) {
                type = "options";
            }
            Log.v("Node", "Type - " + type);
            switch (type) {
                case "text":
                    // askText(questionNode, context, adapter);
                    addTextEnterView(node, holder, index);
                    break;
                case "date":
                    //askDate(questionNode, context, adapter);
                    addDateView(node, holder, index);
                    break;
                case "location":
                    //askLocation(questionNode, context, adapter);
                    break;
                case "number":
                    // askNumber(questionNode, context, adapter);
                    addNumberView(node, holder, index);
                    break;
                case "area":
                    // askArea(questionNode, context, adapter);
                    break;
                case "duration":
                    // askDuration(questionNode, context, adapter);
                    addDurationView(node, holder, index);
                    break;
                case "range":
                    // askRange(questionNode, context, adapter);
                    addNumberView(node, holder, index);
                    break;
                case "frequency":
                    //askFrequency(questionNode, context, adapter);
                    addNumberView(node, holder, index);
                    break;
                case "camera":
                    // openCamera(context, imagePath, imageName);
                    break;

                case "options":
                    // openCamera(context, imagePath, imageName);
                    //showOptionsData(genericViewHolder, genericViewHolder.node.getOptionsList());
                    break;
            }
        } else {
            holder.tvQuestionDesc.setVisibility(View.VISIBLE);
            holder.recyclerView.setVisibility(View.VISIBLE);
            holder.recyclerView.setLayoutManager(new GridLayoutManager(mContext, options.size() == 1 ? 1 : 2));
            OptionsChipsGridAdapter optionsChipsGridAdapter = new OptionsChipsGridAdapter(holder.recyclerView, mContext, options, new OptionsChipsGridAdapter.OnItemSelection() {
                @Override
                public void onSelect(Node data) {
                    mOnItemSelection.onSelect(data);
                    //Toast.makeText(mContext, "Selected : " + data, Toast.LENGTH_SHORT).show();
                }
            });
            holder.recyclerView.setAdapter(optionsChipsGridAdapter);
        }

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
        View view = View.inflate(mContext, R.layout.ui2_visit_reason_time_range, null);
        final TextView numberRangeTextView = view.findViewById(R.id.tv_number_range);
        final TextView durationTypeTextView = view.findViewById(R.id.tv_duration_type);
        Button submitButton = view.findViewById(R.id.btn_submit);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (numberRangeTextView.getText().toString().isEmpty()) {
                    Toast.makeText(mContext, "Please select the duration number", Toast.LENGTH_SHORT).show();
                    return;
                } else if (durationTypeTextView.getText().toString().isEmpty()) {
                    Toast.makeText(mContext, "Please select the duration type", Toast.LENGTH_SHORT).show();
                    return;
                }
                String durationString = numberRangeTextView.getText() + " " + durationTypeTextView.getText();

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                notifyDataSetChanged();
                mOnItemSelection.onSelect(node);
            }
        });
        numberRangeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNumberListing(numberRangeTextView, "Select duration number", 0, 100);
            }
        });
        durationTypeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDurationTypes(durationTypeTextView);
            }
        });

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
        View view = View.inflate(mContext, R.layout.visit_reason_input_text, null);
        Button submitButton = view.findViewById(R.id.btn_submit);
        final EditText editText = view.findViewById(R.id.actv_reasons);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(mContext, "Please enter the value", Toast.LENGTH_SHORT).show();
                } else {
                    if (!editText.getText().toString().equalsIgnoreCase("")) {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", editText.getText().toString()));
                        } else {
                            node.addLanguage(editText.getText().toString());
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }
                        node.setSelected(true);
                    } else {
                        //if (node.isRequired()) {
                        node.setSelected(false);
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
                    mOnItemSelection.onSelect(node);
                }
            }
        });

        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setHint(node.getText());
        holder.singleComponentContainer.addView(view);
    }

    private void addTextEnterView(Node node, GenericViewHolder holder, int index) {
        View view = View.inflate(mContext, R.layout.visit_reason_input_text, null);
        Button submitButton = view.findViewById(R.id.btn_submit);
        final EditText editText = view.findViewById(R.id.actv_reasons);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(mContext, "Please enter the value", Toast.LENGTH_SHORT).show();
                } else {
                    if (!editText.getText().toString().equalsIgnoreCase("")) {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", editText.getText().toString()));
                        } else {
                            node.addLanguage(editText.getText().toString());
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }
                        node.setSelected(true);
                    } else {
                        //if (node.isRequired()) {
                        node.setSelected(false);
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
                    mOnItemSelection.onSelect(node);
                }
            }
        });

        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setHint(node.getText());
        holder.singleComponentContainer.addView(view);
    }

    private void addDateView(Node node, GenericViewHolder holder, int index) {
        View view = View.inflate(mContext, R.layout.visit_reason_date, null);
        final Button submitButton = view.findViewById(R.id.btn_submit);
        final CalendarView calendarView = view.findViewById(R.id.cav_date);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // display the selected date by using a toast
                submitButton.setText(dayOfMonth + "-" + month + "-" + year);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!submitButton.getText().toString().trim().contains("-")) {
                    Toast.makeText(mContext, "Please select the date", Toast.LENGTH_SHORT).show();
                } else
                    mOnItemSelection.onSelect(node);
            }
        });

        holder.singleComponentContainer.addView(view);
    }

    private void addCameraiew(Node node, GenericViewHolder holder, int index) {
        View view = View.inflate(mContext, R.layout.visit_reason_date, null);
        final Button submitButton = view.findViewById(R.id.btn_submit);
        final CalendarView calendarView = view.findViewById(R.id.cav_date);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // display the selected date by using a toast
                submitButton.setText(dayOfMonth + "-" + month + "-" + year);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!submitButton.getText().toString().trim().contains("-")) {
                    Toast.makeText(mContext, "Please select the date", Toast.LENGTH_SHORT).show();
                } else
                    mOnItemSelection.onSelect(node);
            }
        });

        holder.singleComponentContainer.addView(view);
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvQuestionDesc, tvQuestionCounter;
        Node node;
        RecyclerView recyclerView;
        // this will contain independent view like, edittext, date, time, range, etc
        LinearLayout singleComponentContainer;
        SpinKitView spinKitView;
        RelativeLayout bodyRelativeLayout;

        GenericViewHolder(View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.rcv_container);
            singleComponentContainer = itemView.findViewById(R.id.ll_single_component_container);
            spinKitView = itemView.findViewById(R.id.spin_kit);
            bodyRelativeLayout = itemView.findViewById(R.id.rl_body);
            spinKitView.setVisibility(View.VISIBLE);
            bodyRelativeLayout.setVisibility(View.GONE);

            tvQuestion = itemView.findViewById(R.id.tv_question);
            tvQuestionDesc = itemView.findViewById(R.id.tv_question_desc);
            tvQuestionCounter = itemView.findViewById(R.id.tv_question_counter);


        }


    }


}

