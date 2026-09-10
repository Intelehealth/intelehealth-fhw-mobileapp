package org.intelehealth.app.activities.visit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.intelehealth.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared visit-list item adapter for the Received / Pending / Referrals tabs
 * (avatar + name/gender + date + up to 2 status badges).
 *
 * This only renders whatever badges it's given — deciding WHICH badges apply
 * (e.g. "Latest" for the newest encounter, "Referral Declined" from a referral
 * status field) is not wired up yet and is left to each fragment/caller to add
 * later once that condition data exists.
 */
public class VisitStatusAdapter extends RecyclerView.Adapter<VisitStatusAdapter.ViewHolder> {

    public enum BadgeColor { GREEN, PURPLE, ORANGE, RED }

    public static class Badge {
        final String text;
        final BadgeColor color;

        public Badge(String text, BadgeColor color) {
            this.text = text;
            this.color = color;
        }
    }

    public static class VisitStatusItem {
        private final String patientName;
        private final String genderAge;
        private final String visitDate;
        private final String photoUrl;
        private final Badge primaryBadge;
        private final Badge secondaryBadge;
        // Identifiers needed to act on a row (e.g. open the visit's prescription) —
        // optional, so the original 6-arg constructor still works for any caller
        // that only needs to display a row.
        private final String patientUuid;
        private final String visitUuid;
        private final String openmrsId;
        private final String gender;
        private final String dob;

        public VisitStatusItem(String patientName, String genderAge, String visitDate,
                                @Nullable String photoUrl,
                                @Nullable Badge primaryBadge, @Nullable Badge secondaryBadge) {
            this(patientName, genderAge, visitDate, photoUrl, primaryBadge, secondaryBadge,
                    null, null, null, null, null);
        }

        public VisitStatusItem(String patientName, String genderAge, String visitDate,
                                @Nullable String photoUrl,
                                @Nullable Badge primaryBadge, @Nullable Badge secondaryBadge,
                                @Nullable String patientUuid, @Nullable String visitUuid,
                                @Nullable String openmrsId, @Nullable String gender,
                                @Nullable String dob) {
            this.patientName = patientName;
            this.genderAge = genderAge;
            this.visitDate = visitDate;
            this.photoUrl = photoUrl;
            this.primaryBadge = primaryBadge;
            this.secondaryBadge = secondaryBadge;
            this.patientUuid = patientUuid;
            this.visitUuid = visitUuid;
            this.openmrsId = openmrsId;
            this.gender = gender;
            this.dob = dob;
        }

        public String getPatientName() { return patientName; }
        public String getGenderAge() { return genderAge; }
        public String getVisitDate() { return visitDate; }
        public String getPhotoUrl() { return photoUrl; }
        public Badge getPrimaryBadge() { return primaryBadge; }
        public Badge getSecondaryBadge() { return secondaryBadge; }
        @Nullable public String getPatientUuid() { return patientUuid; }
        @Nullable public String getVisitUuid() { return visitUuid; }
        @Nullable public String getOpenmrsId() { return openmrsId; }
        @Nullable public String getGender() { return gender; }
        @Nullable public String getDob() { return dob; }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private final Context context;
    private final List<VisitStatusItem> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public VisitStatusAdapter(Context context, List<VisitStatusItem> items,
                               @Nullable OnItemClickListener listener) {
        this.context = context;
        if (items != null) this.items.addAll(items);
        this.listener = listener;
    }

    public void setData(List<VisitStatusItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Nullable
    public VisitStatusItem getItem(int position) {
        if (position < 0 || position >= items.size()) return null;
        return items.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.visit_status_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VisitStatusItem item = items.get(position);

        holder.name.setText(item.getPatientName());
        holder.genderAge.setText(item.getGenderAge());
        holder.date.setText(item.getVisitDate());

        Glide.with(holder.itemView.getContext())
                .load(item.getPhotoUrl())
                .placeholder(R.drawable.avatar1)
                .error(R.drawable.avatar1)
                .override(50, 50)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.profileImage);

        bindBadge(holder.badgePrimary, item.getPrimaryBadge());
        bindBadge(holder.badgeSecondary, item.getSecondaryBadge());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(holder.getAbsoluteAdapterPosition());
        });
    }

    private void bindBadge(TextView view, @Nullable Badge badge) {
        if (badge == null) {
            view.setVisibility(View.GONE);
            return;
        }
        view.setVisibility(View.VISIBLE);
        view.setText(badge.text);
        int bgRes;
        int textColorRes;
        switch (badge.color) {
            case PURPLE:
                bgRes = R.drawable.bg_badge_purple;
                textColorRes = R.color.badgePurpleText;
                break;
            case ORANGE:
                bgRes = R.drawable.bg_badge_orange;
                textColorRes = R.color.badgeOrangeText;
                break;
            case RED:
                bgRes = R.drawable.bg_badge_red;
                textColorRes = R.color.badgeRedText;
                break;
            case GREEN:
            default:
                bgRes = R.drawable.bg_badge_green;
                textColorRes = R.color.badgeGreenText;
                break;
        }
        view.setBackgroundResource(bgRes);
        view.setTextColor(ContextCompat.getColor(context, textColorRes));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView name, genderAge, date, badgePrimary, badgeSecondary;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.vsi_profile_image);
            name = itemView.findViewById(R.id.vsi_patname_txtview);
            genderAge = itemView.findViewById(R.id.vsi_gender_age_txtview);
            date = itemView.findViewById(R.id.vsi_date_txtview);
            badgePrimary = itemView.findViewById(R.id.vsi_badge_primary);
            badgeSecondary = itemView.findViewById(R.id.vsi_badge_secondary);
        }
    }
}
