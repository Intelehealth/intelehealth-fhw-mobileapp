package org.intelehealth.app.abdm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.model.Account;

import java.util.List;

/**
 * Created by - Prajwal W. on 07/02/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
public class MultipleAccountsAdapter extends RecyclerView.Adapter<MultipleAccountsAdapter.MyViewHolder> {
    private Context context;
    private List<Account> accountList;
    private MultipleAccountsAdapter.OnItemClick onItemClick;

    public MultipleAccountsAdapter(Context context, List<Account> accountList, MultipleAccountsAdapter.OnItemClick onItemClick) {
        this.context = context;
        this.accountList = accountList;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public MultipleAccountsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_account_selection_login, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Account account = accountList.get(position);
        holder.tvAbhaAddress.setText(account.getPreferredAbhaAddress());
        holder.tvFullname.setText(account.getName());

        holder.itemView.setOnClickListener(v -> {
            onItemClick.OnItemSelected(account);
        });
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAbhaAddress, tvFullname;
        private ImageView ivCheckedIcon;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAbhaAddress = itemView.findViewById(R.id.tvAbhaAddress);
            tvFullname = itemView.findViewById(R.id.tvFullname);
            ivCheckedIcon = itemView.findViewById(R.id.ivCheckedIcon);
        }
    }

    public interface OnItemClick {
        void OnItemSelected(Account account);
    }
}
