package org.intelehealth.app.activities.help.adapter;

import android.content.Context;
import org.intelehealth.app.utilities.CustomLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.models.DocumentObject;

public class ChatSupportAdapter_New extends RecyclerView.Adapter<ChatSupportAdapter_New.MyViewHolder> {
    Context context;

    public ChatSupportAdapter_New(Context context) {
        this.context = context;

    }

    @Override
    public ChatSupportAdapter_New.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_chat_support_ui2, parent, false);
        ChatSupportAdapter_New.MyViewHolder myViewHolder = new ChatSupportAdapter_New.MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(ChatSupportAdapter_New.MyViewHolder holder, int position) {

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
   /* public void add(DocumentObject doc) {
        boolean bool = documentList.add(doc);
        if (bool) CustomLog.d(TAG, "add: Item added to list");
        notifyDataSetChanged();
    }*/


}
