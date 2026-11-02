package org.intelehealth.app.knowledgeEngine;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common internal model for an age expressed in years, months and days.
 * <p>
 * Used both for the "age_min" / "age_max" limits of a mindmap node and for the patient's age.
 * The limits support two JSON formats:
 * <pre>
 *   Old: "age_min": 12                                  -> 12 years, 0 months, 0 days
 *   New: "age_min": {"year": 12, "month": 6, "days": 15}
 * </pre>
 * Old numeric limits keep the legacy "years.months" meaning (e.g. 0.6 = 0 years 6 months) and are
 * compared at year/month precision, exactly like the previous float comparison. New object limits
 * are compared at full year/month/day precision.
 */
public class AgeLimit implements Serializable {

    private static final Pattern LEGACY_PATTERN = Pattern.compile("^(\\d+)(?:\\.(\\d*))?$");

    private final int years;
    private final int months;
    private final int days;
    // true when parsed from the old numeric format; days are then ignored in comparisons
    private final boolean legacy;

    public AgeLimit(int years, int months, int days) {
        this(years, months, days, false);
    }

    private AgeLimit(int years, int months, int days, boolean legacy) {
        this.years = years;
        this.months = months;
        this.days = days;
        this.legacy = legacy;
    }

    /**
     * Parses an "age_min" / "age_max" value in either the old (number) or new (object) format.
     *
     * @param raw value returned by {@link JSONObject#opt(String)}
     * @return the parsed limit, or null if the value is missing, empty or malformed
     */
    public static AgeLimit parse(Object raw) {
        if (raw == null || raw == JSONObject.NULL) return null;

        if (raw instanceof JSONObject) return fromJson((JSONObject) raw);

        String value = raw.toString().trim();
        if (value.isEmpty() || value.equalsIgnoreCase("null")) return null;

        if (value.startsWith("{")) {
            try {
                return fromJson(new JSONObject(value));
            } catch (JSONException e) {
                return null;
            }
        }

        return parseLegacy(value);
    }

    private static AgeLimit fromJson(JSONObject json) {
        if (!json.has("year") && !json.has("month") && !json.has("days")) return null;

        int years = readPart(json, "year");
        int months = readPart(json, "month");
        int days = readPart(json, "days");
        if (years < 0 || months < 0 || days < 0) return null;

        return new AgeLimit(years, months, days, false);
    }

    /**
     * @return the value of the key, 0 if it is missing or null, or -1 if it is not a valid number
     */
    private static int readPart(JSONObject json, String key) {
        if (json.isNull(key)) return 0;

        Object value = json.opt(key);
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Old format: whole years ("12") or the legacy "years.months" encoding ("0.6", "5.11").
     */
    private static AgeLimit parseLegacy(String value) {
        Matcher matcher = LEGACY_PATTERN.matcher(value);
        if (!matcher.matches()) return null;

        try {
            int years = Integer.parseInt(matcher.group(1));
            String fraction = matcher.group(2);
            int months = (fraction == null || fraction.isEmpty()) ? 0 : Integer.parseInt(fraction);
            return new AgeLimit(years, months, 0, true);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converts the legacy "years.months" float (see DateAndTimeUtils.getFloat_Age_Year_Month) into an age.
     */
    public static AgeLimit fromYearMonthFloat(float ageYearMonth) {
        AgeLimit age = parseLegacy(String.valueOf(Math.max(ageYearMonth, 0f)));
        return age != null ? new AgeLimit(age.years, age.months, 0, false) : new AgeLimit(0, 0, 0);
    }

    /**
     * Exact age between the date of birth and today. Negative components are clamped to 0.
     */
    public static AgeLimit fromDateOfBirth(LocalDate dateOfBirth, LocalDate today) {
        Period period = new Period(dateOfBirth, today, PeriodType.yearMonthDay());
        return new AgeLimit(Math.max(period.getYears(), 0),
                Math.max(period.getMonths(), 0),
                Math.max(period.getDays(), 0));
    }

    /**
     * Patient's age from the stored date of birth ("yyyy-MM-dd..."), falling back to the
     * legacy "years.months" float when the date of birth is missing or unparsable.
     */
    public static AgeLimit forPatient(String dateOfBirth, float fallbackAgeYearMonth) {
        if (dateOfBirth != null && dateOfBirth.trim().length() >= 10) {
            try {
                LocalDate dob = LocalDate.parse(dateOfBirth.trim().substring(0, 10));
                return fromDateOfBirth(dob, new LocalDate());
            } catch (IllegalArgumentException e) {
                // fall through to the legacy float age
            }
        }
        return fromYearMonthFloat(fallbackAgeYearMonth);
    }

    /**
     * Same rule as before: filtering applies only when both limits are present, and a node is
     * excluded when the age is below the minimum or above the maximum (boundaries are inclusive).
     */
    public static boolean isOutsideRange(AgeLimit age, AgeLimit min, AgeLimit max) {
        if (age == null || min == null || max == null) return false;
        return age.compareToLimit(min) < 0 || age.compareToLimit(max) > 0;
    }

    /**
     * Rule used by the visit flow (VisitUtils.checkNodeValidByGenderAndAge): each limit is checked
     * on its own and a zero limit means "not set". Boundaries are inclusive.
     */
    public static boolean isWithinVisitRange(AgeLimit age, AgeLimit min, AgeLimit max) {
        if (age == null) return true;
        if (min != null && !min.isZero() && age.compareToLimit(min) < 0) return false;
        if (max != null && !max.isZero() && age.compareToLimit(max) > 0) return false;
        return true;
    }

    public boolean isZero() {
        return years == 0 && months == 0 && days == 0;
    }

    /**
     * Compares this age with a limit; days are ignored when the limit uses the old numeric format.
     */
    public int compareToLimit(AgeLimit limit) {
        if (years != limit.years) return years < limit.years ? -1 : 1;
        if (months != limit.months) return months < limit.months ? -1 : 1;
        if (limit.legacy || days == limit.days) return 0;
        return days < limit.days ? -1 : 1;
    }

    public int getYears() {
        return years;
    }

    public int getMonths() {
        return months;
    }

    public int getDays() {
        return days;
    }

    public boolean isLegacy() {
        return legacy;
    }

    @Override
    public String toString() {
        return years + "y " + months + "m " + days + "d" + (legacy ? " (legacy)" : "");
    }
}
