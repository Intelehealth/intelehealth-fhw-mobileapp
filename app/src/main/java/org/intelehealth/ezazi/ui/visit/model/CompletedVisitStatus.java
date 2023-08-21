package org.intelehealth.ezazi.ui.visit.model;

import org.intelehealth.ezazi.utilities.UuidDictionary;

/**
 * Created by Vaghela Mithun R. on 19-08-2023 - 18:58.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public enum CompletedVisitStatus {
    LABOUR(Labour.LIVE_BIRTH.label),
    MOTHER_DECEASED(MotherDeceased.MOTHER_DECEASED_FLAG.value()),
    REFER_TO_HOSPITAL(ReferType.REFER_TO_OTHER.value),
    SELF_DISCHARGE(ReferType.SELF_DISCHARGE.value),
    SHIFT_TO_C_SECTION(ReferType.SHIFT_TO_C_SECTION.value),
    REFER_TO_HIGH_ICU(ReferType.REFER_TO_ICU.value),
    OTHER_COMMENT(OtherComment.OTHER.value());

    public final String label;

    CompletedVisitStatus(String label) {
        this.label = label;
    }

    public enum Labour implements VisitStatus {
        LIVE_BIRTH(UuidDictionary.BIRTH_OUTCOME, "Live Birth", "Live Birth"),
        STILL_BIRTH(UuidDictionary.BIRTH_OUTCOME, "Still Birth", "Still Birth"),
        OTHER(UuidDictionary.BIRTH_OUTCOME, "Other", "Labour Other");
        private final String value;
        private final String uuid;
        private final String sortValue;

        public final String label = "Labour completed";

        Labour(String uuid, String value, String sortValue) {
            this.uuid = uuid;
            this.value = value;
            this.sortValue = sortValue;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public String uuid() {
            return uuid;
        }

        @Override
        public String sortValue() {
            return sortValue;
        }

        public static String conceptUuid() {
            return Labour.LIVE_BIRTH.uuid();
        }
    }

    public enum MotherDeceased implements VisitStatus {
        MOTHER_DECEASED_FLAG(UuidDictionary.MOTHER_DECEASED_FLAG, "Mother Deceased", "Mother Deceased"),
        MOTHER_DECEASED_REASON(UuidDictionary.MOTHER_DECEASED, "Mother Deceased", "Mother Deceased");
        private final String value;
        private final String uuid;
        private final String sortValue;

        MotherDeceased(String uuid, String value, String sortValue) {
            this.uuid = uuid;
            this.value = value;
            this.sortValue = sortValue;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public String uuid() {
            return uuid;
        }

        @Override
        public String sortValue() {
            return sortValue;
        }
    }

    public enum ReferType implements VisitStatus {
        REFER_TO_OTHER(UuidDictionary.REFER_TYPE, "Refer to other Hospital", "Referred"),
        SHIFT_TO_C_SECTION(UuidDictionary.REFER_TYPE, "Shift to C-Section", "Shift to C"),
        REFER_TO_ICU(UuidDictionary.REFER_TYPE, "Refer to high dependency unit / ICU", "Referred / ICU"),
        SELF_DISCHARGE(UuidDictionary.REFER_TYPE, "Self discharge against Medical Advice", "Self Discharged");

        private final String value;
        private final String uuid;
        private final String sortValue;

        ReferType(String uuid, String value, String sortValue) {
            this.uuid = uuid;
            this.value = value;
            this.sortValue = sortValue;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public String uuid() {
            return uuid;
        }

        @Override
        public String sortValue() {
            return sortValue;
        }

        public static String conceptUuid() {
            return ReferType.REFER_TO_OTHER.uuid();
        }
    }

    public enum OtherComment implements VisitStatus {
        OTHER(UuidDictionary.END_2ND_STAGE_OTHER, "Enter other comment", "Other");

        private final String value;
        private final String uuid;
        private final String sortValue;

        OtherComment(String uuid, String value, String sortValue) {
            this.uuid = uuid;
            this.value = value;
            this.sortValue = sortValue;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public String uuid() {
            return uuid;
        }

        @Override
        public String sortValue() {
            return sortValue;
        }

    }
}


