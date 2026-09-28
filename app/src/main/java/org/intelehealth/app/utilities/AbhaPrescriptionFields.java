package org.intelehealth.app.utilities;

import android.content.Context;
import android.text.TextUtils;

import org.intelehealth.app.R;

/**
 * The ABHA identifiers as they appear on a printed or shared prescription.
 *
 * Every renderer asks the same two questions — is there anything to show, and how should it read — so
 * they are answered once here rather than in each of the seven places a prescription is produced.
 *
 * The number is held on the patient record; the address is a visit attribute, because a patient may
 * carry several ABHA addresses and the prescription should name the one the visit was conducted under.
 * Callers therefore supply them from different sources and this class only decides presentation.
 */
public final class AbhaPrescriptionFields {

    private AbhaPrescriptionFields() {
    }

    /**
     * Whether a value is worth printing. The server returns the literal "NA" for a patient with no
     * ABHA and the pull stores it verbatim, so a blank-only check puts "ABHA Number: NA" on every
     * ordinary prescription.
     */
    public static boolean isPresent(String value) {
        return !TextUtils.isEmpty(value) && !value.trim().equalsIgnoreCase("NA");
    }

    /**
     * The label and value for one field, or an empty string when there is nothing to show. The two
     * fields are independent: a patient can hold a number without an address, and the reverse.
     *
     * The label resources carry their own colon, as label_age and label_patient_id beside them do,
     * and are deliberately English-only: the prescription is an English document — its section
     * headings are hardcoded constants — so a translated label here would read as the one stray
     * word in another script rather than as localisation.
     */
    public static String line(Context context, int labelRes, String value) {
        if (!isPresent(value)) return "";
        return context.getString(labelRes) + " " + value.trim();
    }

    /**
     * The paragraph appended after the visit details on the HTML prescriptions. Empty when the patient
     * has neither identifier, so the row is absent rather than blank.
     */
    public static String htmlBlock(Context context, String abhaNumber, String abhaAddress) {
        String number = line(context, R.string.label_abha_number, abhaNumber);
        String address = line(context, R.string.label_abha_address, abhaAddress);

        if (number.isEmpty() && address.isEmpty()) return "";

        String body = number.isEmpty() || address.isEmpty()
                ? number + address
                : number + " | " + address;

        return "<p id=\"abha_details\" style=\"font-size:12pt; margin-top:0px; margin-bottom:0px; "
                + "padding: 0px;\">" + body + "</p>";
    }

    /**
     * [htmlBlock] with percent signs doubled, for concatenation into a String.format template.
     *
     * The download prescriptions are assembled by String.format calls carrying upwards of twenty
     * positional arguments. Adding a placeholder for this block would mean inserting its argument at
     * exactly the right index, and an off-by-one there shifts every field after it silently — the
     * address printing the patient id, and so on, with no crash to reveal it. Concatenating the
     * rendered block into the template instead leaves the existing arguments untouched; the escape
     * keeps a percent sign in the data from being read as a placeholder.
     */
    public static String htmlBlockForFormatTemplate(Context context, String abhaNumber, String abhaAddress) {
        return htmlBlock(context, abhaNumber, abhaAddress).replace("%", "%%");
    }
}
