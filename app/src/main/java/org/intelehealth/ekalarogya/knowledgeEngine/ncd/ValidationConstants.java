package org.intelehealth.ekalarogya.knowledgeEngine.ncd;

/**
 *
 */
public class ValidationConstants {
    //types
    public static final String TYPE_NAVIGATE = "NAVIGATE";
    public static final String TYPE_RANGE_TEXT = "RANGE_TEXT";
    public static final String TYPE_TEXT_DATE = "TEXT_DATE";
    public static final String RECURRING_NUMBER_SET = "RECURRING_NUMBER_SET";

    //source data types
    public static final String SOURCE_DATA_TYPE_PATIENT_ATTRIBUTE = "PATIENT_ATTRIBUTE";
    public static final String SOURCE_DATA_TYPE_NODE_VAL_INT = "NODE_VAL_INT";
    public static final String SOURCE_DATA_TYPE_NODE_VAL_DOUBLE = "NODE_VAL_DOUBLE";
    public static final String SOURCE_DATA_TYPE_NODE_VAL_STR = "NODE_VAL_STR";
    public static final String SOURCE_DATA_TYPE_THIS_NODE_VAL_LIST_INT_SET = "THIS_NODE_VAL_LIST_INT_SET";

    // source data
    public static final String SOURCE_DATA_NAME_AGE = "AGE";
    public static final String SOURCE_DATA_NAME_GENDER = "GENDER";


    // check
    public static final String CHECK_EQUAL = "EQUAL";
    public static final String CHECK_NOT_EQUAL = "NOT_EQUAL";
    public static final String CHECK_GREATER_THAN = "GREATER_THAN";
    public static final String CHECK_LESS_THAN = "LESS_THAN";
    public static final String CHECK_GREATER_THAN_EQUAL = "GREATER_THAN_EQUAL";
    public static final String CHECK_LESS_THAN_EQUAL = "LESS_THAN_EQUAL";

    public static final String CHECK_AND = "AND";
    public static final String CHECK_OR = "OR";

    // it will check for the alert message if the conditions is satisfying and the message will be taken from "DATA" key
    public static final String THEN_ALERT = "[ALT]";
    // it will clear the current node value & UI value
    public static final String THEN_CLEAR_FIELD = "[CLR]";

    // data type
    public static final String INTEGER = "INT";
    public static final String DOUBLE = "DOB";
    public static final String STRING = "STR";
    public static final String BOOLEAN = "BOL";
    public static final String DATE = "DAT";

    public static final String ACTION_TYPE_SET_SINGLE_OPTION_VALUE = "SET_SINGLE_OPTION_VALUE";
    public static final String ACTION_TYPE_WAIT_FOR_RECURRING_DATA_CAPTURE = "WAIT_FOR_RECURRING_DATA_CAPTURE";


}
