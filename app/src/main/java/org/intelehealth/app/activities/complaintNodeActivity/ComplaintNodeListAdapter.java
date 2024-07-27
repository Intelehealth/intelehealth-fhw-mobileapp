package org.intelehealth.app.activities.complaintNodeActivity;

import android.content.Context;

import org.intelehealth.app.utilities.CustomLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Locale;

import org.intelehealth.app.R;
import org.intelehealth.app.knowledgeEngine.Node;

public class ComplaintNodeListAdapter extends RecyclerView.Adapter<ComplaintNodeListAdapter.ItemViewHolder> {
    private static final  String TAG = "CNodeListAdapter";

    private Context mContext;
    private int layoutResourceID;
    private ImmutableList<Node> mNodes;
    private List<Node> mNodesFilter;

    public  ComplaintNodeListAdapter(Context context, List<Node> nodes){
        this.mContext = context;
        this.mNodesFilter = nodes;
        this.mNodes= ImmutableList.copyOf(mNodesFilter);
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.layout_chip, parent, false);
        return new ItemViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintNodeListAdapter.ItemViewHolder itemViewHolder, int position) {
        final Node thisNode = mNodesFilter.get(position);
        itemViewHolder.mChipText.setText(thisNode.findDisplay());


       /* .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                .setBackground(ContextCompat.getDrawable(mContext,R.drawable.rounded_rectangle_blue));
                CustomLog.e("Set View","clicked");
            }
        });*/
       /* if(thisNode.isSelected()){
            itemViewHolder.mChip.setCloseIconVisible(true);
            itemViewHolder.mChip.setChipBackgroundColor((ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorAccent))));
            itemViewHolder.mChip.setTextColor((ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.white))));
        }else{
            itemViewHolder.mChip.setCloseIconVisible(false);
            itemViewHolder.mChip.setChipBackgroundColor((ColorStateList.valueOf(ContextCompat.getColor(mContext, android.R.color.transparent))));
            itemViewHolder.mChip.setTextColor((ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.primary_text))));
        }*/
       if(thisNode.isSelected())
       {
           itemViewHolder.mChipText.setTextColor(ContextCompat.getColor(mContext, R.color.white));
           itemViewHolder.mChipText.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_blue));
       }
       else
       {
           itemViewHolder.mChipText.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_orange));
           itemViewHolder.mChipText.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
       }
        itemViewHolder.mChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   CustomLog.e("Pos",position+"");

               // itemViewHolder.mChipText.setTextColor(ContextCompat.getColor(mContext,R.color.amber));
                if(!thisNode.isSelected()) {
                    thisNode.setSelected(true);
                    itemViewHolder.mChipText.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    itemViewHolder.mChipText.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_blue));

                }else {
                    itemViewHolder.mChipText.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                    thisNode.setSelected(false);
                    itemViewHolder.mChipText.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_orange));
                }

                // notifyItemChanged(position);
                //thisNode.toggleSelected();
              //  notify();
                notifyDataSetChanged();


                //
            }
        });

       /* itemViewHolder.mChip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisNode.toggleSelected();
                notifyDataSetChanged();
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return (mNodesFilter!=  null? mNodesFilter.size():0 );
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout mChip;
        TextView mChipText;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            mChip = itemView.findViewById(R.id.complaint_chip);
            mChipText = itemView.findViewById(R.id.tvChipText);
        }



    }


    // Filter Class
    public void filter(String charText) {
        CustomLog.i(TAG, "filter: Entered Filter");
        CustomLog.i(TAG, "filter: "+ mNodes.size());
        CustomLog.i(TAG, "filter: "+ mNodesFilter.size());
        mNodesFilter.clear();
        CustomLog.i(TAG, "filter: "+ mNodes.size());
        CustomLog.i(TAG, "filter: "+ mNodesFilter.size());
        charText = charText.toLowerCase(Locale.getDefault());
        CustomLog.i(TAG, "filter: "+charText);
        if (!charText.trim().isEmpty()) {
            CustomLog.i(TAG, "filter: Not Empty" );
            for (Node node : mNodes) {
                CustomLog.i(TAG, "filter: " + node.getText());
                CustomLog.i(TAG, "filter: " + node.findDisplay());
                if (!node.findDisplay().isEmpty()) {
                    if (node.findDisplay().toLowerCase(Locale.getDefault())
                            .contains(charText)) {
                        mNodesFilter.add(node);
                        CustomLog.i(TAG, "filter: Node Matched");
                    }
                }
            }
        } else {
            mNodesFilter.addAll(mNodes);
        }
        notifyDataSetChanged();
    }

    public ImmutableList<Node> getmNodes() {
        return mNodes;
    }
}
