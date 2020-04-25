package app.intelehealth.client.activities.questionNodeActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.intelehealth.client.R;
import app.intelehealth.client.activities.pastMedicalHistoryActivity.PastMedicalHistoryActivity;
import app.intelehealth.client.knowledgeEngine.Node;

/**
 * Created by Sagar Shimpi
 * Github - TheSeasApps
 */
public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.ChipsAdapterViewHolder> {

    LayoutInflater layoutInflater;
    Context context;
    Node currentNode;
    int pos;
    RecyclerView recyclerView;
    FabClickListener _mListener;
    String _mCallingClass;
    boolean isAssociateSym;

    public interface FabClickListener {
        void fabClickedAtEnd(Node node);

        void onChildListClickEvent(Node node, int groupPos, int childPos);
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

    @Override
    public QuestionsAdapter.ChipsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.quesionnode_list_item, parent, false);
        return new ChipsAdapterViewHolder(row);
    }

    @Override
    public void onBindViewHolder(QuestionsAdapter.ChipsAdapterViewHolder holder, int position) {
        Node _mNode = currentNode;
        if (isAssociateSym && currentNode.getOptionsList().size() == 1) {
            holder.tvQuestion.setText(_mNode.getOptionsList().get(0).findDisplay());
        } else {
            holder.tvQuestion.setText(_mNode.getOptionsList().get(position).findDisplay());
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(_mCallingClass.equalsIgnoreCase(QuestionNodeActivity.class.getSimpleName())){
                    ((QuestionNodeActivity) context).AnimateView(holder.ivAyu);
                    ((QuestionNodeActivity) context).AnimateView(holder.tvQuestion);
                    ((QuestionNodeActivity) context).AnimateView(holder.tvSwipe);
                }else if(_mCallingClass.equalsIgnoreCase(PastMedicalHistoryActivity.class.getSimpleName())){
                    ((PastMedicalHistoryActivity) context).AnimateView(holder.ivAyu);
                    ((PastMedicalHistoryActivity) context).AnimateView(holder.tvQuestion);
                    ((PastMedicalHistoryActivity) context).AnimateView(holder.tvSwipe);
                }

            }
        });

        if (position != 0) {
            holder.tvSwipe.setVisibility(View.VISIBLE);
        } else {
            holder.tvSwipe.setVisibility(View.GONE);
        }

        if (position == getItemCount() - 1) {
            holder.rvChips.setNestedScrollingEnabled(true);
            recyclerView.setNestedScrollingEnabled(false);
            holder.fab.setVisibility(View.VISIBLE);
        } else {
            holder.rvChips.setNestedScrollingEnabled(false);
            recyclerView.setNestedScrollingEnabled(true);
            holder.fab.setVisibility(View.INVISIBLE);
        }


        holder.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*context.startActivity(new Intent(context, PhysicalExamActivity.class));
                ((QuestionNodeActivity) context).finish();*/
                _mListener.fabClickedAtEnd(currentNode);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (isAssociateSym && currentNode.getOptionsList().size() == 1) {
            List<Node> nodeList = currentNode.getOptionsList().get(0).getOptionsList();
            if (nodeList.size() > 5) {
                int optionChildToAddCount = (nodeList.size() / 5) + ((nodeList.size() % 5) == 0 ? 0 : 1);
                List<List<Node>> spiltList = Lists.partition(currentNode.getOptionsList().get(0).getOptionsList(), 5);
                //List<List<Node>> spiltList = partitionList(currentNode.getOptionsList().get(0).getOptionsList(),5);
                Log.e("TAG", "SPLITLIST COUNT: " + spiltList.size());
                return spiltList.size();
            } else {
                return currentNode.getOptionsList().size();
            }
        } else {
            return currentNode.getOptionsList().size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        pos = position;
        return position;
    }

    public class ChipsAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView tvQuestion;
        TextView tvSwipe;
        ImageView ivAyu;
        RecyclerView rvChips;
        FloatingActionButton fab;
        ComplaintNodeListAdapter chipsAdapter;


        public ChipsAdapterViewHolder(View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_complaintQuestion);
            rvChips = itemView.findViewById(R.id.rv_chips);
            fab = itemView.findViewById(R.id.fab);
            tvSwipe = itemView.findViewById(R.id.tv_swipe);


           /* RecyclerView.LayoutManager layoutManager = new CustomLinearLayoutManager(context);
            layoutManager.setAutoMeasureEnabled(true);
            rvChips.setLayoutManager(layoutManager);*/

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            //linearLayoutManager.setAutoMeasureEnabled(true);
            rvChips.setLayoutManager(linearLayoutManager);

           /* StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL);
            rvChips.setLayoutManager(staggeredGridLayoutManager);*/

            rvChips.setItemAnimator(new DefaultItemAnimator());
            rvChips.setNestedScrollingEnabled(true);

            List<Node> chipList = new ArrayList<>();
            if (isAssociateSym && currentNode.getOptionsList().size() == 1) {
                int childOptionCount = currentNode.getOptionsList().get(0).getOptionsList().size();
                if (childOptionCount > 5) {
                    //  int optionChildToAddCount = (childOptionCount / 5) + ((childOptionCount % 5) == 0 ? 0 : 1);
                    // List<List<Node>> spiltList = partitionList(currentNode.getOptionsList().get(0).getOptionsList(),5);
                    List<List<Node>> spiltList = Lists.partition(currentNode.getOptionsList().get(0).getOptionsList(), 5);
                    chipList.addAll(spiltList.get(pos));
                } else {
                    Node node = currentNode.getOptionsList().get(0);
                    for (int i = 0; i < node.getOptionsList().size(); i++) {
                        chipList.add(node.getOptionsList().get(i));
                    }
                }
            } else {
                Node node = currentNode.getOptionsList().get(pos);
                for (int i = 0; i < node.getOptionsList().size(); i++) {
                    chipList.add(node.getOptionsList().get(i));
                }
            }


            int groupPos = (isAssociateSym && currentNode.getOptionsList().size() == 1) ? 0 : pos;
            chipsAdapter = new ComplaintNodeListAdapter(context, chipList, currentNode, groupPos, _mListener, _mCallingClass);
            rvChips.setAdapter(chipsAdapter);

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

        public ComplaintNodeListAdapter(Context context, List<Node> nodes, Node groupNode, int groupPos,
                                        QuestionsAdapter.FabClickListener listener, String callingClass) {
            this.mContext = context;
            this.mNodesFilter = nodes;
            this.mNodes = ImmutableList.copyOf(mNodesFilter);
            mGroupNode = groupNode;
            mGroupPos = groupPos;
            this._mListener = listener;
            this._mCallingClass = callingClass;
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
            itemViewHolder.mChip.setText(thisNode.findDisplay());
            Node groupNode = mGroupNode.getOption(mGroupPos);
            if (groupNode.getText().equalsIgnoreCase("Associated symptoms")) {
                switch (_mCallingClass) {
                    case "ComplaintNodeActivity":
                        itemViewHolder.mChip.setChecked(thisNode.isSelected());
                        break;
                    default:
                        if (thisNode.isSelected()) {
                            if (thisNode.findDisplay().equalsIgnoreCase("yes")) {
                                itemViewHolder.mChip.setChecked(true);
                            }
                        } else {
                            if (thisNode.findDisplay().equalsIgnoreCase("No")) {
                                if (thisNode.isNoSelected()) {
                                    itemViewHolder.mChip.setChecked(true);
                                } else {
                                    itemViewHolder.mChip.setChecked(false);
                                }
                            }

                        }
                        break;
                }
            } else {
                itemViewHolder.mChip.setChecked(thisNode.isSelected());
            }
            if (thisNode.isSelected()) {
                itemViewHolder.mChip.setCloseIconVisible(true);
                itemViewHolder.mChip.setChipBackgroundColor((ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorAccent))));
                itemViewHolder.mChip.setTextColor((ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.white))));
            } else {
                itemViewHolder.mChip.setCloseIconVisible(false);
                itemViewHolder.mChip.setChipBackgroundColor((ColorStateList.valueOf(ContextCompat.getColor(mContext, android.R.color.transparent))));
                itemViewHolder.mChip.setTextColor((ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.primary_text))));
            }
            itemViewHolder.mChip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (groupNode.getText().equalsIgnoreCase("Associated symptoms")) {
                        if (thisNode.findDisplay().equalsIgnoreCase("yes")) {
                            thisNode.setNoSelected(false);
                            // _mListener.onChildListClickEvent(mGroupNode,mGroupPos,position);
                        } else {
                            thisNode.setNoSelected(true);
                            thisNode.setUnselected();
                        }
                    } else {
                        //thisNode.toggleSelected();
                    }
                    List<Node> childNode  = mGroupNode.getOptionsList().get(mGroupPos).getOptionsList();
                    int indexOfCheckedNode = childNode.indexOf(thisNode);
                    _mListener.onChildListClickEvent(mGroupNode, mGroupPos, indexOfCheckedNode);
                    notifyDataSetChanged();
                }
            });

            itemViewHolder.mChip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    thisNode.toggleSelected();
                    List<Node> childNode  = mGroupNode.getOptionsList().get(mGroupPos).getOptionsList();
                    int indexOfCheckedNode = childNode.indexOf(thisNode);
                    _mListener.onChildListClickEvent(mGroupNode, mGroupPos, indexOfCheckedNode);
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return (mNodesFilter != null ? mNodesFilter.size() : 0);
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            Chip mChip;

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                mChip = itemView.findViewById(R.id.complaint_chip);
            }
        }


        public ImmutableList<Node> getmNodes() {
            return mNodes;
        }
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



