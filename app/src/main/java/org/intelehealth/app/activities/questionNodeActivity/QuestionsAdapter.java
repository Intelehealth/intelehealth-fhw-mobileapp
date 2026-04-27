package org.intelehealth.app.activities.questionNodeActivity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.physcialExamActivity.PhysicalExamActivity;
import org.intelehealth.app.activities.questionNodeActivity.adapters.AssociatedSysAdapter;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.visit.pocdevice.DigitalStethoscopeDialogFragment;
import org.intelehealth.app.database.InteleHealthDatabaseHelper;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.knowledgeEngine.PhysicalExam;

/**
 * Created by Sagar Shimpi
 * Github - TheSeasApps
 */
public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.ChipsAdapterViewHolder> implements AssociatedSysAdapter.FabVisibility {

    LayoutInflater layoutInflater;
    Context context;
    Node currentNode;
    int pos;
    RecyclerView recyclerView;
    FabClickListener _mListener;
    String _mCallingClass;
    boolean isAssociateSym;
    boolean showPopUp;
    private HashSet<String> skippedStethoscopeNodes = new HashSet<>();


    public void updateNode(Node currentNode) {
        this.currentNode = currentNode;
        notifyDataSetChanged();
    }

    boolean isChildNeedRefresh = false;

    public void refreshChildAdapter() {
        this.isChildNeedRefresh = true;
    }

    @Override
    public void setVisibility(boolean data) {
        showPopUp = data;
    }

    public interface FabClickListener {
        void fabClickedAtEnd();

        void onChildListClickEvent(int groupPos, int childPos, int physExamPos);
    }


    public QuestionsAdapter(Context _context, Node node, RecyclerView _rvQuestions, String callingClass,
                            FabClickListener _mListener, boolean isAssociateSym) {
        this.context = _context;
        this.currentNode = node;
        this.recyclerView = _rvQuestions;
        this._mCallingClass = callingClass;
        this._mListener = _mListener;
        this.isAssociateSym = isAssociateSym;
    }

    PhysicalExam physicalExam;

    public QuestionsAdapter(Context _context, PhysicalExam node, RecyclerView _rvQuestions, String callingClass,
                            FabClickListener _mListener, boolean isAssociateSym) {
        this.context = _context;
        this.physicalExam = node;
        this.recyclerView = _rvQuestions;
        this._mCallingClass = callingClass;
        this._mListener = _mListener;
        this.isAssociateSym = isAssociateSym;
    }

    @Override
    public QuestionsAdapter.ChipsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        View row = layoutInflater.inflate(R.layout.quesionnode_list_item, parent, false);
        return new ChipsAdapterViewHolder(row);
    }

    @Override
    public void onBindViewHolder(QuestionsAdapter.ChipsAdapterViewHolder holder, int position) {
        Node _mNode;
        Log.i("QuestionsAdapter " + position, "onBindViewHolder :" );

        if (_mCallingClass.equalsIgnoreCase(PhysicalExamActivity.class.getSimpleName())) {
            _mNode = physicalExam.getExamNode(position).getOption(0);
            Log.d("AyuDebug", "Node: " + _mNode);

            final String parent_name = physicalExam.getExamParentNodeName(position);
            String nodeText = parent_name + " : " + _mNode.findDisplay();

            holder.physical_exam_text_view.setText(nodeText);
            holder.physical_exam_text_view.setVisibility(View.GONE);
            if (_mNode.isAidAvailable()) {
                String type = _mNode.getJobAidType();
                if (type.equals("video")) {
                    holder.physical_exam_image_view.setVisibility(View.GONE);
                } else if (type.equals("image")) {
                    holder.physical_exam_image_view.setVisibility(View.VISIBLE);
                    String drawableName = "physicalExamAssets/" + _mNode.getJobAidFile() + ".jpg";
                    try {
                        // get input stream
                        InputStream ims = context.getAssets().open(drawableName);
                        // load image as Drawable
                        Drawable d = Drawable.createFromStream(ims, null);
                        // set image to ImageView
                        holder.physical_exam_image_view.setImageDrawable(d);
                        holder.physical_exam_image_view.setMinimumHeight(500);
                        holder.physical_exam_image_view.setMinimumWidth(500);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        holder.physical_exam_image_view.setVisibility(View.GONE);
                    }
                } else {
                    holder.physical_exam_image_view.setVisibility(View.GONE);
                }
            } else {
                holder.physical_exam_image_view.setVisibility(View.GONE);
            }
            holder.tvQuestion.setText(_mNode.findDisplay());
        } else {
            _mNode = currentNode;
            Log.d("AyuDebug", "Node: " + _mNode);
            if (isAssociateSym && currentNode.getOptionsList().size() == 1) {
                holder.tvQuestion.setText(_mNode.getOptionsList().get(0).findDisplay());
            } else {
                holder.tvQuestion.setText(_mNode.getOptionsList().get(position).findDisplay());
            }
            holder.physical_exam_image_view.setVisibility(View.GONE);
            holder.physical_exam_text_view.setVisibility(View.GONE);
        }

        if (position == getItemCount() - 1) {
            holder.fab.setVisibility(View.VISIBLE);
        } else {
            holder.fab.setVisibility(View.INVISIBLE);
        }
        holder.btnConnectDeviceItem.setOnClickListener(v -> {
            if (context instanceof QuestionNodeActivity) {
                ((QuestionNodeActivity) context).onAyuDeviceRequest(_mNode);
            } else if (context instanceof PhysicalExamActivity) {
                ((PhysicalExamActivity) context).onAyuDeviceRequest(_mNode);
            }
        });

        holder.tvSkipStethoscope.setOnClickListener(v -> {
            if (_mNode != null) {
                skippedStethoscopeNodes.add(_mNode.getId());
                notifyItemChanged(position);
            }
        });
        holder.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showPopUp) {
                    Toast.makeText(context, context.getString(R.string.select_all_answers), Toast.LENGTH_LONG).show();

                } else {
                    _mListener.fabClickedAtEnd();
                }
            }


        });

        if (isChildNeedRefresh) {
            if (holder.rvChips.getAdapter() != null) {
                holder.rvChips.getAdapter().notifyDataSetChanged();
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        if (_mCallingClass.equalsIgnoreCase(PhysicalExamActivity.class.getSimpleName())) {
            return physicalExam.getTotalNumberOfExams();
        } else {
            if (isAssociateSym && currentNode.getOptionsList().size() == 1) {
                return 1;
            } else {
                return currentNode.getOptionsList().size();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        pos = position;
        return position;
    }

    public class ChipsAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView tvQuestion, physical_exam_text_view;
        ImageView ivAyu, physical_exam_image_view;
        RecyclerView rvChips;
        FloatingActionButton fab;
        ComplaintNodeListAdapter chipsAdapter;
        AssociatedSysAdapter associatedSysAdapter;

        LinearLayout llDigitalContainer;
        TextView tvRecommendationReason, tvUpcomingCount, tvHeartPending, tvLungPending, tvSkipStethoscope;
        Button btnConnectDeviceItem;

        public ChipsAdapterViewHolder(View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_complaintQuestion);
            rvChips = itemView.findViewById(R.id.rv_chips);
            fab = itemView.findViewById(R.id.fab);
            physical_exam_text_view = itemView.findViewById(R.id.physical_exam_text_view);
            physical_exam_image_view = itemView.findViewById(R.id.physical_exam_image_view);

            llDigitalContainer = itemView.findViewById(R.id.ll_digital_auscultation_container);
            tvRecommendationReason = itemView.findViewById(R.id.tv_recommendation_reason);
            tvUpcomingCount = itemView.findViewById(R.id.tv_upcoming_count);
            tvHeartPending = itemView.findViewById(R.id.tv_heart_pending);
            tvLungPending = itemView.findViewById(R.id.tv_lung_pending);
            tvSkipStethoscope = itemView.findViewById(R.id.tv_skip_stethoscope);
            btnConnectDeviceItem = itemView.findViewById(R.id.btn_connect_device_item);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
            rvChips.setLayoutManager(linearLayoutManager);
            rvChips.setHasFixedSize(true);
            rvChips.setNestedScrollingEnabled(true);

            Node groupNode;
            List<Node> chipList = new ArrayList<>();
            if (_mCallingClass.equalsIgnoreCase(PhysicalExamActivity.class.getSimpleName())) {
                groupNode = physicalExam.getExamNode(pos).getOption(0);
                for (int i = 0; i < groupNode.getOptionsList().size(); i++) {
                    chipList.add(groupNode.getOptionsList().get(i));
                }
            } else {
                groupNode = currentNode;
                if (isAssociateSym && currentNode.getOptionsList().size() == 1) {
                    chipList = currentNode.getOptionsList().get(0).getOptionsList();
                } else {
                    Node node = currentNode.getOptionsList().get(pos);
                    for (int i = 0; i < node.getOptionsList().size(); i++) {
                        chipList.add(node.getOptionsList().get(i));
                    }
                }
            }


            int groupPos = (_mCallingClass.equalsIgnoreCase(PhysicalExamActivity.class.getSimpleName()) || (isAssociateSym && currentNode.getOptionsList().size() == 1)) ? 0 : pos;

            if (groupNode.getOption(groupPos).getText().equalsIgnoreCase(Node.ASSOCIATE_SYMPTOMS) || groupNode.getOption(groupPos).getText().equalsIgnoreCase("जुड़े लक्षण")) {
                associatedSysAdapter = new AssociatedSysAdapter(context, chipList, groupNode, groupPos, _mListener, _mCallingClass, pos);
                rvChips.setAdapter(associatedSysAdapter);

            } else {
                chipsAdapter = new ComplaintNodeListAdapter(context, chipList, groupNode, groupPos, _mListener, _mCallingClass, pos);
                rvChips.setAdapter(chipsAdapter);
            }
           /* btnConnectDeviceItem.setOnClickListener(v -> {
                if (context instanceof QuestionNodeActivity) {
                    ((QuestionNodeActivity) context).onAyuDeviceRequest(currentNode);
                }
            });

            tvSkipStethoscope.setOnClickListener(v -> {
                if (currentNode != null) {
                    skippedStethoscopeNodes.add(currentNode.getId());
                }
                llDigitalContainer.setVisibility(View.GONE);
            });*/
        }
    }


    class ComplaintNodeListAdapter extends RecyclerView.Adapter<ComplaintNodeListAdapter.ItemViewHolder> {
        private static final String TAG = "CNodeListAdapter";

        private Context mContext;
        private int layoutResourceID;
        private ImmutableList<Node> mNodes;
        private List<Node> mNodesFilter;
        private Node mGroupNode;
        private int mGroupPos;
        private QuestionsAdapter.FabClickListener _mListener;
        String _mCallingClass;
        private int physExamNodePos;

        public ComplaintNodeListAdapter(Context context, List<Node> nodes, Node groupNode, int groupPos,
                                        QuestionsAdapter.FabClickListener listener, String callingClass, int nodePos) {
            this.mContext = context;
            this.mNodesFilter = nodes;
            this.mNodes = ImmutableList.copyOf(mNodesFilter);
            mGroupNode = groupNode;
            mGroupPos = groupPos;
            this._mListener = listener;
            this._mCallingClass = callingClass;
            this.physExamNodePos = nodePos;
        }


        @NonNull
        @Override
        public ComplaintNodeListAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View row = inflater.inflate(R.layout.layout_chip, parent, false);
            return new ComplaintNodeListAdapter.ItemViewHolder(row);
        }

        @Override
        public void onBindViewHolder(@NonNull ComplaintNodeListAdapter.ItemViewHolder itemViewHolder, int position) {
            final Node thisNode = mNodesFilter.get(position);
            itemViewHolder.mChipText.setText(thisNode.findDisplay());

            Node groupNode = mGroupNode.getOption(mGroupPos);

            if ((groupNode.getText().equalsIgnoreCase(Node.ASSOCIATE_SYMPTOMS) && thisNode.isNoSelected()) || (groupNode.getText().equalsIgnoreCase("जुड़े लक्षण") && thisNode.isNoSelected()) || thisNode.isSelected()) {
                itemViewHolder.mChipText.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                itemViewHolder.mChipText.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_blue));
            } else {
                itemViewHolder.mChipText.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                itemViewHolder.mChipText.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_orange));
            }

            itemViewHolder.mChip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (groupNode.getText() != null) {
                        if (groupNode.getText().equalsIgnoreCase(Node.ASSOCIATE_SYMPTOMS) || groupNode.getText().equalsIgnoreCase("जुड़े लक्षण")) {
                            showAssociatedSymptomsDialog(thisNode);
                        } else {
                            String category = groupNode.getText().toLowerCase();
                            String item = thisNode.findDisplay().toLowerCase();
                            String fullDisplay = category + ": " + item;
                            String inputType = thisNode.getInputType();

                            String examRequirements = thisNode.getPhysicalExams();
                            boolean hasStethoscopeExam = examRequirements != null && (examRequirements.contains("heart_sound") || examRequirements.contains("lung_sound"));

                            if ((_mCallingClass.equalsIgnoreCase(PhysicalExamActivity.class.getSimpleName()) || hasStethoscopeExam) &&
                                    (fullDisplay.contains("heart_sound") || fullDisplay.contains("lung_sound") || hasStethoscopeExam || (inputType != null && inputType.equalsIgnoreCase("ayu_device")))) {

                                String patientUuid = "";
                                String visitUuid = "";
                                try {
                                    if (mContext instanceof PhysicalExamActivity) {
                                        PhysicalExamActivity activity = (PhysicalExamActivity) mContext;
                                        patientUuid = PhysicalExamActivity.patientUuid;
                                        visitUuid = PhysicalExamActivity.visitUuid;
                                    } else if (mContext instanceof QuestionNodeActivity) {
                                        QuestionNodeActivity activity = (QuestionNodeActivity) mContext;
                                        patientUuid = activity.patientUuid;
                                        visitUuid = activity.visitUuid;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
/*
                                InteleHealthDatabaseHelper db = new InteleHealthDatabaseHelper(mContext);
                                String encounterUuid = db.getEncounter(visitUuid);
                                String examType = thisNode.findDisplay();*/

                             /*   if (hasStethoscopeExam) {
                                    examType = examRequirements.contains("heart_sound") ? "heart" : "lung";
                                } else if (inputType != null && inputType.equalsIgnoreCase("ayu_device")) {
                                    examType = thisNode.getText().toLowerCase().contains("heart") ? "heart" : "lung";
                                }

                                DigitalStethoscopeDialogFragment dialog = DigitalStethoscopeDialogFragment.newInstance(examType, patientUuid, visitUuid, encounterUuid);
                                dialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), "stethoscope_popup");*/
                            } else {
                                int indexOfCheckedNode;
                                if (_mCallingClass.equalsIgnoreCase(PhysicalExamActivity.class.getSimpleName())) {
                                    indexOfCheckedNode = position;
                                } else {
                                    List<Node> childNode = mGroupNode.getOptionsList().get(mGroupPos).getOptionsList();
                                    indexOfCheckedNode = childNode.indexOf(thisNode);
                                }
                                _mListener.onChildListClickEvent(mGroupPos, indexOfCheckedNode, physExamNodePos);
                                notifyDataSetChanged();
                            }
                        }
                    } else {
                        Toast.makeText(mContext, context.getString(R.string.some_issue_with_mindmap), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        private void showAssociatedSymptomsDialog(Node thisNode) {
            MaterialAlertDialogBuilder confirmDialog = new MaterialAlertDialogBuilder(context);
            confirmDialog.setTitle(R.string.have_symptom);
            confirmDialog.setCancelable(false);
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View convertView = layoutInflater.inflate(R.layout.list_expandable_item_radio, null);
            confirmDialog.setView(convertView);
            RadioButton radio_yes = convertView.findViewById(R.id.radio_yes);
            RadioButton radio_no = convertView.findViewById(R.id.radio_no);
            confirmDialog.setPositiveButton(context.getString(R.string.ok), (dialog, int_which) -> dialog.dismiss());
            AlertDialog alertDialog = confirmDialog.create();

            radio_yes.setOnClickListener(v -> {
                thisNode.setNoSelected(false);
                List<Node> childNode = mGroupNode.getOptionsList().get(mGroupPos).getOptionsList();
                int indexOfCheckedNode = childNode.indexOf(thisNode);
                _mListener.onChildListClickEvent(mGroupPos, indexOfCheckedNode, physExamNodePos);
                notifyDataSetChanged();
                alertDialog.dismiss();
            });

            radio_no.setOnClickListener(v -> {
                thisNode.setNoSelected(true);
                thisNode.setUnselected();
                notifyDataSetChanged();
                alertDialog.dismiss();
            });

            if (_mCallingClass.equalsIgnoreCase("ComplaintNodeActivity")) {
                if (thisNode.isSelected()) radio_yes.setChecked(true);
                else radio_no.setChecked(true);
            } else {
                if (thisNode.isSelected()) radio_yes.setChecked(true);
                else radio_no.setChecked(thisNode.isNoSelected());
            }

            alertDialog.show();
            IntelehealthApplication.setAlertDialogCustomTheme(context, alertDialog);
        }

        @Override
        public int getItemCount() {
            return (mNodesFilter != null ? mNodesFilter.size() : 0);
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView mChipText;
            RelativeLayout mChip;

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                mChip = itemView.findViewById(R.id.complaint_chip);
                mChipText = itemView.findViewById(R.id.tvChipText);
            }
        }


        public ImmutableList<Node> getmNodes() {
            return mNodes;
        }
    }


    private boolean hasRecursiveStethoscopeRequirement(Node node) {
        if (node == null) return false;
        String examRequirements = node.getPhysicalExams();
        if (examRequirements != null && (examRequirements.toLowerCase().contains("heart_sound") || examRequirements.toLowerCase().contains("lung_sound"))) {
            return true;
        }
        if (node.getOptionsList() != null) {
            for (Node child : node.getOptionsList()) {
                if (hasRecursiveStethoscopeRequirement(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateStethoscopeCounts(Node node, ChipsAdapterViewHolder holder) {
        int heartCount = 0;
        int lungCount = 0;
        List<Node> allNodes = getAllNodesRecursive(node, new ArrayList<>());
        for (Node n : allNodes) {
            String exams = n.getPhysicalExams();
            if (exams != null) {
                if (exams.toLowerCase().contains("heart_sound")) heartCount++;
                if (exams.toLowerCase().contains("lung_sound")) lungCount++;
            }
        }
        int total = heartCount + lungCount;
        holder.tvUpcomingCount.setText(total + " Upcoming");
        holder.tvHeartPending.setText("0/" + heartCount + " PENDING");
        holder.tvLungPending.setText("0/" + lungCount + " PENDING");
    }

    private List<Node> getAllNodesRecursive(Node node, List<Node> nodes) {
        if (node == null) return nodes;
        nodes.add(node);
        if (node.getOptionsList() != null) {
            for (Node child : node.getOptionsList()) {
                getAllNodesRecursive(child, nodes);
            }
        }
        return nodes;
    }

    public static <T> List<List<T>> partitionList(List<T> list, int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Invalid  size to partition: " + chunkSize);
        }
        List<List<T>> chunkList = new ArrayList<>(list.size() / chunkSize);
        for (int i = 0; i < list.size(); i += chunkSize) {
            chunkList.add(list.subList(i, i + chunkSize >= list.size() ? list.size() - 1 : i + chunkSize));
        }
        return chunkList;
    }


}
