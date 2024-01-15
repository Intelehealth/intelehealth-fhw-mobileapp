package org.intelehealth.nak.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.nak.R;
import org.intelehealth.nak.utilities.DialogUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ImagePickerListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<String> mItemList = new ArrayList<String>();
    private boolean mIsSupportLocalLang = true;


    private DialogUtils.ImagePickerDialogListener mImagePickerDialogListener;

    public ImagePickerListAdapter(RecyclerView recyclerView, Context context, List<String> itemList, DialogUtils.ImagePickerDialogListener imagePickerDialogListener) {
        mContext = context;
        mItemList = itemList;
        mImagePickerDialogListener = imagePickerDialogListener;
        //mAnimator = new RecyclerViewAnimator(recyclerView);
    }

    private JSONObject mThisScreenLanguageJsonObject = new JSONObject();


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ui2_simple_dialog_list_item_view_1, parent, false);
        /**
         * First item's entrance animations.
         */
        //mAnimator.onCreateViewHolder(itemView);

        return new GenericViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (holder instanceof GenericViewHolder) {
            GenericViewHolder genericViewHolder = (GenericViewHolder) holder;
            genericViewHolder.index = holder.getAbsoluteAdapterPosition();
            genericViewHolder.simpleItemData = mItemList.get(genericViewHolder.index);

            genericViewHolder.titleView.setText(genericViewHolder.simpleItemData);

            /*if (genericViewHolder.simpleItemData.isSelected()) {
                genericViewHolder.statusImageView.setVisibility(View.VISIBLE);
            } else {
                genericViewHolder.statusImageView.setVisibility(View.INVISIBLE);
            }*/
            if (genericViewHolder.index == 0) {
                genericViewHolder.statusImageView.setImageResource(R.drawable.baseline_photo_camera_24);
            } else if (genericViewHolder.index == 1) {
                genericViewHolder.statusImageView.setImageResource(R.drawable.baseline_photo_library_24);
            }else {
                genericViewHolder.statusImageView.setImageResource(R.drawable.baseline_cancel_24);
            }
            if (genericViewHolder.index == mItemList.size() - 1) {
                genericViewHolder.lineView.setVisibility(View.INVISIBLE);
            } else {
                genericViewHolder.lineView.setVisibility(View.VISIBLE);
            }


        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        ImageView statusImageView;
        View lineView;
        String simpleItemData;
        int index;

        GenericViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.title_tv);
            statusImageView = itemView.findViewById(R.id.tv_check_status);
            lineView = itemView.findViewById(R.id.line_view);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mImagePickerDialogListener.onActionDone(index);
                }
            });
        }
    }
}
