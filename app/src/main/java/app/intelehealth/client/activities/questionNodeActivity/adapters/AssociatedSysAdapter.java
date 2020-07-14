package app.intelehealth.client.activities.questionNodeActivity.adapters;

import android.content.Context;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.collect.ImmutableList;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import app.intelehealth.client.R;
import app.intelehealth.client.activities.physcialExamActivity.PhysicalExamActivity;
import app.intelehealth.client.activities.questionNodeActivity.QuestionsAdapter;
import app.intelehealth.client.app.IntelehealthApplication;
import app.intelehealth.client.knowledgeEngine.Node;
/**
 * Created by Shubham mittal
 */
public class AssociatedSysAdapter extends RecyclerView.Adapter<AssociatedSysAdapter.ItemViewHolder> {





    public interface FabVisibility {
        void setVisibility(boolean data);




    }
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

        public AssociatedSysAdapter(Context mContext, List<Node> nodes, Node groupNode, int groupPos,
                                    QuestionsAdapter.FabClickListener listener, String callingClass, int nodePos) {
            this.mContext = mContext;
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
        public AssociatedSysAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View row = inflater.inflate(R.layout.associated_sym_layout, parent, false);
            return new AssociatedSysAdapter.ItemViewHolder(row);
        }

        @Override
        public void onBindViewHolder(@NonNull AssociatedSysAdapter.ItemViewHolder itemViewHolder, int position) {
            final Node thisNode = mNodesFilter.get(position);
            itemViewHolder.symp.setText(thisNode.findDisplay());

            Node groupNode = mGroupNode.getOption(mGroupPos);
            itemViewHolder.chip_No.setTextColor(ContextCompat.getColor(mContext, R.color.red));

           // itemViewHolder.chip_yes.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_blue));
            //itemViewHolder.chip_No.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_blue));

            if ( thisNode.isSelected() && thisNode.getAssociated_symptoms()==1) {
               itemViewHolder.chip_yes.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                itemViewHolder.chip_yes.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_blue));
                itemViewHolder.chip_No.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                itemViewHolder.chip_No.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_orange));
            } else if(!thisNode.isSelected() && thisNode.getAssociated_symptoms()==-1) {
                itemViewHolder.chip_No.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                itemViewHolder.chip_No.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_blue));
                itemViewHolder.chip_yes.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                itemViewHolder.chip_yes.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_orange));
            }
            else{
                itemViewHolder.chip_No.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                itemViewHolder.chip_No.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_orange));
                itemViewHolder.chip_yes.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                itemViewHolder.chip_yes.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_orange));

            }




                        itemViewHolder.chip_yes.setOnClickListener(v -> {
                                thisNode.setNoSelected(false);

                           if(thisNode.getAssociated_symptoms()==1){
                                thisNode.setAssociated_symptoms(0);
                            }
                            else{
                                thisNode.setAssociated_symptoms(1);
                            }

                            List<Node> childNode = mGroupNode.getOptionsList().get(mGroupPos).getOptionsList();
                            int indexOfCheckedNode = childNode.indexOf(thisNode);
                            _mListener.onChildListClickEvent(mGroupPos, indexOfCheckedNode, physExamNodePos);
                            notifyDataSetChanged();
                        });

                        itemViewHolder.chip_No.setOnClickListener(v -> {

                            // To manage to unselected state

                            if(thisNode.isNoSelected()) {
                                thisNode.setNoSelected(false);

                            }else{
                                thisNode.setNoSelected(true);
                            }
                                thisNode.setUnselected();


                            if (thisNode.getAssociated_symptoms() == -1) {
                                thisNode.setAssociated_symptoms(0);
                            } else {
                                thisNode.setAssociated_symptoms(-1);
                            }
                            notifyDataSetChanged();
                        });

                        Log.e("->>>",thisNode.isNoSelected()+"");



        }

        @Override
        public int getItemCount() {
            return (mNodesFilter != null ? mNodesFilter.size() : 0);
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView chip_yes;
                TextView chip_No;
                TextView symp;
                LinearLayout linearLayout;


            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                symp = itemView.findViewById(R.id.symp);
                chip_No = itemView.findViewById(R.id.chip_no);
                chip_yes = itemView.findViewById(R.id.chip_yes);
                linearLayout=itemView.findViewById(R.id.LL_associated);
            }
        }


        public ImmutableList<Node> getmNodes() {
            return mNodes;
        }
    }
