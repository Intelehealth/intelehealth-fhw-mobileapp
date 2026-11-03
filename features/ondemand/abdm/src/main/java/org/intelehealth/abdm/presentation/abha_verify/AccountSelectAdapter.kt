package org.intelehealth.abdm.presentation.abha_verify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.abdm.R
import org.intelehealth.abdm.databinding.ItemAbhaAccountBinding

/** Single-selection list of ABHA accounts, rendered as legacy-style rich rows. */
internal class AccountSelectAdapter(
    private val accounts: List<AccountChoice>,
    private val onSelected: (AccountChoice) -> Unit,
) : RecyclerView.Adapter<AccountSelectAdapter.AccountViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class AccountViewHolder(
        val binding: ItemAbhaAccountBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(account: AccountChoice, position: Int, isSelected: Boolean) {
            val context = binding.root.context
            binding.tvIndex.text = context.getString(R.string.abdm_account_index, (position + 1).toString())
            binding.tvFullName.text = context.getString(R.string.abdm_account_name, account.name)
            binding.tvAbhaNumber.text = context.getString(R.string.abdm_account_number, account.abhaNumber.orEmpty())
            binding.tvGender.text = context.getString(R.string.abdm_account_gender, account.gender.orEmpty())
            binding.tvStatus.text = context.getString(
                if (account.isRegisteredLocally) {
                    R.string.abdm_account_status_registered
                } else {
                    R.string.abdm_account_status_not_registered
                },
            )

            binding.ivCheckedIcon.visibility = if (isSelected) View.VISIBLE else View.GONE
            binding.layoutAccount.setBackgroundResource(
                if (isSelected) R.drawable.abdm_bg_account_selected else R.drawable.bg_textbox_outline,
            )

            binding.root.setOnClickListener {
                val previous = selectedPosition
                selectedPosition = adapterPosition
                if (previous != RecyclerView.NO_POSITION) notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)
                onSelected(account)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemAbhaAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(accounts[position], position, position == selectedPosition)
    }

    override fun getItemCount(): Int = accounts.size
}
