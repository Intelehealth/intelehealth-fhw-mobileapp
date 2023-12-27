package org.intelehealth.ekalarogya.activities.complaintNodeActivity;

import android.content.Context;

import android.util.Log;
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

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.knowledgeEngine.Node;

public class SuggestedComplaintNodeListAdapter extends RecyclerView.Adapter<SuggestedComplaintNodeListAdapter.ItemViewHolder> {
    private static final  String TAG = "SCNodeListAdapter";

    private Context mContext;
    private int layoutResourceID;
    private ImmutableList<Node> mNodes;
    private List<Node> mNodesFilter;

    public  SuggestedComplaintNodeListAdapter(Context context, List<Node> nodes){
        this.mContext = context;
        this.mNodesFilter = nodes;
        this.mNodes= ImmutableList.copyOf(mNodesFilter);
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.layout_suggested_complaint_chip, parent, false);
        return new ItemViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestedComplaintNodeListAdapter.ItemViewHolder itemViewHolder, int position) {
        final Node thisNode = mNodesFilter.get(position);
        itemViewHolder.mChipText.setText(thisNode.findDisplay());
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
                if(!thisNode.isSelected()) {
                    thisNode.setSelected(true);
                    itemViewHolder.mChipText.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    itemViewHolder.mChipText.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_blue));

                }else {
                    itemViewHolder.mChipText.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                    thisNode.setSelected(false);
                    itemViewHolder.mChipText.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_orange));
                }
                notifyDataSetChanged();
            }
        });
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
        Log.i(TAG, "filter: Entered Filter");
        Log.i(TAG, "filter: "+ mNodes.size());
        Log.i(TAG, "filter: "+ mNodesFilter.size());
        mNodesFilter.clear();
        Log.i(TAG, "filter: "+ mNodes.size());
        Log.i(TAG, "filter: "+ mNodesFilter.size());
        charText = charText.toLowerCase(Locale.getDefault());
        Log.i(TAG, "filter: "+charText);
        if (!charText.trim().isEmpty()) {
            Log.i(TAG, "filter: Not Empty" );
            for (Node node : mNodes) {
                Log.i(TAG, "filter: " + node.getText());
                Log.i(TAG, "filter: " + node.findDisplay());
                if (!node.findDisplay().isEmpty()) {
                    if (node.findDisplay().toLowerCase(Locale.getDefault())
                            .contains(charText)) {
                        mNodesFilter.add(node);
                        Log.i(TAG, "filter: Node Matched");
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
