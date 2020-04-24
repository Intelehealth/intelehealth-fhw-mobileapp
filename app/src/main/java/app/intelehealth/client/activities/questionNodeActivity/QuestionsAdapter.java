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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.intelehealth.client.R;
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

    interface FabClickListener{
        void fabClickedAtEnd(Node node);
        void onChildListClickEvent(Node node, int groupPos,int childPos);
    }

    public QuestionsAdapter(Context _context,  Node node, RecyclerView _rvQuestions,String callingClass,FabClickListener _mListener) {
        this.context = _context;
        this.currentNode = node;
        this.recyclerView = _rvQuestions;
        this._mCallingClass = callingClass;
        this._mListener = _mListener;

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
        holder.tvQuestion.setText(_mNode.getOptionsList().get(position).findDisplay());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                ((QuestionNodeActivity) context).AnimateView(holder.ivAyu);
                ((QuestionNodeActivity) context).AnimateView(holder.tvQuestion);
                ((QuestionNodeActivity) context).AnimateView(holder.tvSwipe);
            }
        });

        if (position != 0) {
            holder.tvSwipe.setVisibility(View.VISIBLE);
        } else {
            holder.tvSwipe.setVisibility(View.GONE);
        }

        if (position == getItemCount()-1) {
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
        return currentNode.getOptionsList().size();
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
            Node node = currentNode.getOptionsList().get(pos);
            for (int i = 0; i < node.getOptionsList().size(); i++) {
                chipList.add(node.getOptionsList().get(i));
            }

            chipsAdapter = new ComplaintNodeListAdapter(context, chipList,currentNode,pos,_mListener,_mCallingClass);
            rvChips.setAdapter(chipsAdapter);

        }
    }



    class ComplaintNodeListAdapter extends RecyclerView.Adapter<ComplaintNodeListAdapter.ItemViewHolder> {
        private static final  String TAG = "CNodeListAdapter";

        private Context mContext;
        private int layoutResourceID;
        private ImmutableList<Node> mNodes;
        private List<Node> mNodesFilter;
        private Node mGroupNode;
        private  int mGroupPos;
        private QuestionsAdapter.FabClickListener _mListener;
        String _mCallingClass;

        public ComplaintNodeListAdapter(Context context, List<Node> nodes, Node groupNode, int groupPos,
                                        QuestionsAdapter.FabClickListener listener,String callingClass){
            this.mContext = context;
            this.mNodesFilter = nodes;
            this.mNodes= ImmutableList.copyOf(mNodesFilter);
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
            if(groupNode.getText().equalsIgnoreCase("Associated symptoms")){
                switch (_mCallingClass) {
                    case "ComplaintNodeActivity":
                        itemViewHolder.mChip.setChecked(thisNode.isSelected());
                        break;
                    default:
                        if (thisNode.isSelected()) {
                            if(thisNode.findDisplay().equalsIgnoreCase("yes")){
                                itemViewHolder.mChip.setChecked(true);
                            }
                        } else {
                            if(thisNode.findDisplay().equalsIgnoreCase("No")){
                                if (thisNode.isNoSelected()) {
                                    itemViewHolder.mChip.setChecked(true);
                                } else {
                                    itemViewHolder.mChip.setChecked(false);
                                }
                            }

                        }
                        break;
                }
            }else{
                itemViewHolder.mChip.setChecked(thisNode.isSelected());
            }
            if(thisNode.isSelected()){
                itemViewHolder.mChip.setCloseIconVisible(true);
                itemViewHolder.mChip.setChipBackgroundColor((ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorAccent))));
                itemViewHolder.mChip.setTextColor((ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.white))));
            }else{
                itemViewHolder.mChip.setCloseIconVisible(false);
                itemViewHolder.mChip.setChipBackgroundColor((ColorStateList.valueOf(ContextCompat.getColor(mContext, android.R.color.transparent))));
                itemViewHolder.mChip.setTextColor((ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.primary_text))));
            }
            itemViewHolder.mChip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(groupNode.getText().equalsIgnoreCase("Associated symptoms")){
                        if(thisNode.findDisplay().equalsIgnoreCase("yes")){
                            thisNode.setNoSelected(false);
                           // _mListener.onChildListClickEvent(mGroupNode,mGroupPos,position);
                        }else{
                            thisNode.setNoSelected(true);
                            thisNode.setUnselected();
                        }
                    }else{
                        //thisNode.toggleSelected();
                    }
                    _mListener.onChildListClickEvent(mGroupNode,mGroupPos,position);
                   notifyDataSetChanged();
                }
            });

            itemViewHolder.mChip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    thisNode.toggleSelected();
                    notifyDataSetChanged();
                    _mListener.onChildListClickEvent(mGroupNode,mGroupPos,position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return (mNodesFilter!=  null? mNodesFilter.size():0 );
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


    class CustomLinearLayoutManager extends LinearLayoutManager {

        //private static final String TAG = CustomLinearLayoutManager.class.getSimpleName();

        public CustomLinearLayoutManager(Context context) {
            super(context);
        }

        public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        private int[] mMeasuredDimension = new int[2];

        @Override
        public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {

            final int widthMode = View.MeasureSpec.getMode(widthSpec);
            final int heightMode = View.MeasureSpec.getMode(heightSpec);
            final int widthSize = View.MeasureSpec.getSize(widthSpec);
            final int heightSize = View.MeasureSpec.getSize(heightSpec);

            int width = 0;
            int height = 0;
            for (int i = 0; i < getItemCount(); i++) {
                measureScrapChild(recycler, i, View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                        mMeasuredDimension);


                if (getOrientation() == HORIZONTAL) {
                    width = width + mMeasuredDimension[0];
                    if (i == 0) {
                        height = mMeasuredDimension[1];
                    }
                } else {
                    height = height + mMeasuredDimension[1];
                    if (i == 0) {
                        width = mMeasuredDimension[0];
                    }
                }
            }
            switch (widthMode) {
                case View.MeasureSpec.EXACTLY:
                    width = widthSize;
                case View.MeasureSpec.AT_MOST:
                case View.MeasureSpec.UNSPECIFIED:
            }

            switch (heightMode) {
                case View.MeasureSpec.EXACTLY:
                    height = heightSize;
                case View.MeasureSpec.AT_MOST:
                case View.MeasureSpec.UNSPECIFIED:
            }

            setMeasuredDimension(width, height);
        }

        private void measureScrapChild(RecyclerView.Recycler recycler, int position, int widthSpec,
                                       int heightSpec, int[] measuredDimension) {
            try {
                View view = recycler.getViewForPosition(0);//fix IndexOutOfBoundsException

                if (view != null) {
                    RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();

                    int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                            getPaddingLeft() + getPaddingRight(), p.width);

                    int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                            getPaddingTop() + getPaddingBottom(), p.height);

                    view.measure(childWidthSpec, childHeightSpec);
                    measuredDimension[0] = view.getMeasuredWidth() + p.leftMargin + p.rightMargin;
                    measuredDimension[1] = view.getMeasuredHeight() + p.bottomMargin + p.topMargin;
                    recycler.recycleView(view);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }






}



