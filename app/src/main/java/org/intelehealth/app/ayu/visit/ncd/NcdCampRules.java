package org.intelehealth.app.ayu.visit.ncd;

import androidx.annotation.Nullable;

import org.intelehealth.app.ayu.visit.ncd.NcdScreeningPlan.SkipReason;

import java.util.EnumSet;
import java.util.Set;

/**
 * Camp eligibility shown on Visit summary (no Android code, so it is unit tested). Only the screenings the
 * patient was offered count: final BP at or above 140/90 for HTN, RBS above 140 for DM. A CBAC score of
 * 4 or less offers neither, so the patient is not eligible.
 */
public final class NcdCampRules {

    public static final double RBS_THRESHOLD = 140;

    public enum Condition { DIABETES, HYPERTENSION }

    private NcdCampRules() {
    }

    // null when no banner is shown: no NCD screening, consent declined, urgent or not eligible
    @Nullable
    public static Boolean campEligible(@Nullable NcdScreeningPlan plan, @Nullable BpReading finalBp, @Nullable Double rbs) {
        Set<Condition> conditions = campConditions(plan, finalBp, rbs);
        return conditions == null ? null : !conditions.isEmpty();
    }

    // The conditions the patient goes to camp for (empty when not eligible); null when no banner is shown
    @Nullable
    public static Set<Condition> campConditions(@Nullable NcdScreeningPlan plan, @Nullable BpReading finalBp, @Nullable Double rbs) {
        if (plan == null || screeningEnded(plan)) return null;
        Set<Condition> conditions = EnumSet.noneOf(Condition.class);
        if (plan.getDmSkipReason() == null && rbs != null && rbs > RBS_THRESHOLD) conditions.add(Condition.DIABETES);
        if (plan.getHtnSkipReason() == null && finalBp != null && BpScreeningRules.atOrAboveThreshold(finalBp)) {
            conditions.add(Condition.HYPERTENSION);
        }
        return conditions;
    }

    // Consent declined, urgent or not eligible: no DM/HTN screening happened
    static boolean screeningEnded(NcdScreeningPlan plan) {
        return endsScreening(plan.getDmSkipReason()) || endsScreening(plan.getHtnSkipReason());
    }

    private static boolean endsScreening(@Nullable SkipReason reason) {
        return reason == SkipReason.CONSENT_DECLINED || reason == SkipReason.URGENT || reason == SkipReason.NOT_ELIGIBLE;
    }
}
