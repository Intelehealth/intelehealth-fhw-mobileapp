package org.intelehealth.app.activities.chatHelp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

public class ChatHelpAdapter_New extends RecyclerView.Adapter<ChatHelpAdapter_New.MyViewHolder> {
    Context context;

    public ChatHelpAdapter_New(Context context) {
        this.context = context;

    }

    @Override
    public ChatHelpAdapter_New.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_chat_support_ui2, parent, false);
        ChatHelpAdapter_New.MyViewHolder myViewHolder = new ChatHelpAdapter_New.MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(ChatHelpAdapter_New.MyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 30;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        public MyViewHolder(View itemView) {
            super(itemView);
            //name = itemView.findViewById(R.id.tv);

        }
    }


}
