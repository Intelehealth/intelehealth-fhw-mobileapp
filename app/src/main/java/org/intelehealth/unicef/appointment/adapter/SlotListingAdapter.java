package org.intelehealth.unicef.appointment.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;


import org.intelehealth.unicef.R;
import org.intelehealth.unicef.appointment.model.SlotInfo;
import org.intelehealth.unicef.utilities.SessionManager;
import org.intelehealth.unicef.utilities.StringUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SlotListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<SlotInfo> mItemList = new ArrayList<SlotInfo>();

    public interface OnItemSelection {
        public void onSelect(SlotInfo slotInfo);
    }

    private OnItemSelection mOnItemSelection;

    public SlotListingAdapter(RecyclerView recyclerView, Context context, List<SlotInfo> itemList, OnItemSelection onItemSelection) {
        mContext = context;
        mItemList = itemList;
        mOnItemSelection = onItemSelection;
        //mAnimator = new RecyclerViewAnimator(recyclerView);
    }

    private JSONObject mThisScreenLanguageJsonObject = new JSONObject();

    public void setLabelJSON(JSONObject json) {
        mThisScreenLanguageJsonObject = json;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.appointment_slot_listing_view, parent, false);
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
            genericViewHolder.slotInfo = mItemList.get(position);

            genericViewHolder.tvTime.setText(genericViewHolder.slotInfo.getSlotTime());
            genericViewHolder.tvDuration.setText(String.format("%d %s", genericViewHolder.slotInfo.getSlotDuration(),
                    mContext.getString(R.string.minutes_txt)));

        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvDuration;
        RelativeLayout rlRoot;
        SlotInfo slotInfo;

        GenericViewHolder(View itemView) {
            super(itemView);

            tvTime = itemView.findViewById(R.id.tvTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);

            rlRoot = itemView.findViewById(R.id.rlRoot);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                            .setMessage(mContext.getResources().getString(R.string.appointment_booking_confirmation_txt)
                                    + "\n\n" + mContext.getString(R.string.slot_info) + "- \n" + slotInfo.getSlotDate()
                                    + "\n" + slotInfo.getSlotTime()
                                    + "\n" + StringUtils.getTranslatedDays(slotInfo.getSlotDay(), new SessionManager(mContext).getAppLanguage())
                            )
                            //set positive button
                            .setPositiveButton(mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mOnItemSelection.onSelect(slotInfo);
                                }
                            })
                            //set negative button
                            .setNegativeButton(mContext.getString(R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .show();

                }
            });

        }


    }


}

