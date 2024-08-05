package org.intelehealth.app.appointmentNew;

import android.content.Context;
import android.content.DialogInterface;
import org.intelehealth.app.utilities.CustomLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.appointment.model.SlotInfo;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.json.JSONObject;

import java.util.List;

public class PickUpTimeSlotsAdapter extends RecyclerView.Adapter<PickUpTimeSlotsAdapter.GenericViewHolder> {
    private static final String TAG = "PickUpTimeSlotsAdapter";
    Context context;
    List<SlotInfo> mItemList;
    private OnItemSelection mOnItemSelection;
    String appointmentSlot;
    //   OnItemClickListener listener;
    private int selectedPos = -1;

    public interface OnItemSelection {
        public void onSelect(SlotInfo slotInfo);
    }

    public PickUpTimeSlotsAdapter(Context context, List<SlotInfo> itemList, String appointmentSlot,
                                  OnItemSelection onItemSelection) {
        this.context = context;
        this.mItemList = itemList;
        this.appointmentSlot = appointmentSlot;
        this.mOnItemSelection = onItemSelection;

    }

    @Override
    public PickUpTimeSlotsAdapter.GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pick_up_time_slot_ui2, parent, false);
        return new GenericViewHolder(itemView);
    }

    private JSONObject mThisScreenLanguageJsonObject = new JSONObject();

    public void setLabelJSON(JSONObject json) {
        mThisScreenLanguageJsonObject = json;
    }

    @Override
    public void onBindViewHolder(PickUpTimeSlotsAdapter.GenericViewHolder holder, int position) {
        if (holder instanceof GenericViewHolder) {
            GenericViewHolder genericViewHolder = (GenericViewHolder) holder;
            genericViewHolder.slotInfo = mItemList.get(position);

            genericViewHolder.tvTime.setText(genericViewHolder.slotInfo.getSlotTime().toLowerCase());
            genericViewHolder.tvDuration.setText(String.format("%d %s", genericViewHolder.slotInfo.getSlotDuration(), context.getString(R.string.minutes_txt)));
            changeToSelect(selectedPos, position, holder);

            holder.layoutParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemSelection != null) {
                        //  int position = getAdapterPosition();
                        CustomLog.d(TAG, "onClick: getAdapterPosition : " + holder.getBindingAdapterPosition());
                        CustomLog.d(TAG, "onClick: RecyclerView.NO_POSITION : " + RecyclerView.NO_POSITION);

                        if (holder.getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                            mOnItemSelection.onSelect(holder.slotInfo);
                            notifyItemChanged(selectedPos);
                            selectedPos = holder.getBindingAdapterPosition();
                            notifyItemChanged(selectedPos);
                        }
                    } else {
                        CustomLog.d(TAG, "onClick:listener is null");
                    }
                }
            });
        }
    }

    public void changeToSelect(int selectedPos, int position, PickUpTimeSlotsAdapter.GenericViewHolder holder) {
        holder.layoutParent.setSelected(selectedPos == position);
//        if (selectedPos == position) {
//            holder.tvTime.setTextColor(ContextCompat.getColor(context,R.color.textColorWhite));
//            holder.layoutParent.setBackground(ContextCompat.getDrawable(context,R.drawable.bg_selcted_time_slot_ui2));
//        } else {
//
//            holder.layoutParent.setBackground(ContextCompat.getDrawable(context,R.drawable.ui2_bg_disabled_time_slot));
//            holder.tvTime.setTextColor(ContextCompat.getColor(context,R.color.textColorGray));
//        }
    }


    @Override
    public int getItemCount() {
        return mItemList.size();
    }


    public class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvDuration;
        SlotInfo slotInfo;
        LinearLayout layoutParent;

        public GenericViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime_new);
            tvDuration = itemView.findViewById(R.id.tvDuration_new);
            layoutParent = itemView.findViewById(R.id.parent_time_slot);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setMessage(context.getResources().getString(R.string.appointment_booking_confirmation_txt)
                                    + "\n\n" + context.getString(R.string.slot_info) + "- \n" + slotInfo.getSlotDate()
                                    + "\n" + slotInfo.getSlotTime()
                                    + "\n" + StringUtils.getTranslatedDays(slotInfo.getSlotDay(), new SessionManager(context).getAppLanguage())
                            )
                            //set positive button
                            .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mOnItemSelection.onSelect(slotInfo);
                                }
                            })
                            //set negative button
                            .setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .show();

                }
            });

        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public interface OnItemClickListener {
        void onItemClick(SlotInfo slotInfo);
    }
}
