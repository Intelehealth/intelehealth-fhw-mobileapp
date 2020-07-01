package app.intelehealth.client.activities.complaintNodeActivity;

import android.content.Context;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Locale;

import app.intelehealth.client.R;
import app.intelehealth.client.knowledgeEngine.Node;

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
                Log.e("Set View","clicked");
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
             //   Log.e("Pos",position+"");

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
