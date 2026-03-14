package org.intelehealth.abdm.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.abdm.R;
import org.intelehealth.abdm.model.Account;
import org.intelehealth.abdm.utils.StringUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by - Prajwal W. on 07/02/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
public class MultipleAccountsAdapter extends RecyclerView.Adapter<MultipleAccountsAdapter.MyViewHolder> {
    private final Context context;
    private final List<Account> accountList;
    private final MultipleAccountsAdapter.OnItemClick onItemClick;
    private int checkedPosition = -1;

    private ExecutorService patientExecutor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public MultipleAccountsAdapter(Context context, List<Account> accountList, MultipleAccountsAdapter.OnItemClick onItemClick) {
        this.context = context;
        this.accountList = accountList;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public MultipleAccountsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_abha_accounts, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Account account = accountList.get(position);
        holder.tvIndex.setText(context.getString(R.string.multiple_accounts_abha_index, String.valueOf(holder.getLayoutPosition() + 1)));
        holder.tvFullName.setText(context.getString(R.string.multiple_accounts_abha_full_name, account.getName()));
        holder.tvAbhaNumber.setText(context.getString(R.string.multiple_accounts_abha_number, account.getABHANumber()));
        holder.tvGender.setText(context.getString(R.string.multiple_accounts_abha_gender, account.getGender()));

        patientExecutor.execute(() -> {
            String abhaNumberLastFourDigits = StringUtils.extractLastFour(account.getABHANumber());
            String firstName = account.getName().split(" ")[0];
            String lastName = account.getName().split(" ")[1];

            boolean isPatientPresent = false; /*PatientsDAO.isPatientPresentInLocal(abhaNumberLastFourDigits, firstName, lastName);*/
            mainHandler.post(() -> {
                if (isPatientPresent) {
                    holder.tvStatus.setText(context.getString(R.string.multiple_accounts_abha_positive_status));
                } else {
                    holder.tvStatus.setText(context.getString(R.string.multiple_accounts_abha_negative_status));
                }
            });
        });

        holder.itemView.setOnClickListener(v -> {
            if (checkedPosition == -1) {
                if ((Integer) holder.itemView.getTag() == R.drawable.bg_textbox_outline) {
                    holder.ivCheckedIcon.setVisibility(View.VISIBLE);
                    holder.itemView.setBackground(AppCompatResources.getDrawable(context, R.drawable.ic_chat_bubble_rounded_square));
                    holder.itemView.setTag(R.drawable.ic_chat_bubble_rounded_square);
                    checkedPosition = position;
                    onItemClick.OnItemSelected(account, true);
                }
            } else if ((Integer) holder.itemView.getTag() == R.drawable.ic_chat_bubble_rounded_square) {
                holder.ivCheckedIcon.setVisibility(View.GONE);
                holder.itemView.setBackground(AppCompatResources.getDrawable(context, R.drawable.bg_textbox_outline));
                holder.itemView.setTag(R.drawable.bg_textbox_outline);
                checkedPosition = -1;
                onItemClick.OnItemSelected(account, false);
            } else {
                holder.ivCheckedIcon.setVisibility(View.GONE);
                holder.itemView.setBackground(AppCompatResources.getDrawable(context, R.drawable.bg_textbox_outline));
                holder.itemView.setTag(R.drawable.bg_textbox_outline);
                //  onItemClick.OnItemSelected(account, false);
            }

        });
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvIndex;
        private final TextView tvFullName;
        private final TextView tvAbhaNumber;
        private final TextView tvGender;
        private final TextView tvStatus;

        private final ImageView ivCheckedIcon;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tvIndex);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvAbhaNumber = itemView.findViewById(R.id.tvAbhaNumber);
            tvGender = itemView.findViewById(R.id.tvGender);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivCheckedIcon = itemView.findViewById(R.id.ivCheckedIcon);
            itemView.setTag(R.drawable.bg_textbox_outline);
        }
    }

    public interface OnItemClick {
        void OnItemSelected(Account account, boolean isChecked);
    }
}
