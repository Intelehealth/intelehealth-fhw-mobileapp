package org.intelehealth.app.reactnative;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

/**
 * Home-screen status banner payload shared between the native host and the
 * React Native {@code StatusBannerModule}, and persisted as JSON (see
 * {@link StatusBannerUpdater}).
 *
 * <p>{@link Keep} and the explicit {@link SerializedName} names pin the JSON
 * keys so the persisted format survives R8 field renaming in release builds and
 * stays stable across app updates.
 */
@Keep
public class StatusBannerData {
    @SerializedName("variant")
    private String variant;
    @SerializedName("title")
    private String title;
    @SerializedName("subtitle")
    private String subtitle;
    @SerializedName("actionLabel")
    private String actionLabel;

    public StatusBannerData(String variant, String title, String subtitle, String actionLabel) {
        this.variant = variant;
        this.title = title;
        this.subtitle = subtitle;
        this.actionLabel = actionLabel;
    }

    public String getVariant() { return variant; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getActionLabel() { return actionLabel; }
}
